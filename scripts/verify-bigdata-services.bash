#!/bin/bash

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[1;34m'
CYAN='\033[1;36m'
NC='\033[0m'

TICK="${GREEN}✅${NC}"
CROSS="${RED}❌${NC}"

log_status() {
    local service="$1"
    local running="$2"
    if [ "$running" -eq 0 ]; then
        echo -e "${TICK} ${BLUE}${service}${NC} is ${GREEN}RUNNING${NC}"
    else
        echo -e "${CROSS} ${BLUE}${service}${NC} is ${RED}STOPPED${NC}"
    fi
}

echo -e "${CYAN}Verifying Hadoop, Hive, and PostgreSQL Services${NC}"
echo "------------------------------------------------"

# 1. ZooKeeper
pgrep -f 'org.apache.zookeeper.server.quorum.QuorumPeerMain' > /dev/null 2>&1
log_status "ZooKeeper" $?

# 2. PostgreSQL
pg_isready > /dev/null 2>&1
log_status "PostgreSQL" $?

# 3. Hadoop NameNode
pgrep -f 'org.apache.hadoop.hdfs.server.namenode.NameNode' > /dev/null 2>&1
log_status "Hadoop NameNode" $?

# 4. Hadoop DataNode
pgrep -f 'org.apache.hadoop.hdfs.server.datanode.DataNode' > /dev/null 2>&1
log_status "Hadoop DataNode" $?

# 5. YARN ResourceManager
pgrep -f 'org.apache.hadoop.yarn.server.resourcemanager.ResourceManager' > /dev/null 2>&1
log_status "YARN ResourceManager" $?

# 6. YARN NodeManager
pgrep -f 'org.apache.hadoop.yarn.server.nodemanager.NodeManager' > /dev/null 2>&1
log_status "YARN NodeManager" $?

# 7. Hive Metastore
pgrep -f 'org.apache.hadoop.hive.metastore.HiveMetaStore' > /dev/null 2>&1
log_status "Hive Metastore" $?

# 8. HiveServer2
pgrep -f 'org.apache.hive.service.server.HiveServer2' > /dev/null 2>&1
log_status "HiveServer2" $?

echo "------------------------------------------------"
echo -e "${CYAN}Verification Complete.${NC}"
