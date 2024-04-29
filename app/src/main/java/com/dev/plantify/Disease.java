package com.dev.plantify;

// Disease.java
public class Disease {
    private String name;
    private String info;

    public Disease(String name, String info) {
        this.name = name;
        this.info = info;
    }

    public String getName() {
        return name;
    }

    public String getInfo() {
        return info;
    }
}
