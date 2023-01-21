package edu.uob;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class AlterCommand extends CommandType {
    private boolean isAlterCommand;
    private int length;
    private String NameOfTable;
    private String TypeOfAlter;
    private String NameOfAttribute;
    private String[] comArray;


    public AlterCommand(String command) {
        isAlterCommand = parseAlterCommand(command);
        super.setCommandParsingValid(isAlterCommand);
    }

    private boolean parseAlterCommand(String command) {
        String[] commandArray = command.split("\\s+");
        length = commandArray.length;

        if (commandArray[0].equals("ALTER")) {
            super.setCommandType("Alter");
        } else {
            super.setCommandType(null);
            return false;
        }

        if (length != 5) {
            super.setParsingError("Expecting 5 elements in <ALTER>. ");
            return false;
        }

        if (!commandArray[1].equals("TABLE")) {
            super.setParsingError("Expecting 'TABLE' in <ALTER>. ");
            return false;
        }

        PlainText tableNameText = new PlainText(commandArray[2]);
        if (!tableNameText.getParsingValid()) {
            super.setParsingError("<TableName>: "+tableNameText.getErrorMessage());
            return false;
        }
        NameOfTable = commandArray[2];

        if (!commandArray[3].equals("ADD")
        && !commandArray[3].equals("DROP")) {
            super.setParsingError("Expecting 'ADD' or 'DROP' as <AlterationType> in <ALTER>. ");
            return false;
        }
        TypeOfAlter = commandArray[3];

        PlainText attributeNameText = new PlainText(commandArray[4]);
        if (!attributeNameText.getParsingValid()) {
            super.setParsingError("<AttributeName>: "+attributeNameText.getErrorMessage());
            return false;
        }
        NameOfAttribute = commandArray[4];

        comArray = commandArray;
        return true;

    }

    public void interpretCommand(QueryHandler handler) throws IOException {
        super.setInterpretingState(false);
        if (handler.getCurrDatabase().equals("")) {
            super.setInterpretingError("Set a database as current database when use <ALTER>. ");
            return;
        }

        File oneTable = new File( DBServer.getDatabaseDirectory().toPath()+ File.separator +
                handler.getCurrDatabase()+File.separator+NameOfTable+".tab");
        if (!oneTable.exists()) {
            super.setInterpretingError("The <TableName> in <ALTER> does not exist. ");
            return;
        }

        Table newTable= new Table(oneTable);
        int newTableColumns = newTable.getNumOfColumns();

        if (comArray[3].equals("ADD")) {
            ArrayList<String[]> newTableLine = newTable.getTableLine();
            boolean isAttribute=false;
            // i begins from 0
            for (int i=0; i<newTableColumns; i++) {
                if (newTableLine.get(0)[i].equals(NameOfAttribute)) {
                    isAttribute=true;
                }
            }
            if (isAttribute) {
                setInterpretingError("The <AttributeName> in <ALTER> 'Add' already exists. ");
                return;
            }
            newTable.addTableAttribute(oneTable, NameOfAttribute);
            super.setInterpretingState(true);
            return;
        //drop
        } else {
            if (newTableColumns<1) {
                super.setInterpretingError("There is not sufficient column(attribute) in Table for <ALTER> 'Drop'. ");
                return;
            }
            ArrayList<String[]> newTableLine = newTable.getTableLine();
            // isAttribute: whether the NameOfAttribute exists in the Table
            boolean isAttribute=false;
            int IndexOfAttribute=1;
            // i begins from 1 (avoiding from drop "id")
            for (int i=1; i<newTableColumns; i++) {
                if (newTableLine.get(0)[i].equals(NameOfAttribute)) {
                    isAttribute=true;
                    IndexOfAttribute = i;
                }
            }
            if (!isAttribute) {
                super.setInterpretingError("There is no such <AttributeName> in Table for <ALTER> 'Drop'. ");
                return;
            }
            newTable.dropTableAttribute(oneTable, IndexOfAttribute);
            super.setInterpretingState(true);
            return;

        }
    }
}
