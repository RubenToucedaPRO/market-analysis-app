package com.market.analysis.presentation.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.market.analysis.application.dto.ProhibitedKeywordDTO;
import com.market.analysis.application.dto.ProhibitedTickerDTO;
import com.market.analysis.domain.port.in.ManageProhibitedKeywordUseCase;
import com.market.analysis.domain.port.in.ManageProhibitedTickerUseCase;
import com.market.analysis.presentation.dto.UiNotification;
import com.market.analysis.presentation.util.WebConstants;

import lombok.RequiredArgsConstructor;


/**
 * Controller for managing ProhibitedTicker views and operations.
 * Handles CRUD operations for prohibited tickers through the presentation
 * layer.
 */
@Controller
@RequestMapping("/prohibited-tickers")
@RequiredArgsConstructor
public class ProhibitedTickerController {

    private final ManageProhibitedTickerUseCase manageProhibitedTickerUseCase;
    private final ManageProhibitedKeywordUseCase manageProhibitedKeywordUseCase;
    private final MessageSource messageSource;

    @GetMapping
    public String listProhibitedTickers(Model model) {
        List<ProhibitedTickerDTO> prohibitedTickers = manageProhibitedTickerUseCase.getAllProhibitedTickers();
        List<ProhibitedKeywordDTO> prohibitedKeywords = manageProhibitedKeywordUseCase.getAllProhibitedKeywords();

        model.addAttribute("prohibitedTickers", prohibitedTickers);
        model.addAttribute("prohibitedKeywords", prohibitedKeywords);
        return "prohibited-tickers/list";
    }

    @PostMapping("/delete")
    public String deleteProhibitedTicker(@RequestParam("ticker") String ticker,
            RedirectAttributes redirectAttributes) {
        manageProhibitedTickerUseCase.removeProhibitedTicker(ticker);
        redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success("Ticker '" + ticker + "' desbloqueado y eliminado correctamente."));
        return "redirect:/prohibited-tickers";
    }

    @PostMapping("/keywords")
    public String addProhibitedKeyword(@RequestParam("keyword") String keyword, RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        String displayKeyword = keyword == null ? "" : keyword.trim();
        try {
            manageProhibitedKeywordUseCase
                    .addProhibitedKeyword(ProhibitedKeywordDTO.builder().keyword(displayKeyword).build());
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.success(
                            messageSource.getMessage("prohibited.tickers.keyword.added", new Object[] { displayKeyword },
                                    locale)));
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.error(ex.getMessage()));
        }
        return "redirect:/prohibited-tickers";
    }

    @PostMapping("/keywords/delete")
    public String deleteProhibitedKeyword(@RequestParam("keyword") String keyword,
            RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        String displayKeyword = keyword == null ? "" : keyword.trim();
        manageProhibitedKeywordUseCase.removeProhibitedKeyword(displayKeyword);
        redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success(
                        messageSource.getMessage("prohibited.tickers.keyword.removed", new Object[] { displayKeyword },
                                locale)));
        return "redirect:/prohibited-tickers";
    }

}
