package app.ij.mlwithtensorflowlite.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import app.ij.mlwithtensorflowlite.R;
import app.ij.mlwithtensorflowlite.views.SelectModalityViewModel;

public class SelectModalityActivity extends AppCompatActivity {

    private Button ultrasoundButton;
    private Button mammographyButton;
    private SelectModalityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        ultrasoundButton = findViewById(R.id.ultrasoundButton);
        mammographyButton = findViewById(R.id.mammographyButton);

        viewModel = new ViewModelProvider(this).get(SelectModalityViewModel.class);

        ultrasoundButton.setOnClickListener(view -> viewModel.onUltrasoundButtonClicked());
        mammographyButton.setOnClickListener(view -> viewModel.onMammographyButtonClicked());

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getNavigateToUltrasound().observe(this, navigate -> {
            if (navigate) {
                Intent intent = new Intent(SelectModalityActivity.this, UltrasoundActivity.class);
                startActivity(intent);
            }
        });

        viewModel.getNavigateToMammography().observe(this, navigate -> {
            if (navigate) {
                Intent intent = new Intent(SelectModalityActivity.this, MammographyActivity.class);
                startActivity(intent);
            }
        });
    }
}
