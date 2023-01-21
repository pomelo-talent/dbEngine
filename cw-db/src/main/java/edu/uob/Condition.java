package edu.uob;

import java.io.File;
import java.io.IOException;
import java.io.SyncFailedException;
import java.util.*;

public class Condition {
    private boolean isCondition;
    private String ConditionString="";
    private String errorMessage;
    private ArrayList<String> conditionElementArray= new ArrayList<>();
    private ArrayList<String> AttributeArray= new ArrayList<>();
    private ArrayList<ArrayList<String[]>> finalTableStorer = new ArrayList<>();

    public Condition(String[] cond) {
        isCondition = parseCondition(cond);
    }

    /* Some examples:
    SELECT * FROM table1 WHERE name!='Tom';
    SELECT * FROM table1 WHERE (name=='Tom') AND (mark>70);
    SELECT * FROM table1 WHERE (name=='Tom') AND (mark>70) OR (age>17);
     */
    private boolean parseCondition(String[] cond) {
        for (String cnd:cond) {
            ConditionString += cnd;
        }

        // Find Operator
        if (containOperatorValue(ConditionString)==null) {
            errorMessage = "There is no <Operator> in <Condition>. ";
            return false;
        }
        // Find AND/OR; Split
        // Load conditionElementArray. It will store like this: {"PASS", "==", "False", "And", "Mark", ">", "35")
        if (!containAndOr(ConditionString)) {
            // there is no ()
            ConditionString = ConditionString.substring(0,ConditionString.length());
            // <AttributeName>
            String AttributeName = ConditionString.substring(0, findOperatorValue(ConditionString));
            PlainText AttributeText = new PlainText(AttributeName);
            if (!AttributeText.getParsingValid()) {
                errorMessage = "<AttributeName>: "+AttributeText.getErrorMessage();
                return false;
            }
            conditionElementArray.add(AttributeName);
            AttributeArray.add(AttributeName);
            // <Operator>
            int operatorLength = containOperatorValue(ConditionString).length();
            conditionElementArray.add(containOperatorValue(ConditionString));
            // <Value>
            String conValue = ConditionString.substring(findOperatorValue(ConditionString)+operatorLength);
            Value ConditionValue = new Value(conValue);
            if (!ConditionValue.getParsingValid()) {
                errorMessage = "<Value>: "+AttributeText.getErrorMessage();
                return false;
            }
            conditionElementArray.add(conValue);
            //System.out.println(conValue);
        } else {
            String[] ConditionArray1 = splitIncludRegex(ConditionString,"AND|OR");
            for (int i = 0; i < ConditionArray1.length; i++) {
                // "AND" | "OR"
                if (ConditionArray1[i].equals("AND") ||
                        ConditionArray1[i].equals("OR")) {
                    this.conditionElementArray.add(ConditionArray1[i]);
                    //System.out.println(ConditionArray1[i]());
                } else {
                    // <AttributeName>; Delete (
                    if (ConditionArray1[i].charAt(0) != '(') {
                        errorMessage = "There is not sufficient '(' in <Condition>. ";
                        return false;
                    }
                    String AttributeName = ConditionArray1[i].substring(1, findOperatorValue(ConditionArray1[i]));
                    PlainText AttributeText = new PlainText(AttributeName);
                    if (!AttributeText.getParsingValid()) {
                        errorMessage = "<AttributeName>: " + AttributeText.getErrorMessage();
                        return false;
                    }
                    this.conditionElementArray.add(AttributeName);
                    this.AttributeArray.add(AttributeName);
                    //System.out.println(AttributeName);
                    int operatorLength = containOperatorValue(ConditionArray1[i]).length();
                    //this.conditionElementArray.add(containOperatorValue(ConditionString));
                    this.conditionElementArray.add(containOperatorValue(ConditionArray1[i]));
                    //System.out.println(containOperatorValue(ConditionString));
                    // <Value>; Delete )
                    if (ConditionArray1[i].charAt(ConditionArray1[i].length() - 1) != ')') {
                        errorMessage = "There is not sufficient ')' in <Condition>. ";
                        return false;
                    }
                    String conValue = ConditionArray1[i].substring(findOperatorValue(ConditionArray1[i]) + operatorLength,
                            ConditionArray1[i].length() - 1);
                    Value ConditionValue = new Value(conValue);
                    if (!ConditionValue.getParsingValid()) {
                        errorMessage = "<Value>: " + AttributeText.getErrorMessage();
                        return false;
                    }
                    this.conditionElementArray.add(conValue);
                    //System.out.println(conValue);
                }
            }
        }
        return true;
    }

