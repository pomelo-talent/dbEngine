package edu.uob;

public class Value {
    private boolean isValue;
    private String errorMessage;

    public Value(String Text) {
        isValue = parseValue(Text);

    }

    private boolean parseValue(String Text) {
        // NULL
        if (Text==null) {
            return true;
        }

        // <StringLiteral>
        if (Text.equals("")) {
            return true;
        }
        if ((Text.charAt(0)== '\'') && (Text.charAt(Text.length() - 1) == '\'')) {
            String StringLiteral = Text.substring(1,Text.length()-1);
            for (int i=0; i<StringLiteral.length(); i++) {
                if (!isSymbol(StringLiteral.charAt(i)) &&
                        !Character.isAlphabetic(StringLiteral.charAt(i)) &&
                        StringLiteral.charAt(i)!=' ') {
                    errorMessage = "Incorrect <StringLiteral>. ";
                    return false;
                }
            }
            return true;
        }

        // <BooleanLiteral>
        if (Text.equals("TRUE") || Text.equals("FALSE")) {
            return true;
        }

        // <FloatLiteral>/<IntegerLiteral>
        if (Character.isDigit(Text.charAt(0)) ||
                Text.charAt(0)=='+' ||
                Text.charAt(0)=='-') {
            for (int i = 1; i < Text.length() - 1; i++) {
                if (!Character.isDigit(Text.charAt(i)) && Text.charAt(i) != '.') {
                    errorMessage = "Incorrect <FloatLiteral>/<IntegerLiteral>. ";
                    return false;
                }
            }
            if (!Character.isDigit(Text.charAt(Text.length() - 1))) {
                errorMessage = "Incorrect <FloatLiteral>/<IntegerLiteral>. ";
                return false;
            }
            return true;
        }
        errorMessage = "Incorrect <Value>. "+Text;
        return false;
    }

    private boolean isSymbol(char ch) {
        if (ch == 0x40) return true;
        if (ch == 0x2D || ch == 0x2F) return true;
        if (0x23 <= ch && ch <= 0x26) return true;
        if (0x28 <= ch && ch <= 0x2B) return true;
        if (0x3C <= ch && ch <= 0x3E) return true;
        if (0x5B <= ch && ch <= 0x60) return true;
        if (0x7B <= ch && ch <= 0x7E) return true;
        return false;
    }

    public boolean getParsingValid() {
        return isValue;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
