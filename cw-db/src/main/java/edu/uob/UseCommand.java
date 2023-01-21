package edu.uob;

import java.io.File;

public class UseCommand extends CommandType {
    private boolean isUseCommand;
    private String DatabaseName;

    public UseCommand(String command) {
        isUseCommand = parseUseCommand(command);
        super.setCommandParsingValid(isUseCommand);
    }

    private boolean parseUseCommand(String command) {
        // Split by one or multiple spaces
        String[] commandArray = command.split("\\s+");

        if (commandArray[0].equals("USE")) {
            super.setCommandType("Use");
        } else {
            super.setCommandType(null);
            return false;
        }

        if (commandArray.length != 2) {
            super.setParsingError("Expecting 2 elements in <USE>. ");
            //System.err.println("Expecting 2 elements in <USE>");
            return false;
        }
        PlainText NameText = new PlainText(commandArray[1]);
        if (!NameText.getParsingValid()) {
            super.setParsingError("<DatabaseName>: "+NameText.getErrorMessage());
            return false;
        }
        DatabaseName = commandArray[1];
        return true;
    }

    @Override
    public void interpretCommand(QueryHandler handler) {
        super.setInterpretingState(false);
        File databaseFolder = new File(DBServer.getDatabaseDirectory().toPath()+ File.separator+DatabaseName);
        if (databaseFolder.exists() && databaseFolder.isDirectory()) {
            handler.setCurrDatabase(DatabaseName);
            super.setInterpretingState(true);
            //System.out.println(super.getCurrentDatabase());
        } else {
            super.setInterpretingError("The <DatabaseName> in <USE> does not exist. ");
            return;
        }
    }
}
