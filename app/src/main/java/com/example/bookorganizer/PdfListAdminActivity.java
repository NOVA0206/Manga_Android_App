package com.example.bookorganizer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookorganizer.databinding.ActivityPdfListAdminBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PdfListAdminActivity extends AppCompatActivity {
    private ActivityPdfListAdminBinding binding;

    private ArrayList<ModelPdf> pdfArrayList;
    private AdapterPdfAdmin adapterPdfAdmin;
    private String categoryId, categoryTitle;

    private static final String TAG = "PDF_LIST_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfListAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        categoryId = intent.getStringExtra("categoryId");
        categoryTitle = intent.getStringExtra("categoryTitle");

        if (categoryId == null || categoryId.isEmpty()) {
            Toast.makeText(this, "Error: Category ID is missing!", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if there's no valid category
            return;
        }

        binding.subtitleTv.setText(categoryTitle);

        // Load PDF list for the category
        loadPdfList();

        // Search functionality
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapterPdfAdmin != null) {
                    adapterPdfAdmin.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Back Button Click
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadPdfList() {
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance("https://book-organizer-dd855-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Books");

        ref.orderByChild("categoryId").equalTo(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();

                        if (!snapshot.exists()) {
                            Toast.makeText(PdfListAdminActivity.this, "No books found!", Toast.LENGTH_SHORT).show();
                        }

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelPdf model = ds.getValue(ModelPdf.class);
                            if (model != null) {
                                pdfArrayList.add(model);
                                Log.d(TAG, "Loaded PDF: " + model.getId() + " - " + model.getTitle());
                            }
                        }

                        if (adapterPdfAdmin == null) {
                            adapterPdfAdmin = new AdapterPdfAdmin(PdfListAdminActivity.this, pdfArrayList);
                            binding.bookRv.setAdapter(adapterPdfAdmin);
                        } else {
                            adapterPdfAdmin.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Firebase Error: " + error.getMessage());
                        Toast.makeText(PdfListAdminActivity.this, "Failed to load books!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
