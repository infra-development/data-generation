#!/bin/bash

set -e

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[1;34m'
CYAN='\033[1;36m'
NC='\033[0m' # No Color

TICK="${GREEN}✅${NC}"
CROSS="${RED}❌${NC}"
WARN="${YELLOW}⚠️${NC}"
WAIT="${CYAN}⏳${NC}"

is_running() {
    pgrep -f "$1" > /dev/null 2>&1
}

is_postgres_ready() {
    pg_isready > /dev/null 2>&1
}

log_step() {
    echo -e "${BLUE}$1${NC}"
}

log_ok() {
    echo -e "   ${TICK} $1"
}

log_err() {
    echo -e "   ${CROSS} $1"
}

log_warn() {
    echo -e "   ${WARN} $1"
}

log_wait() {
    echo -e "   ${WAIT} $1"
}

log_divider() {
    echo -e "${CYAN}-------------------------------------------${NC}"
}

stop_service() {
    local service="$1"
    local pattern="$2"
    local stop_cmd="$3"
    log_step "Checking $service status..."
    if is_running "$pattern"; then
        log_wait "$service is running. Stopping $service..."
        # Suppress warnings/info, keep only critical errors
        eval "$stop_cmd" > /tmp/${service// /_}_stop.log 2>&1
        sleep 3
        if is_running "$pattern"; then
            log_err "Failed to stop $service."
            log_warn "Check /tmp/${service// /_}_stop.log for more details."
            exit 1
        else
            log_ok "$service stopped successfully."
        fi
    else
        log_ok "$service is already stopped."
    fi
    log_divider
}

# 1. Stop ZooKeeper
stop_service "ZooKeeper" "QuorumPeerMain" "/opt/zookeeper/bin/zkServer.sh stop"

# 2. Stop HiveServer2
stop_service "HiveServer2" "org.apache.hive.service.server.HiveServer2" \
    "pkill -f org.apache.hive.service.server.HiveServer2"

# 3. Stop Hive Metastore
stop_service "Hive Metastore" "org.apache.hadoop.hive.metastore.HiveMetaStore" \
    "pkill -f org.apache.hadoop.hive.metastore.HiveMetaStore"

# 4. Stop YARN NodeManager
stop_service "YARN NodeManager" "org.apache.hadoop.yarn.server.nodemanager.NodeManager" \
    "\$HADOOP_HOME/sbin/yarn-daemon.sh stop nodemanager"

# 5. Stop YARN ResourceManager
stop_service "YARN ResourceManager" "org.apache.hadoop.yarn.server.resourcemanager.ResourceManager" \
    "\$HADOOP_HOME/sbin/yarn-daemon.sh stop resourcemanager"

# 6. Stop Hadoop DataNode
stop_service "Hadoop DataNode" "org.apache.hadoop.hdfs.server.datanode.DataNode" \
    "\$HADOOP_HOME/sbin/hadoop-daemon.sh stop datanode"

# 7. Stop Hadoop NameNode
stop_service "Hadoop NameNode" "org.apache.hadoop.hdfs.server.namenode.NameNode" \
    "\$HADOOP_HOME/sbin/hadoop-daemon.sh stop namenode"

# 8. Stop PostgreSQL
log_step "Checking PostgreSQL status..."
if is_postgres_ready; then
    log_wait "Stopping PostgreSQL..."
    sudo bash -c 'systemctl stop postgresql > /tmp/PostgreSQL_stop.log 2>&1'
    sleep 3
    if ! is_postgres_ready; then
        log_ok "PostgreSQL stopped successfully."
    else
        log_err "Failed to stop PostgreSQL."
        log_warn "Check /tmp/PostgreSQL_stop.log for details."
        exit 1
    fi
else
    log_ok "PostgreSQL is already stopped."
fi
log_divider

echo -e "${GREEN}🎉 All Hadoop, Hive, ZooKeeper, and PostgreSQL services are stopped!${NC}"
