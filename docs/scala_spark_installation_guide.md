# Detailed Documentation: Scala and Apache Spark Installation & Troubleshooting (Post-Hadoop Setup)

This document outlines, in detail, the steps performed after Hadoop installation to set up Scala and Apache Spark, along with explanations, best practices, and challenges encountered.

---

## 1. Scala Installation

### 1.1 Downloading Scala

- **What is Scala?**  
  Scala is a powerful JVM language that Spark is built on. Spark’s “spark-shell” and many Spark applications run on Scala, so it must be installed.

- **Command used:**
  ```sh
  wget https://downloads.lightbend.com/scala/2.12.18/scala-2.12.18.tgz
  ```

- **Challenge:**  
  The download was stuck at “Connecting to ...”. This is a common issue when `wget` tries to use IPv6 in environments where IPv6 routing/firewalling is not set up correctly.

#### Solution: Forcing IPv4

- **Why force IPv4?**  
  Many networks, especially in data centers or behind some ISPs, don’t have proper IPv6 connectivity. When a tool tries IPv6 first (which is the default), it can hang or fail.

- **How we fixed it:**
  ```sh
  wget -4 https://downloads.lightbend.com/scala/2.12.18/scala-2.12.18.tgz
  ```
  The `-4` flag forces IPv4, resulting in a successful download.

---

### 1.2 Extracting and Moving the Scala Archive

- **Extraction command:**
  ```sh
  tar -xzf scala-2.12.18.tgz
  ```
  This decompresses the downloaded tarball, creating a `scala-2.12.18` folder.

- **Moving to /opt:**
  ```sh
  sudo mv scala-2.12.18 /opt/scala
  ```
    - `/opt` is a standard location for optional or third-party software, keeping the system organized and making upgrades easier.

---

### 1.3 Setting Environment Variables

- **Purpose:**  
  Environment variables let your shell and applications know where to find Scala and its executables.

- **Steps:**  
  Edit your `~/.bashrc` (user-specific shell config file) and add:
  ```sh
  export SCALA_HOME=/opt/scala
  export PATH=$PATH:$SCALA_HOME/bin
  ```
    - `SCALA_HOME` points to the Scala installation directory.
    - `PATH` is updated so you can run `scala`, `scalac`, etc. from any directory.

- **Applying changes:**
  ```sh
  source ~/.bashrc
  ```
  This reloads your shell configuration so changes take effect immediately.

---

### 1.4 Verifying Scala Installation

- **Checking version:**
  ```sh
  scala -version
  ```
  Confirms that the correct version of Scala is installed and available in your `PATH`.

- **Testing the REPL (Read-Eval-Print Loop):**
  ```sh
  scala
  ```
  Type:
  ```scala
  println("Hello, Scala!")
  ```
  You should see output:
  ```
  Hello, Scala!
  ```
  Exit with `:quit`.

---

## 2. Apache Spark Installation

### 2.1 Choosing the Correct Spark Version

- **Why version compatibility matters:**
    - Spark is built and tested with specific Hadoop and Scala versions.
    - Using mismatched versions can lead to subtle failures or missing features.

- **Our setup:**
    - Hadoop: 3.3.6
    - Scala: 2.12.18

- **Selected Spark:**  
  Spark 3.5.1 “Pre-built for Hadoop 3.3 and later”, which uses Scala 2.12 internally.

---

### 2.2 Downloading Spark

- **Initial problem:**  
  The attempted link to `dlcdn.apache.org` produced a 404 error; the file wasn't available at that mirror.

