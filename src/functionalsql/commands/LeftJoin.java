package functionalsql.commands;

public class LeftJoin extends Join {
    public LeftJoin() {
        super(JOIN_TYPE.LEFT);
    }
}
