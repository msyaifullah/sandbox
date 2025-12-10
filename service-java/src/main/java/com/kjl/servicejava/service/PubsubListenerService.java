package com.kjl.servicejava.service;

import com.kjl.servicejava.repository.RedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service that listens to Redis pubsub and writes messages to Redis list for long polling.
 */
@Service
public class PubsubListenerService {
    private final RedisRepository redisRepository;
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final ConcurrentMap<String, MessageListener> activeListeners = new ConcurrentHashMap<>();

    @Autowired
    public PubsubListenerService(RedisRepository redisRepository, 
                                 RedisMessageListenerContainer redisMessageListenerContainer) {
        this.redisRepository = redisRepository;
        this.redisMessageListenerContainer = redisMessageListenerContainer;
    }

    /**
     * Start listening to pubsub channel for a query and write messages to Redis list.
     */
    public void startListener(String queryId) {
        // Don't start if already listening
        if (activeListeners.containsKey(queryId)) {
            return;
        }

        MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(Message message, byte[] pattern) {
                String payload = new String(message.getBody());
                
                // Write message to Redis list
                redisRepository.pushFlightResult(queryId, payload);
                
                // Increment count
                redisRepository.incrementFlightCount(queryId);
                
                System.out.println("Wrote pubsub message to Redis for query " + queryId);
            }
        };

        ChannelTopic topic = new ChannelTopic("flight:" + queryId);
        redisMessageListenerContainer.addMessageListener(listener, topic);
        activeListeners.put(queryId, listener);
        
        System.out.println("Started pubsub listener for query " + queryId);
    }

    /**
     * Stop listening to pubsub channel for a query.
     */
    public void stopListener(String queryId) {
        MessageListener listener = activeListeners.remove(queryId);
        if (listener != null) {
            ChannelTopic topic = new ChannelTopic("flight:" + queryId);
            redisMessageListenerContainer.removeMessageListener(listener, topic);
            System.out.println("Stopped pubsub listener for query " + queryId);
        }
    }
}
