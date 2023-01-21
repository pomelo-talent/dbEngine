package edu.uob;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class UpdateCommand extends CommandType  {
    private boolean isUpdateCommand;
    private int length;
    private String NameOfTable;
    private Condition updateCondition;
    private String[] NameValueLst;

    public UpdateCommand(String command) {
        isUpdateCommand = parseUpdateCommand(command);
        super.setCommandParsingValid(isUpdateCommand);
    }

    private boolean parseUpdateCommand(String command) {
        String[] commandArray = command.split("\\s+");
        length = commandArray.length;

        if (commandArray[0].equals("UPDATE")) {
            super.setCommandType("Update");
        } else {
            super.setCommandType(null);
            return false;
        }

        if (length<6) {
            super.setParsingError("Expecting at least 5 elements in <Update>. ");
            return false;
        }

        PlainText tableNameText = new PlainText(commandArray[1]);
        if (!tableNameText.getParsingValid()) {
            super.setParsingError("<TableName>: "+tableNameText.getErrorMessage());
            return false;
        }
        NameOfTable = commandArray[1];

        if (!commandArray[2].equals("SET")) {
            super.setParsingError("Expecting 'From' in <Update>. ");
            return false;
        }

        // Check whether "Where" is in <Update>
        boolean whereFinder=false;
        for (int i=0; i<length; i++) {
            if (commandArray[i].equals("WHERE")) {
                whereFinder=true;
            }
        }
        if (!whereFinder) {
            super.setParsingError("Expecting 'Where' in <Update>. ");
            return false;
        }
        // Check <NameValueList>
        String NameValueLstText = command.substring(command.indexOf("SET")+3,
                command.indexOf("WHERE"));
        NameValueLstText = NameValueLstText.replace(" ","");
        String[] NameValueTextLst= NameValueLstText.split(",");
        for (int i=0; i< NameValueTextLst.length; i++) {
            //System.out.println(NameValueTextLst[i]);
            NameValuePair oneNameValuePair = new NameValuePair(NameValueTextLst[i]);
            if (!oneNameValuePair.getParsingValid()) {
                super.setParsingError("<NameValueList>: "+oneNameValuePair.getErrorMessage());
                return false;
            }
        }
        NameValueLst=NameValueTextLst;

        // Check <Condition>
        int indexBegOfCond = command.indexOf("WHERE")+5;
        for (int i=indexBegOfCond; i<command.length(); i++) {
            if (command.charAt(i) != ' ') {
                indexBegOfCond = i;
                break;
            }
        }
        String Condition = command.substring(indexBegOfCond, command.length());
        //System.out.println("condition is"+ Condition);
        Condition con = new Condition(Condition.split("\\s+"));
        if (!con.getParsingValid()) {
            super.setParsingError("<Condition>: "+con.getErrorMessage());
            return false;
        }
        updateCondition = con;

        return true;
    }

    public void interpretCommand(QueryHandler handler) throws IOException {

        setInterpretingState(false);
        if (handler.getCurrDatabase().equals("")) {
            super.setInterpretingError("Set a database as current database when use <UPDATE>. ");
            return;
        }

        File oneTable = new File( DBServer.getDatabaseDirectory().toPath()+ File.separator +
                handler.getCurrDatabase()+File.separator+NameOfTable+".tab");
        if (!oneTable.exists()) {
            super.setInterpretingError("The <TableName> in <UPDATE> does not exist. ");
            return;
        }

        Table newTable = new Table(oneTable);
        ArrayList<String[]> finalTable = updateCondition.getFinalTable(newTable);
        if (finalTable==null) {
            setInterpretingError("Please check your query.");
            return;
        }

        for (String NameValuePair:NameValueLst) {
             String[] NameValuePairArray = splitIncludRegex(NameValuePair, "=");
             String AttributeName = NameValuePairArray[0];
             int AttributeIndex = getAttributeColumn(AttributeName, newTable);
             String Value = NameValuePairArray[2];
             newTable.updateTableData(oneTable, AttributeIndex, Value, finalTable);
        }

        super.setInterpretingState(true);
    }

    private static String[] splitIncludRegex(String s, String regex) {
        String[] tokens = s.split(regex);

        String[] newTokens = new String[2*tokens.length-1];
        String remainingString = null;

        int size=0;
        for (int i=0; i< tokens.length; i++) {
            newTokens[2*i] = tokens[i];
            size += newTokens[2*i].length();
            remainingString = s.substring(size);
            if (i== tokens.length-1) break;
            int j=remainingString.indexOf(tokens[i+1]);
            newTokens[2*i+1] = remainingString.substring(0,j);
            size += newTokens[2*i+1].length();
        }
        if (remainingString!=null && remainingString.length()>0) {
            String[] finalTokens= new String[newTokens.length+1];
            System.arraycopy(newTokens,0,finalTokens,0,newTokens.length);
            finalTokens[finalTokens.length-1] = remainingString;
            return finalTokens;
        } else
            return newTokens;
    }

    private int getAttributeColumn(String Text, Table newTable) {
        String[] attributes = newTable.getTableLine().get(0);
        // include "id"
        for (int i=0; i<attributes.length; i++) {
            if (attributes[i].equals(Text)) {
                return i;
            }
        }
        return -1;
    }
}
