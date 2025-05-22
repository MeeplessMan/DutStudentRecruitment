// Correct import for Firestore:
package com.example.dutstudentrecruitment; // Ensure this is your correct package

import com.google.firebase.firestore.PropertyName;
import com.google.firebase.Timestamp;
import java.util.List;
import java.util.ArrayList; // Import ArrayList if you initialize with it

public class JobOffer {
    // Fields
    private String offerId;
    private String employerUserId;
    private String jobTitle;
    private String jobDescription;
    private String companyName;
    private String address;
    private List<String> requiredSkills;
    private String jobType;
    private Timestamp dateCreated;
    private Timestamp dateCompleted;
    private String status;

    // New fields
    private List<String> applicantUids; // Stores User IDs of applicants
    private int applicantCount;         // Stores the number of applicants

    // Public no-argument constructor is ESSENTIAL for Firestore deserialization
    public JobOffer() {
        // Initialize lists to avoid null pointer exceptions if they are accessed before being set from Firestore
        this.requiredSkills = new ArrayList<>();
        this.applicantUids = new ArrayList<>();
        // applicantCount will default to 0 for int, which is fine.
    }

    // Getters and Setters using Firestore's @PropertyName

    @PropertyName("offerId")
    public String getOfferId() { return offerId; }
    public void setOfferId(String offerId) { this.offerId = offerId; }

    @PropertyName("employerUserId")
    public String getEmployerUserId() { return employerUserId; }
    public void setEmployerUserId(String employerUserId) { this.employerUserId = employerUserId; }

    @PropertyName("jobTitle")
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    @PropertyName("jobDescription")
    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

    @PropertyName("companyName")
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    @PropertyName("address")
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @PropertyName("requiredSkills")
    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }

    @PropertyName("jobType")
    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    @PropertyName("dateCreated")
    public Timestamp getDateCreated() { return dateCreated; }
    public void setDateCreated(Timestamp dateCreated) { this.dateCreated = dateCreated; }

    @PropertyName("dateCompleted")
    public Timestamp getDateCompleted() { return dateCompleted; }
    public void setDateCompleted(Timestamp dateCompleted) { this.dateCompleted = dateCompleted; }

    @PropertyName("status")
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Getters and Setters for new fields

    @PropertyName("applicantUids")
    public List<String> getApplicantUids() { return applicantUids; }
    public void setApplicantUids(List<String> applicantUids) { this.applicantUids = applicantUids; }

    @PropertyName("applicantCount")
    public int getApplicantCount() { return applicantCount; }
    public void setApplicantCount(int applicantCount) { this.applicantCount = applicantCount; }

}
