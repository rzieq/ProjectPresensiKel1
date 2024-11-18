package com.example.projectpresensikel1.Model;

public class User {
    private String userId;
    private String name;
    private String kelas;

    public User() {
    }

    public User(String userId, String name, String kelas) {
        this.userId = userId;
        this.name = name;
        this.kelas = kelas;
    }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getKelas() { return kelas; }

    public void setKelas(String kelas) { this.kelas = kelas; }
}
