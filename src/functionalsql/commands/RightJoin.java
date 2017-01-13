package functionalsql.commands;

import functionalsql.commands.Join;

public class RightJoin extends Join {
    public RightJoin() {
        super(JOIN_TYPE.RIGHT);
    }
}

