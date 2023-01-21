package edu.uob;

import java.io.File;
import java.io.IOException;

public class InsertCommand extends CommandType {
    private boolean isInsertCommand;
    private int length;
    private String NameOfTable;
    private String[] ValueArray;

    public InsertCommand(String command) {
        isInsertCommand = parseInsertCommand(command);
        super.setCommandParsingValid(isInsertCommand);
    }

    private boolean parseInsertCommand(String command) {
        String[] commandArray = command.split("\\s+");
        length = commandArray.length;

        if (commandArray[0].equals("INSERT")) {
            super.setCommandType("Insert");
        } else {
            super.setCommandType(null);
            return false;
        }

        if (length<5) {
            super.setParsingError("Expecting at least 5 elements in <INSERT>. ");
            return false;
        }

        if (!commandArray[1].equals("INTO")) {
            super.setParsingError("Expecting 'Into' in <INSERT>. ");
            return false;
        }

        PlainText tableNameText = new PlainText(commandArray[2]);
        if (!tableNameText.getParsingValid()) {
            super.setParsingError("<TableName>: "+tableNameText.getErrorMessage());
            return false;
        }
        NameOfTable = commandArray[2];

        if (!commandArray[3].contains("VALUES")) {
            super.setParsingError("Expecting 'Values' in <INSERT>." );
            return false;
        }

        if (command.charAt(command.length()-1)!=')') {
            super.setParsingError("Expecting ')' in <INSERT>. ");
            return false;
        }
        int indexBegOfValueList=command.indexOf("VALUES")+6;
        for (int i=indexBegOfValueList; i<command.length(); i++) {
            if (command.charAt(i)=='(') {
                indexBegOfValueList = i;
            }
        }
        String conSubstring = command.substring(indexBegOfValueList+1, command.length()-1);
        if (conSubstring.contains(",")) {
            conSubstring = conSubstring.replace(" ","");
            String[] conSubstringList = conSubstring.split(",");
            for (int i=0; i<conSubstringList.length; i++) {
                //System.out.println(conSubstringList[i]);
                Value oneValue = new Value(conSubstringList[i]);
                if (!oneValue.getParsingValid()) {
                    super.setParsingError("<Value>: "+oneValue.getErrorMessage());
                    return false;
                }
            }
            ValueArray = conSubstringList;
        } else {
            Value singleValue = new Value(conSubstring);
            if (!singleValue.getParsingValid()) {
                super.setParsingError("<Value>: "+singleValue.getErrorMessage());
                return false;
            }
            String[] tempArray = {conSubstring};
            ValueArray = tempArray;
        }
        return true;
    }

    public void interpretCommand(QueryHandler handler) throws IOException {
        super.setInterpretingState(false);
        if (handler.getCurrDatabase().equals("")) {
            super.setInterpretingError("Set a database as current database when use <INSERT>. ");
            return;
        }

        File oneTable = new File( DBServer.getDatabaseDirectory().toPath()+ File.separator +
                handler.getCurrDatabase()+File.separator+NameOfTable+".tab");
        if (!oneTable.exists()) {
            super.setInterpretingError("The <TableName> in <INSERT> does not exist. ");
            return;
        }

        Table newTable= new Table(oneTable);
        if (newTable.getNumOfColumns() != ValueArray.length+1) {
            super.setInterpretingError("Number of <Value> of <INSERT> is not equal to NumOfColumns of the table. ");
            return;
        }
        newTable.insertTable(oneTable, ValueArray);
        super.setInterpretingState(true);

    }

}
