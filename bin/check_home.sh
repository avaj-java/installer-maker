#!/usr/bin/env sh
######################################################################################################################################################
#
#
#   Build
#
#
######################################################################################################################################################

####################################################################################################
#####
##### PATH VARIABLES
#####
####################################################################################################
##################################################
##### Get Relative Path For This App Automatically
##################################################
THIS_FILE="`basename $0`"
THIS_RELATIVE_PATH="`dirname $0`"
THIS_RELATIVE_HOME_PATH="${THIS_RELATIVE_PATH%'/bin'}"
THIS_RELATIVE_HOME_PATH="${THIS_RELATIVE_PATH%'\\bin'}"
THIS_RELATIVE_HOME_PATH="${THIS_RELATIVE_PATH%'\bin'}"
THIS_RELATIVE_HOME_PATH="${THIS_RELATIVE_PATH%'bin'}"
# HOME/bin에서 startup.sh를 실행함.
if [ "$THIS_RELATIVE_HOME_PATH" = "." ]; then
	THIS_RELATIVE_HOME_PATH=".."
# HOME에서 bin/startup.sh를 실행함.
elif [ "$THIS_RELATIVE_HOME_PATH" = "" ]; then
	THIS_RELATIVE_HOME_PATH="."
fi
##################################################
##### Get Absolute Path For This App Automatically
##################################################
# THIS_ABSOLUTE_DIR_PATH=$(cd "${0%/*}/.."; pwd)
THIS_ABSOLUTE_DIR_PATH=$(cd "$(dirname "$0")/"; pwd)
##################################################
##### SYSTEM VALUE SETUP
##################################################
NOW_DATE="`date +%Y%m%d`"
NOW_TIME="`date +%H%M%S`"
THIS_DIR="$THIS_ABSOLUTE_DIR_PATH"
LOG_PATH="$THIS_HOME/logs"
LOG_FILE="$LOG_PATH/log_${NOW_DATE}_${NOW_TIME}.log"
BAK_PATH="$THIS_HOME/bak"
NOW_PATH=`pwd`

##### CHECK OS
CHECK_OS="`uname -s`"
case "$CHECK_OS" in
    Darwin*)    THIS_OS="MAC";;
    Linux*)     THIS_OS="LIN";;
    MINGW32*)   THIS_OS="WIN";;
    MINGW64*)   THIS_OS="WIN";;
    CYGWIN*)  THIS_OS="WIN";;
esac

#echo "OS CHECK = ${CHECK_OS}"
#echo "OS is ${THIS_OS}"
#echo "This Dir is ${THIS_DIR}"



####################################################################################################
#####
##### MAIN SCRIPTS
#####
####################################################################################################
echo "- INSTALLER_MAKER_HOME: $INSTALLER_MAKER_HOME"
echo "- HOYA_HOME: $HOYA_HOME"