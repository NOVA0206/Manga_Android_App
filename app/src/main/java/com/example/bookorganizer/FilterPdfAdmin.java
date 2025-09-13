package com.example.bookorganizer;

import android.annotation.SuppressLint;
import android.widget.Filter;
import java.util.ArrayList;

public class FilterPdfAdmin extends Filter {
    private final ArrayList<ModelPdf> originalList;
    private final AdapterPdfAdmin adapterPdfAdmin;

    public FilterPdfAdmin(ArrayList<ModelPdf> originalList, AdapterPdfAdmin adapterPdfAdmin) {
        this.originalList = new ArrayList<>(originalList); // Keep original list intact
        this.adapterPdfAdmin = adapterPdfAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        if (constraint != null && constraint.length() > 0) {
            String searchQuery = constraint.toString().toLowerCase().trim();
            ArrayList<ModelPdf> filteredModels = new ArrayList<>();

            for (ModelPdf pdf : originalList) {
                if (pdf.getTitle().toLowerCase().contains(searchQuery)) {
                    filteredModels.add(pdf);
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;
        } else {
            // If no filter query, return full list
            results.count = originalList.size();
            results.values = originalList;
        }
        return results;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        if (results.values != null) {
            adapterPdfAdmin.pdfArrayList = (ArrayList<ModelPdf>) results.values;
            adapterPdfAdmin.notifyDataSetChanged();
        }
    }
}
