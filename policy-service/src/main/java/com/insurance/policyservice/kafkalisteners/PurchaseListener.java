package com.insurance.policyservice.kafkalisteners;

import java.util.*;
import com.insurance.policyservice.controllers.PolicyController;
import com.insurance.policyservice.repository.ReactivePolicyRepository;
import com.insurance.policyservice.repository.ReactiveOptionalRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Mono;
import com.insurance.policyservice.model.Optional;

public class PurchaseListener {

    @Autowired
    ReactivePolicyRepository repository;

    @Autowired
    ReactiveOptionalRepository optional_repository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value(value = "${KAFKA_TOPIC_2}")
    private String kafkaTopic2;

    @Value(value = "${KAFKA_TOPIC_3}")
    private String kafkaTopic3;

    @KafkaListener(topics="${KAFKA_TOPIC_2}")
    public void listen(String message) {
        System.out.println("Received message " + message);
        String[] messageParts = message.split("\\|");

        if (messageParts[0].equals("PolicyPurchase: Checking Policy")) {
            String purchase_id = messageParts[1];
            String pid = messageParts[2];
            repository.existsById(new ObjectId(pid)).flatMap(exists -> {
                if(exists) {
                    kafkaTemplate.send(kafkaTopic2, "PolicyExists|" + purchase_id);
                }
                else {
                    kafkaTemplate.send(kafkaTopic2, "PolicyNotExists|" + purchase_id);
                }
                return Mono.just(exists);
            }).subscribe();
        }

        if (messageParts[0].equals("PolicyConfirmed")) {
            String purchase_id = messageParts[1];
            String uid = messageParts[2];
            String optionals_list_str = messageParts[3];
            String pid = messageParts[4];
            String[] strParts = optionals_list_str.split("\\,");
            Set<String> optionals_list = new HashSet<String>();
            for (int i = 0; i < strParts.length; i++) {
                String s = strParts[i];
                optionals_list.add(s);
            }
            double price = optionalsPriceCalculator(optionals_list);
            String optionals_price_str = String.valueOf(price);
            kafkaTemplate.send(kafkaTopic3, "GetUserInfo|" + purchase_id + "|" + uid + "|" + optionals_price_str + "|" + pid);
        }
    }

    private double optionalsPriceCalculator(Set<String> optionals_list) {
        double price = 0.0;
        for(String optional_id : optionals_list) {
            Optional opt = optional_repository.findById(new ObjectId(optional_id)).block();
            price += opt.getPrice();
        }
        return price;
    }

}