    private boolean containAndOr(String text) {
        if (text.contains("AND") ||
                text.contains("OR")) {
            return true;
        }
        return false;
    }

    private String containOperatorValue(String text) {
        String[] operators = {"==", ">=", "<=", ">", "<", "!=", "LIKE"};
        for (int i = 0; i < operators.length; i++) {
            if (text.contains(operators[i])) {
                return operators[i];
            }
        }
        return null;
    }

    private int findOperatorValue(String text) {
        String[] operators = {"==", ">=", "<=", ">", "<", "!=", "LIKE"};
        int index = 0;
        for (int i = 0; i < operators.length; i++) {
            if (text.indexOf(operators[i]) != -1) {
                index = text.indexOf(operators[i]);
            }
        }
        return index;
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

    public ArrayList<String> getCondElementArray() {
        return conditionElementArray;
    }

    public ArrayList<String[]> getFinalTable(Table newTable) throws IOException {
        if (!containAttribute(newTable)) {
            errorMessage = "Some <AttributeName> in <Condition> does not exist in the <TableName>. ";
            return null;
        }

        Stack<String> conditionElementStack = new Stack<>();
        ArrayList<ArrayList<String[]>> tableStorer = new ArrayList<>();
        int tableIndex = 0;
        for (int i=0; i<conditionElementArray.size(); i++) {
            //System.out.println("i---" + conditionElementArray.get(i));
            // conditionElementArray.get(i) not <Value>
            if (i==0||containOperatorValue(conditionElementArray.get(i-1))==null) {
                conditionElementStack.push(conditionElementArray.get(i));
                //System.out.println(conditionElementArray.get(i)+"has been pushed");
              // conditionElementArray.get(i) is <Value>
            } else {
                conditionElementStack.push(conditionElementArray.get(i));
                //System.out.println(conditionElementArray.get(i)+"has been pushed");
                // simple calculation (with operators)
                CalculateTable(conditionElementStack, tableStorer, newTable);
                conditionElementStack.push("table" + tableIndex);
                //System.out.println("table" + tableIndex+"has been pushed");
                tableIndex++;
            }
        }
        // "AND"/"OR" calculation
        while (conditionElementStack.size()>1) {
            CalculateTable(conditionElementStack, tableStorer, newTable);
            conditionElementStack.push("table"+tableIndex);
            tableIndex++;
        }
        if (conditionElementStack.size()==1) {
            return tableStorer.get(tableStorer.size()-1);
        }
        return null;
    }

    private boolean containAttribute(Table newTable) throws IOException {
        int isAttribute =0;
        for (int j=0; j< AttributeArray.size(); j++) {
           for (int i=0; i< newTable.getNumOfColumns(); i++) {
                if (newTable.getTableLine().get(0)[i].equals(AttributeArray.get(j))) {
                    isAttribute++;
                    break;
                }
            }
        }

        if (isAttribute==AttributeArray.size()) {
            return true;
        }
        return false;
    }

    private void CalculateTable(Stack<String> newStack, ArrayList<ArrayList<String[]>> Storer, Table newTable) {
        String element2 = newStack.pop();
        //System.out.println("element2 "+element2 +"has been popped");
        String operationSymbol = newStack.pop();
        //System.out.println("operationSymbol "+operationSymbol+"has been popped");
        String element1 = newStack.pop();
       // System.out.println("element1 "+element1+"has been popped");
        // is <Operator>
        if (containOperatorValue(operationSymbol)!=null) {
            Storer.add(simpleCalculateTable(element1, operationSymbol, element2, newTable));
            finalTableStorer = Storer;
            //printTableStorer(finalTableStorer);
        // is "AND"/"OR"
        } else {
            // use table's index to find those temp tables in
            int element1Index = Integer.parseInt(element1.substring(5));
            ///System.out.println("element1 "+element1Index);
            int element2Index = Integer.parseInt(element2.substring(5));
            //System.out.println("element2 "+element2Index);
            Storer.add(logiCalcultateTable(Storer.get(element1Index), Storer.get(element2Index),
                    operationSymbol));
            finalTableStorer = Storer;
            //printTableStorer(finalTableStorer);
        }

    }
    public void printTableStorer(ArrayList<ArrayList<String[]>> TableStorer) {
        int a=0;
        for (ArrayList<String[]> table:TableStorer) {
            //System.out.println(a);
            for (int i = 0; i < table.size(); i++) {
                for (int j = 0; j < table.get(0).length; j++) {
                    System.out.print(table.get(i)[j] + "  ");
                }
                System.out.print("\n");
            }
            System.out.print("\n");
            a++;
        }
    }

    private ArrayList<String[]> simpleCalculateTable(String AttributeName, String operator, String Value, Table newTable) {
        ArrayList<String[]> resultTable = new ArrayList<>();
        resultTable.add(newTable.getTableLine().get(0));
        int AttributeColumn = getAttributeColumn(AttributeName, newTable);
        if (operator.equals("==")||operator.equals("!=")) {
            equalCalculate(AttributeColumn, operator, Value, newTable, resultTable);
            //System.out.println(AttributeColumn);
            //System.out.println(operator);
            //System.out.println(Value);
        } else if (operator.equals("LIKE")) {
            likeCalculate(AttributeColumn, operator, Value, newTable, resultTable);
            //
        } else {
            compareCalculate(AttributeColumn, operator, Value, newTable, resultTable);
        }
        return resultTable;
    }

    private ArrayList<String[]> logiCalcultateTable(ArrayList<String[]> table1, ArrayList<String[]> table2,
                                                    String operationSymbol) {
        ArrayList<String[]> resultTable = new ArrayList<>();
        resultTable.add(table1.get(0));
        /*
        System.out.println("table1 \n");
        for (int i=0;i<table1.size();i++) {
            for (int j=0; j<table1.get(0).length; j++) {
                System.out.println(table1.get(i)[j]);
            }
        }

        System.out.println("table2 \n");
        for (int i=0;i<table2.size();i++) {
            for (int j=0; j<table2.get(0).length; j++) {
                System.out.println(table2.get(i)[j]);
            }
        }*/

        if (operationSymbol.equals("AND")) {
            if (table1.size()==1) {
                //System.out.println("yes");
                return resultTable;
            }
            if (table2.size()==1) {
                //System.out.println("yes");
                return resultTable;
            }
            for (int i=1; i<table1.size(); i++) {
                int indexOfTable1 = Integer.parseInt(table1.get(i)[0]);
                for (int j=1; j<table2.size(); j++) {
                    int indexOfTable2 = Integer.parseInt(table2.get(j)[0]);
                    if (indexOfTable2 == indexOfTable1) {
                        //System.out.println(indexOfTable1);
                        resultTable.add(table1.get(i));
                        //break;
                    }
                    if (indexOfTable2>indexOfTable1) {
                        break;
                    }
                }
            }
        } else {
            // "or"
            //System.out.println("yes");
            if (table1.size()==1) {
                return table2;
            }
            if (table2.size()==1) {
                return table1;
            }
            ArrayList<Integer> IdOfTable1 = new ArrayList<>();
            ArrayList<Integer> IdOfTable2 = new ArrayList<>();
            ArrayList<Integer> Ids = new ArrayList<>();
            for (int i=1; i<table1.size(); i++) {
                IdOfTable1.add(Integer.parseInt(table1.get(i)[0]));
                //System.out.println(Integer.parseInt(table1.get(i)[0]));
            }
            for (int j=1; j<table2.size(); j++) {
                IdOfTable2.add(Integer.parseInt(table2.get(j)[0]));
                //System.out.println(Integer.parseInt(table2.get(j)[0]));
            }
            //System.out.println("size is ");
            int p1=0;
            int p2=0;
            int l1 = IdOfTable1.size();
            //System.out.println(IdOfTable1.size());
            int l2 = IdOfTable2.size();
            Ids.addAll(IdOfTable1);
            Ids.addAll(IdOfTable2);
            //System.out.println(IdOfTable2.size());
            /*
            while (p1<l1 || p2<l2) {
                if (p1==l1) {
                    Ids.add(IdOfTable2.get(p2));
                    p2++;
                } else if (p2==l2) {
                    Ids.add(IdOfTable1.get(p1));
                    p1++;
                } else if (IdOfTable1.get(p1)<IdOfTable2.get(p2)) {
                    Ids.add(IdOfTable1.get(p1));
                    p1++;
                } else {
                    Ids.add(IdOfTable2.get(p2));
                    p2++;
                }
            }*/

            Collections.sort(Ids);
            Set<Integer> set = new LinkedHashSet<>();
            set.addAll(Ids);
            /*
            System.out.print("Ids: \n");
            for (int j=0; j<Ids.size(); j++) {
                System.out.print(Ids.get(j) + "\t");
            }
            System.out.print("\n");*/
            int q=1; int w=1;
            for (int singleId: set) {
                System.out.print("from" + singleId + "\t");
                //System.out.println("set:" +singleId);
                if (q<table1.size()&&singleId==Integer.parseInt(table1.get(q)[0])) {
                    //System.out.print("from" + singleId + "\t");
                    resultTable.add(table1.get(q));
                    q++;
                }
                System.out.print("\n");
                if (w<table2.size()&&singleId==Integer.parseInt(table2.get(w)[0])) {
                    //System.out.print("from"+singleId + "\t");
                    resultTable.add(table2.get(w));
                    w++;
                }
            }


        }
        return resultTable;
    }

    private boolean isIDInTable(ArrayList<String[]> resultTable) {
        if (resultTable==null) {
            return false;
        }

        return true;
    }

    private void equalCalculate(int AttributeColumn, String operator, String Value, Table newTable,
                                ArrayList<String[]> TableInStorer) {
        if (operator.equals("==")) {
            for (int i=1; i<newTable.getNumOfRows(); i++) {
                if (newTable.getTableLine().get(i)[AttributeColumn].equals(Value)) {
                    TableInStorer.add(newTable.getTableLine().get(i));

                }
            }
        } else {
            for (int i=1; i<newTable.getNumOfRows(); i++) {
                if (!newTable.getTableLine().get(i)[AttributeColumn].equals(Value)) {
                    TableInStorer.add(newTable.getTableLine().get(i));
                }
            }
        }
    }

    private void likeCalculate(int AttributeColumn, String operator, String Value, Table newTable,
                               ArrayList<String[]> TableInStorer) {
        if (operator.equals("LIKE")) {
            for (int i=0; i<newTable.getNumOfRows(); i++) {
                if (newTable.getTableLine().get(i)[AttributeColumn].contains(Value.substring(1,Value.length()-1))) {
                    TableInStorer.add(newTable.getTableLine().get(i));
                }
            }
        }
    }

    private void compareCalculate(int AttributeColumn, String operator, String Value, Table newTable,
                                  ArrayList<String[]> TableInStorer) {
        Float num2 = Float.parseFloat(Value);
        //System.out.println("col"+ AttributeColumn);
        for (int i=1; i< newTable.getNumOfRows(); i++) {
            Float num1 = Float.parseFloat(newTable.getTableLine().get(i)[AttributeColumn]);
            if (operator.equals(">")) {
                if (num1>num2) {
                    TableInStorer.add(newTable.getTableLine().get(i));
                }
            } else if (operator.equals(">=")) {
                if (num1>=num2) {
                    TableInStorer.add(newTable.getTableLine().get(i));
                }
            } else if(operator.equals("<")) {
                if (num1<num2) {
                    TableInStorer.add(newTable.getTableLine().get(i));
                }
            } else if(operator.equals("<=")) {
                if (num1<=num2) {
                    TableInStorer.add(newTable.getTableLine().get(i));
                }
            }
        }
    }

    private int getAttributeColumn(String Text, Table newTable) {
        String[] attributes = newTable.getTableLine().get(0);
        for (int i=0; i<attributes.length; i++) {
            if (attributes[i].equals(Text)) {
                return i;
            }
        }
        return 0;
    }

    public boolean getParsingValid() {
        return isCondition;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
