package edu.uob;

import java.io.*;
import java.util.*;

public class Table {
    private int PrimaryKey = 0;
    private int ForeignKey = -1;
    private ArrayList<String[]> tableLine = new ArrayList<>();
    private String errorMessage;


    // Load Table from File
    public Table(File tableFile) throws IOException {
        try (FileReader reader = new FileReader(tableFile);
        BufferedReader buffReader = new BufferedReader(reader)) {
            String firstLine = buffReader.readLine();
            if (firstLine == null) {
                /*
                String[] newRow = new String[1];
                newRow[0] = null;
                tableLine.add(newRow);*/
                return;
            }
            String[] tableHeader = firstLine.split("\t");
            if (!tableHeader[0].equals("id")) {
                System.err.println("ERROR: The first line of the first column is not 'id'");
                return;
            }
            this.PrimaryKey = 0;
            this.tableLine.add(tableHeader);
            //this.columns = tableHeader.length;
            String otherLine = "";
            int count = 0;
            while ((otherLine = buffReader.readLine()) != null) {
                count++;
                String[] tableRow = otherLine.split("\t");
                this.tableLine.add(tableRow);
            }
            //buffReader.close();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public void setTableAttribute(File tableFile, String[] AttributeList) throws IOException {
        Table newTable = new Table(tableFile);
        // Check if the table is empty
        if (newTable.getNumOfRows()==0) {
            int columns = AttributeList.length;
            //System.out.println(columns);
            String[] newRow = new String[columns+1];
            newRow[0] = "id";
            for (int i=0; i<columns; i++) {
                int j=i+1;
                newRow[j] = AttributeList[i];
                //System.out.println(newRow[i]);
            }
            newTable.tableLine.add(newRow);
            //System.out.println(newTable.tableLine.get(0)[0]);
            newTable.saveTable(tableFile);
            //return;
        }
    }

    public void addTableAttribute(File tableFile, String AttributeName) throws IOException {
        Table newTable = new Table(tableFile);
        // Check if the table is empty
        // If the table is empty
        if (newTable.getNumOfRows()==0) {
            String[] newRow = new String[2];
            newRow[0] = "id";
            newRow[1] = AttributeName;
            newTable.tableLine.add(newRow);
            newTable.saveTable(tableFile);
        // If the table is not empty
        } else {
            ArrayList<String[]> emptyTableLine = new ArrayList<>();
            String[] firstRow = new String[newTable.getNumOfColumns()+1];
            for (int j=0; j<newTable.getNumOfColumns(); j++) {
                firstRow[j] = newTable.tableLine.get(0)[j];
            }
            firstRow[newTable.getNumOfColumns()] = AttributeName;
            emptyTableLine.add(firstRow);
            for (int i=1; i< newTable.getNumOfRows(); i++) {
                emptyTableLine.add(newTable.tableLine.get(i));
            }
            newTable.tableLine = emptyTableLine;
            newTable.saveTable(tableFile);
            /*
            int columns = newTable.getNumOfColumns();
            newTable.tableLine.get(0)[columns]=AttributeName;
            newTable.saveTable(tableFile);*/
        }
    }

    public void dropTableAttribute(File tableFile, int AttributeIndex) throws IOException {
        Table newTable = new Table(tableFile);
        ArrayList<String[]> emptyTableLine = new ArrayList<>();
        String[] newRow = new String[newTable.getNumOfColumns()-1];

        for (int i=0; i< newTable.getNumOfRows(); i++) {
            int k = 0;
            for (int j = 0; j < newTable.getNumOfColumns() - 1; j++) {
                if (j == AttributeIndex) {
                    continue;
                }
                newRow[k] = newTable.tableLine.get(i)[j];
                k++;
            }
            emptyTableLine.add(newRow);
        }
        newTable.tableLine = emptyTableLine;
        newTable.saveTable(tableFile);

    }

    public void insertTable(File tableFile, String[] values) throws  IOException {
        Table newTable = new Table(tableFile);
        String ValuesWithTab = String.join("\t", values);

        try (
        FileWriter writer = new FileWriter(tableFile,true);
        BufferedWriter buffWriter = new BufferedWriter(writer);) {
            buffWriter.write(newTable.getNumOfRows() + "\t" + ValuesWithTab + "\n");
            buffWriter.flush();
        } catch (IOException e) {
            System.err.println(e);
        }

    }

    public void deleteFromTable(File tableFile, ArrayList<String[]> deletedData) throws IOException {
        for (int i=1; i<deletedData.size(); i++) {
            for (int j=1; j<tableLine.size(); j++) {
                // if "id" equals
                if (deletedData.get(i)[0].equals(tableLine.get(j)[0])) {
                    tableLine.remove(tableLine.get(j));
                    break;
                }
            }
        }
        saveTable(tableFile);
    }


    public ArrayList<Integer> getJoinedIndexLst(Table anotherTable, int MatchedCol1, int MatchedCol2) {
        ArrayList<Integer> tempIntegerLst = new ArrayList<>();
        for (int i=0; i<this.getNumOfRows(); i++) {
            for (int j=0; j<anotherTable.getNumOfRows(); j++) {
                if (this.tableLine.get(i)[MatchedCol1].equals(anotherTable.getTableLine().get(j)[MatchedCol2])){
                    tempIntegerLst.add(i);
                    tempIntegerLst.add(j);
                }
            }
        }
        return tempIntegerLst;

    }

    public void updateTableData(File tableFile, int AttributeIndex, String Value, ArrayList<String[]> tempData) throws IOException {
        for (int i=1; i<tempData.size(); i++) {
            for (int j=1; j<tableLine.size(); j++) {
                if (tempData.get(i)[0].equals(tableLine.get(j)[0])) {
                    tableLine.get(j)[AttributeIndex]=Value;
                    break;
                }
            }
        }
        saveTable(tableFile);

    }

    private void saveTable(File tableFile) throws IOException {
        try (
                FileWriter writer = new FileWriter(tableFile);
                BufferedWriter buffWriter = new BufferedWriter(writer)) {
            //test: buffWriter.write("1");
            //System.out.println(columns);
            for (int i = 0; i < tableLine.size(); i++) {
                for (int j = 0; j < tableLine.get(0).length; j++) {
                    if (j == tableLine.get(0).length - 1) {
                        buffWriter.write(tableLine.get(i)[j] + "\n");
                        //buffWriter.write("1"+"\n");
                    } else {
                        buffWriter.write(tableLine.get(i)[j] + "\t");
                    }
                }
            }
            buffWriter.flush();
        } catch (IOException e) {
            System.err.println(e);
        }
    }



    /*
    public void printTable() {
        String[][] joinedTable = (String[][])tableLine.toArray(new String[0][0]);
        for (int i=0; i<joinedTable.length; i++) {
            for (int j=0; j<joinedTable[i].length; j++) {
                System.out.println(joinedTable[i][j]);
            }
            System.out.println("\n");
        }
    }

     */

    public int getNumOfRows() {
        return tableLine.size();
    }

    public int getNumOfColumns() {
        return tableLine.get(0).length;
    }

    public ArrayList<String[]> getTableLine() {
        return tableLine;
    }

    public int getKey() {
        return PrimaryKey;
    }

    public void setPrimaryKeyKey(int priKey) {
        PrimaryKey = priKey;
    }

    public int getForeignKey() {
        return ForeignKey;
    }

    public void setForeignKey(int forKey) {
        ForeignKey = forKey;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
