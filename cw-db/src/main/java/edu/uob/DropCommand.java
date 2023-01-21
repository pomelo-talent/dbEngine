package edu.uob;

import java.io.File;
import java.util.Locale;

public class DropCommand extends CommandType {
    private boolean isDropCommand;
    private String NameOfDropping;
    private String TypeOfDropping;

    public DropCommand(String command){
        isDropCommand = parseDropCommand(command);
        super.setCommandParsingValid(isDropCommand);
    }

    private boolean parseDropCommand(String command) {
        String[] commandArray = command.split("\\s+");

        if (commandArray[0].equals("DROP")) {
            super.setCommandType("Drop");
        } else {
            super.setCommandType(null);
            return false;
        }

        if (commandArray.length != 3) {
            super.setParsingError("Expecting 3 elements in <DROP>. ");
            return false;
        }
        if (!commandArray[1].equals("TABLE") &&
                !commandArray[1].equals("DATABASE")){
            super.setParsingError("Expecting 'DATABASE' or 'TABLE' in <DROP>. ");
            return false;
        }

        /*
        PlainText TypeText = new PlainText(commandArray[1]);
        if (!TypeText.getParsingValid()) {
            super.setParsingError(TypeText.getErrorMessage());
            return false;
        }*/
        PlainText NameText = new PlainText(commandArray[2]);
        if (!NameText.getParsingValid()) {
            super.setParsingError("<DatabaseName>/<TableName>: "+NameText.getErrorMessage());
            return false;
        }
        TypeOfDropping = commandArray[1];
        NameOfDropping = commandArray[2];
        return true;
    }

    public void interpretCommand(QueryHandler handler) {
        super.setInterpretingState(false);

        // Drop Table
        if (TypeOfDropping.equals("TABLE")) {
            if (DBServer.getDatabase().equals("")){
                super.setInterpretingError("Set a database as current database when use <DROP>.");
                return;
            }
            File oneTable = new File(DBServer.getDatabaseDirectory().toPath()+
                    File.separator+DBServer.getDatabase()+File.separator+NameOfDropping+".tab");
            if (!oneTable.exists()) {
                super.setInterpretingError("The <TableName> in <DROP> does not exist. ");
                return;
            }
            oneTable.delete();
            super.setInterpretingState(true);
            return;
        }

        // Drop Database
        File Database = new File(DBServer.getDatabaseDirectory().toPath()+
                File.separator+NameOfDropping);
        if (!Database.exists() || !Database.isDirectory()) {
            super.setInterpretingError("The <DatabaseName> in <DROP> does not exist.");
            return;
        }
        File[] TableOfDatabase = Database.listFiles();
        for (int i=0; i<TableOfDatabase.length; i++) {
            TableOfDatabase[i].delete();
        }
        Database.delete();
        if(DBServer.getDatabase()!=null) {
            if (DBServer.getDatabase().equals(NameOfDropping)) {
                handler.setCurrDatabase(null);
            }
        }
        super.setInterpretingState(true);


    }

}
