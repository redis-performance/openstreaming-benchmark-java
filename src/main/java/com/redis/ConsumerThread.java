package com.redis;

import com.google.common.util.concurrent.RateLimiter;
import com.redis.streams.AckMessage;
import com.redis.streams.TopicEntry;
import com.redis.streams.command.serial.ConsumerGroup;
import com.redis.streams.exception.TopicNotFoundException;
import org.HdrHistogram.ConcurrentHistogram;
import org.HdrHistogram.Histogram;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;

public class ConsumerThread extends Thread {
    private final int requests;
    private final JedisPooled rg;
    private final String topicName;
    private final String consumerGroupName;
    private final String consumerName;
    private final Histogram histogram;
    private final RateLimiter rateLimiter;
    private final boolean verbose;


    ConsumerThread(JedisPooled rg, Integer requests, String topicName, String consumerGroupName, String consumerName, ConcurrentHistogram histogram, boolean verbose) {
        super("Client thread");
        this.requests = requests;
        this.rg = rg;
        this.topicName = topicName;
        this.consumerGroupName = consumerGroupName;
        this.consumerName = consumerName;
        this.histogram = histogram;
        this.rateLimiter = null;
        this.verbose = verbose;

    }

    ConsumerThread(JedisPooled rg, Integer requests, String topicName, String consumerGroupName, String consumerName, ConcurrentHistogram histogram, boolean verbose, RateLimiter perClientRateLimiter) {
        super("Client thread");
        this.requests = requests;
        this.rg = rg;
        this.topicName = topicName;
        this.consumerGroupName = consumerGroupName;
        this.consumerName = consumerName;
        this.histogram = histogram;
        this.rateLimiter = perClientRateLimiter;
        this.verbose = verbose;
    }

    public void run() {
        ConsumerGroup consumer = new ConsumerGroup(this.rg, topicName, consumerGroupName);

        if (this.verbose) {
            System.out.println("Created consumer for topic '" + topicName + "' and group '" + consumerGroupName + "'.\n");
        }
        StreamEntryID id = null;
        for (int i = 0; i < requests; i++) {
            if (rateLimiter != null) {
                // blocks the executing thread until a permit is available.
                rateLimiter.acquire(1);
            }
            long startTime = System.nanoTime();
            TopicEntry consumedMessage = null;
            try {
                //consume a message from the topic:
                consumedMessage = consumer.consume(consumerName);
            } catch (TopicNotFoundException e) {
                System.out.println(">>consumer FAILED to consume a message<<");
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            // we only consider an operation if a message was consumed
            if (consumedMessage != null) {
                AckMessage ack = new AckMessage(consumedMessage.getStreamName(), consumedMessage.getGroupName(), consumedMessage.getId().getStreamEntryId());
                boolean success = consumer.acknowledge(ack);
                long durationMicros = (System.nanoTime() - startTime) / 1000;
                histogram.recordValue(durationMicros);
                if (this.verbose) {
                    System.out.println("Consumed message id with: " + consumedMessage.getId().getStreamEntryId());
                }
            }

        }
    }
}
