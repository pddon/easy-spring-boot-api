#!/bin/sh
#-------------------------------------------------------------------------------------------------------------
#该脚本的使用方式为-->[sh startup.sh]
#该脚本可在服务器上的任意目录下执行,不会影响到日志的输出位置等
#-------------------------------------------------------------------------------------------------------------
#初始化配置信息环境变量
DIR=$(cd "$(dirname "$0")"; pwd)
APP_HOME=${DIR}/..
APP_CONF=${APP_HOME}/conf
CLASSPATH=
# 外置配置（conf/application.yml）以“附加配置位置”叠加加载，而不是把 conf 目录放进 -classpath：
# Boot 加载 classpath:/application.yml 时只取第一个命中的资源，conf 一旦进入 classpath 会整体遮蔽
# jar 内的同名文件，导致 jar 内的 basePackages/channels 等配置全部丢失；
# additional-location 是叠加语义：只覆盖同名键，jar 内其余配置继续生效，且优先级高于 jar 内配置。
CONF_ARGS=
if [ -f "${APP_CONF}/application.yml" ] || [ -f "${APP_CONF}/application.properties" ]; then
    CONF_ARGS="--spring.config.additional-location=file:${APP_CONF}/"
fi
#日志可配置路径
if [ ! -n "$APP_LOG" ]; then
    export APP_LOG=${APP_HOME}/logs
fi

APP_MAIN=com.pddon.framework.demo.easyapi.startup.Startup

JAVA_OPTS="$JAVA_OPTS -Duser.timezone=GMT+0 -Dfile.encoding=UTF-8"

#-------------------------------------------------------------------------------------------------------------
# JVM 堆/GC/Metaspace/直接内存参数（降内存 A 类配置）
#
# 设计原则：不写死固定的大 -Xmx，而是按“可用内存的比例”推导，换机器/换容器不用改脚本。
# 取值依据：
#   - 堆占可用内存 50%（可用 JAVA_HEAP_PERCENT 环境变量调整）：
#     剩下 50% 要留给 Metaspace(<=256m)、直接内存(<=256m)、线程栈、CodeCache、
#     GC 自身结构与 mmap/文件缓存，堆占比过高会把这些挤到交换区甚至直接 OOM。
#   - 原脚本没有任何 -X 参数：默认堆 = 宿主机物理内存的 1/4，在容器里会按宿主机内存计算，
#     这是“内存占用偏高”的首要原因。
#   - 容器感知：JDK10+ 与 8u191+ 支持 -XX:MaxRAMPercentage（按 cgroup 内存上限算堆），优先使用；
#     更老的 JDK 不识别该参数会直接 Unrecognized VM option 启动失败，因此按版本回退到手工计算。
#-------------------------------------------------------------------------------------------------------------
JAVA_HEAP_PERCENT=${JAVA_HEAP_PERCENT:-50}

JAVA_VER=$("$JAVA_HOME/bin/java" -version 2>&1 | awk -F'"' '/version/ {print $2}')
JAVA_MAJOR=$(echo "$JAVA_VER" | awk -F'.' '{ if ($1 == "1") print $2; else print $1 }')
JAVA_UPDATE=$(echo "$JAVA_VER" | sed -n 's/^1\.8\.0_\([0-9][0-9]*\).*/\1/p')
case "$JAVA_MAJOR" in
    ''|*[!0-9]*) JAVA_MAJOR=8 ;;   # 探测失败时按最低版本处理，走安全回退分支
esac

USE_RAM_PERCENT=0
if [ "$JAVA_MAJOR" -ge 10 ] 2>/dev/null; then
    USE_RAM_PERCENT=1
elif [ "$JAVA_MAJOR" -eq 8 ] && [ "${JAVA_UPDATE:-0}" -ge 191 ] 2>/dev/null; then
    USE_RAM_PERCENT=1
fi

if [ "$USE_RAM_PERCENT" = "1" ]; then
    # 容器内按 cgroup 内存上限计算：初始堆=上限堆，避免运行期反复扩堆（扩堆会带来额外分配与 Full GC）
    JAVA_OPTS="$JAVA_OPTS -XX:InitialRAMPercentage=${JAVA_HEAP_PERCENT} -XX:MaxRAMPercentage=${JAVA_HEAP_PERCENT}"
