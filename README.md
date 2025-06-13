# data-generation

Run the application with the following command:

```bash
spark-submit \
  --class com.project.app.FinanceDataGeneratorApp \
  --master yarn \
  --deploy-mode cluster \
  /home/hadoop/jars/data-generation-1.0-SNAPSHOT-all.jar \
  hdfs \
  hdfs://namenode-host:8020 \
  json \
  /path/to/config.json 
```

## Usage

### For HDFS + JSON
FinanceDataGeneratorApp hdfs hdfs://host:port json /path/to/config.json

### For HDFS + YAML
FinanceDataGeneratorApp hdfs hdfs://host:port yaml /path/to/config.yaml

### For ZooKeeper + JSON
FinanceDataGeneratorApp zookeeper zk-host:2181 json /path/to/config.json

### For ZooKeeper + YAML
FinanceDataGeneratorApp zookeeper zk-host:2181 yaml /path/to/config.yaml


{
"businessDate": "2025-06-13",
"threshold": 1000,
"generateAccountData": true
}