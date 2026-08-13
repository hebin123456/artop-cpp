@echo off
setlocal
for %%a in ("%~dp0..\..") do set GIT_ROOT=%%~fa

docker run --rm -it ^
  -v %USERPROFILE%\.m2:/root/.m2 ^
  -v %GIT_ROOT%:/workspace ^
  -e WORKSPACE=/workspace ^
  -w /workspace ^
  -p 8000:8000 ^
  maven:3.6.1-jdk-8 ^
  mvn -P platform-oxygen -f releng/org.eclipse.sphinx.releng.builds verify

endlocal