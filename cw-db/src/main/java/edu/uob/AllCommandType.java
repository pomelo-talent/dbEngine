package edu.uob;

import java.io.IOException;
import java.util.*;

public class AllCommandType {
    private ArrayList<CommandType> types = new ArrayList<>();

    public AllCommandType(String command) {
        types.add(new UseCommand(command));
        types.add(new CreateCommand(command));
        types.add(new DropCommand(command));
        types.add(new AlterCommand(command));
        types.add(new InsertCommand(command));
        types.add(new SelectCommand(command));
        types.add(new JoinCommand(command));
        types.add(new DeleteCommand(command));
        types.add(new UpdateCommand(command));
    }

    public ArrayList<CommandType> getAllCommandType() {
        return types;
    }

}
