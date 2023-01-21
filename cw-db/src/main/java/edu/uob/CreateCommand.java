package edu.uob;


import java.io.File;
import java.io.IOException;

public class CreateCommand extends CommandType {
    private boolean isCreateCommand;
    private int length;
    private String[] AttributeArray;
    private String[] comArray;
    private String NameOfCreating;

    public CreateCommand(String command) {
        isCreateCommand = parseCreateCommand(command);
        super.setCommandParsingValid(isCreateCommand);
    }

    private boolean parseCreateCommand(String command) {
        String[] commandArray = command.split("\\s+");
        length = commandArray.length;

        if (commandArray[0].equals("CREATE")) {
            super.setCommandType("Create");
        } else {
            super.setCommandType(null);
            return false;
        }

        if (length < 3) {
            super.setParsingError("Expecting at least 3 elements in <CREATE>. ");
            //System.err.println("Expecting at least 3 elements in <CREATE>");
            return false;
        }

        if (!commandArray[1].equals("DATABASE")
                &&!commandArray[1].equals("TABLE")) {
            super.setParsingError("Expecting 'DATABASE' or 'TABLE' in <CREATE>. ");
            //System.err.println("Expecting <DATABASE> or <TABLE> in <CREATE>");
            return false;
        }

        PlainText NameText = new PlainText(commandArray[2]);
        if (!NameText.getParsingValid()) {
            super.setParsingError("<DatabaseName>/<TableName>: "+NameText.getErrorMessage());
            return false;
        }
        NameOfCreating = commandArray[2];

        if (length>3){
            // Check 'CREATE DATABASE'
             if (commandArray[1].equals("DATABASE")){
                super.setParsingError("Expecting 'CREATE DATABASE' <DatabaseName>. ");
                return false;
            }

            // Check 'CREATE TABLE'
            if (commandArray[3].charAt(0)!='(' ||
                    commandArray[length-1].charAt(commandArray[length-1].length()-1)!=')') {
                super.setParsingError("Expecting ( ) outside <AttributeList>. ");
                return false;
            }
            // The substring does not include "(" and ")"
            //System.out.println(command.substring(command.indexOf('(')+1,command.length()-1));
            String[] attList = command.substring(command.indexOf('(')+1,command.length()-1).split("\\s+");
            AttributeList lst = new AttributeList(attList);
            if (!lst.getParsingValid()) {
                super.setParsingError(lst.getErrorMessage());
                return false;
            }
            AttributeArray = lst.getAttributeArray();
        }
        comArray = commandArray;
        return true;
    }

    public void interpretCommand(QueryHandler handler) throws IOException {
        super.setInterpretingState(false);
        if (comArray[1].equals("DATABASE")) {
            createDatabase(handler);
        } else {
            createTable(handler);
        }
    }

    private void createTable(QueryHandler handler) throws IOException {

        if (handler.getCurrDatabase().equals("")){
            super.setInterpretingError("Set a database as current database when use <CreateTable> .");
            return;
        }
        File oneTable = new File( DBServer.getDatabaseDirectory().toPath()+ File.separator +
                handler.getCurrDatabase()+File.separator+NameOfCreating+".tab");
        if (oneTable.exists()) {
            super.setInterpretingError("The <TableName> in <CreateTable> already exists. ");
            return;
        }
        oneTable.createNewFile();
        Table newTable = new Table(oneTable);
        if (AttributeArray!=null) {
            boolean duplicate = false;
            for (int i=0; i<AttributeArray.length; i++) {
                for (int j=i+1; j<AttributeArray.length; j++) {
                    if (AttributeArray[i].equals(AttributeArray[j])) {
                        duplicate = true;
                    }
                }
            }
            if (duplicate) {
                super.setInterpretingError("<AttributeList> in <CreateTable> has duplicate elements. ");
                return;
            }
            newTable.setTableAttribute(oneTable, AttributeArray);
        }
        super.setInterpretingState(true);

    }

    private void createDatabase(QueryHandler handler) {
        File oneDatabase = new File(DBServer.getDatabaseDirectory().toPath()+File.separator+
                NameOfCreating);
        if (oneDatabase.exists()) {
            super.setInterpretingError("The <DatabaseName> in <Create> already exists. ");
            return;
        }
        oneDatabase.mkdir();
        super.setInterpretingState(true);
    }

}
