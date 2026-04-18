package dataProcessing.exceptions;

import lombok.Getter;

@Getter
public class DataProcessorException extends RuntimeException
{
    private final String exceptionField;

    public DataProcessorException(String message, String exceptionField)
    {
        super(message);
        this.exceptionField = exceptionField;
    }
}
