package edu.uob;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DeleteCommand extends CommandType {
    private boolean isDeleteCommand;
    private int length;
    private String NameOfTable;
    private Condition deleteCondition;

    public DeleteCommand(String command) {
        isDeleteCommand = parseDeleteCommand(command);
        super.setCommandParsingValid(isDeleteCommand);
    }

    private boolean parseDeleteCommand(String command) {
        String[] commandArray = command.split("\\s+");
        length = commandArray.length;

        if (commandArray[0].equals("DELETE")) {
            super.setCommandType("Delete");
        } else {
            super.setCommandType(null);
            return false;
        }

        if (length<5) {
            super.setParsingError("Expecting at least 5 elements in <Delete>. ");
            return false;
        }

        if (!commandArray[1].equals("FROM")) {
            super.setParsingError("Expecting 'FROM' in <Delete>. ");
            return false;
        }

        PlainText tableNameText = new PlainText(commandArray[2]);
        if (!tableNameText.getParsingValid()) {
            super.setParsingError("<TableName>: "+tableNameText.getErrorMessage());
            return false;
        }
        NameOfTable = commandArray[2];

        if (!commandArray[3].equals("WHERE")) {
            super.setParsingError("Expecting 'Where' in <Delete>. ");
            return false;
        }

        int indexBegOfCond = command.indexOf("WHERE")+5;
        for (int i=indexBegOfCond; i<command.length(); i++) {
            if (command.charAt(i) != ' ') {
                indexBegOfCond = i;
                break;
            }
        }
        String Condition = command.substring(indexBegOfCond, command.length());;
        //System.out.println("condition is"+ Condition);
        Condition con = new Condition(Condition.split("\\s+"));
        if (!con.getParsingValid()) {
            super.setParsingError("<Condition>: "+con.getErrorMessage());
            return false;
        }
        deleteCondition = con;

        return true;
    }

    public void interpretCommand(QueryHandler handler) throws IOException {
        super.setInterpretingState(false);
        if (DBServer.getDatabase().equals("")){
            super.setInterpretingError("Set a database as current database when use <DELETE>.");
            return;
        }
        File oneTable = new File(DBServer.getDatabaseDirectory().toPath()+
                File.separator+DBServer.getDatabase()+File.separator+NameOfTable+".tab");
        if (!oneTable.exists()) {
            super.setInterpretingError("The <TableName> in <DELETE> does not exist. ");
            return;
        }

        Table newTable = new Table(oneTable);
        ArrayList<String[]> finalTable = deleteCondition.getFinalTable(newTable);
        if (finalTable==null) {
            setInterpretingError("Please check your query.");
            return;
        }
        newTable.deleteFromTable(oneTable, finalTable);
        super.setInterpretingState(true);
    }
}
