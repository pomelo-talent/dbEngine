package edu.uob;

public class NameValuePair {
    private boolean isNameValuePair;
    private String errorMessage;
    private String[] NameValueArray;

    public NameValuePair(String Text) {
        isNameValuePair = parseNameValuePair(Text);
    }

    private boolean parseNameValuePair(String Text) {
        Text = Text.replace(" ", "");
        System.out.println(Text);
        String[] Array = Text.split("=");
        for (int i=0; i<Array.length; i++) {
            if ((i+1)%2==0) {
                PlainText AttributeText = new PlainText(Array[i]);
                if (!AttributeText.getParsingValid()) {
                    errorMessage = "<AttributeName>: " +AttributeText.getErrorMessage();
                    return false;
                }
            } else {
                Value newValue = new Value(Array[i]);
                if (newValue.getParsingValid()) {
                    errorMessage = "<AttributeName>: " +newValue.getErrorMessage();
                    return false;
                }
            }
        }
        return true;
    }

    public boolean getParsingValid() {
        return isNameValuePair;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String[] getNameValueArray() {
        return NameValueArray;
    }
}
