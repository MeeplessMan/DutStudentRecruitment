package com.example.dutstudentrecruitment; // Replace with your actual package name

import com.google.firebase.firestore.PropertyName;
import com.google.firebase.Timestamp; // For Firestore Timestamps

import java.io.Serializable;

public class JobOffer implements Serializable { // Implement Serializable to pass via Intent

    private String offerId; // Document ID from Firestore
    private String employerUserId;
    private String companyName;
    private String jobTitle; // Added this as it's common
    private String address; // Or more detailed location fields
    private String jobType; // "Full-time", "Part-time", "Remote", "Temporary", "Internship"
    private Timestamp dateCreated;
    private Timestamp dateCompleted; // For completed offers
    private String status; // e.g., "open", "completed", "closed"

    // Required empty public constructor for Firestore
    public JobOffer() {}

    public JobOffer(String employerUserId, String companyName, String jobTitle, String address, String jobType, Timestamp dateCreated, String status) {
        this.employerUserId = employerUserId;
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.address = address;
        this.jobType = jobType;
        this.dateCreated = dateCreated;
        this.status = status;
    }

    // Getters
    public String getOfferId() { return offerId; }
    public String getEmployerUserId() { return employerUserId; }
    public String getCompanyName() { return companyName; }
    public String getJobTitle() { return jobTitle; }
    public String getAddress() { return address; }
    public String getJobType() { return jobType; }
    public Timestamp getDateCreated() { return dateCreated; }
    public Timestamp getDateCompleted() { return dateCompleted; }
    public String getStatus() { return status; }


    // Setters (Firestore needs setters if you are constructing objects manually from snapshots sometimes)
    // Or use @PropertyName if your field names differ from Firestore document keys
    public void setOfferId(String offerId) { this.offerId = offerId; }
    public void setEmployerUserId(String employerUserId) { this.employerUserId = employerUserId; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public void setAddress(String address) { this.address = address; }
    public void setJobType(String jobType) { this.jobType = jobType; }
    public void setDateCreated(Timestamp dateCreated) { this.dateCreated = dateCreated; }
    public void setDateCompleted(Timestamp dateCompleted) { this.dateCompleted = dateCompleted; }
    public void setStatus(String status) { this.status = status; }

    // It's good practice to add @PropertyName if your Firestore field names are different
    // e.g., @PropertyName("user_id") if Firestore uses snake_case
}
