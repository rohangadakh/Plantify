package com.dev.plantify.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import com.dev.plantify.Disease;
import com.dev.plantify.Product;
import com.dev.plantify.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ManageFragment extends Fragment {

    private DatabaseReference databaseReference;
    private DiseaseAdapter diseaseAdapter;
    private List<Disease> diseaseList;

    public ManageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference().child("products");

        // Create sample disease data
        diseaseList = new ArrayList<>();
        diseaseList.add(new Disease("Tomato Late Blight", "Symptoms:\nWater-soaked spots on leaves, stems, and fruit\nBrownish-black lesions that grow larger and become irregular\nFuzzy white mold on the underside of leaves in humid conditions\nRotting of fruit\n\nPrecautions:\nPurchase disease-resistant tomato varieties\nRotate your crops to avoid planting tomatoes in the same location in consecutive years\nProvide adequate spacing between plants to allow for air circulation\nWater plants at the base to avoid wetting the leaves\nRemove and destroy infected plant debris\n\nPrevention:\nApply copper-based fungicides as a preventative measure before symptoms appear\nMonitor weather conditions and apply fungicides more frequently during cool, wet weather\nKeep the garden clean and free of weeds"));
        diseaseList.add(new Disease("Strawberry Leaf Scorch", "Symptoms:\nLeaves develop a reddish-brown scorch along the margins, progressing inward\nScorched leaves may eventually become dry and brittle\nStunted plant growth and reduced fruit yield\n\nPrecautions:\nPurchase certified disease-free strawberry plants\nWater plants deeply and regularly, especially during hot, dry weather\nApply a layer of mulch around plants to help retain moisture and suppress weeds\nRemove and destroy infected plant debris\n\nPrevention:\nIrrigate with drip irrigation to avoid wetting the foliage\nAvoid planting strawberries in areas with poor air circulation\nThere are no fungicides currently registered for control of leaf scorch"));
        diseaseList.add(new Disease("Grape Black Rot", "Symptoms:\nSmall, brown or reddish-brown spots on leaves, fruit, and petioles (leaf stalks)\nSpots on leaves grow larger and become sunken, with a feathered margin\nInfected fruit become shriveled and mummified, covered with black fungal structures\nInfected berries may split open and leak a pinkish liquid\n\nPrecautions:\nPrune grapevines to improve air circulation and sunlight penetration\nWater plants deeply and infrequently, allowing the soil to dry slightly between waterings\nHarvest grapes as soon as they ripen to minimize fruit exposure to the fungus\nRemove and destroy infected grape clusters and fallen leaves\n\nPrevention:\nApply fungicides before bud break and during the growing season, especially during wet weather\nSeveral fungicides are available for control of black rot, so consult with your local extension office for specific recommendations"));
        diseaseList.add(new Disease("Corn Northern Leaf Blight", "Symptoms:\nLarge, oval-shaped lesions on leaves, typically appearing on the upper leaves first\nLesions are initially pale green and water-soaked, becoming tan to light brown with age\nLesions may have a narrow, yellow border\nShredding of leaf tissue within lesions\n\nPrecautions:\nPurchase corn varieties with resistance to northern leaf blight\nRotate your crops to avoid planting corn in the same location in consecutive years\nTill crop residues after harvest to help decompose infected plant debris\nScout cornfields regularly for signs of disease\n\nPrevention:\nApply fungicides as a preventative measure before symptoms appear, especially in fields with a history of northern leaf blight\nThere are several fungicides available for control of northern leaf blight, so consult with your local extension office for specific recommendations"));
        // Add more diseases here...
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage, container, false);

        // Set up RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up RecyclerView adapter
        diseaseAdapter = new DiseaseAdapter(diseaseList);
        recyclerView.setAdapter(diseaseAdapter);

        // Set up image view click listener to show popup dialog
        ImageView imageView = view.findViewById(R.id.imageView); // Replace with your image view id
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupDialog();
            }
        });

        return view;
    }

    private void showPopupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_layout, null);

        EditText priceEditText = dialogView.findViewById(R.id.price_edit_text);
        EditText productNameEditText = dialogView.findViewById(R.id.product_name_edit_text);
        EditText locationEditText = dialogView.findViewById(R.id.location_edit_text);

        builder.setView(dialogView)
                .setTitle("Enter Details")
                .setPositiveButton("OK", (dialog, which) -> {
                    String price = priceEditText.getText().toString();
                    String productName = productNameEditText.getText().toString();
                    String location = locationEditText.getText().toString();

                    // Create a unique key for the product
                    String productId = databaseReference.push().getKey();

                    // Create a Product object
                    Product product = new Product(productId, productName, price, location);

                    // Insert data into Firebase database
                    databaseReference.child(productId).setValue(product);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
