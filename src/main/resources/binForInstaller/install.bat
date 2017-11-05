@echo off

rem // Parameter To Run installer
set REL_PATH_BIN_TO_HOME=..\
set REL_PATH_HOME_TO_LIB=.\lib   

rem // Save current directory and change to target directory
set PATH_TO_BIN_DIR=%~dp0
pushd %PATH_TO_BIN_DIR%
pushd %REL_PATH_BIN_TO_HOME%
set PATH_TO_INSTALLER_HOME=%CD%
pushd %REL_PATH_HOME_TO_LIB%
set PATH_TO_LIB_DIR=%CD%
popd
popd
popd


:init
@rem Get command-line arguments, handling Windowz variants

if not "%OS%" == "Windows_NT" goto win9xME_args
if "%@eval[2+2]" == "4" goto 4NT_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*
goto execute

:4NT_args
@rem Get arguments from the 4NT Shell from JP Software
set CMD_LINE_ARGS=%$

:execute
java -Xss50m -cp "%PATH_TO_LIB_DIR%\*" install.Starter --installer ask install %CMD_LINE_ARGS%

:pause
set /p DUMMY=Hit [ENTER] to continue...
