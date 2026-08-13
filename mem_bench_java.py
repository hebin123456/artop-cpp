#!/usr/bin/env python3
# mem_bench_java.py — 测量 Java arxml benchmark 各规模峰值 RSS（轮询 /proc）
# 用法: python3 mem_bench_java.py <input.arxml> [label]
import subprocess, os, sys, time

JAVA8 = "/root/.local/share/mise/installs/java/temurin-8.0.482+8/bin/java"
WORKSPACE = "/workspace"
LAUNCHER = "/workspace/artop/plugins/org.eclipse.equinox.launcher_1.5.500.v20190715-1310.jar"
CONFIG = "file:/workspace/artop/headless-config/"

def read_rss_kb(pid):
    try:
        with open(f"/proc/{pid}/status") as f:
            for line in f:
                if line.startswith("VmRSS:"):
                    return int(line.split()[1])
    except (FileNotFoundError, ProcessLookupError, ValueError):
        pass
    return 0

def main():
    inp = os.path.abspath(sys.argv[1])
    label = sys.argv[2] if len(sys.argv) > 2 else os.path.basename(inp)
    out = "/tmp/java_mem_out.arxml"
    iters = "1"

    cp_parts = []

    env = dict(os.environ)
    cmd = [JAVA8, "-Xmx8g", "-jar", LAUNCHER,
           "-configuration", CONFIG,
           "-application", "com.example.arxml.validation.headless.arxmlBenchmarkApp",
           "-input", inp, "-iterations", iters]
    start = time.time()
    proc = subprocess.Popen(cmd, cwd=WORKSPACE + "/artop", env=env,
                            stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    peak = 0
    while proc.poll() is None:
        rss = read_rss_kb(proc.pid)
        if rss > peak:
            peak = rss
        time.sleep(0.2)
    out_data = proc.stdout.read().decode(errors="replace")
    elapsed = time.time() - start
    os.path.exists(out) and os.remove(out)
    for line in out_data.splitlines():
        if any(k in line for k in ["Iter", "load", "save", "total", "Size", "DONE", "error", "Error", "OOM", "Exception"]):
            print(line)
    print(f"[{label}] peakRSS={peak} KB ({peak/1024:.1f} MB) elapsed={elapsed:.1f}s rc={proc.returncode}")

if __name__ == "__main__":
    main()
