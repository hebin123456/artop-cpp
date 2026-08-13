@echo off
W:
cd W:\eclipse-sphinx\org.eclipse.sphinx\examples\org.eclipse.sphinx.examples.workflows\launch

set ECLIPSE_HOME=C:/Eclipse/committers-neon/eclipse
set APPLICATION_NAME=org.eclipse.sphinx.platform.launching.Launcher
set APPLICATION_ARGS=-launch "/org.eclipse.sphinx.examples.workflows/launch/Simple Java Workflow (Java).launch"
set PROGRAM_ARGS=
set VM_ARGS=
set WORKSPACE_LOC=W:/eclipse-sphinx/.runtime
set DEV_WORKSPACE_URL=file:W:/eclipse-sphinx
set DEV_LAUNCH_CONFIG_NAME=Launcher

call ../../../plugins/org.eclipse.sphinx.platform.launching/resources/launch/runEclipseApplication.bat
pause