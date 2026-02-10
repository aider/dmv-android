package com.dmv.texas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ResultsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        int correct = getIntent().getIntExtra("CORRECT", 0);
        int total = getIntent().getIntExtra("TOTAL", 0);
        int percentage = total > 0 ? (correct * 100 / total) : 0;

        TextView scoreText = findViewById(R.id.score_text);
        TextView passStatus = findViewById(R.id.pass_status);

        scoreText.setText(String.format("Score: %d/%d (%d%%)", correct, total, percentage));

        // Passing score is typically 70% or higher
        if (percentage >= 70) {
            passStatus.setText("You Passed!");
            passStatus.setTextColor(getColor(R.color.correct));
        } else {
            passStatus.setText("Keep Practicing");
            passStatus.setTextColor(getColor(R.color.incorrect));
        }

        Button backButton = findViewById(R.id.btn_back_menu);
        Button tryAgainButton = findViewById(R.id.btn_try_again);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        tryAgainButton.setOnClickListener(v -> {
            finish();
        });
    }
}
