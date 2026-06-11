package com.market.analysis.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.market.analysis.presentation.util.WebConstants;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return WebConstants.TEMPLATE_HOME;
    }

    @GetMapping("/login")
    public String login() {
        return WebConstants.TEMPLATE_LOGIN;
    }
}
