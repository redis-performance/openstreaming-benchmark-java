package com.redis;

import com.google.common.util.concurrent.RateLimiter;
import org.HdrHistogram.ConcurrentHistogram;
import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import redis.clients.jedis.Connection;
import redis.clients.jedis.JedisPooled;

import java.util.ArrayList;
import java.util.Random;

@CommandLine.Command(name = "openstreaming-benchmark-java", mixinStandardHelpOptions = true, version = "openstreaming benchmark Java 0.0.1")
public class BenchmarkRunner implements Runnable {

    @Option(names = {"-a", "--password"},
            description = "Redis password.")
    private String password = null;

    @Option(names = {"-s", "--server"},
            description = "Server hostname.", defaultValue = "localhost")
    private String hostname;
    @Option(names = {"-m", "--mode"},
            description = "Mode. Either 'producer' or 'consumer'.", defaultValue = "producer")
    private String mode;

    @Option(names = {"-c", "--clients"},
            description = "Number of clients.", defaultValue = "50")
    private Integer clients;

    @Option(names = {"--consumers-per-stream-max"},
            description = "Consumers per stream.", defaultValue = "50")
    private Integer consumersPerStreamMax;

    @Option(names = {"--consumers-per-stream-min"},
            description = "Consumers per stream.", defaultValue = "10")
    private Integer consumersPerStreamMin;


    @Option(names = {"-d", "--datasize"},
            description = "Datasize in bytes.", defaultValue = "70")
    private Integer dataSize;

    @Option(names = {"-p", "--port"},
            description = "Number of clients.", defaultValue = "6379")
    private Integer port;

    @Option(names = {"--rps"},
            description = "Max rps. If 0 no limit is applied and the DB is stressed up to maximum.", defaultValue = "0")
    private Integer rps;

    @Option(names = {"--keyspace-len"},
            description = "Keyspace len.", defaultValue = "1")
    private Integer keyspaceLen;

    @Option(names = {"--timeout"},
            description = "Jedis Pool timeout in millis", defaultValue = "300000")
    private Integer timeout;

    @Option(names = {"-n", "--number-requests"},
            description = "Number of requests.", defaultValue = "1000000")
    private Integer numberRequests;
    @Option(names = {"--seed"},
            description = "Random seed", defaultValue = "12345")
    private Integer seed;


    @Option(names = {"--retention-time-secs"},
            description = "Retention time secs", defaultValue = "3600")
    private Integer retentionTimeSecs;

    @Option(names = {"--max-stream-length"},
            description = "Retention time secs", defaultValue = "250000")
    private Integer maxStreamLength;


    @Option(names = "--verbose")
    private boolean verbose;

    public static void main(String[] args) {
        // By implementing Runnable or Callable, parsing, error handling and handling user
        // requests for usage help or version help can be done with one line of code.
        int exitCode = new CommandLine(new BenchmarkRunner()).execute(args);
        System.exit(exitCode);
    }

