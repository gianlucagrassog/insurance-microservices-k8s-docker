package com.insurance.purchaseservice.kafkalisteners;


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
public class ReceiptListener {

    @Autowired
    ReactivePurchaseRepository repository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value(value = "${KAFKA_TOPIC_5}")
    private String kafkaTopic5;

    @KafkaListener(topics = "${KAFKA_TOPIC_5}")
    public void listen(String message) {
        System.out.println("Received message " + message);

        String[] messageParts = message.split("\\|");

        if (messageParts[0].equals("mailsent")) {
            String purchase_id = messageParts[1];
            setPurchaseStatus(purchase_id,PurchaseStatus.CONFIRMED);
        }
        if (messageParts[0].equals("mailnotsent")) {
            String purchase_id = messageParts[1];
            setPurchaseStatus(purchase_id,PurchaseStatus.REJECTED);
        }
    }

    private void setPurchaseStatus(String purchase_id, PurchaseStatus status) {
        repository.existsById(new ObjectId(purchase_id)).flatMap(exists -> {
            if (exists) {
                repository.findById(new ObjectId(purchase_id)).flatMap(purchase -> {
                    purchase.setStatus(status);
                    return repository.save(purchase);
                }).subscribe();
            } else {
               System.out.println("Not Exists");
            }
            return Mono.just(exists);
        }).subscribe();
    }
}