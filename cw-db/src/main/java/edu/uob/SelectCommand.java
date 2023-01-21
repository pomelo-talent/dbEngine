package edu.uob;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SelectCommand extends CommandType {
    private boolean isSelectCommand;
    private int length;
    private String NameOfTable;
    private Condition selectCondition;
    private String[] WildAttribArray;
    private String Information;

    public SelectCommand(String command) {
        isSelectCommand = parseSelectCommand(command);
        super.setCommandParsingValid(isSelectCommand);
    }

    private boolean parseSelectCommand(String command) {
        String[] commandArray = command.split("\\s+");
        length =commandArray.length;

        if (commandArray[0].equals("SELECT")) {
            super.setCommandType("Select");
        } else {
            super.setCommandType(null);
            return false;
        }

        if (length<4) {
            super.setParsingError("Expecting at least 4 elements in <Select>. ");
            return false;
        }
        // Check whether "From" is in <Select>
        boolean fromFinder=false;
        int fromIndex=0;
        for (int i=0; i<length; i++) {
            if (commandArray[i].equals("FROM")) {
                fromFinder=true;
                fromIndex = i;
            }
        }
        if (!fromFinder) {
            super.setParsingError("Expecting 'FROM' in <Select>. ");
            return false;
        }
        // Check <TableName>
        PlainText NameText = new PlainText(commandArray[fromIndex+1]);
        if (!NameText.getParsingValid()) {
            super.setParsingError("<TableName>: "+NameText.getErrorMessage());
            return false;
        }
        NameOfTable = commandArray[fromIndex+1];
        // Check whether "WHERE" <Condition> is in <Select>
        if (length>4) {
            if (length<6) {
                super.setParsingError("Expecting <Condition> in <Select>. ");
                return false;
            }
            if (!commandArray[fromIndex+2].equals("WHERE")) {
                super.setParsingError("Expecting 'WHERE' after <TableName> in <Select>.");
                return false;
            }
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
            selectCondition = con;
        }
        // Check WildAttribList
        // SELECT has 6 characters (index:0-5),
        // and index6 is one space, so <WildAttriblist> starts from index7
        int indexEndOfWild = 7;
        for (int i=7; i<command.length(); i++) {
            if (command.charAt(i) == ' ') {
                indexEndOfWild = i;
                break;
            }
        }
        WildAttribList lst= new WildAttribList(command.substring(7, indexEndOfWild).split("\\s+"));
        if (!lst.getParsingValid()) {
            super.setParsingError(lst.getErrorMessage());
            return false;
        }
        WildAttribArray = lst.getAttributeArray();
        return true;
    }

    public void interpretCommand(QueryHandler handler) throws IOException {
        setInterpretingState(false);
        if (handler.getCurrDatabase().equals("")) {
            super.setInterpretingError("Set a database as current database when use <Select>. ");
            return;
        }

        File oneTable = new File( DBServer.getDatabaseDirectory().toPath()+ File.separator +
                handler.getCurrDatabase()+File.separator+NameOfTable+".tab");
        if (!oneTable.exists()) {
            super.setInterpretingError("The <TableName> in <Select> does not exist. ");
            return;
        }

        Table newTable= new Table(oneTable);
        ArrayList<String[]> finalTable;
        // Expecting <Condition>
        if (length>4) {
            finalTable = selectCondition.getFinalTable(newTable);
            //System.out.println("yes");
        } else {
        // no <Condition>
            finalTable = newTable.getTableLine();
        }

        if (finalTable==null) {
            setInterpretingError("Please check your query.");
            return;
        }

        StringBuffer InformationBuffer = new StringBuffer();
        // SELECT *
        if (WildAttribArray.length==1 &&WildAttribArray[0].equals("*")) {
            InformationBuffer.append("\n");
            for (int i=0;i<finalTable.size();i++){
                for (int j=0;j<finalTable.get(i).length;j++){
                    InformationBuffer.append(finalTable.get(i)[j]).append("\t");
                }
                InformationBuffer.append("\n");
            }
        } else {
            // SELECT <ValueList>
            ArrayList<Integer> number = new ArrayList<>();
            for (int i=0; i< WildAttribArray.length; i++) {
                for (int j = 0; j < finalTable.get(0).length; j++) {
                    if (WildAttribArray[i].equals(finalTable.get(0)[j])) {
                        number.add(j);
                        break;
                    }
                }
            }
            InformationBuffer.append("\n");
            for (String[] strings : finalTable) {
                for (Integer integer : number) {
                    InformationBuffer.append(strings[integer]).append("\t");
                }
                InformationBuffer.append("\n");
            }
        }
        Information = InformationBuffer.toString();
        setInterpretingState(true);
    }

    public String getInformation() {
        return Information;
    }
}
