package com.dmv.texas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private QuestionBank questionBank;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        questionBank = new QuestionBank(this);

        setupTopicButtons();
    }

    private void setupTopicButtons() {
        findViewById(R.id.btn_all_topics).setOnClickListener(v -> startQuiz(null, 30));
        findViewById(R.id.btn_signs).setOnClickListener(v -> startQuiz("SIGNS", 30));
        findViewById(R.id.btn_traffic_signals).setOnClickListener(v -> startQuiz("TRAFFIC_SIGNALS", 30));
        findViewById(R.id.btn_pavement_markings).setOnClickListener(v -> startQuiz("PAVEMENT_MARKINGS", 30));
        findViewById(R.id.btn_right_of_way).setOnClickListener(v -> startQuiz("RIGHT_OF_WAY", 30));
        findViewById(R.id.btn_speed_distance).setOnClickListener(v -> startQuiz("SPEED_AND_DISTANCE", 30));
        findViewById(R.id.btn_parking).setOnClickListener(v -> startQuiz("PARKING", 30));
        findViewById(R.id.btn_safe_driving).setOnClickListener(v -> startQuiz("SAFE_DRIVING", 30));
        findViewById(R.id.btn_special_situations).setOnClickListener(v -> startQuiz("SPECIAL_SITUATIONS", 30));
    }

    private void startQuiz(String topic, int questionCount) {
        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra("TOPIC", topic);
        intent.putExtra("QUESTION_COUNT", questionCount);
        startActivity(intent);
    }
}
