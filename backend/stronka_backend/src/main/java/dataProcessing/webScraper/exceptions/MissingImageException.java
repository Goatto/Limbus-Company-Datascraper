package dataProcessing.webScraper.exceptions;

public class MissingImageException extends ScraperException
{
    public MissingImageException(String exceptionField)
    {
        super("Missing image for: " + exceptionField, exceptionField);
    }
}
