package dataProcessing.webScraper.extractors;

import dataProcessing.DTOBuilders;
import dataProcessing.ScraperDataDTOs;
import dataProcessing.services.PassiveService;
import dataProcessing.webScraper.ScraperNodeVisitors;
import dataProcessing.webScraper.WikimediaScraper;
import dataProcessing.webScraper.enums.PassiveCategory;
import dataProcessing.webScraper.exceptions.MissingSectionException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class PassiveScraper
{
    private final PassiveService passiveService;

    public PassiveScraper(PassiveService passiveService)
    {
        this.passiveService = passiveService;

    }

    // TODO Dodać dodawanie pasywek do DB
    public void scrapePassiveData(Document htmlContent, DTOBuilders.BaseEquippableBuilder<?> builder)
    {
        Set<PassiveCategory> previousCategories = EnumSet.noneOf(PassiveCategory.class);
        // Ciekawy styl jest z tym b:contains...
        Elements prePassiveHeaders = htmlContent.select("b:contains(Passive), "
                + "b:contains(Combat Passives), b:contains(Support Passive), b:contains(Passives)");

        PassiveCategory passiveCategory;
        for (Element prePassiveHeader : prePassiveHeaders)
        {
            String headerText = prePassiveHeader.text().trim();
            passiveCategory = PassiveCategory.passiveParser(headerText);
            if (passiveCategory == PassiveCategory.UNKNOWN)
            {
                continue;
            }
            // Z racji struktury strony, mamy kilka razy kontenery o tej samej zawartości, z racji,
            // że nie chcę wypisywac ich wszystkich, robimy continue, z jakiejś racji dochodzi do tego tylko dla E.G.O,
            // więc w poniższy warunek jest odpowiedni

            // Set zwraca false, jeżeli metoda add napotka już istniejący element
            if (!previousCategories.add(passiveCategory))
            {
                continue;
            }
            log.info(String.valueOf(passiveCategory));
            Element currentParent = prePassiveHeader.parent();
            // Pozwala nam an operowanie na stronach E.G.O jak i ID
            Element passiveContainer = Optional.ofNullable(currentParent)
                    .map(Element::nextElementSibling)
                    // Jeżeli nie możemy wyłapać następcę dla currentParent,
                    // przechodzimy do jego parenta i szukamy jego następcy
                    .orElseGet(() -> Optional.ofNullable(currentParent)
                            .map(Element::parent)
                            .map(Element::nextElementSibling)
                            // W innym wypadku dajemy pustego diva, żeby program się nie wyrzucił,
                            .orElseThrow(() -> new MissingSectionException("passiveContainer")));
            Elements divPassiveContainers = passiveContainer.select("div[style*=padding:10px]");

            for (Element divPassiveContainer : divPassiveContainers)
            {
                ScraperDataDTOs.Passive passive = ScraperNodeVisitors.extractPassive(divPassiveContainer);
                UUID uuid = passiveService.saveNewPassive(passive);
                switch (passiveCategory) {
                    case COMBAT_PASSIVE -> {
                        if (builder instanceof DTOBuilders.HasCombatPassive<?> combatBuilder)
                        {
                            combatBuilder.addCombatPassive(uuid);
                        }
                    }
                    case SUPPORT_PASSIVE -> {
                        if (builder instanceof DTOBuilders.HasSupportPassive<?> supportBuilder)
                        {
                            supportBuilder.addSupportPassive(uuid);
                        }
                    }
                }
            }
        }
    }
}