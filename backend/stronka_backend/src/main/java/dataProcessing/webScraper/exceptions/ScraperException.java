package dataProcessing.webScraper.exceptions;

import lombok.Getter;

@Getter
public class ScraperException extends RuntimeException
{
    private final String exceptionField;

    public ScraperException(String message, String exceptionField)
    {
        super(message);
        this.exceptionField = exceptionField;
    }
}
