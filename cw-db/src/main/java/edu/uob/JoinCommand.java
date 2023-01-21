package edu.uob;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class JoinCommand extends CommandType {
    private boolean isJoinCommand;
    private int length;
    private String NameOfTable1;
    private String NameOfTable2;
    private String NameOfAtt1;
    private String NameOfAtt2;
    private String Information;

    public JoinCommand(String command) {
        isJoinCommand = parseJoinCommand(command);
        super.setCommandParsingValid(isJoinCommand);
    }

    private boolean parseJoinCommand(String command) {
        String[] commandArray = command.split("\\s+");
        length = commandArray.length;

        if (commandArray[0].equals("JOIN")) {
            super.setCommandType("Join");
        } else {
            super.setCommandType(null);
            return false;
        }

        if (length != 8) {
            super.setParsingError("Expecting 8 elements in <JOIN>. ");
            return false;
        }

        PlainText tableNameText1 = new PlainText(commandArray[1]);
        if (!tableNameText1.getParsingValid()) {
            super.setParsingError("<TableName>: "+tableNameText1.getErrorMessage());
            return false;
        }
        NameOfTable1 = commandArray[1];

        if (!commandArray[2].equals("AND")) {
            super.setParsingError("Expecting 'AND' in <JOIN>. ");
            return false;
        }

        PlainText tableNameText2= new PlainText(commandArray[3]);
        if (!tableNameText2.getParsingValid()) {
            super.setParsingError("<TableName>: "+tableNameText2.getErrorMessage());
            return false;
        }
        NameOfTable2 = commandArray[3];

        if (!commandArray[4].equals("ON")) {
            super.setParsingError("Expecting 'ON' in <JOIN>. ");
            return false;
        }

        PlainText attName1= new PlainText(commandArray[5]);
        if (!attName1.getParsingValid()) {
            super.setParsingError("<AttributeName>: "+attName1.getErrorMessage());
            return false;
        }
        NameOfAtt1 = commandArray[5];

        if (!commandArray[6].equals("AND")) {
            super.setParsingError("Expecting 'AND' in <JOIN>. ");
            return false;
        }

        PlainText attName2= new PlainText(commandArray[7]);
        if (!attName2.getParsingValid()) {
            super.setParsingError("<AttributeName>: "+attName2.getErrorMessage());
            return false;
        }
        NameOfAtt2 = commandArray[7];


        return true;
    }
    public void interpretCommand(QueryHandler handler) throws IOException {
        super.setInterpretingState(false);
        if (DBServer.getDatabase().equals("")){
            super.setInterpretingError("Set a database as current database when use <JOIN>.");
            return;
        }

        File FileOfTable1 = new File(DBServer.getDatabaseDirectory().toPath()+
                File.separator+DBServer.getDatabase()+File.separator+NameOfTable1+".tab");
        if (!FileOfTable1.exists()) {
            super.setInterpretingError("The 1st <TableName> in <JOIN> does not exist. ");
            return;
        }
        File FileOfTable2 = new File(DBServer.getDatabaseDirectory().toPath()+
                File.separator+DBServer.getDatabase()+File.separator+NameOfTable2+".tab");
        if (!FileOfTable2.exists()) {
            super.setInterpretingError("The 2nd <TableName> in <JOIN> does not exist. ");
            return;
        }

        Table firTable = new Table(FileOfTable1);
        Table secTable = new Table(FileOfTable2);
        int IndexOfAtt1= getAttributeColumn(NameOfAtt1, firTable);
        int IndexOfAtt2 = getAttributeColumn(NameOfAtt2, secTable) ;
        if (IndexOfAtt1==-1||IndexOfAtt2==-1) {
            setInterpretingError("Some <AttributeName> cannot be found in table. ");
            return;
        }
        ArrayList<Integer> joinedIndexLst = firTable.getJoinedIndexLst(secTable, IndexOfAtt1, IndexOfAtt2);
        Information = getFinalInformation(firTable, secTable, NameOfAtt1, NameOfAtt2,
                IndexOfAtt1, IndexOfAtt2, joinedIndexLst);

        super.setInterpretingState(true);

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

    // Join Table to String
    private String getFinalInformation(Table firstTable, Table secondTable,
                                  String attName1, String attName2,
                                  int MatchedCol1, int MatchedCol2,
                                  ArrayList<Integer> joinList){
        StringBuffer InformationBuffer = new StringBuffer("\nid\t");
        for (int i = 1;i<firstTable.getNumOfColumns();i++){
            if (attName1!="id" && i ==MatchedCol1) {
                continue;
            }
            // the first part of the 0th row;
            InformationBuffer.append(firstTable.getTableLine().get(0)[i]).append("\t");
        }
        for (int i = 1;i<secondTable.getNumOfColumns();i++){
            if (attName2!="id" && i ==MatchedCol2) {
                continue;
            }
            // the second part of the 0th row;
            InformationBuffer.append(secondTable.getTableLine().get(0)[i]).append("\t");
        }
        InformationBuffer.append("\n");
        for (int i = 0;i<joinList.size()/2;i++){
            InformationBuffer.append(i+1).append("\t");
            for (int j = 1;j<firstTable.getNumOfColumns();j++){
                if (attName1!="id" && j ==MatchedCol1){
                    continue;
                }
                // the first part of this row;
                String tempString1 = firstTable.getTableLine().get(joinList.get(2*i))[j];
                InformationBuffer.append(tempString1).append("\t");
            }
            for (int k = 1;k<secondTable.getNumOfColumns();k++){
                if (attName2!="id" && k ==MatchedCol2){
                    continue;
                }
                // the second part of this row;
                String tempString2 = secondTable.getTableLine().get(joinList.get(2 * i + 1))[k];
                InformationBuffer.append(tempString2).append("\t");
            }
            InformationBuffer.append("\n");
        }
        return InformationBuffer.toString();
    }

    public String getInformation() {
        return Information;
    }

}
