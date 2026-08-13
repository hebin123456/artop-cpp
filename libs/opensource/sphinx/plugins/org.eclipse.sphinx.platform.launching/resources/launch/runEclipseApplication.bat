rem <copyright>
rem
rem Copyright (c) 2016 itemis and others.
rem All rights reserved. This program and the accompanying materials
rem are made available under the terms of the Eclipse Public License v2.0
rem which accompanies this distribution, and is available at
rem https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
rem
rem Contributors:
rem     itemis - Initial API and implementation
rem
rem </copyright>
rem
rem ====================================================================
rem Eclipse headless application runner
rem ====================================================================
rem
rem Supports binary applications being part of an Eclipse product as well as
rem applications under development that are located in a development workspace
rem and have an associated launch configuration
rem
rem Environment variables:
rem
rem   ECLIPSE_HOME:
rem     the installation location of Eclipse product used to run the headless
rem     application
rem     (required)
rem
rem   APPLICATION_NAME:
rem     the identifier of the headless application to run
rem     (required)
rem
rem   APPLICATION_ARGS:
rem     the arguments to be passed to the headless application
rem     (optional)
rem
rem  PROGRAM_ARGS:
rem    command line arguments processed by various parts of the Eclipse runtime (e.g., -consoleLog)
rem    (optional)
rem
rem  VM_ARGS:
rem    command line arguments to be passed to the JVM the Eclipse product runs in (e.g., -Xss4m)
rem    (optional)
rem
rem   WORKSPACE_LOC:
rem     the location of the (runtime) workspace to be used by the headless
rem     application
rem     (optional, defaults to %ECLIPSE_HOME%\workspace)
rem
rem   DEV_WORKSPACE_URL:
rem     the location of the development workspace that contains the plug-in
rem     project with the headless application under development
rem     (required for headless applications under development)
rem
rem   DEV_LAUNCH_CONFIG_NAME:
rem     a launch configuration for the headless application under development
rem     in the development workspace
rem     (required for headless applications under development)
rem
@echo off
if "%ECLIPSE_HOME%" == "" (
	echo ECLIPSE_HOME must be set
	goto end
)

if "%APPLICATION_NAME%" == "" (
	echo APPLICATION_NAME must be set
	goto end
)

if "%WORKSPACE_LOC%" == "" goto devModeArgs
set DATA_ARG=-data %WORKSPACE_LOC%

:devModeArgs
if "%DEV_WORKSPACE_URL%" == "" goto run
if "%DEV_LAUNCH_CONFIG_NAME%" == "" goto run
set CONFIGURATION_ARG=-configuration "%DEV_WORKSPACE_URL%/.metadata/.plugins/org.eclipse.pde.core/%DEV_LAUNCH_CONFIG_NAME%/"
set DEV_ARG=-dev "%DEV_WORKSPACE_URL%/.metadata/.plugins/org.eclipse.pde.core/%DEV_LAUNCH_CONFIG_NAME%/dev.properties"

:run
%ECLIPSE_HOME%\eclipsec.exe %VM_ARGS% -noSplash %DATA_ARG% %CONFIGURATION_ARG% %DEV_ARG% %PROGRAM_ARGS% -application %APPLICATION_NAME% %APPLICATION_ARGS%

:end