#!/bin/sh
APP_MAIN=com.pddon.framework.demo.easyapi.startup.Startup
PID=0

getPID(){
    javaps=`$JAVA_HOME/bin/jps -l | grep $APP_MAIN`
    if [ -n "$javaps" ]; then
        PID=`echo $javaps | awk '{print $1}'`
    else
        PID=0
    fi
}

getServerStatus(){
    getPID
    echo "================================================================================================================"
    if [ $PID -ne 0 ]; then
        echo "$APP_MAIN is running(PID=$PID)"
        echo "================================================================================================================"
    else
        echo "$APP_MAIN is not running"
        echo "================================================================================================================"
    fi
}
getServerStatus