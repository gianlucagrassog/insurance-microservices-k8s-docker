package com.insurance.userservice.kafkalisteners;

import com.insurance.userservice.controllers.UserController;
import com.insurance.userservice.repository.ReactiveUserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Mono;

import java.util.List;

public class PurchaseListener {

    @Autowired
    ReactiveUserRepository repository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value(value = "${KAFKA_TOPIC_1}")
    private String kafkaTopic1;

    @KafkaListener(topics="${KAFKA_TOPIC_1}")
    public void listen(String message) {
        System.out.println("Received message " + message);

        String[] messageParts = message.split("\\|");

        if (messageParts[0].equals("PolicyPurchase: Checking User")) {
            String uid = messageParts[1];
            repository.existsById(new ObjectId(uid)).flatMap(exists -> {
                kafkaTemplate.send(kafkaTopic1, (exists?"UserExists|":"UserNotExists|") + messageParts[2]);
                return Mono.just(exists);
            }).subscribe();
        }
    }
}
