package edu.uob;


public class AttributeList {
    private boolean isAttributeList;
    private String AttributeString="";
    private String errorMessage;
    private String[] Array;


    public AttributeList(String[] list) {
        isAttributeList = parseAttributeList(list);
    }

    private boolean parseAttributeList(String[] list) {

        // Check whether there is a ',' before an attribute
        for (int i = 1;i<list.length;i++){
            if (list[i].charAt(0)!=','){
                if (list[i-1].charAt(list[i-1].length()-1)!=','){
                    //System.out.println(list[i]);
                    errorMessage = "The ','s in <AttributeList> are not enough. ";
                    return false;
                }
            }
        }

        for (String lst: list) {
            AttributeString += lst;
        }
        //System.out.println(AttributeString);

        // Check whether the character is <Letter> or ','
        for (int i=0; i<AttributeString.length(); i++) {
            String s = String.valueOf(AttributeString.charAt(i));
            PlainText lst = new PlainText(s);
            if (!lst.getParsingValid() &&
                 AttributeString.charAt(i)!=',' ) {
                errorMessage = "Only ',' or <LETTER> can exist in <AttributeList>. ";
                return false;
            }
        }
        // Check redundant ','
        for (int i=1; i<AttributeString.length(); i++){
            int j=i-1;
            if (AttributeString.charAt(i)==',' && AttributeString.charAt(j)==',') {
                //System.out.println("i"+AttributeString.charAt(i));
                //System.out.println("j"+AttributeString.charAt(j));
                errorMessage = "There is redundant ',' in <AttributeList>. ";
                return false;

            }
        }
        Array = AttributeString.split(",");
        return true;
    }

    public boolean getParsingValid() {
        return isAttributeList;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String[] getAttributeArray() {
        return Array;
    }

}