else
    # 旧 JDK 回退：读取 cgroup v2 / cgroup v1 内存上限，取不到再读物理内存
    MEM_LIMIT=""
    if [ -f /sys/fs/cgroup/memory.max ]; then
        MEM_LIMIT=$(cat /sys/fs/cgroup/memory.max 2>/dev/null)
    elif [ -f /sys/fs/cgroup/memory/memory.limit_in_bytes ]; then
        MEM_LIMIT=$(cat /sys/fs/cgroup/memory/memory.limit_in_bytes 2>/dev/null)
    fi
    case "$MEM_LIMIT" in
        ''|*[!0-9]*) MEM_LIMIT="" ;;   # 内容为 "max" 或读取失败时视作无上限
    esac
    # cgroup 未设置上限时会是一个接近 2^63 的哨兵值，同样视作无上限
    if [ -n "$MEM_LIMIT" ] && [ "$MEM_LIMIT" -ge 9223372036854771712 ] 2>/dev/null; then
        MEM_LIMIT=""
    fi
    if [ -z "$MEM_LIMIT" ]; then
        MEM_LIMIT=$(awk '/MemTotal/ {printf "%d", $2 * 1024}' /proc/meminfo 2>/dev/null)
    fi
    MEM_MB=$(awk -v m="${MEM_LIMIT:-0}" 'BEGIN {printf "%d", m / 1024 / 1024}')
    HEAP_MB=$(( ${MEM_MB:-0} * JAVA_HEAP_PERCENT / 100 ))
    [ "$HEAP_MB" -lt 256 ] && HEAP_MB=256
    JAVA_OPTS="$JAVA_OPTS -Xms${HEAP_MB}m -Xmx${HEAP_MB}m"
fi

# Metaspace：默认 MaxMetaspaceSize 无上限，动态代理/热加载异常时会持续膨胀不回收；
# 本框架（Spring + swagger + 各种 starter）实测在 80~150MB，故初始高水位 128m
# （避免启动初期因元数据不足反复触发 Full GC），硬上限 256m（约 2 倍余量）。
JAVA_OPTS="$JAVA_OPTS -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m"

# 直接内存：Undertow 开启 direct-buffers=true 后使用堆外缓冲区，而 JDK 默认 MaxDirectMemorySize
# 约等于堆上限；按 io=4 × buffer-size(8KB) × 每线程缓冲池，256m 足够，超限会抛 OutOfMemoryError: Direct buffer memory。
JAVA_OPTS="$JAVA_OPTS -XX:MaxDirectMemorySize=256m"

# GC：Java 8 默认 ParallelGC，老年代只扩不缩，堆空闲后不归还操作系统；
# 显式启用 G1（可按 Region 回收并把空 Region 归还）。Java 9+ 默认已是 G1，无需重复设置。
# MaxGCPauseMillis=200 是 G1 的默认停顿目标，显式写出便于后续调优时定位。
if [ "$JAVA_MAJOR" -lt 9 ] 2>/dev/null; then
    JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
fi

# OOM 时保留堆转储（沿用原配置），便于事后用 MAT 定位
JAVA_OPTS="$JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${APP_HOME}/logs/dump.hprof"

echo "JAVA_VERSION=$JAVA_VER USE_RAM_PERCENT=$USE_RAM_PERCENT HEAP_PERCENT=${JAVA_HEAP_PERCENT}%"


echo "JAVA_HOME="$JAVA_HOME
echo "CLASSPATH="$CLASSPATH
echo "JAVA_OPTS="$JAVA_OPTS

#-------------------------------------------------------------------------------------------------------------
#   程序开始
#-------------------------------------------------------------------------------------------------------------
for appJar in "$APP_HOME"/lib/*.jar;
do
   if [ -z "$CLASSPATH" ]; then
       CLASSPATH="$appJar"
   else
       CLASSPATH="$CLASSPATH":"$appJar"
   fi
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
        nohup $JAVA_HOME/bin/java $JAVA_OPTS -classpath $CLASSPATH $APP_MAIN $CONF_ARGS > ${APP_HOME}/logs/nohup.log 2>&1 &
        for i in $(seq 5)
        do
        sleep 0.8
        printf "."
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