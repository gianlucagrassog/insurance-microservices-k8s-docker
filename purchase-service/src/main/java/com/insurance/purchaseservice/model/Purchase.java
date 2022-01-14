package com.insurance.purchaseservice.model;

import java.util.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Purchase {
    @Id
    private ObjectId _id;
    private ObjectId user;
    private String description;
    private PurchaseStatus status;
    private double total;

    @JsonCreator
    public Purchase(ObjectId user, String description){
        this._id = new ObjectId();
        this.user = user;
        this.description = description;
        this.status = PurchaseStatus.PENDING;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ObjectId getUser() {
        return user;
    }

    public void setUser(ObjectId user) {
        this.user = user;
    }

    @JsonGetter("_id")
    public String get_id_string() {
        return _id.toHexString();
    }

    @JsonGetter("user")
    public String get_user_string() {
        return user.toHexString();
    }

    public PurchaseStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseStatus status) {
        this.status = status;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
