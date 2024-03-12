#!/bin/sh
#-------------------------------------------------------------------------------------------------------------
#该脚本的使用方式为-->[sh startup.sh]
#该脚本可在服务器上的任意目录下执行,不会影响到日志的输出位置等
#-------------------------------------------------------------------------------------------------------------
#初始化配置信息环境变量
DIR=$(cd "$(dirname "$0")"; pwd)
APP_HOME=${DIR}/..
CLASSPATH=$APP_HOME/conf
#日志可配置路径
if [ ! -n "$APP_LOG" ]; then
    export APP_LOG=${APP_HOME}/logs
fi

APP_MAIN=com.pddon.framework.demo.easyapi.startup.Startup

JAVA_OPTS="$JAVA_OPTS -Duser.timezone=GMT+0 -Dfile.encoding=UTF-8"
JAVA_OPTS="$JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${APP_HOME}/logs/dump.hprof"


echo "JAVA_HOME="$JAVA_HOME
echo "CLASSPATH="$CLASSPATH
echo "JAVA_OPTS="$JAVA_OPTS

#-------------------------------------------------------------------------------------------------------------
#   程序开始
#-------------------------------------------------------------------------------------------------------------
for appJar in "$APP_HOME"/lib/*.jar;
do
   CLASSPATH="$CLASSPATH":"$appJar"
done
PID=0

getPID(){
    javaps=`$JAVA_HOME/bin/jps -l | grep $APP_MAIN`
    if [ -n "$javaps" ]; then
        PID=`echo $javaps | awk '{print $1}'`
    else
        PID=0
    fi
}

startup(){
    getPID
    echo "================================================================================================================"
    if [ $PID -ne 0 ]; then
        echo "$APP_MAIN already started(PID=$PID)"
        echo "================================================================================================================"
    else
        echo -n "Starting $APP_MAIN"
         if [ ! -d "$APP_LOG" ]; then
            mkdir "$APP_LOG"
         fi
         if [ ! -d "${APP_HOME}/logs" ]; then
            mkdir "${APP_HOME}/logs"
         fi
        nohup $JAVA_HOME/bin/java $JAVA_OPTS -classpath $CLASSPATH $APP_MAIN > ${APP_HOME}/logs/nohup.log 2>&1 &
        for i in $(seq 5)
        do
        sleep 0.8
        echo -e  ".\c"
        done
        getPID
        if [ $PID -ne 0 ]; then
            echo "(PID=$PID)...[Success]"
            echo "================================================================================================================"
        else
            echo "[Failed]"
            echo "================================================================================================================"
        fi
    fi
}

startup