package com.redis;

import com.google.common.util.concurrent.RateLimiter;
import com.redis.streams.command.serial.ProducerState;
import com.redis.streams.command.serial.SerialTopicConfig;
import com.redis.streams.command.serial.TopicManager;
import com.redis.streams.command.serial.TopicProducer;
import com.redis.streams.exception.TopicNotFoundException;
import org.HdrHistogram.ConcurrentHistogram;
import org.HdrHistogram.Histogram;
import org.apache.commons.text.RandomStringGenerator;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;

import java.util.Map;

public class ProducerThread extends Thread {
    private final int requests;
    private final JedisPooled rg;
    private final String topicName;
    private final Histogram histogram;
    private final RateLimiter rateLimiter;
    private final SerialTopicConfig topicConfig;
    private final boolean verbose;

    private final int datasize;


    ProducerThread(JedisPooled rg, Integer requests, Integer datasize, String topicName, long maxStreamLength, long retentionTimeSeconds, ConcurrentHistogram histogram, boolean verbose) {
        super("Client thread");
        this.requests = requests;
        this.datasize = datasize;
        this.rg = rg;
        this.topicName = topicName;
        this.histogram = histogram;
        this.rateLimiter = null;
        this.verbose = verbose;

        // Create another topic with custom configuration
        long streamCycleSeconds = 86400; // Create a new stream after one day, regardless of the current stream's length
        long checkIntervalSeconds = 1; // Interval at which a producer checks for new streams.
        this.topicConfig = new SerialTopicConfig(
                topicName,
                retentionTimeSeconds,
                maxStreamLength,
                streamCycleSeconds,
                checkIntervalSeconds,
                SerialTopicConfig.TTLFuzzMode.RANDOM // Adds a random number of seconds to the TTL to prevent concurrent expires
        );
    }

    ProducerThread(JedisPooled rg, Integer requests, Integer datasize, String topicName, long maxStreamLength, long retentionTimeSeconds, ConcurrentHistogram histogram, boolean verbose, RateLimiter perClientRateLimiter) {
        super("Client thread");
        this.requests = requests;
        this.datasize = datasize;
        this.rg = rg;
        this.topicName = topicName;
        this.histogram = histogram;
        this.rateLimiter = perClientRateLimiter;
        this.verbose = verbose;

        // Create another topic with custom configuration
        long streamCycleSeconds = 86400; // Create a new stream after one day, regardless of the current stream's length
        long checkIntervalSeconds = 1; // Interval at which a producer checks for new streams.
        this.topicConfig = new SerialTopicConfig(
                topicName,
                retentionTimeSeconds,
                maxStreamLength,
                streamCycleSeconds,
                checkIntervalSeconds,
                SerialTopicConfig.TTLFuzzMode.RANDOM // Adds a random number of seconds to the TTL to prevent concurrent expires
        );

    }

    public void run() {
        // Generates a 20 code point string, using only the letters a-z
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('a', 'z').build();
        String payload = generator.generate(datasize);

        // Create a new topic with default configuration
        if (this.verbose) {
            System.out.println("BEGIN TEST (RESPONSE TO PING) -->   " + this.rg.ping());
        }
        TopicManager manager = TopicManager.createTopic(this.rg, this.topicConfig);
        if (this.verbose) {
            System.out.println("Created topic with config: " + manager.getConfig());
        }
        //create Producer to publish incoming messages to a Topic
        TopicProducer producer = new TopicProducer(this.rg, this.topicName);
        if (this.verbose) {
            System.out.println("Created producer for topic: " + producer.getTopicName());
        }
        StreamEntryID id = null;
        for (int i = 0; i < requests; i++) {
            if (rateLimiter != null) {
                // blocks the executing thread until a permit is available.
                rateLimiter.acquire(1);
            }
            if (this.verbose) {
                ProducerState producerState = producer.getState();
                if (producerState != null) {
                    System.out.println("\tproducer writing a message to :" + producerState.getCurrentStreamName());
                }
            }

            long startTime = System.nanoTime();
            try {
                //Write a message to our topic:
                id = producer.produce(Map.of("field", payload));
            } catch (TopicNotFoundException e) {
                System.out.println(">>producer FAILED to write a message<<");
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            long durationMicros = (System.nanoTime() - startTime) / 1000;
            histogram.recordValue(durationMicros);
            if (this.verbose) {
                System.out.println("Published message id with: " + id.toString());
            }
        }
    }
}