- **How we resolved it:**  
  We used the official Apache archives:
  ```sh
  wget https://archive.apache.org/dist/spark/spark-3.5.1/spark-3.5.1-bin-hadoop3.tgz
  ```
  **Tip:** Always use the [official Spark downloads page](https://spark.apache.org/downloads.html) to get the correct and current URL.

---

### 2.3 Extracting and Moving Spark

- **Extraction command:**
  ```sh
  tar -xzf spark-3.5.1-bin-hadoop3.tgz
  ```
    - This creates a directory: `spark-3.5.1-bin-hadoop3`

- **Moving to /opt for system-wide use:**
  ```sh
  sudo mv spark-3.5.1-bin-hadoop3 /opt/spark
  ```

---

### 2.4 Setting Up Spark Environment Variables

- **Purpose:**  
  To make Spark commands globally accessible and to define where Spark is installed.

- **Edit `~/.bashrc` and add:**
  ```sh
  export SPARK_HOME=/opt/spark
  export PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin
  ```
    - `SPARK_HOME`: Points to Spark’s install location.
    - `PATH`: Adding both `bin` and `sbin` allows access to user commands (like `spark-shell`, `spark-submit`) and admin/cluster management scripts (like `start-master.sh`, `start-worker.sh`).

- **Apply changes:**
  ```sh
  source ~/.bashrc
  ```

---

### 2.5 Configuring Spark for Hadoop/YARN Integration

- **Why is this important?**
    - By default, Spark runs in “local” mode (single machine).
    - For cluster/distributed operation, Spark needs to know how to talk to Hadoop’s HDFS and YARN ResourceManager.

- **Create/modify `spark-env.sh`:**
  ```sh
  cp $SPARK_HOME/conf/spark-env.sh.template $SPARK_HOME/conf/spark-env.sh
  nano $SPARK_HOME/conf/spark-env.sh
  ```

- **Add the following lines (edit paths for your system):**
  ```sh
  export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
  export HADOOP_CONF_DIR=/opt/hadoop/etc/hadoop
  ```
    - `JAVA_HOME`: Explicitly tells Spark which Java to use. This avoids ambiguity if multiple Java versions are installed.
    - `HADOOP_CONF_DIR`: Points Spark to Hadoop’s configuration files, enabling it to access HDFS and submit jobs to YARN.

#### What if you forget these variables?

- **If `JAVA_HOME` is missing:**
    - Spark may try to use the system default Java or fail with “JAVA_HOME is not set” errors.
    - If multiple Java versions are present, it may use an incompatible one, causing runtime errors.

- **If `HADOOP_CONF_DIR` is missing:**
    - Spark won’t find Hadoop/YARN configs, so it can’t access HDFS or submit to YARN.
    - Jobs may only run in standalone/local mode, not distributed.

---

### 2.6 Testing Spark Installation

- **Start Spark shell (Scala REPL with Spark context):**
  ```sh
  spark-shell
  ```
    - You should see startup logs and a prompt.
    - Try running a simple command to ensure Spark is working.

- **(Optional) Submit a job to YARN:**  
  Make sure Hadoop/YARN is running, then:
  ```sh
  spark-submit --master yarn --deploy-mode client --class org.apache.spark.examples.SparkPi $SPARK_HOME/examples/jars/spark-examples*.jar 10
  ```
    - Output should include something like:  
      `Pi is roughly 3.141592653589793`
    - This confirms Spark is integrated with Hadoop/YARN.

---

## 3. Understanding the Directory Structure

### Why add both `bin` and `sbin` to PATH?

- **`$SPARK_HOME/bin`:**
    - Contains Spark’s user-facing commands, such as:
        - `spark-shell` (Scala REPL for Spark)
        - `spark-submit` (submit Spark jobs)
        - `spark-sql` (run Spark SQL queries)
    - These are used by anyone running Spark jobs or interactive shells.

- **`$SPARK_HOME/sbin`:**
    - Contains system/cluster administration scripts, such as:
        - `start-master.sh` / `stop-master.sh` (manage Spark master daemon)
        - `start-worker.sh` / `stop-worker.sh` (manage Spark worker daemons)
        - `start-history-server.sh` (run Spark history server for job logs)
    - These are mainly for administrators managing Spark in standalone/cluster mode.

- **Adding both to `PATH` ensures:**
    - End users can run Spark jobs and interactive shells from anywhere.
    - Cluster administrators can manage Spark services easily.

---

## 4. Challenges and Troubleshooting

| Challenge                                  | Cause/Detail                                 | Solution                                              |
|---------------------------------------------|----------------------------------------------|-------------------------------------------------------|
| Download stuck at "Connecting to ..."       | Network/IPv6 issues                          | Use `wget -4` to force IPv4                           |
| 404 Not Found on Spark download             | File not available on main mirror            | Use Apache archive URL or check official downloads    |
| Spark/Scala not found after install         | PATH not updated or typo in env var          | Ensure correct PATH and variable names                |
| Spark can't find Hadoop/YARN or Java        | Missing `HADOOP_CONF_DIR` or `JAVA_HOME`     | Set these in `spark-env.sh`                           |
| Spark jobs not running on YARN              | Incomplete Hadoop config, missing env vars   | Set `HADOOP_CONF_DIR`, verify Hadoop/YARN is running  |
| Multiple Java versions causing issues       | Conflicting Java installations               | Set/verify `JAVA_HOME` points to compatible Java      |

---

## 5. Best Practices & Recommendations

- Always verify compatibility between Hadoop, Spark, and Scala versions before installing.
- Use `/opt` or another dedicated directory for third-party tools.
- Keep environment variables organized in `~/.bashrc` for user installs or `/etc/profile.d/` for system-wide.
- Always test installations with version checks and basic commands.
- Document any errors and their fixes for future reference.

---

## 6. Next Steps

- (Optional) Proceed with Apache Hive installation and integration.
- Validate Spark cluster operation by running distributed jobs.
- Set up monitoring/logging (e.g., Spark history server).

---

**This document serves as a comprehensive reference for the installation, configuration, and troubleshooting of Scala and Apache Spark following a Hadoop setup.**