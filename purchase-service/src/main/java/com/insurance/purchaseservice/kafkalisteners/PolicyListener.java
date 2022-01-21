package com.insurance.purchaseservice.kafkalisteners;

import java.util.*;
import com.insurance.purchaseservice.model.Purchase;
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
public class PolicyListener {

    @Autowired
    ReactivePurchaseRepository repository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value(value = "${KAFKA_TOPIC_2}")
    private String kafkaTopic2;

    @Value(value = "${KAFKA_TOPIC_4}")
    private String kafkaTopic4;

    @KafkaListener(topics="${KAFKA_TOPIC_2}")
    public void listen(String message) {
        System.out.println("Received message " + message);

        String[] messageParts = message.split("\\|");

        if (messageParts[0].equals("PolicyExists")) {
            String purchase_id = messageParts[1];
            Purchase o = repository.findById(new ObjectId(purchase_id)).block();
            String uid = o.get_user_string();
            setPurchaseStatus(message, purchase_id, uid, PurchaseStatus.POLICY_CONFIRMED, "PolicyConfirmed|");
        }

        if (messageParts[0].equals("PolicyNotExists")) {
            String purchase_id = messageParts[1];
            String uid = messageParts[2];
            setPurchaseStatus(message, purchase_id, uid, PurchaseStatus.REJECTED, "PurchaseRejected|");
        }

        if (messageParts[0].equals("PurchaseConfirmed")) {
            String purchase_id = messageParts[1];
            String total_str = messageParts[2];
            String policyType = messageParts[3];
            String optionals_price = messageParts[4];
            String user_name = messageParts[5];
            double total = Double.parseDouble(total_str);
            setPurchaseAttributes(message, purchase_id, total, PurchaseStatus.PRICE_CALCULATED, "PriceCalculated|", policyType, optionals_price, user_name);
        }

    }

    private void setPurchaseStatus(String message, String purchase_id, String uid, PurchaseStatus status, String key) {
        repository.existsById(new ObjectId(purchase_id)).flatMap(exists -> {
            if (exists) {
                repository.findById(new ObjectId(purchase_id)).flatMap(purchase -> {
                    purchase.setStatus(status);
                    Set<String> optionals_list = purchase.getOptionals_list();
                    String optionals_list_str = "";
                    for(String value : optionals_list) {
                        optionals_list_str = optionals_list_str + "," + value;
                    }
                    optionals_list_str = optionals_list_str.substring(1);
                    kafkaTemplate.send(kafkaTopic2, key + purchase_id + "|" + uid + "|" + optionals_list_str + "|" + purchase.getPolicy());
                    return repository.save(purchase);
                }).subscribe();
            } else {
                kafkaTemplate.send(kafkaTopic2,"BadMessage||" + message);
            }
            return Mono.just(exists);
        }).subscribe();
    }

    private void setPurchaseAttributes(String message, String purchase_id, double total, PurchaseStatus status, String key, String policyType, String optionalsPrice, String userName) {
        repository.existsById(new ObjectId(purchase_id)).flatMap(exists -> {
            if (exists) {
                repository.findById(new ObjectId(purchase_id)).flatMap(purchase -> {
                    purchase.setTotal(total);
                    purchase.setStatus(status);
                    return repository.save(purchase);
                }).subscribe();
                kafkaTemplate.send(kafkaTopic4, purchase_id+"|"+userName+"|"+policyType+"|"+total+"|"+optionalsPrice);

            } else {
                //kafkaTemplate.send(kafkaTopic4,"BadMessage||" + message);
            }
            return Mono.just(exists);
        }).subscribe();
//        Purchase o = repository.findById(new ObjectId(purchase_id)).block();
//        o.setTotal(total);
//        o.setStatus(status);
        //kafkaTemplate.send(kafkaTopic4, key + purchase_id + ...);
    }

}
