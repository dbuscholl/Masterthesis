/**
 * This is a custom exception which occurs, when the Importer could not find a column or column name.
 */
public class ColumnNameException extends Exception {
    public ColumnNameException() {}
    public ColumnNameException(String s) {
        super(s);
    }
}
