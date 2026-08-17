package com.conexa.starwars.integration;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

// boots the full spring context with H2 db and WireMock standing in for Swapi
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTest {

    @RegisterExtension
    static WireMockExtension SWAPI = WireMockExtension.newInstance()
            .options(wireMockConfig().port(8089))
            .build();

    @org.springframework.beans.factory.annotation.Autowired
    protected MockMvc mockMvc;

    @BeforeEach
    void resetWireMock() {
        SWAPI.resetAll();
    }
}
