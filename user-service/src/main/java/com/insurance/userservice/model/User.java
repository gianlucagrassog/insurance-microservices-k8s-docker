package com.insurance.userservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class User {
    @Id
    private ObjectId _id;
    private String name;
    private int age;
    private int bmclass;

    @JsonCreator
    public User(String name, int age, int bmclass) {
        this._id = new ObjectId();
        this.name = name;
        this.age = age;
        this.bmclass = bmclass;
    }

    public ObjectId get_id() {
        return _id;
    }

    @JsonGetter("_id")
    public String get_id_string() {
        return _id.toHexString();
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getBmclass() {
        return bmclass;
    }

    public void setBmclass(int bmclass) {
        this.bmclass = bmclass;
    }

}
