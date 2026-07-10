package com.stumanager.model;

public class Student {
    private int id;
    private String studentId; // MSSV
    private String fullName;
    private String className;
    private String subject;
    private Double tuitionFee;

    public Student() {
    }

    public Student(String studentId, String fullName, String className, String subject, Double tuitionFee) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.className = className;
        this.subject = subject;
        this.tuitionFee = tuitionFee;
    }

    public Student(int id, String studentId, String fullName, String className, String subject, Double tuitionFee) {
        this.id = id;
        this.studentId = studentId;
        this.fullName = fullName;
        this.className = className;
        this.subject = subject;
        this.tuitionFee = tuitionFee;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Double getTuitionFee() {
        return tuitionFee;
    }

    public void setTuitionFee(Double tuitionFee) {
        this.tuitionFee = tuitionFee;
    }
}
