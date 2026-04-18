package dataProcessing.webScraper.exceptions;

import dataProcessing.exceptions.DataProcessorException;

public class MissingTextException extends DataProcessorException {
    public MissingTextException(String exceptionField)
    {
        super("Missing text for: " + exceptionField, exceptionField);
    }
}
