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
import com.insurance.policyservice.model.Policy;

public class UserListener {

    @Autowired
    ReactivePolicyRepository repository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value(value = "${KAFKA_TOPIC_2}")
    private String kafkaTopic2;

    @Value(value = "${KAFKA_TOPIC_3}")
    private String kafkaTopic3;

    @KafkaListener(topics="${KAFKA_TOPIC_3}")
    public void listen(String message) {
        System.out.println("Received message " + message);
        String[] messageParts = message.split("\\|");
        if (messageParts[0].equals("UserData")) {
            String purchase_id_str = messageParts[1];
            String age_str = messageParts[2];
            String bmClass_str = messageParts[3];
            String optionals_price_str = messageParts[4];
            String pid = messageParts[5];
            String user_name = messageParts[6];
            Policy p = repository.findById(new ObjectId(pid)).block();
            String policyType = p.getType();
            double optionals_price = Double.parseDouble(optionals_price_str);
            int age = Integer.parseInt(age_str);
            int bmClass = Integer.parseInt(bmClass_str);
            double total = totalCalculator(optionals_price, policyType, age, bmClass);
            String total_str = String.valueOf(total);
            kafkaTemplate.send(kafkaTopic2, "PurchaseConfirmed|" + purchase_id_str + "|" + total_str + "|" + policyType +"|"+optionals_price_str+"|"+user_name);
        }
    }

    private double totalCalculator(double optionals_price, String policyType, int age, int bmClass) {
        double totalPrice = optionals_price;
        if(age<23) {
            totalPrice += 100;
        }
        if(policyType.compareTo("bonus malus")==0) {
            totalPrice += (25 * bmClass);
        }
        else if(policyType.compareTo("temporanea")==0){
            totalPrice += 200;
        }
        return totalPrice;
    }

}
