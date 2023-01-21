package edu.uob;

import java.io.*;

public class QueryHandler {
    private String errorMessage;
    private String currDatabase;
    private String Information;

    public QueryHandler() {
        currDatabase = "";
        Information = "";
    }

    public boolean handleQuery(String command) throws IOException {
       if(!isQueryValid(command)) {
           return false;
       }
       // Eliminate the "," in the end of the command
       command = command.substring(0, command.length()-1);
       if (!isQueryParsingValid(command)) {
           return false;
       }
       return true;
    }

    public void setCurrDatabase(String Database) {
        if (!Database.equals("")&&Database!=null) {
            currDatabase = Database;
        }
    }

    public String getCurrDatabase() {
        return currDatabase;
    }

    public boolean isQueryValid(String command) {
        if (command.length() < 2) {
            //System.err.println("The Query is too short.");
            errorMessage = "The <Command> is too short. ";
            return false;
        }
        if (command.charAt(command.length()-1)!=';') {
            errorMessage = "The <Command> should end with ';'. ";
            return false;
        }
        return true;
    }

    public boolean isQueryParsingValid(String command) throws IOException {
        try {
            AllCommandType AllCommand = new AllCommandType(command);
            for (CommandType oneCommand : AllCommand.getAllCommandType()) {
                if (oneCommand.getCommandParsingValid()) {
                    oneCommand.interpretCommand(this);
                    //System.out.println("database" +oneCommand.getCurrentDatabase());
                    //setCurrDatabase(oneCommand.getCurrentDatabase());
                    //System.out.println("this" +this.currDatabase);
                    if (!oneCommand.getInterpretingState()) {
                        errorMessage = oneCommand.getInterpretingError();
                        return false;
                    }
                    if (oneCommand.getCommandType().equals("Select")||oneCommand.getCommandType().equals("Join")) {
                        Information = oneCommand.getInformation();
                    }
                    return true;
                } else if (oneCommand.getCommandType() != null) {
                    errorMessage = oneCommand.getParsingError();
                    return false;
                }
            }

        } catch (IOException e) {
            System.err.println(e);
        }
        errorMessage = "Invalid <CommandType>. ";
        return false;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getInformation() {
        return Information;
    }


}
