package com.market.analysis.presentation.controller;

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
import com.market.analysis.domain.model.PageResult;
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
    public String listProhibitedTickers(
            @RequestParam(value = "tickerPage", defaultValue = "0") int tickerPage,
            @RequestParam(value = "keywordPage", defaultValue = "0") int keywordPage,
            Model model) {
        tickerPage = Math.max(0, tickerPage);
        keywordPage = Math.max(0, keywordPage);
        int pageSize = WebConstants.DEFAULT_PAGE_SIZE;
        PageResult<ProhibitedTickerDTO> tickerPageResult =
                manageProhibitedTickerUseCase.getProhibitedTickers(tickerPage, pageSize);
        PageResult<ProhibitedKeywordDTO> keywordPageResult =
                manageProhibitedKeywordUseCase.getProhibitedKeywords(keywordPage, pageSize);

        model.addAttribute(WebConstants.ATTR_TICKER_PAGE, tickerPageResult);
        model.addAttribute(WebConstants.ATTR_KEYWORD_PAGE, keywordPageResult);
        return WebConstants.TEMPLATE_PROHIBITED_TICKERS_LIST;
    }

    @PostMapping("/delete")
    public String deleteProhibitedTicker(
            @RequestParam("ticker") String ticker,
            @RequestParam(value = "tickerPage", defaultValue = "0") int tickerPage,
            @RequestParam(value = "keywordPage", defaultValue = "0") int keywordPage,
            RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        manageProhibitedTickerUseCase.removeProhibitedTicker(ticker);
        String message = messageSource.getMessage("prohibited.tickers.removed",
                new Object[] { ticker }, locale);
        redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success(message));
        return WebConstants.REDIRECT_PROHIBITED_TICKERS
                + "?tickerPage=" + tickerPage + "&keywordPage=" + keywordPage;
    }

    @PostMapping("/keywords")
    public String addProhibitedKeyword(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "tickerPage", defaultValue = "0") int tickerPage,
            @RequestParam(value = "keywordPage", defaultValue = "0") int keywordPage,
            RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        manageProhibitedKeywordUseCase
                .addProhibitedKeyword(ProhibitedKeywordDTO.builder().keyword(keyword).build());
        String message = messageSource.getMessage("prohibited.tickers.keyword.added",
                new Object[] { keyword }, locale);
        redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success(message));
        return WebConstants.REDIRECT_PROHIBITED_TICKERS
                + "?tickerPage=" + tickerPage + "&keywordPage=" + keywordPage;
    }

    @PostMapping("/keywords/delete")
    public String deleteProhibitedKeyword(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "tickerPage", defaultValue = "0") int tickerPage,
            @RequestParam(value = "keywordPage", defaultValue = "0") int keywordPage,
            RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        manageProhibitedKeywordUseCase.removeProhibitedKeyword(keyword);
        String message = messageSource.getMessage("prohibited.tickers.keyword.removed",
                new Object[] { keyword }, locale);
        redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success(message));
        return WebConstants.REDIRECT_PROHIBITED_TICKERS
                + "?tickerPage=" + tickerPage + "&keywordPage=" + keywordPage;
    }

}
