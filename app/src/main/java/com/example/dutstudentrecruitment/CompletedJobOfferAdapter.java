package com.example.dutstudentrecruitment; // Replace with your actual package name

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
// Make sure you have your JobOffer model imported
// import com.example.dutstudentrecruitment.JobOffer;

public class CompletedJobOfferAdapter extends RecyclerView.Adapter<CompletedJobOfferAdapter.ViewHolder> {

    private List<JobOffer> jobOfferList;
    private Context context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public CompletedJobOfferAdapter(Context context, List<JobOffer> jobOfferList) {
        this.context = context;
        this.jobOfferList = jobOfferList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job_offer_completed, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JobOffer offer = jobOfferList.get(position);

        holder.jobTitleTextView.setText(offer.getJobTitle() != null ? offer.getJobTitle() : "N/A");
        holder.jobLocationTextView.setText(offer.getAddress() != null ? offer.getAddress() : "N/A");

        if (offer.getDateCompleted() != null) {
            holder.completionDateTextView.setText("Completed: " + dateFormat.format(offer.getDateCompleted().toDate()));
            holder.completionDateTextView.setVisibility(View.VISIBLE);
        } else {
            holder.completionDateTextView.setVisibility(View.GONE);
        }

        // Example for Hired Candidate - you'll need to fetch/store this info
        // if (offer.getHiredCandidateName() != null && !offer.getHiredCandidateName().isEmpty()) {
        //    holder.hiredCandidateTextView.setText("Hired: " + offer.getHiredCandidateName());
        //    holder.hiredCandidateTextView.setVisibility(View.VISIBLE);
        // } else {
        holder.hiredCandidateTextView.setVisibility(View.GONE); // Hide if no info
        // }

        // You might add an OnClickListener to the item view if you want to view details of a completed offer
        // holder.itemView.setOnClickListener(v -> {
        //     Intent intent = new Intent(context, EmployerViewOfferActivity.class); // Or a specific "ViewCompletedOfferActivity"
        //     intent.putExtra("offerId", offer.getOfferId());
        //     context.startActivity(intent);
        // });
    }

    @Override
    public int getItemCount() {
        return jobOfferList.size();
    }

    public void updateData(List<JobOffer> newOffers) {
        jobOfferList.clear();
        jobOfferList.addAll(newOffers);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView jobTitleTextView;
        TextView jobLocationTextView;
        TextView completionDateTextView;
        TextView hiredCandidateTextView; // Assuming you add this ID to item_job_offer_completed.xml

        ViewHolder(View itemView) {
            super(itemView);
            jobTitleTextView = itemView.findViewById(R.id.textViewJobTitleCompleted);
            jobLocationTextView = itemView.findViewById(R.id.textViewJobLocationCompleted);
            completionDateTextView = itemView.findViewById(R.id.textViewCompletionDate);
            hiredCandidateTextView = itemView.findViewById(R.id.textViewHiredCandidate); // Make sure this ID exists
        }
    }
}
