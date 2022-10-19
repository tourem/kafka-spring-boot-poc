package com.larbotech.demo.kafka.consumer;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.MemberDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

@Service
public class KafkaConsumer {

    @Autowired
    private AdminClient adminClient;

    private static final Logger logger = Logger.getLogger(KafkaConsumer.class.getName());

    @KafkaListener(topics = "mytopic", groupId = "group_id")
    public void consume(String message) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info(String.format("Consumed --------------------- message -> %s", message));
        showConsumers();
    }

    public void showConsumers()
            throws InterruptedException, ExecutionException, TimeoutException {
        String group = "group_id";
        List<String> groups = Collections.singletonList(group);

        Collection<MemberDescription> memberDescriptions = adminClient
                .describeConsumerGroups(groups) // DescribeConsumerGroupsResult
                .describedGroups()              // Map<String, KafkaFuture<ConsumerGroupDescription>>
                .get(group)                     // KafkaFuture<ConsumerGroupDescription>
                .get(2, TimeUnit.SECONDS)       // ConsumerGroupDescription
                .members();                     // Collection<MemberDescription>

        logger.info(String.format("nb ---------------------- memberDescriptions -> %s", memberDescriptions.size()));
        memberDescriptions.forEach(nd -> logger.info(String.format(" #################### Consumed id -> %s", nd.toString())));
    }
}