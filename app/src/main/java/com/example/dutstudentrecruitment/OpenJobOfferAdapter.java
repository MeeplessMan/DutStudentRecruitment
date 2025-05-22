package com.example.dutstudentrecruitment; // Replace with your actual package name

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OpenJobOfferAdapter extends RecyclerView.Adapter<OpenJobOfferAdapter.ViewHolder> {

    private List<JobOffer> jobOfferList;
    private Context context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());


    public OpenJobOfferAdapter(Context context, List<JobOffer> jobOfferList) {
        this.context = context;
        this.jobOfferList = jobOfferList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job_offer_open, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JobOffer offer = jobOfferList.get(position);

        holder.jobTitleTextView.setText(offer.getJobTitle() != null ? offer.getJobTitle() : "N/A");
        holder.jobLocationTextView.setText(offer.getAddress() != null ? offer.getAddress() : "N/A");
        // For applicants count, you might need another query or store it in the offer
        holder.applicantsCountTextView.setText(offer.getApplicantCount() + " Applicants"); // Placeholder

        holder.buttonViewApplicants.setOnClickListener(v -> {
            Intent intent = new Intent(context, EmployerViewOfferActivity.class);
            intent.putExtra("offerId", offer.getOfferId());
            context.startActivity(intent);
        });

        holder.buttonEditOffer.setOnClickListener(v -> {
            Intent intent = new Intent(context, EmployerEditOfferActivity.class);
            intent.putExtra("offerId", offer.getOfferId());
            context.startActivity(intent);
        });
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
        TextView applicantsCountTextView;
        Button buttonViewApplicants;
        Button buttonEditOffer;

        ViewHolder(View itemView) {
            super(itemView);
            jobTitleTextView = itemView.findViewById(R.id.textViewJobTitleOpen);
            jobLocationTextView = itemView.findViewById(R.id.textViewJobLocationOpen);
            applicantsCountTextView = itemView.findViewById(R.id.textViewApplicantsCountOpen);
            buttonViewApplicants = itemView.findViewById(R.id.buttonViewApplicants);
            buttonEditOffer = itemView.findViewById(R.id.buttonEditOfferOpen);
        }
    }
}
