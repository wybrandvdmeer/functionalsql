package functionalsql.commands;

import functionalsql.commands.Join;

public class LeftJoin extends Join {
    public LeftJoin() {
        super(JOIN_TYPE.LEFT);
    }
}
