package com.stark.exam.model;

public class User {
    private int id;
    private String username;
    private String fullName;
    private String role;
    private String erpId;
    private int year;
    private String department;
    private String section; // <--- NEW FIELD

    public User() {}

    public User(int id, String username, String fullName, String role, String erpId, int year, String department, String section) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.erpId = erpId;
        this.year = year;
        this.department = department;
        this.section = section;
    }
    private String photoPath; // Add this field

    // Add Getter and Setter
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }


    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getErpId() { return erpId; }
    public void setErpId(String erpId) { this.erpId = erpId; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    @Override
    public String toString() { return fullName + " (" + role + ")"; }
}
