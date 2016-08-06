package com.adhi.backstage.com.adhi.backstage.cardlist;

public class User {

    public String id, name, avatar, role, phone;

    public User() {

    }

    public User(String id, String name, String avatar, String role, String phone) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.role = role;
        this.phone = phone;
    }

    public String getID() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public String getRole() {
        return this.role;
    }

    public String getPhone() {
        return this.phone;
    }
}
