package com.example.bookorganizer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookorganizer.databinding.RowPdfAdminBinding;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {

    public static final long MAX_BYTES_PDF = 50000000;
    private final Context context;
    public ArrayList<ModelPdf> pdfArrayList, filterList;
    private FilterPdfAdmin filter;

    private static final String TAG = "PDF_ADAPTER_TAG";

    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowPdfAdminBinding binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderPdfAdmin(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {
        ModelPdf model = pdfArrayList.get(position);
        String title = model.getTitle();
        String description = model.getDescription();
        long timestamp = model.getTimestamp();

        String formattedDate = MyApplication.formatTimestamp(timestamp);

        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formattedDate);

        loadCategory(model, holder);
        loadPdfFromUrl(model, holder);
        loadPdfSize(model, holder);
    }

    private void loadPdfSize(ModelPdf model, HolderPdfAdmin holder) {
        String pdfUrl = model.getUrl();

        StorageReference storageReference = FirebaseStorage.getInstance("gs://book-organizer-dd855.firebasestorage.app")
                .getReferenceFromUrl(pdfUrl);
        storageReference.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        double bytes = storageMetadata.getSizeBytes();
                        double kb = bytes / 1024;
                        double mb = kb / 1024;

                        if (mb >= 1) {
                            holder.sizeTv.setText(String.format("%.2f MB", mb));
                        } else if (kb >= 1) {
                            holder.sizeTv.setText(String.format("%.2f KB", kb));
                        } else {
                            holder.sizeTv.setText(String.format("%.2f bytes", bytes));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to get PDF size: " + e.getMessage()));
    }

    private void loadPdfFromUrl(ModelPdf model, HolderPdfAdmin holder) {
        String pdfUrl = model.getUrl();
        holder.progressBar.setVisibility(View.VISIBLE);

        StorageReference storageReference = FirebaseStorage.getInstance("gs://book-organizer-dd855.firebasestorage.app").getReferenceFromUrl(pdfUrl);
        storageReference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(bytes -> {
                    holder.pdfView.fromBytes(bytes)
                            .pages(0)
                            .spacing(10)
                            .swipeHorizontal(false)
                            .enableSwipe(true)
                            .onError(t -> {
                                holder.progressBar.setVisibility(View.INVISIBLE);
                                Log.e(TAG, "Error loading PDF: " + t.getMessage());
                            })
                            .onPageError((page, t) -> Log.e(TAG, "Page error: " + t.getMessage()))
                            .onLoad(nbPages -> {
                                holder.progressBar.setVisibility(View.INVISIBLE);
                                Log.d(TAG, "PDF Loaded Successfully");
                            })
                            .load();
                })
                .addOnFailureListener(e -> {
                    holder.progressBar.setVisibility(View.INVISIBLE);
                    Log.e(TAG, "Failed to load PDF: " + e.getMessage());
                });
    }

    private void loadCategory(ModelPdf model, HolderPdfAdmin holder) {
        String categoryId = model.getCategoryId();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String category = snapshot.child("category").getValue(String.class);
                holder.categoryTv.setText(category);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load category: " + error.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterPdfAdmin(filterList, this);
        }
        return filter;
    }

    static class HolderPdfAdmin extends RecyclerView.ViewHolder {
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        ImageButton moreBtn;

        public HolderPdfAdmin(@NonNull RowPdfAdminBinding binding) {
            super(binding.getRoot());
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            moreBtn = binding.moreBtn;
        }
    }
}
