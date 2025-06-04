# Hadoop Single Node Installation and Operation Guide (Ubuntu, Java 11, YARN, MapReduce)

---

## Table of Contents

1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Step 1: Install Java 11](#step-1-install-java-11)
4. [Step 2: Download and Install Hadoop](#step-2-download-and-install-hadoop)
5. [Step 3: Configure Hadoop](#step-3-configure-hadoop)
    - [Hadoop Environment Variables](#hadoop-environment-variables)
    - [core-site.xml](#core-sitexml)
    - [hdfs-site.xml](#hdfs-sitexml)
    - [mapred-site.xml](#mapred-sitexml)
    - [yarn-site.xml](#yarn-sitexml)
6. [Step 4: SSH Setup for Hadoop](#step-4-ssh-setup-for-hadoop)
7. [Step 5: Format the Hadoop Filesystem](#step-5-format-the-hadoop-filesystem)
8. [Step 6: Start Hadoop Services](#step-6-start-hadoop-services)
9. [Step 7: Verify Hadoop Services](#step-7-verify-hadoop-services)
10. [Step 8: Running a Sample MapReduce Job](#step-8-running-a-sample-mapreduce-job)
11. [Error Faced During Setup and Solution](#error-faced-during-setup-and-solution)
12. [Key Concepts and Learning Path](#key-concepts-and-learning-path)
13. [References](#references)

---

## Introduction

This guide provides a comprehensive walkthrough to installing and configuring Apache Hadoop in single-node (pseudo-distributed) mode on Ubuntu, using Java 11. It covers Hadoop Distributed File System (HDFS), YARN, MapReduce, and includes verification and troubleshooting steps. The guide also documents encountered errors and their solutions, and outlines foundational concepts for deeper learning.

---

## Prerequisites

- **Operating System:** Ubuntu Linux (tested on Ubuntu 20.04+, should work on similar distros)
- **User:** Non-root user with sudo privileges (e.g., `hadoop` user)
- **Internet Access:** Required for downloading packages
- **Basic Skills:** Familiarity with Linux terminal and editing text files

---

## Step 1: Install Java 11

Hadoop requires Java. We'll use OpenJDK 11.

```sh
sudo apt update
sudo apt install openjdk-11-jdk -y
java -version
```
- Ensure that `java -version` outputs Java 11.

---

## Step 2: Download and Install Hadoop

1. **Download Hadoop**

   Visit [https://hadoop.apache.org/releases.html](https://hadoop.apache.org/releases.html) and copy the download link for the desired version (e.g., 3.3.6):

   ```sh
   wget https://downloads.apache.org/hadoop/common/hadoop-3.3.6/hadoop-3.3.6.tar.gz
   ```

2. **Extract and Move**

   ```sh
   tar -xzf hadoop-3.3.6.tar.gz
   sudo mv hadoop-3.3.6 /opt/hadoop
   ```

---

## Step 3: Configure Hadoop

### Hadoop Environment Variables

Add the following lines to your `~/.bashrc` (or `~/.profile`):

```sh
export HADOOP_HOME=/opt/hadoop
export PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
```

Reload your shell:

```sh
source ~/.bashrc
```

### Update Hadoop's Java Path

Edit `/opt/hadoop/etc/hadoop/hadoop-env.sh` and set:

```sh
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
```

---

### core-site.xml

Edit `/opt/hadoop/etc/hadoop/core-site.xml`:

```xml
<configuration>
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://localhost:9000</value>
    </property>
</configuration>
```

---

### hdfs-site.xml

Edit `/opt/hadoop/etc/hadoop/hdfs-site.xml`:

```xml
<configuration>
    <property>
        <name>dfs.replication</name>
        <value>1</value>
    </property>
    <property>
        <name>dfs.namenode.name.dir</name>
        <value>file:///home/hadoop/hadoopdata/hdfs/namenode</value>
    </property>
    <property>
        <name>dfs.datanode.data.dir</name>
        <value>file:///home/hadoop/hadoopdata/hdfs/datanode</value>
    </property>
</configuration>
```
- Create the directories if they don't exist:
    ```sh
    mkdir -p ~/hadoopdata/hdfs/namenode
    mkdir -p ~/hadoopdata/hdfs/datanode
    ```

---

### mapred-site.xml

Copy the template if it doesn't exist:

```sh
cp /opt/hadoop/etc/hadoop/mapred-site.xml.template /opt/hadoop/etc/hadoop/mapred-site.xml
```

Edit `/opt/hadoop/etc/hadoop/mapred-site.xml`:

```xml
<configuration>
    <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
    </property>
    <!-- Required to fix MRAppMaster error (see error section) -->
    <property>
      <name>yarn.app.mapreduce.am.env</name>
      <value>HADOOP_MAPRED_HOME=/opt/hadoop</value>
    </property>
    <property>
      <name>mapreduce.map.env</name>
      <value>HADOOP_MAPRED_HOME=/opt/hadoop</value>
    </property>
    <property>
      <name>mapreduce.reduce.env</name>
      <value>HADOOP_MAPRED_HOME=/opt/hadoop</value>
    </property>
</configuration>
```
- Replace `/opt/hadoop` with your actual Hadoop directory if different.

---

### yarn-site.xml

Edit `/opt/hadoop/etc/hadoop/yarn-site.xml`:

```xml
<configuration>
    <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
    </property>
</configuration>
```

---

## Step 4: SSH Setup for Hadoop

Hadoop requires SSH even for single-node setups.

### Install and Enable SSH

```sh
sudo apt install openssh-server -y
sudo systemctl enable ssh
sudo systemctl start ssh
```

### Set Up Passwordless SSH

```sh
ssh-keygen -t rsa -P ""
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
ssh localhost
```

You should be able to SSH to localhost without a password.

---

## Step 5: Format the Hadoop Filesystem

**This step initializes HDFS and should only be run once (unless you want to erase all HDFS data):**

```sh
hdfs namenode -format
```

---

## Step 6: Start Hadoop Services

### Start HDFS (NameNode, DataNode, SecondaryNameNode)

```sh
start-dfs.sh
```

### Start YARN (ResourceManager, NodeManager)

```sh
start-yarn.sh
```

---

## Step 7: Verify Hadoop Services

### Check Java Processes

```sh
jps
```

You should see:
- NameNode
- DataNode
- SecondaryNameNode
- ResourceManager
- NodeManager

### Check Web Interfaces

- **HDFS NameNode:** [http://localhost:9870/](http://localhost:9870/)
- **YARN ResourceManager:** [http://localhost:8088/](http://localhost:8088/)

### Check HDFS via Command Line

```sh
hdfs dfs -ls /
```

---

## Step 8: Running a Sample MapReduce Job

### 1. Create Sample Input Data

```sh
mkdir ~/input
echo "Hello Hadoop" > ~/input/file1.txt
echo "Hadoop is running" > ~/input/file2.txt
```

### 2. Put the Data into HDFS

```sh
hdfs dfs -mkdir /input
hdfs dfs -put ~/input/* /input/
```

### 3. Run the WordCount Example

```sh
hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-*.jar wordcount /input /output
```

### 4. View the Output

```sh
hdfs dfs -ls /output
hdfs dfs -cat /output/part-r-00000
```

You should see output like:
```
Hadoop    2
Hello     1
is        1
running   1
```

---

## Error Faced During Setup and Solution

### Error

When running the MapReduce job, the following error was encountered:

```
Error: Could not find or load main class org.apache.hadoop.mapreduce.v2.app.MRAppMaster
Caused by: java.lang.ClassNotFoundException: org.apache.hadoop.mapreduce.v2.app.MRAppMaster

Please check whether your <HADOOP_HOME>/etc/hadoop/mapred-site.xml contains the below configuration:
<property>
  <name>yarn.app.mapreduce.am.env</name>
  <value>HADOOP_MAPRED_HOME=${full path of your hadoop distribution directory}</value>
</property>
...
```

#### **Cause**

- YARN containers did not have the Hadoop MapReduce libraries in their classpath because the environment variable `HADOOP_MAPRED_HOME` was not set.

#### **Solution**

Add the following properties to your `mapred-site.xml` file:

```xml
<property>
  <name>yarn.app.mapreduce.am.env</name>
  <value>HADOOP_MAPRED_HOME=/opt/hadoop</value>
</property>
<property>
  <name>mapreduce.map.env</name>
  <value>HADOOP_MAPRED_HOME=/opt/hadoop</value>
</property>
<property>
  <name>mapreduce.reduce.env</name>
  <value>HADOOP_MAPRED_HOME=/opt/hadoop</value>
</property>
```

Then, restart all Hadoop daemons:

```sh
stop-yarn.sh
stop-dfs.sh
start-dfs.sh
start-yarn.sh
```

This ensures that all MapReduce ApplicationMasters and containers can locate the required classes.

---

## Key Concepts and Learning Path

To confidently perform and expand on this setup, you should focus on the following concepts:

### 1. Hadoop Ecosystem
- **HDFS:** Core distributed filesystem for Hadoop.
- **YARN:** Resource management and job scheduling/execution framework.
- **MapReduce:** Programming model for distributed computation.

### 2. Hadoop Architecture
- **NameNode:** Stores HDFS metadata.
- **DataNode:** Stores actual data blocks.
- **ResourceManager:** Allocates cluster resources for jobs.
- **NodeManager:** Manages resources on a single node.

### 3. Linux System Administration
- Installing packages.
- File permissions and SSH keys.
- Editing configuration files.

### 4. Java and Environment Variables
- Installing and configuring Java.
- Setting `JAVA_HOME` and other environment variables.

### 5. Hadoop Configuration
- Understanding and editing XML config files:
    - `core-site.xml`, `hdfs-site.xml`, `mapred-site.xml`, `yarn-site.xml`

### 6. Troubleshooting Techniques
- Reading log files (found in `$HADOOP_HOME/logs/`).
- Interpreting Hadoop/YARN web UI for job and cluster status.
- Searching error messages for solutions.

### 7. Running and Verifying Jobs
- Creating HDFS directories.
- Uploading data to HDFS.
- Running sample and custom MapReduce jobs.
- Retrieving and interpreting job output.

### 8. (Advanced) Cluster Setup & Ecosystem Tools
- Multi-node Hadoop cluster setup.
- Integrating Spark, Hive, Pig, HBase, etc.

---

## References

- [Hadoop Official Documentation](https://hadoop.apache.org/docs/)
- [Hadoop: The Definitive Guide (Book)](https://www.oreilly.com/library/view/hadoop-the-definitive/9781491901687/)
- [YARN ResourceManager Web UI](http://localhost:8088/)
- [HDFS NameNode Web UI](http://localhost:9870/)

---

**End of Document**