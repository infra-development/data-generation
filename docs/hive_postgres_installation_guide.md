# Hive & Postgres Installation and Troubleshooting Guide

This guide provides detailed, step-by-step instructions for installing Apache Hive and PostgreSQL on Ubuntu, along with solutions to common issues encountered during the process.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Install Java (OpenJDK)](#install-java-openjdk)
3. [Install and Configure PostgreSQL](#install-and-configure-postgresql)
4. [Install Hadoop (required for Hive)](#install-hadoop-required-for-hive)
5. [Install Apache Hive](#install-apache-hive)
6. [Configure Hive with PostgreSQL Metastore](#configure-hive-with-postgresql-metastore)
7. [Start Services and Verify Installation](#start-services-and-verify-installation)
8. [Common Problems and Solutions](#common-problems-and-solutions)
9. [Best Practices](#best-practices)
10. [References](#references)

---

## Prerequisites

- Ubuntu Linux (tested on 20.04+)
- sudo/root access
- Internet access

---

## Install Java (OpenJDK)

Hive and Hadoop require Java. Install OpenJDK 11:

```bash
sudo apt update
sudo apt install openjdk-11-jdk -y
```

Verify installation:

```bash
java -version
```

Set JAVA_HOME in your environment (update path if needed):

```bash
echo "export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))" >> ~/.bashrc
source ~/.bashrc
```

---

## Install and Configure PostgreSQL

1. **Install PostgreSQL:**
    ```bash
    sudo apt update
    sudo apt install postgresql postgresql-contrib -y
    ```

2. **Start PostgreSQL and enable on boot:**
    ```bash
    sudo systemctl start postgresql
    sudo systemctl enable postgresql
    ```

3. **Switch to the postgres user:**
    ```bash
    sudo -i -u postgres
    ```

4. **Create a database and user for Hive:**
    ```bash
    createuser hiveuser --pwprompt
    createdb metastore_db -O hiveuser
    ```

   *Note: Remember the password for `hiveuser` for later use.*

5. **Grant privileges (if needed):**
    ```bash
    psql
    ALTER USER hiveuser CREATEDB;
    \q
    exit
    ```

6. **Allow password authentication:**
    - Edit `/etc/postgresql/<version>/main/pg_hba.conf`
    - Change `peer` or `ident` to `md5` for local connections:
      ```
      local   all             all                                   md5
      ```
    - Restart PostgreSQL:
      ```bash
      sudo systemctl restart postgresql
      ```

---

## Install Hadoop (required for Hive)

1. **Install Hadoop (single-node for testing):**
    - Download Hadoop:
      ```bash
      wget https://downloads.apache.org/hadoop/common/hadoop-3.3.6/hadoop-3.3.6.tar.gz
      tar -xzvf hadoop-3.3.6.tar.gz
      sudo mv hadoop-3.3.6 /opt/hadoop
      ```
    - Set environment variables in `~/.bashrc`:
      ```bash
      export HADOOP_HOME=/opt/hadoop
      export PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
      ```
      Then run:
      ```bash
      source ~/.bashrc
      ```

    - Configure Hadoop minimally for local use (edit `/opt/hadoop/etc/hadoop/core-site.xml` and `hdfs-site.xml`).

---

## Install Apache Hive

1. **Download Hive:**
    ```bash
    wget https://downloads.apache.org/hive/hive-3.1.3/apache-hive-3.1.3-bin.tar.gz
    tar -xzvf apache-hive-3.1.3-bin.tar.gz
    sudo mv apache-hive-3.1.3-bin /opt/hive
    ```

2. **Set environment variables in `~/.bashrc`:**
    ```bash
    export HIVE_HOME=/opt/hive
    export PATH=$PATH:$HIVE_HOME/bin
    ```
   Then run:
    ```bash
    source ~/.bashrc
    ```

3. **Download and place the PostgreSQL JDBC driver:**
    ```bash
    wget https://jdbc.postgresql.org/download/postgresql-42.7.3.jar
    cp postgresql-42.7.3.jar $HIVE_HOME/lib/
    ```

---

## Configure Hive with PostgreSQL Metastore

1. **Edit Hive configuration:**
    - Copy template file:
      ```bash
      cp $HIVE_HOME/conf/hive-default.xml.template $HIVE_HOME/conf/hive-site.xml
      ```
    - Edit `$HIVE_HOME/conf/hive-site.xml`, set the following properties:

    ```xml
    <property>
      <name>javax.jdo.option.ConnectionURL</name>
      <value>jdbc:postgresql://localhost:5432/metastore_db</value>
    </property>
    <property>
      <name>javax.jdo.option.ConnectionDriverName</name>
      <value>org.postgresql.Driver</value>
    </property>
    <property>
      <name>javax.jdo.option.ConnectionUserName</name>
      <value>hiveuser</value>
    </property>
    <property>
      <name>javax.jdo.option.ConnectionPassword</name>
      <value>your_password_here</value>
    </property>
    ```

2. **Initialize the Hive metastore schema:**
    ```bash
    schematool -dbType postgres -initSchema
    ```

   *If you get errors, check the `hive-site.xml` and that PostgreSQL is running.*

---

## Start Services and Verify Installation

1. **Start Hadoop services (if not already):**
    ```bash
    start-dfs.sh
    start-yarn.sh
    ```

2. **Start Hive Metastore and HiveServer2:**
    ```bash
    hive --service metastore &
    hive --service hiveserver2 &
    ```

3. **Connect to Hive:**
    ```bash
    beeline -u jdbc:hive2://localhost:10000 -n hiveuser
    ```

   *Or use the Hive CLI for basic operations:*
    ```bash
    hive
    ```

---

## Common Problems and Solutions

### 1. **Java system property not substituted in hive-site.xml**

- **Problem:**  
  Hive creates directories like `${system:java.io.tmpdir}` instead of substituting the value.
- **Solution:**  
  Ensure Hive is started via the correct scripts and Java is installed. If the literal string is still created, hardcode `/tmp/<user>` in `hive-site.xml`.

### 2. **AppImage FUSE error when running JetBrains Toolbox**

- **Problem:**  
  Error: `dlopen(): error loading libfuse.so.2 ... AppImages require FUSE to run`
- **Solution:**  
  Install FUSE with:
  ```bash
  sudo apt-get install libfuse2
  ```
  Then re-run the AppImage.

### 3. **PostgreSQL connection/authentication issues**

- **Problem:**  
  `FATAL: password authentication failed for user "hiveuser"` or similar.
- **Solution:**
    - Ensure `pg_hba.conf` is set to use `md5` for local connections.
    - Double-check username/password.
    - Restart PostgreSQL after config changes.

### 4. **Hive Metastore schema tool errors**

- **Problem:**  
  `schematool` fails to connect or initialize schema.
- **Solution:**
    - Ensure Postgres JDBC driver is present in `$HIVE_HOME/lib`.
    - Confirm database and user exist, and credentials are correct.
    - Check for typos in JDBC URL and config.

### 5. **Exporting VirtualBox OVA takes a long time**

- **Problem:**  
  Exporting VM to OVA is slow.
- **Solution:**
    - Be patient, it's normal for large VMs.
    - Close other apps to free resources.
    - Export to a fast SSD if possible.

### 6. **OVA file size and zipping**

- **Problem:**  
  OVA files are large and compress only slightly.
- **Solution:**
    - Use `zip` or 7-Zip to compress, but expect limited reduction (5–20%).
    - On Windows, right-click → Send to → Compressed (zipped) folder.

---

## Best Practices

- Always keep backup copies of your configuration files before editing.
- Use strong passwords for database users.
- Document DB credentials securely.
- Regularly back up your Hive metastore database.
- Use OVA export for VM backups and migrations.

---

## References

- [Apache Hive Documentation](https://cwiki.apache.org/confluence/display/Hive/GettingStarted)
- [PostgreSQL Official Docs](https://www.postgresql.org/docs/)
- [Hadoop Official Docs](https://hadoop.apache.org/docs/)
- [VirtualBox User Manual](https://www.virtualbox.org/manual/UserManual.html)
- [AppImage FUSE FAQ](https://github.com/AppImage/AppImageKit/wiki/FUSE)

---