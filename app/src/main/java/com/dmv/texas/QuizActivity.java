package com.dmv.texas;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.InputStream;
import java.util.List;

public class QuizActivity extends AppCompatActivity {
    private QuestionBank questionBank;
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;
    private int totalAnswered = 0;

    private TextView questionProgress;
    private TextView scoreDisplay;
    private TextView questionText;
    private ImageView questionImage;
    private RadioGroup choicesGroup;
    private RadioButton choiceA, choiceB, choiceC, choiceD;
    private Button submitButton;
    private LinearLayout feedbackLayout;
    private TextView feedbackTitle;
    private TextView feedbackExplanation;

    private boolean isAnswered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        initializeViews();
        loadQuestions();
        displayQuestion();
    }

    private void initializeViews() {
        questionProgress = findViewById(R.id.question_progress);
        scoreDisplay = findViewById(R.id.score_display);
        questionText = findViewById(R.id.question_text);
        questionImage = findViewById(R.id.question_image);
        choicesGroup = findViewById(R.id.choices_group);
        choiceA = findViewById(R.id.choice_a);
        choiceB = findViewById(R.id.choice_b);
        choiceC = findViewById(R.id.choice_c);
        choiceD = findViewById(R.id.choice_d);
        submitButton = findViewById(R.id.btn_submit);
        feedbackLayout = findViewById(R.id.feedback_layout);
        feedbackTitle = findViewById(R.id.feedback_title);
        feedbackExplanation = findViewById(R.id.feedback_explanation);

        submitButton.setOnClickListener(v -> handleSubmit());
    }

    private void loadQuestions() {
        questionBank = new QuestionBank(this);
        String topic = getIntent().getStringExtra("TOPIC");
        int count = getIntent().getIntExtra("QUESTION_COUNT", 30);

        if (topic == null) {
            questions = questionBank.getRandomQuestions(count);
        } else {
            questions = questionBank.getRandomQuestionsByTopic(topic, count);
        }
    }

    private void displayQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            showResults();
            return;
        }

        Question question = questions.get(currentQuestionIndex);

        // Update progress
        questionProgress.setText(String.format("Question %d of %d",
            currentQuestionIndex + 1, questions.size()));
        scoreDisplay.setText(String.format("Score: %d/%d", correctAnswers, totalAnswered));

        // Display question
        questionText.setText(question.getText());

        // Display image if available
        if (question.hasImage()) {
            try {
                String assetPath = "svg/" + question.getImage().getAssetId() + ".svg";
                InputStream is = getAssets().open(assetPath);
                // Note: SVG rendering would require additional library
                // For now, hide the image view
                questionImage.setVisibility(View.GONE);
                is.close();
            } catch (Exception e) {
                questionImage.setVisibility(View.GONE);
            }
        } else {
            questionImage.setVisibility(View.GONE);
        }

        // Display choices
        String[] choices = question.getChoices();
        choiceA.setText(choices[0]);
        choiceB.setText(choices[1]);
        choiceC.setText(choices[2]);
        choiceD.setText(choices[3]);

        // Reset state
        choicesGroup.clearCheck();
        feedbackLayout.setVisibility(View.GONE);
        submitButton.setText(R.string.submit_answer);
        isAnswered = false;

        // Reset choice colors
        resetChoiceColors();
    }

    private void handleSubmit() {
        if (!isAnswered) {
            // Submit answer
            int selectedId = choicesGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                return; // No answer selected
            }

            int selectedIndex = getSelectedIndex(selectedId);
            Question question = questions.get(currentQuestionIndex);

            boolean correct = question.isCorrect(selectedIndex);
            totalAnswered++;
            if (correct) {
                correctAnswers++;
            }

            // Show feedback
            showFeedback(correct, question);

            // Update button
            if (currentQuestionIndex < questions.size() - 1) {
                submitButton.setText(R.string.next_question);
            } else {
                submitButton.setText(R.string.finish_quiz);
            }

            isAnswered = true;
        } else {
            // Move to next question
            currentQuestionIndex++;
            displayQuestion();
        }
    }

    private int getSelectedIndex(int selectedId) {
        if (selectedId == R.id.choice_a) return 0;
        if (selectedId == R.id.choice_b) return 1;
        if (selectedId == R.id.choice_c) return 2;
        if (selectedId == R.id.choice_d) return 3;
        return -1;
    }

    private void showFeedback(boolean correct, Question question) {
        feedbackLayout.setVisibility(View.VISIBLE);

        if (correct) {
            feedbackTitle.setText(R.string.correct);
            feedbackTitle.setTextColor(getColor(R.color.correct));
            feedbackLayout.setBackgroundColor(Color.parseColor("#E8F5E9"));
        } else {
            feedbackTitle.setText(R.string.incorrect);
            feedbackTitle.setTextColor(getColor(R.color.incorrect));
            feedbackLayout.setBackgroundColor(Color.parseColor("#FFEBEE"));
        }

        feedbackExplanation.setText(question.getExplanation());

        // Highlight correct and incorrect answers
        highlightAnswers(question.getCorrectIndex());
    }

    private void highlightAnswers(int correctIndex) {
        RadioButton[] choices = {choiceA, choiceB, choiceC, choiceD};
        int selectedId = choicesGroup.getCheckedRadioButtonId();
        int selectedIndex = getSelectedIndex(selectedId);

        for (int i = 0; i < choices.length; i++) {
            if (i == correctIndex) {
                choices[i].setTextColor(getColor(R.color.correct));
            } else if (i == selectedIndex) {
                choices[i].setTextColor(getColor(R.color.incorrect));
            }
        }
    }

    private void resetChoiceColors() {
        int defaultColor = getColor(R.color.text_primary);
        choiceA.setTextColor(defaultColor);
        choiceB.setTextColor(defaultColor);
        choiceC.setTextColor(defaultColor);
        choiceD.setTextColor(defaultColor);
    }

    private void showResults() {
        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra("CORRECT", correctAnswers);
        intent.putExtra("TOTAL", totalAnswered);
        startActivity(intent);
        finish();
    }
}
