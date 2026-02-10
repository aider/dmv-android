package com.dmv.texas;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestionBank {
    private Context context;
    private List<Question> allQuestions;

    public QuestionBank(Context context) {
        this.context = context;
        loadQuestions();
    }

    private void loadQuestions() {
        allQuestions = new ArrayList<>();
        String[] topics = {
            "signs.json",
            "traffic_signals.json",
            "pavement_markings.json",
            "right_of_way.json",
            "speed_and_distance.json",
            "parking.json",
            "safe_driving.json",
            "special_situations.json"
        };

        Gson gson = new Gson();
        Type listType = new TypeToken<List<Question>>(){}.getType();

        for (String topic : topics) {
            try {
                InputStream is = context.getAssets().open("questions/" + topic);
                InputStreamReader reader = new InputStreamReader(is);
                List<Question> questions = gson.fromJson(reader, listType);
                allQuestions.addAll(questions);
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Question> getAllQuestions() {
        return new ArrayList<>(allQuestions);
    }

    public List<Question> getQuestionsByTopic(String topic) {
        List<Question> filtered = new ArrayList<>();
        for (Question q : allQuestions) {
            if (q.getTopic().equals(topic)) {
                filtered.add(q);
            }
        }
        return filtered;
    }

    public List<Question> getRandomQuestions(int count) {
        List<Question> shuffled = new ArrayList<>(allQuestions);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }

    public List<Question> getRandomQuestionsByTopic(String topic, int count) {
        List<Question> topicQuestions = getQuestionsByTopic(topic);
        Collections.shuffle(topicQuestions);
        return topicQuestions.subList(0, Math.min(count, topicQuestions.size()));
    }

    public int getTotalQuestionCount() {
        return allQuestions.size();
    }

    public int getTopicQuestionCount(String topic) {
        return getQuestionsByTopic(topic).size();
    }
}
