package com.insurance.purchaseservice.kafkalisteners;

import java.util.*;
import com.insurance.purchaseservice.model.PurchaseStatus;
import com.insurance.purchaseservice.repository.ReactivePurchaseRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserListener {

    @Autowired
    ReactivePurchaseRepository repository;


    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value(value = "${KAFKA_TOPIC_1}")
    private String kafkaTopic1;

    @Value(value = "${KAFKA_TOPIC_2}")
    private String kafkaTopic2;

    @KafkaListener(topics="${KAFKA_TOPIC_1}")
    public void listen(String message) {
        System.out.println("Received message " + message);

        String[] messageParts = message.split("\\|");

        if (messageParts[0].equals("UserExists")) {
            String purchase_id = messageParts[1];
            setPurchaseStatus(message, purchase_id, PurchaseStatus.USER_CONFIRMED, "UserConfirmed|");
        }

        if (messageParts[0].equals("UserNotExists")) {
            String purchase_id = messageParts[1];
            setPurchaseStatus(message, purchase_id, PurchaseStatus.REJECTED, "PurchaseRejected|");
        }

    }

    private void setPurchaseStatus(String message, String purchase_id, PurchaseStatus status, String key) {

        repository.existsById(new ObjectId(purchase_id)).flatMap(exists -> {
            if (exists) {
                repository.findById(new ObjectId(purchase_id)).flatMap(purchase -> {
                    purchase.setStatus(status);
                    kafkaTemplate.send(kafkaTopic2, "PolicyPurchase: Checking Policy|" + purchase.get_id_string() + "|" + purchase.getPolicy());
                    return repository.save(purchase);
                }).subscribe();
            } else {
               // kafkaTemplate.send(kafkaTopic2,"BadMessage||" + message);
            }
            return Mono.just(exists);
        }).subscribe();
    }

}
