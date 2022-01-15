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

public class PolicyListener {

    @Autowired
    ReactiveUserRepository repository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value(value = "${KAFKA_TOPIC_3}")
    private String kafkaTopic3;

    @KafkaListener(topics="${KAFKA_TOPIC_3}")
    public void listen(String message) {
        System.out.println("Received message " + message);
        String[] messageParts = message.split("\\|");
        if (messageParts[0].equals("GetUserInfo")) {
            String purchase_id = messageParts[1];
            String uid = messageParts[2];
            String opt_price_str = messageParts[3];
            String pid = messageParts[4];
            repository.findById(new ObjectId(uid)).flatMap(u -> {
                kafkaTemplate.send(kafkaTopic3, "UserData|" + purchase_id + "|" + String.valueOf(u.getAge()) + "|" + String.valueOf(u.getBmclass())+ "|" + opt_price_str + "|" + pid+"|"+u.getName());
                return Mono.just(u);
            }).subscribe();
        }
    }

}


