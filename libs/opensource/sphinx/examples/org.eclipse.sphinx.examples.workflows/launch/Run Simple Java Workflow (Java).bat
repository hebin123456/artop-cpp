@echo off
W:
cd W:\eclipse-sphinx\org.eclipse.sphinx\examples\org.eclipse.sphinx.examples.workflows\launch

set ECLIPSE_HOME=C:/Eclipse/committers-neon/eclipse
set APPLICATION_NAME=org.eclipse.sphinx.emf.mwe.dynamic.headless.WorkflowRunner
set APPLICATION_ARGS=-workflow /org.eclipse.sphinx.examples.workflows/src/org/eclipse/sphinx/examples/workflows/simple/java/SimpleJavaWorkflow.java
set PROGRAM_ARGS=-consoleLog
set VM_ARGS=
set WORKSPACE_LOC=W:/eclipse-sphinx/.runtime
set DEV_WORKSPACE_URL=file:W:/eclipse-sphinx
set DEV_LAUNCH_CONFIG_NAME=Run Simple Java Workflow (Java)

call ../../../plugins/org.eclipse.sphinx.platform.launching/resources/launch/runEclipseApplication.bat
pause