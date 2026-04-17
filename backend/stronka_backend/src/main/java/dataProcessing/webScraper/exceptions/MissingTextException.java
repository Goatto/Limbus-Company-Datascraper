package dataProcessing.webScraper.exceptions;

public class MissingTextException extends ScraperException {
    public MissingTextException(String exceptionField)
    {
        super("Missing text for: " + exceptionField, exceptionField);
    }
}
