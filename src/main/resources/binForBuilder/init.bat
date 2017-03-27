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

java -cp "%PATH_TO_LIB_DIR%/*" install.Start -init
