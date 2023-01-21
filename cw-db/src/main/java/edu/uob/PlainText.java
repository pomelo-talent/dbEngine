package edu.uob;

public class PlainText {
    private boolean isPlainText;
    private String errorMessage;

    public PlainText(String Text) {
        isPlainText = parsePlainText(Text);
    }

    private boolean parsePlainText(String Text) {
        for (int i=0; i<Text.length(); i++) {
            if (!Character.isDigit(Text.charAt(i)) &&
                    !Character.isAlphabetic(Text.charAt(i))) {
                errorMessage = "Only <DIGIT> or <LETTER> can exist in <PlainText>. ";
                return false;
            }
        }
        return true;
    }

    public boolean getParsingValid() {
        return isPlainText;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
