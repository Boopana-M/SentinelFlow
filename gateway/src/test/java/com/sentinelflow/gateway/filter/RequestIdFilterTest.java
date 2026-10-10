package com.sentinelflow.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RequestIdFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGenerateRequestIdWhenHeaderIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/gateway/status"))
                .andExpect(status().isOk())
                .andExpect(header().exists(RequestIdFilter.REQUEST_ID_HEADER))
                .andExpect(header().string(
                        RequestIdFilter.REQUEST_ID_HEADER,
                        matchesPattern("^[0-9a-fA-F\\-]{36}$")
                ));
    }

    @Test
    void shouldPropagateProvidedRequestId() throws Exception {
        String customId = "trace-custom-987654";
        mockMvc.perform(get("/api/v1/gateway/status")
                        .header(RequestIdFilter.REQUEST_ID_HEADER, customId))
                .andExpect(status().isOk())
                .andExpect(header().string(RequestIdFilter.REQUEST_ID_HEADER, customId));
    }
}
