package edu.uob;

public class WildAttribList {
    private boolean isWildAttribList;
    private String errorMessage;
    private String[] Array;

    public WildAttribList(String[] list) {
        isWildAttribList = parseWildAttribList(list);
    }

    private boolean parseWildAttribList(String[] list) {
        if (list.length==1 && list[0].equals("*")) {
            String[] tempArray = {"*"};
            Array = tempArray;
            return true;
        }
        AttributeList lst = new AttributeList(list);
        if (lst.getParsingValid()) {
            Array = lst.getAttributeArray();
            return true;
        } else {
            errorMessage = "<WildAttribList> should be '*' or <AttributeList>. &" +lst.getErrorMessage();
        }
        return false;
    }

    public boolean getParsingValid() {
        return isWildAttribList;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String[] getAttributeArray() {
        return Array;
    }
}
