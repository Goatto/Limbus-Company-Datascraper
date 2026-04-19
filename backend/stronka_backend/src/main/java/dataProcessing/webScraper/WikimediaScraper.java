package dataProcessing.webScraper;

import dataProcessing.DTOBuilders;
import dataProcessing.services.EGOService;
import dataProcessing.services.IDService;
import dataProcessing.webScraper.extractors.AbilityScraper;
import dataProcessing.webScraper.extractors.EGOScraper;
import dataProcessing.webScraper.extractors.IDScraper;
import dataProcessing.webScraper.extractors.PassiveScraper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WikimediaScraper
{
    private final IDScraper idScraper;
    private final EGOScraper egoScraper;
    private final AbilityScraper abilityScraper;
    private final PassiveScraper passiveScraper;
    private final IDService idService;
    private final EGOService egoService;

    public WikimediaScraper(IDScraper idScraper, EGOScraper egoScraper,
                            AbilityScraper abilityScraper, PassiveScraper passiveScraper,
                            IDService idService, EGOService egoService)
    {
        this.idScraper = idScraper;
        this.egoScraper = egoScraper;
        this.abilityScraper = abilityScraper;
        this.passiveScraper = passiveScraper;
        this.idService = idService;
        this.egoService = egoService;
    }

    public void processIdentity(Document htmlContent)
    {
        DTOBuilders.IDDataBuilder builder = new DTOBuilders.IDDataBuilder();
        if(idScraper.scrapeGeneralIDData(htmlContent, builder) != null)
        {
            idScraper.scrapeSanityData(htmlContent, builder);
            abilityScraper.scrapeIDAbilityData(htmlContent, builder);
            passiveScraper.scrapePassiveData(htmlContent, builder);
            idService.saveNewID(builder.buildIDData());
        }
    }

    public void processEgo(Document htmlContent)
    {
        DTOBuilders.EGODataBuilder builder = new DTOBuilders.EGODataBuilder();
        if(egoScraper.scrapeEGOAbilities(htmlContent, builder) != null)
        {
            passiveScraper.scrapePassiveData(htmlContent, builder);
            egoService.saveNewEGO(builder.buildEGOData());
        }
    }
}