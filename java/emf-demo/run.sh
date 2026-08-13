#!/bin/bash
# 编译并运行 Java ARTOP ArxmlLoader Demo
#
# 用法:
#   ./run.sh                       # 加载默认目录 /workspace/decompiler/autosar448/model/library
#   ./run.sh /path/to/arxml_dir    # 加载指定目录
set -e
cd "$(dirname "$0")"

# 拼接 classpath: emf-demo/lib (EMF) + /workspace/libs/artop-runtime/plugins (ARTOP + Eclipse)
CP="$(ls lib/*.jar | tr '\n' ':')"
if [ -d /workspace/libs/artop-runtime/plugins ]; then
    CP="$CP$(ls /workspace/libs/artop-runtime/plugins/*.jar | tr '\n' ':')"
fi

rm -rf build/classes
mkdir -p build/classes

# 编译
javac -encoding UTF-8 -d build/classes -cp "$CP" \
    $(find src/main/java -name "*.java")

# 运行
java -cp "build/classes:$CP" com.example.emfdemo.ArtopArxmlLoaderDemo "$@"
