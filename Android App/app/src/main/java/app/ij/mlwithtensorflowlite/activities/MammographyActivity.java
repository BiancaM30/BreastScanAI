package app.ij.mlwithtensorflowlite.activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;

import app.ij.mlwithtensorflowlite.R;
import app.ij.mlwithtensorflowlite.views.MammographyViewModel;

public class MammographyActivity extends AppCompatActivity {

    private Button buttonUpload, buttonReset, buttonMakePrediction, buttonGetMask, buttonGetBbox;
    private ImageView imageView;
    private TextView textViewResult;
    private ProgressBar progressBar;
    private Bitmap buttonUploadedImage;
    private MammographyViewModel viewModel;

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    try {
                        Bitmap originalImage = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        int targetWidth = 1024;
                        int targetHeight = 1024;
                        buttonUploadedImage = Bitmap.createScaledBitmap(originalImage, targetWidth, targetHeight, false);
                        imageView.setImageBitmap(originalImage);
                        buttonMakePrediction.setVisibility(View.VISIBLE);
                        buttonUpload.setVisibility(View.GONE);
                    } catch (IOException e) {
                        Log.e("MammographyActivity", "Error getting image", e);
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mammography);

        buttonUpload = findViewById(R.id.button_upload_mammo);
        buttonMakePrediction = findViewById(R.id.button_make_prediction_mammo);
        buttonGetMask = findViewById(R.id.button_get_mask_mammo);
        buttonGetBbox = findViewById(R.id.button_get_bbox_mammo);
        buttonReset = findViewById(R.id.button_reset_mammo);
        textViewResult = findViewById(R.id.result_mammo);
        imageView = findViewById(R.id.imageView_mammo);
        progressBar = findViewById(R.id.progressBar_ultrasound);

        viewModel = new ViewModelProvider(this).get(MammographyViewModel.class);

        progressBar.setVisibility(View.GONE);

        buttonMakePrediction.setVisibility(View.GONE);
        buttonGetMask.setVisibility(View.GONE);
        buttonGetBbox.setVisibility(View.GONE);
        buttonReset.setVisibility(View.GONE);

        buttonUpload.setOnClickListener(view -> mGetContent.launch("image/*"));
        buttonMakePrediction.setOnClickListener(view -> {
            if (buttonUploadedImage != null) {
                String url = getResources().getString(R.string.predict_url);
                viewModel.processImage(buttonUploadedImage, url);
            }
        });

        buttonGetMask.setOnClickListener(view -> {
            JSONObject predictionResult = viewModel.getPredictionLiveData().getValue();
            if (predictionResult != null) {
                viewModel.displayMask(predictionResult);
            }
        });

        buttonGetBbox.setOnClickListener(view -> {
            JSONObject predictionResult = viewModel.getPredictionLiveData().getValue();
            Bitmap originalImage = buttonUploadedImage;
            if (predictionResult != null && originalImage != null) {
                viewModel.displayBbox(predictionResult, originalImage);
            }
        });

        buttonReset.setOnClickListener(view -> {
            viewModel.reset();
            buttonUpload.setVisibility(View.VISIBLE);
            buttonMakePrediction.setVisibility(View.GONE);
            buttonGetMask.setVisibility(View.GONE);
            buttonGetBbox.setVisibility(View.GONE);
            buttonReset.setVisibility(View.GONE);
        });

        viewModel.getImageLiveData().observe(this, image -> imageView.setImageBitmap(image));
        viewModel.getResultLiveData().observe(this, result -> textViewResult.setText(result));
        viewModel.getProgressLiveData().observe(this, isLoading -> progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE));
        viewModel.getPredictionLiveData().observe(this, prediction -> {
            if (prediction != null) {
                buttonMakePrediction.setVisibility(View.GONE);
                buttonGetMask.setVisibility(View.VISIBLE);
                buttonGetBbox.setVisibility(View.VISIBLE);
                buttonReset.setVisibility(View.VISIBLE);
            }
        });
    }
}
