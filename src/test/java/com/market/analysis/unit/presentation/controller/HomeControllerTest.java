package com.market.analysis.unit.presentation.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import com.market.analysis.presentation.controller.HomeController;
import com.market.analysis.presentation.dto.UiNotification;
import com.market.analysis.presentation.util.WebConstants;

@DisplayName("HomeController Unit Tests")
@WebMvcTest(HomeController.class)
@AutoConfigureMockMvc(addFilters = false)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET / should return home view")
    void shouldReturnHomeView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }

    @Test
    @DisplayName("GET /login should return login view")
    void shouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("GET / should render flash notification when present")
    void shouldRenderFlashNotification() throws Exception {
        mockMvc.perform(get("/").flashAttr(
                        WebConstants.UI_NOTIFICATION_KEY,
                        UiNotification.error("Ticker data not found for: 999")))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(containsString("Ticker data not found for: 999")));
    }
}