    public void run() {
        int requestsPerClient = numberRequests / clients;
        int rpsPerClient = rps / clients;
        int totalAccRps = 0;

        Random random = new Random();
        random.setSeed(seed);
        ZipfDistribution zipfDistribution = new ZipfDistribution(rps, 1);


        ConcurrentHistogram histogram = new ConcurrentHistogram(900000000L, 3);

        ArrayList<ProducerThread> threadsArray = new ArrayList<ProducerThread>();
        ArrayList<ConsumerThread> cthreadsArray = new ArrayList<ConsumerThread>();
        System.out.println("Starting benchmark with " + clients + " threads. Requests per thread " + requestsPerClient);
        int aliveClients = 0;
        GenericObjectPoolConfig<Connection> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(clients);
        poolConfig.setMaxIdle(clients);
        long previousRequestCount = 0;
        long startTime = System.currentTimeMillis();
        long previousTime = startTime;
        if (mode.equals("producer")) {
            System.out.println("Starting benchmark in producer mode...");
            for (int i = 0; i < clients; i++) {
                String topicName = String.format("topic-%d", i);
                ProducerThread clientThread;
                JedisPooled uredis = new JedisPooled(poolConfig, hostname, port, timeout, password);
                if (rps > 0) {
                    int clientRps = zipfDistribution.sample();
                    if (clientRps < 1 || totalAccRps >= rps) {
                        clientRps = 1;
                    }
                    totalAccRps += clientRps;
                    RateLimiter rateLimiter = RateLimiter.create(clientRps);
                    if (verbose) {
                        System.out.println("Client #" + i + " rps: " + clientRps);
                    }
                    clientThread = new ProducerThread(uredis, requestsPerClient, dataSize, topicName, retentionTimeSecs, maxStreamLength, histogram, verbose, rateLimiter);
                } else {
                    clientThread = new ProducerThread(uredis, requestsPerClient, dataSize, topicName, retentionTimeSecs, maxStreamLength, histogram, verbose);
                }
                clientThread.start();
                threadsArray.add(clientThread);
                aliveClients++;
            }
            System.out.println("Finished setting up benchmark in producer mode...");
        } else {
            System.out.println("Starting benchmark in consumer mode...");
            for (int i = 0; i < clients; i++) {
                String topicName = String.format("topic-%d", i);
                String consumerGroupName = String.format("consumer-group-%d:topic-%d", 1, i);
                int consumersForThisTopic = random.nextInt(consumersPerStreamMin, consumersPerStreamMax + 1);
                for (int groupConsumerId = 0; groupConsumerId < consumersForThisTopic; groupConsumerId++) {
                    String consumerName = "";
                    ConsumerThread clientThread;
                    JedisPooled uredis = new JedisPooled(poolConfig, hostname, port, timeout, password);
                    if (rps > 0) {
                        RateLimiter rateLimiter = RateLimiter.create(rpsPerClient);
                        clientThread = new ConsumerThread(uredis, requestsPerClient, topicName, consumerGroupName, consumerName, histogram, verbose, rateLimiter);
                    } else {
                        clientThread = new ConsumerThread(uredis, requestsPerClient, topicName, consumerGroupName, consumerName, histogram, verbose);
                    }
                    clientThread.start();
                    cthreadsArray.add(clientThread);
                    aliveClients++;
                }
            }
            System.out.println("Finished setting up benchmark in consumer mode...");
        }
        System.out.println("There is a total of " + aliveClients + " active connections...");

        while (aliveClients > 0) {
            long currentTotalCount = histogram.getTotalCount();
            long currentTime = System.currentTimeMillis();
            double currentp50onClient = histogram.getValueAtPercentile(50.0) / 1000.0f;
            double elapsedSecs = (currentTime - startTime) * 1000.0f;
            double elapsedSincePreviousSecs = (currentTime - previousTime) / 1000.0f;
            long countSincePreviousSecs = currentTotalCount - previousRequestCount;

            double currentRps = countSincePreviousSecs / elapsedSincePreviousSecs;
            System.out.format("Current RPS: %.3f commands/sec; Total requests %d ; Client p50 with RTT(ms): %.3f\n", currentRps, currentTotalCount, currentp50onClient);
            previousRequestCount = currentTotalCount;
            previousTime = currentTime;
            for (ProducerThread ct : threadsArray
            ) {
                if (!ct.isAlive()) {
                    aliveClients--;
                }
            }
            for (ConsumerThread ct : cthreadsArray
            ) {
                if (!ct.isAlive()) {
                    aliveClients--;
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        double totalDurationSecs = (System.currentTimeMillis() - startTime) / 1000.f;
        long totalCommands = histogram.getTotalCount();
        double overallRps = totalCommands / totalDurationSecs;
        System.out.println("################# RUNTIME STATS #################");
        System.out.println("Total Duration " + totalDurationSecs + " Seconds");
        System.out.println("Total Commands issued " + totalCommands);
        System.out.format("Overall RPS: %.3f commands/sec;\n", overallRps);
        System.out.println("Overall Client Latency summary (msec):");
        System.out.println("p50 (ms):" + histogram.getValueAtPercentile(50.0) / 1000.0f);
        System.out.println("p95 (ms):" + histogram.getValueAtPercentile(95.0) / 1000.0f);
        System.out.println("p99 (ms):" + histogram.getValueAtPercentile(99.0) / 1000.0f);
    }
}
