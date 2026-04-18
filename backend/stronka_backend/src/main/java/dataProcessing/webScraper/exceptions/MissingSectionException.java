package dataProcessing.webScraper.exceptions;

import dataProcessing.exceptions.DataProcessorException;

public class MissingSectionException extends DataProcessorException
{
    public MissingSectionException(String exceptionField)
    {
        super("Missing text for: " + exceptionField, exceptionField);
    }
}
