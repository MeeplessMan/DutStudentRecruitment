proguard
# proguard-rules.pro

# --- Rules for your JobOffer class ---
# Replace 'com.example.dutstudentrecruitment.JobOffer' with the
# fully qualified name of your JobOffer class.
-keep class com.example.dutstudentrecruitment.JobOffer { *; }
-keepnames class com.example.dutstudentrecruitment.JobOffer
-keepclassmembers class com.example.dutstudentrecruitment.JobOffer {
    *;
    public <init>();
}

# Keep setters and getters for Firebase data mapping if using @PropertyName on them
-keepclassmembers,allowobfuscation class * {
    @com.google.firebase.firestore.PropertyName <methods>;
}

# Keep fields if @PropertyName is used directly on fields
-keepclassmembers,allowobfuscation class * {
    @com.google.firebase.firestore.PropertyName <fields>;
}

