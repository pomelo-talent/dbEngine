package edu.uob;

import java.io.IOException;

public abstract class CommandType {
    private String type;
    private boolean isCommandParsingValid;
    private String parsingError;
    private boolean interpretingState;
    private String interpretingError;
    private String Information;

    public CommandType() {
        type = "";
    }

    public abstract void interpretCommand(QueryHandler handler) throws IOException;

    public void setCommandType(String type) {
        this.type = type;
    }

    public String getCommandType() {
        return type;
    }

    public void setCommandParsingValid(boolean valid) {
       isCommandParsingValid = valid;
    }

    public boolean getCommandParsingValid() {
        return isCommandParsingValid;
    }

    public void setParsingError(String error) {
        parsingError = error;
    }

    public String getParsingError() {
        return parsingError;
    }

    public void setInterpretingState(boolean state) {
        interpretingState = state;
    }

    public boolean getInterpretingState() {
        return interpretingState;
    }

    public void setInterpretingError(String error) {
        interpretingError = error;
    }

    public String getInterpretingError() {
        return interpretingError;
    }

    public void setInformation(String infor) {
        Information = infor;
    }

    public String getInformation() {
        return Information;
    }

}
