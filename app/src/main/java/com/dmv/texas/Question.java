package com.dmv.texas;

public class Question {
    private String id;
    private String topic;
    private int difficulty;
    private String text;
    private String[] choices;
    private int correctIndex;
    private String explanation;
    private String reference;
    private Image image;

    public static class Image {
        private String type;
        private String assetId;

        public String getType() { return type; }
        public String getAssetId() { return assetId; }
    }

    public String getId() { return id; }
    public String getTopic() { return topic; }
    public int getDifficulty() { return difficulty; }
    public String getText() { return text; }
    public String[] getChoices() { return choices; }
    public int getCorrectIndex() { return correctIndex; }
    public String getExplanation() { return explanation; }
    public String getReference() { return reference; }
    public Image getImage() { return image; }

    public boolean hasImage() {
        return image != null && image.assetId != null;
    }

    public String getCorrectAnswer() {
        return choices[correctIndex];
    }

    public boolean isCorrect(int selectedIndex) {
        return selectedIndex == correctIndex;
    }
}
