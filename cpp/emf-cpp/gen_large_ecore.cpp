// 生成大 xmi 实例文件用于性能测试
// 用 ecore 元模型本身（nsURI 已注册），生成大量 EClass/EAttribute 实例
// 这样 C++ 和 Java 都无需额外注册 package
#include <cstdio>
#include <fstream>
#include <string>

int main(int argc, char** argv) {
    if (argc < 3) {
        std::fprintf(stderr, "Usage: %s <output.xmi> <targetSizeMB>\n", argv[0]);
        return 1;
    }
    std::string outPath = argv[1];
    int targetMB = std::atoi(argv[2]);
    if (targetMB <= 0) targetMB = 100;
    size_t targetBytes = (size_t)targetMB * 1024 * 1024;

    std::ofstream f(outPath, std::ios::binary);
    // 根是 EPackage，含大量 EClass 子对象，每个 EClass 含若干 EAttribute
    // 使用 ecore 元模型，两端自动识别
    f << "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    f << "<ecore:EPackage xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" "
      << "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
      << "xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\" "
      << "name=\"bench\" nsURI=\"http://bench/1.0\" nsPrefix=\"bench\">\n";

    // 每个 EClass 约 350 字节（含 4 个 EAttribute），生成足够多达到目标
    int classIdx = 0;
    size_t current = (size_t)f.tellp();
    while (current < targetBytes) {
        f << "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Cls" << classIdx << "\">\n";
        f << "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\" "
          << "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n";
        f << "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"id\" "
          << "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EInt\"/>\n";
        f << "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"value\" "
          << "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EDouble\"/>\n";
        f << "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"flag\" "
          << "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EBoolean\"/>\n";
        f << "  </eClassifiers>\n";
        classIdx++;
        current = (size_t)f.tellp();
        if (classIdx % 50000 == 0) {
            std::fprintf(stderr, "[gen] %d classes, %.1f MB\n", classIdx, current / 1048576.0);
        }
    }
    f << "</ecore:EPackage>\n";
    f.close();

    std::ifstream check(outPath, std::ios::binary | std::ios::ate);
    size_t finalSize = check.tellg();
    std::fprintf(stderr, "[done] %s: %zu bytes (%.1f MB), %d classes\n",
                 outPath.c_str(), finalSize, finalSize / 1048576.0, classIdx);
    return 0;
}
