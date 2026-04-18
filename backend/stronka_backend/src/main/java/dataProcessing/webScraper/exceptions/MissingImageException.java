package dataProcessing.webScraper.exceptions;

import dataProcessing.exceptions.DataProcessorException;

public class MissingImageException extends DataProcessorException
{
    public MissingImageException(String exceptionField)
    {
        super("Missing image for: " + exceptionField, exceptionField);
    }
}
