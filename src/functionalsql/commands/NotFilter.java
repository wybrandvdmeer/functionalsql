package functionalsql.commands;

/**
 * Syntax: notfilter( column , value1 , value2 , ... )
 */
public class NotFilter extends Filter {
    public NotFilter() { super(false); }
}
