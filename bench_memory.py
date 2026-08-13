#!/usr/bin/env python3
# bench_memory.py — 测量 C++ / Java arxml benchmark 的运行时峰值内存（VmHWM）
# 用法: python3 bench_memory.py
import subprocess
import os
import time
import threading

WORKSPACE = "/workspace"
JAVA8 = "/root/.local/share/mise/installs/java/temurin-8.0.482+8/bin/java"

def read_vm_rss_kb(pid):
    """读取 /proc/<pid>/status 的 VmRSS（当前驻留）和 VmHWM（峰值）"""
    try:
        with open(f"/proc/{pid}/status") as f:
            rss = hwm = None
            for line in f:
                if line.startswith("VmRSS:"):
                    rss = int(line.split()[1])
                elif line.startswith("VmHWM:"):
                    hwm = int(line.split()[1])
            return rss, hwm
    except (FileNotFoundError, ProcessLookupError, ValueError):
        return None, None

def run_and_measure(cmd, cwd=None, env=None, timeout=1200):
    """启动子进程，轮询 VmRSS 取峰值，结束后读 VmHWM"""
    start = time.time()
    proc = subprocess.Popen(cmd, cwd=cwd, env=env,
                            stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    peak_rss = 0
    poll_count = 0
    while proc.poll() is None:
        rss, hwm = read_vm_rss_kb(proc.pid)
        if rss is not None and rss > peak_rss:
            peak_rss = rss
        poll_count += 1
        time.sleep(0.1)
    elapsed = time.time() - start
    # 进程结束后最后读一次 VmHWM（内核记录的峰值，最准确）
    _, final_hwm = read_vm_rss_kb(proc.pid)
    # VmHWM 在进程结束后读不到（/proc/pid 消失），用轮询的 peak_rss
    # 但对于多线程 Java，/proc/pid/status 只反映主线程，需要读 /proc/pid/task/* 汇总
    # 更准确：用 VmHWM（进程结束后读不到），所以用轮询 peak 作为近似
    return proc.returncode, peak_rss, elapsed, poll_count

def mb(kb):
    return kb / 1024.0

def main():
    small = os.path.join(WORKSPACE, "java/demo/output/ECUConfigurationParameters.arxml")
    large = os.path.join(WORKSPACE, "benchmark/data/large_96m.arxml")
    cpp_bench = os.path.join(WORKSPACE, "benchmark/cpp/arxml_benchmark")

    results = []

    env_cpp = dict(os.environ)
    env_cpp["ARXML_BENCHMARK_KEEP"] = "0"
    print("=== C++ small (12MB) ===", flush=True)
    rc, rss, el, _ = run_and_measure(
        [cpp_bench, small, "/tmp/cpp_mem_small.arxml", "2"],
        env=env_cpp, timeout=600)
    print(f"  rc={rc} peakRSS={rss} KB ({mb(rss):.1f} MB) elapsed={el:.1f}s", flush=True)
    results.append(("C++ small (12MB)", rss, el))

    print("=== C++ large (96MB) ===", flush=True)
    rc, rss, el, _ = run_and_measure(
        [cpp_bench, large, "/tmp/cpp_mem_large.arxml", "2"],
        env=env_cpp, timeout=1200)
    print(f"  rc={rc} peakRSS={rss} KB ({mb(rss):.1f} MB) elapsed={el:.1f}s", flush=True)
    results.append(("C++ large (96MB)", rss, el))

    artop = os.path.join(WORKSPACE, "artop")
    launcher = os.path.join(artop, "plugins/org.eclipse.equinox.launcher_1.5.500.v20190715-1310.jar")
    config = "file:/workspace/artop/headless-config/"
    env_java = dict(os.environ)

    print("=== Java small (12MB) ===", flush=True)
    rc, rss, el, _ = run_and_measure(
        [JAVA8, "-jar", launcher,
         "-configuration", config,
         "-application", "com.example.arxml.validation.headless.arxmlBenchmarkApp",
         "-input", small, "-iterations", "2"],
        cwd=artop, env=env_java, timeout=600)
    print(f"  rc={rc} peakRSS={rss} KB ({mb(rss):.1f} MB) elapsed={el:.1f}s", flush=True)
    results.append(("Java small (12MB)", rss, el))

    print("=== Java large (96MB) ===", flush=True)
    rc, rss, el, _ = run_and_measure(
        [JAVA8, "-jar", launcher,
         "-configuration", config,
         "-application", "com.example.arxml.validation.headless.arxmlBenchmarkApp",
         "-input", large, "-iterations", "2"],
        cwd=artop, env=env_java, timeout=1200)
    print(f"  rc={rc} peakRSS={rss} KB ({mb(rss):.1f} MB) elapsed={el:.1f}s", flush=True)
    results.append(("Java large (96MB)", rss, el))

    print("\n=== Summary ===", flush=True)
    print(f"{'Case':<22} {'PeakRSS(MB)':<14} {'Elapsed(s)':<12}", flush=True)
    for name, rss, el in results:
        print(f"{name:<22} {mb(rss):<14.1f} {el:<12.1f}", flush=True)

if __name__ == "__main__":
    main()
