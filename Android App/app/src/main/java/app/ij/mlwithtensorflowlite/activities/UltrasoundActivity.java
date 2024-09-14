package app.ij.mlwithtensorflowlite.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;

import app.ij.mlwithtensorflowlite.R;
import app.ij.mlwithtensorflowlite.views.UltrasoundViewModel;
import app.ij.mlwithtensorflowlite.ml.Model;
import app.ij.mlwithtensorflowlite.ml.Masks;

public class UltrasoundActivity extends AppCompatActivity {

    private Button buttonUpload, buttonReset, buttonMakePrediction, buttonGetMask;
    private ImageView imageView;
    private TextView textViewResult;
    private ProgressBar progressBar;
    private UltrasoundViewModel viewModel;
    private Bitmap uploadedImage;
    private Model model;
    private Masks masksModel;

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                try {
                    buttonUpload.setVisibility(View.GONE);
                    buttonMakePrediction.setVisibility(View.VISIBLE);
                    uploadedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    imageView.setImageBitmap(uploadedImage);
                } catch (IOException e) {
                    Log.e("UltrasoundActivity", "Error getting image", e);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ultrasound);

        buttonUpload = findViewById(R.id.button_upload_ultrasound);
        textViewResult = findViewById(R.id.result_ultrasound);
        imageView = findViewById(R.id.imageView_ultrasound);
        buttonReset = findViewById(R.id.button_reset_ultrasound);
        buttonMakePrediction = findViewById(R.id.button_make_prediction_ultrasound);
        buttonGetMask = findViewById(R.id.button_get_mask_ultrasound);
        progressBar = findViewById(R.id.progressBar_ultrasound);

        progressBar.setVisibility(View.GONE);
        buttonMakePrediction.setVisibility(View.GONE);
        buttonGetMask.setVisibility(View.GONE);
        buttonReset.setVisibility(View.GONE);

        buttonUpload.setOnClickListener(view -> mGetContent.launch("image/*"));
        buttonReset.setOnClickListener(view -> reset());

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(UltrasoundViewModel.class);

        // Initialize TensorFlow Lite Models
        try {
            model = Model.newInstance(getApplicationContext());
            masksModel = Masks.newInstance(getApplicationContext());
        } catch (IOException e) {
            Log.e("UltrasoundActivity", "Error initializing .tflite models", e);
        }

        // Observe image live data
        viewModel.getImageLiveData().observe(this, image -> {
            imageView.setImageBitmap(image);
        });

        // Observe result live data
        viewModel.getResultLiveData().observe(this, result -> {
            textViewResult.setText(getString(R.string.classified_as, result));
            progressBar.setVisibility(View.GONE);
            buttonGetMask.setVisibility(View.VISIBLE);
            buttonReset.setVisibility(View.VISIBLE);
        });

        buttonMakePrediction.setOnClickListener(view -> {
            buttonMakePrediction.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            Bitmap imageForSegmentation = Bitmap.createScaledBitmap(uploadedImage, 256, 256, false);
            Bitmap maskedImage = viewModel.getMask(imageForSegmentation, masksModel);
            maskedImage = viewModel.prepareMaskForClassification(maskedImage);
            viewModel.classifyImage(maskedImage, model);
        });

        buttonGetMask.setOnClickListener(view -> {
            Bitmap imageForSegmentation = Bitmap.createScaledBitmap(uploadedImage, 256, 256, false);
            Bitmap maskedImage = viewModel.getMask(imageForSegmentation, masksModel);
            imageView.setImageBitmap(maskedImage);
        });

    }

    public void reset() {
        imageView.setImageDrawable(null);
        textViewResult.setText("");
        uploadedImage = null;
        progressBar.setVisibility(View.GONE);
        buttonUpload.setVisibility(View.VISIBLE);
        buttonMakePrediction.setVisibility(View.GONE);
        buttonGetMask.setVisibility(View.GONE);
        buttonReset.setVisibility(View.GONE);
    }

}
