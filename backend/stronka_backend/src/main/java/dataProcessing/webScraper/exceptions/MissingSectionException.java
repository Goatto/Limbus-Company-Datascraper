package dataProcessing.webScraper.exceptions;

public class MissingSectionException extends ScraperException
{
    public MissingSectionException(String exceptionField)
    {
        super("Missing text for: " + exceptionField, exceptionField);
    }
}
