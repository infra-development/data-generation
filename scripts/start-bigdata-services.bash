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

# Functions
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

log_service() {
    local service="$1"
    local pattern="$2"
    local start_cmd="$3"
    log_step "Checking $service status..."
    if is_running "$pattern"; then
        log_ok "$service is already running."
    else
        log_wait "Starting $service..."
        # Hide deprecated warnings and info, but keep critical errors
        eval "$start_cmd" > /tmp/${service// /_}_start.log 2>&1 &
        sleep 4
        if is_running "$pattern"; then
            log_ok "$service started successfully."
        else
            log_err "Failed to start $service."
            log_warn "Check /tmp/${service// /_}_start.log for more details."
            exit 1
        fi
    fi
    log_divider
}

# 1. Start PostgreSQL
log_step "Checking PostgreSQL status..."
if is_postgres_ready; then
    log_ok "PostgreSQL is already running."
else
    log_wait "Starting PostgreSQL..."
    sudo systemctl start postgresql > /tmp/PostgreSQL_start.log 2>&1
    sleep 3
    if is_postgres_ready; then
        log_ok "PostgreSQL started successfully."
    else
        log_err "Failed to start PostgreSQL."
        log_warn "Check /tmp/PostgreSQL_start.log for details."
        exit 1
    fi
fi
log_divider

# 2. Start ZooKeeper
log_step "Checking ZooKeeper status..."
if is_running "org.apache.zookeeper.server.quorum.QuorumPeerMain"; then
    log_ok "ZooKeeper is already running."
else
    log_wait "Starting ZooKeeper..."
    /opt/zookeeper/bin/zkServer.sh start > /tmp/ZooKeeper_start.log 2>&1
    sleep 3
    if is_running "org.apache.zookeeper.server.quorum.QuorumPeerMain"; then
        log_ok "ZooKeeper started successfully."
    else
        log_err "Failed to start ZooKeeper."
        log_warn "Check /tmp/ZooKeeper_start.log for more details."
        exit 1
    fi
fi
log_divider


# 3. Start Hadoop NameNode
log_service "Hadoop NameNode" "org.apache.hadoop.hdfs.server.namenode.NameNode" \
    "\$HADOOP_HOME/sbin/hadoop-daemon.sh start namenode"

# 4. Start Hadoop DataNode
log_service "Hadoop DataNode" "org.apache.hadoop.hdfs.server.datanode.DataNode" \
    "\$HADOOP_HOME/sbin/hadoop-daemon.sh start datanode"

# 5. Start YARN ResourceManager
log_service "YARN ResourceManager" "org.apache.hadoop.yarn.server.resourcemanager.ResourceManager" \
    "\$HADOOP_HOME/sbin/yarn-daemon.sh start resourcemanager"

# 6. Start YARN NodeManager
log_service "YARN NodeManager" "org.apache.hadoop.yarn.server.nodemanager.NodeManager" \
    "\$HADOOP_HOME/sbin/yarn-daemon.sh start nodemanager"

# 7. Start Hive Metastore
log_service "Hive Metastore" "org.apache.hadoop.hive.metastore.HiveMetaStore" \
    "\$HIVE_HOME/bin/hive --service metastore &"

# 8. Start HiveServer2
log_service "HiveServer2" "org.apache.hive.service.server.HiveServer2" \
    "\$HIVE_HOME/bin/hive --service hiveserver2 &"

echo -e "${GREEN}🎉 All Hadoop, Hive and PostgreSQL services are up and running!${NC}"
