package com.example.dutstudentrecruitment; // Replace with your actual package name

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query; // Keep this if you change to query by employerUserId directly

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EmployerOurOffersFragment extends Fragment {

    private static final String TAG = "EmployerOurOffersFragment";

    private Button buttonCreateNewJobOffer;
    private RecyclerView recyclerViewOpenJobOffers;
    private RecyclerView recyclerViewCompletedJobOffers;
    private TextView textViewNoOpenJobOffers;
    private TextView textViewNoCompletedJobOffers;

    private OpenJobOfferAdapter openJobOfferAdapter;
    private CompletedJobOfferAdapter completedJobOfferAdapter;

    private List<JobOffer> openOffersList = new ArrayList<>();
    private List<JobOffer> completedOffersList = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    public EmployerOurOffersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_employer_our_offers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buttonCreateNewJobOffer = view.findViewById(R.id.buttonCreateNewJobOffer);
        recyclerViewOpenJobOffers = view.findViewById(R.id.recyclerViewOpenJobOffers);
        recyclerViewCompletedJobOffers = view.findViewById(R.id.recyclerViewCompletedJobOffers);
        textViewNoOpenJobOffers = view.findViewById(R.id.textViewNoOpenJobOffers);
        textViewNoCompletedJobOffers = view.findViewById(R.id.textViewNoCompletedJobOffers);

        setupRecyclerViews();

        buttonCreateNewJobOffer.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EmployerCreateJobOfferActivity.class);
            startActivity(intent);
            Toast.makeText(getContext(), "Navigate to Create Job Offer Screen", Toast.LENGTH_SHORT).show();
        });

        if (currentUser != null) {
            loadEmployerOfferIds();
        } else {
            Log.w(TAG, "No current user. Cannot load offers.");
            Toast.makeText(getContext(), "Please log in to see your offers.", Toast.LENGTH_LONG).show();
            updateEmptyViewVisibility();
        }
    }

    private void setupRecyclerViews() {
        recyclerViewOpenJobOffers.setLayoutManager(new LinearLayoutManager(getContext()));
        openJobOfferAdapter = new OpenJobOfferAdapter(getContext(), openOffersList);
        recyclerViewOpenJobOffers.setAdapter(openJobOfferAdapter);
        recyclerViewOpenJobOffers.setNestedScrollingEnabled(false);

        recyclerViewCompletedJobOffers.setLayoutManager(new LinearLayoutManager(getContext()));
        completedJobOfferAdapter = new CompletedJobOfferAdapter(getContext(), completedOffersList);
        recyclerViewCompletedJobOffers.setAdapter(completedJobOfferAdapter);
        recyclerViewCompletedJobOffers.setNestedScrollingEnabled(false);
    }

    private void loadEmployerOfferIds() {
        if (currentUser == null) return;
        String employerId = currentUser.getUid();
        // Assuming your employer profiles are in a "employers" collection
        DocumentReference employerDocRef = db.collection("employer").document(employerId);

        employerDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Assuming 'offers' is an array of strings (offer IDs) in the employer document
                List<String> offerIds = (List<String>) documentSnapshot.get("offers");
                if (offerIds != null && !offerIds.isEmpty()) {
                    fetchOfferDetails(offerIds);
                } else {
                    Log.d(TAG, "Employer has no offer IDs listed.");
                    updateEmptyViewVisibility(); // No offers to fetch
                }
            } else {
                Log.d(TAG, "Employer profile document does not exist.");
                updateEmptyViewVisibility();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching employer profile: ", e);
            Toast.makeText(getContext(), "Error loading profile.", Toast.LENGTH_SHORT).show();
            updateEmptyViewVisibility();
        });
    }

    private void fetchOfferDetails(List<String> offerIds) {
        if (offerIds.isEmpty()) {
            updateEmptyViewVisibility();
            return;
        }

        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String offerId : offerIds) {
            DocumentReference offerDocRef = db.collection("offers").document(offerId); // Assuming offers are in "jobOffers"
            tasks.add(offerDocRef.get());
        }

        // Wait for all offer fetch tasks to complete
        Tasks.whenAllSuccess(tasks).addOnSuccessListener(list -> {
            openOffersList.clear();
            completedOffersList.clear();

            for (Object snapshotObject : list) {
                if (snapshotObject instanceof DocumentSnapshot) {
                    DocumentSnapshot offerSnapshot = (DocumentSnapshot) snapshotObject;
                    if (offerSnapshot.exists()) {
                        Log.d(TAG, "fetchOfferDetails: Offer snapshot exists. ID: " + offerSnapshot.getId() + ", Data: " + offerSnapshot.getData());
                        JobOffer offer = new JobOffer();
                        offer.setOfferId(offerSnapshot.getId()); // Set the document ID

                        // Manually get each field with type checking and defaults
                        offer.setEmployerUserId(offerSnapshot.getString("employerUserId"));
                        offer.setJobTitle(offerSnapshot.getString("jobTitle"));
                        offer.setJobDescription(offerSnapshot.getString("jobDescription"));
                        offer.setCompanyName(offerSnapshot.getString("companyName")); // Or "company" - check your Firestore
                        offer.setAddress(offerSnapshot.getString("address"));
                        offer.setJobType(offerSnapshot.getString("jobType"));
                        offer.setStatus(offerSnapshot.getString("status"));
                        offer.setDateCreated(offerSnapshot.getTimestamp("dateCreated"));
                        offer.setDateCompleted(offerSnapshot.getTimestamp("dateCompleted"));

                        // For lists like requiredSkills:
                        List<String> skills = (List<String>) offerSnapshot.get("requiredSkills");
                        if (skills != null) {
                            offer.setRequiredSkills(skills);
                        } else {
                            offer.setRequiredSkills(new ArrayList<>()); // Initialize to empty list if null
                        }

                        if (offer.getJobTitle() == null && offer.getCompanyName() == null) {
                            Log.w(TAG, "fetchOfferDetails: Offer object created but critical fields (jobTitle, companyName) are null. Offer ID: " + offerSnapshot.getId());
                        } else {
                            Log.d(TAG, "fetchOfferDetails: Manually parsed JobOffer: " + offer.getJobTitle());
                        }


                        if ("completed".equalsIgnoreCase(offer.getStatus())) {
                            completedOffersList.add(offer);
                        } else {
                            openOffersList.add(offer);
                        }
                    } else {
                        Log.w(TAG, "fetchOfferDetails: Offer snapshot does NOT exist for one of the IDs.");
                    }
                }
            }

            // Sort open offers by dateCreated ascending
            Collections.sort(openOffersList, (o1, o2) -> {
                Timestamp t1 = o1.getDateCreated();
                Timestamp t2 = o2.getDateCreated();
                if (t1 == null && t2 == null) return 0;
                if (t1 == null) return 1; // nulls last
                if (t2 == null) return -1; // nulls last
                return t1.compareTo(t2);
            });

            // Sort completed offers by dateCompleted descending
            Collections.sort(completedOffersList, (o1, o2) -> {
                Timestamp t1 = o1.getDateCompleted();
                Timestamp t2 = o2.getDateCompleted();
                if (t1 == null && t2 == null) return 0;
                if (t1 == null) return 1; // nulls last
                if (t2 == null) return -1; // nulls last
                return t2.compareTo(t1); // Descending
            });

            openJobOfferAdapter.updateData(openOffersList);
            completedJobOfferAdapter.updateData(completedOffersList);
            updateEmptyViewVisibility();

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching one or more offer details: ", e);
            Toast.makeText(getContext(), "Error loading some offers.", Toast.LENGTH_SHORT).show();
            updateEmptyViewVisibility(); // Still update, might have partial data or none
        });
    }


    private void updateEmptyViewVisibility() {
        // Check for Open Offers
        if (openOffersList.isEmpty()) {
            if (recyclerViewOpenJobOffers != null) recyclerViewOpenJobOffers.setVisibility(View.GONE);
            if (textViewNoOpenJobOffers != null) textViewNoOpenJobOffers.setVisibility(View.VISIBLE);
        } else {
            if (recyclerViewOpenJobOffers != null) recyclerViewOpenJobOffers.setVisibility(View.VISIBLE);
            if (textViewNoOpenJobOffers != null) textViewNoOpenJobOffers.setVisibility(View.GONE);
        }

        // Check for Completed Offers
        if (completedOffersList.isEmpty()) {
            if (recyclerViewCompletedJobOffers != null) recyclerViewCompletedJobOffers.setVisibility(View.GONE);
            if (textViewNoCompletedJobOffers != null) textViewNoCompletedJobOffers.setVisibility(View.VISIBLE);
        } else {
            if (recyclerViewCompletedJobOffers != null) recyclerViewCompletedJobOffers.setVisibility(View.VISIBLE);
            if (textViewNoCompletedJobOffers != null) textViewNoCompletedJobOffers.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh data when the fragment becomes visible again,
        // in case new offers were created or statuses changed in another screen.
        if (currentUser != null && mAuth.getCurrentUser() != null) { // Ensure user is still logged in
            // Re-check current user in case of auth state changes while fragment was paused
            currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                loadEmployerOfferIds();
            } else {
                // Handle user logged out scenario
                openOffersList.clear();
                completedOffersList.clear();
                if(openJobOfferAdapter != null) openJobOfferAdapter.updateData(openOffersList);
                if(completedJobOfferAdapter != null) completedJobOfferAdapter.updateData(completedOffersList);
                updateEmptyViewVisibility();
                if(getContext() != null) {
                    Toast.makeText(getContext(), "Please log in again.", Toast.LENGTH_SHORT).show();
                }
                // Potentially navigate to login screen
            }
        } else if (currentUser == null && mAuth.getCurrentUser() == null) {
            // User was not logged in initially or logged out
            openOffersList.clear();
            completedOffersList.clear();
            if(openJobOfferAdapter != null) openJobOfferAdapter.updateData(openOffersList);
            if(completedJobOfferAdapter != null) completedJobOfferAdapter.updateData(completedOffersList);
            updateEmptyViewVisibility();
            if(getContext() != null) {
                Toast.makeText(getContext(), "Please log in to see your offers.", Toast.LENGTH_LONG).show();
            }
        }
    }


    // You might also want to add onStart or onStop if needed,
    // but onResume is a common place for refreshing list data.

}