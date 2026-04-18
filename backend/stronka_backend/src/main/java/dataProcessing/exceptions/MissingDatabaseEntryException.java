package dataProcessing.exceptions;

public class MissingDatabaseEntryException  extends DataProcessorException
{
    public MissingDatabaseEntryException(String exceptionField)
    {
        super("Missing image for: " + exceptionField, exceptionField);
    }
}
