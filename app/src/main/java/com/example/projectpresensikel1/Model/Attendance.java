package com.example.projectpresensikel1.Model;

public class Attendance { //1 usages
    private String userId; //3 usages
    private String name; //3 usages
    private String status; //3 usages
    private String lokasi; //3 usages
    private String waktuKehadiran; //3 usages
    private String fotoBase64; //3 usages

    public Attendance() { //no usages
    }

    public Attendance(String userId, String name, String status, String lokasi, String waktuKehadiran, String fotoBase64) { //1 usage
        this.userId = userId;
        this.name = name;
        this.status = status;
        this.lokasi = lokasi;
        this.waktuKehadiran = waktuKehadiran;
        this.fotoBase64 = fotoBase64;
    }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public String getLokasi() { return lokasi; }

    public void setLokasi(String lokasi) { this.lokasi = lokasi; }

    public String getWaktuKehadiran() { return waktuKehadiran; }

    public void setWaktuKehadiran(String waktuKehadiran) { this.waktuKehadiran = waktuKehadiran; }

    public String getFotoBase64() { return fotoBase64; }

    public void setFotoBase64(String fotoBase64) { this.fotoBase64 = fotoBase64; }

}