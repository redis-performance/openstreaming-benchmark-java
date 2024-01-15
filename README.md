# openstreaming-benchmark-java

 make it easy to benchmark distributed streaming systems in Java. a port of https://github.com/redis-performance/openstreaming-benchmark.


## installation notes

Note that to add the redis-streams-java jar file with all of it's
declared dependencies you should execute the following command from
the directory holding this pom.xml file:


```bash
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=redis-streams-java-0.1.2.jar
mvn package

java -jar target/openstreaming-benchmark-java-1.0-SNAPSHOT-jar-with-dependencies.jar --help
```
## Usage

```bash
$ java -jar target/openstreaming-benchmark-java-1.0-SNAPSHOT-jar-with-dependencies.jar --help
Usage: openstreaming-benchmark-java [-hV] [--verbose] [-a=<password>]
                                    [-c=<clients>]
                                    [--consumers-per-stream-max=<consumersPerStr
                                    eamMax>]
                                    [--consumers-per-stream-min=<consumersPerStr
                                    eamMin>] [-d=<dataSize>]
                                    [--keyspace-len=<keyspaceLen>] [-m=<mode>]
                                    [--max-stream-length=<maxStreamLength>]
                                    [-n=<numberRequests>] [-p=<port>]
                                    [--retention-time-secs=<retentionTimeSecs>]
                                    [--rps=<rps>] [-s=<hostname>]
                                    [--seed=<seed>] [--timeout=<timeout>]
  -a, --password=<password> Redis password.
  -c, --clients=<clients>   Number of clients.
      --consumers-per-stream-max=<consumersPerStreamMax>
                            Consumers per stream.
      --consumers-per-stream-min=<consumersPerStreamMin>
                            Consumers per stream.
  -d, --datasize=<dataSize> Datasize in bytes.
  -h, --help                Show this help message and exit.
      --keyspace-len=<keyspaceLen>
                            Keyspace len.
  -m, --mode=<mode>         Mode. Either 'producer' or 'consumer'.
      --max-stream-length=<maxStreamLength>
                            Retention time secs
  -n, --number-requests=<numberRequests>
                            Number of requests.
  -p, --port=<port>         Number of clients.
      --retention-time-secs=<retentionTimeSecs>
                            Retention time secs
      --rps=<rps>           Max rps. If 0 no limit is applied and the DB is
                              stressed up to maximum.
  -s, --server=<hostname>   Server hostname.
      --seed=<seed>         Random seed
      --timeout=<timeout>   Jedis Pool timeout in millis
  -V, --version             Print version information and exit.
      --verbose
```

### Sample producer with 10 topics

```
$ java -jar target/openstreaming-benchmark-java-1.0-SNAPSHOT-jar-with-dependencies.jar -n 1000000 -c 10 --mode producer --retention-time-secs 120
Starting benchmark with 10 threads. Requests per thread 100000
Starting benchmark in producer mode...
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Finished setting up benchmark in producer mode...
There is a total of 10 active connections...
Current RPS: 0.000 commands/sec; Total requests 0 ; Client p50 with RTT(ms): 0.000
Current RPS: 147500.988 commands/sec; Total requests 148386 ; Client p50 with RTT(ms): 0.050
Current RPS: 149797.000 commands/sec; Total requests 298183 ; Client p50 with RTT(ms): 0.053
Current RPS: 89122.873 commands/sec; Total requests 387395 ; Client p50 with RTT(ms): 0.060
Current RPS: 87734.000 commands/sec; Total requests 475129 ; Client p50 with RTT(ms): 0.071
Current RPS: 90112.883 commands/sec; Total requests 565332 ; Client p50 with RTT(ms): 0.081
Current RPS: 91034.961 commands/sec; Total requests 656458 ; Client p50 with RTT(ms): 0.087
Current RPS: 92097.000 commands/sec; Total requests 748555 ; Client p50 with RTT(ms): 0.090
Current RPS: 92669.326 commands/sec; Total requests 841317 ; Client p50 with RTT(ms): 0.093
Current RPS: 93698.297 commands/sec; Total requests 935109 ; Client p50 with RTT(ms): 0.094
Current RPS: 64826.171 commands/sec; Total requests 1000000 ; Client p50 with RTT(ms): 0.094
################# RUNTIME STATS #################
Total Duration 11.097000122070312 Seconds
Total Commands issued 1000000
Overall RPS: 90114.444 commands/sec;
Overall Client Latency summary (msec):
p50 (ms):0.094
p95 (ms):0.137
p99 (ms):0.25
```

### Sample consumer with 10 topics and a total of > 300 consumers distributed across them

```
$ java -jar target/openstreaming-benchmark-java-1.0-SNAPSHOT-jar-with-dependencies.jar -n 1000000 -c 10 --mode consumer 
Starting benchmark with 10 threads. Requests per thread 100000
Starting benchmark in consumer mode...
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Finished setting up benchmark in consumer mode...
There is a total of 323 active connections...
Current RPS: 11296.473 commands/sec; Total requests 11850 ; Client p50 with RTT(ms): 10.191
Current RPS: 11639.801 commands/sec; Total requests 23548 ; Client p50 with RTT(ms): 18.079
Current RPS: 12590.409 commands/sec; Total requests 36151 ; Client p50 with RTT(ms): 18.415
Current RPS: 12634.000 commands/sec; Total requests 48785 ; Client p50 with RTT(ms): 18.479
Current RPS: 12516.966 commands/sec; Total requests 61327 ; Client p50 with RTT(ms): 18.479
Current RPS: 12235.764 commands/sec; Total requests 73575 ; Client p50 with RTT(ms): 18.703
Current RPS: 12323.676 commands/sec; Total requests 85911 ; Client p50 with RTT(ms): 18.783
Current RPS: 12379.620 commands/sec; Total requests 98303 ; Client p50 with RTT(ms): 18.767
Current RPS: 12662.337 commands/sec; Total requests 110978 ; Client p50 with RTT(ms): 18.767
Current RPS: 12457.542 commands/sec; Total requests 123448 ; Client p50 with RTT(ms): 18.751
Current RPS: 12271.728 commands/sec; Total requests 135732 ; Client p50 with RTT(ms): 18.815
Current RPS: 12793.206 commands/sec; Total requests 148538 ; Client p50 with RTT(ms): 18.799
Current RPS: 12816.368 commands/sec; Total requests 161380 ; Client p50 with RTT(ms): 18.767
Current RPS: 9833.166 commands/sec; Total requests 171223 ; Client p50 with RTT(ms): 19.023
Current RPS: 11871.000 commands/sec; Total requests 183094 ; Client p50 with RTT(ms): 19.087
Current RPS: 9869.130 commands/sec; Total requests 192973 ; Client p50 with RTT(ms): 19.231
Current RPS: 10459.540 commands/sec; Total requests 203443 ; Client p50 with RTT(ms): 19.407
Current RPS: 9878.121 commands/sec; Total requests 213331 ; Client p50 with RTT(ms): 19.567
Current RPS: 11186.813 commands/sec; Total requests 224529 ; Client p50 with RTT(ms): 19.679
Current RPS: 8259.740 commands/sec; Total requests 232797 ; Client p50 with RTT(ms): 19.839
(...)
```
