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

shutdown(){
    getPID
    echo "================================================================================================================"
    if [ $PID -ne 0 ]; then
        echo -n "Stopping $APP_MAIN(PID=$PID)..."
        kill $PID
        if [ $? -eq 0 ]; then
            echo "[Success]"
            echo "================================================================================================================"
        else
            echo "[Failed]"
            echo "================================================================================================================"
        fi
        getPID
        if [ $PID -ne 0 ]; then
            shutdown
        fi
    else
        echo "$APP_MAIN is not running"
        echo "================================================================================================================"
    fi
}

shutdown