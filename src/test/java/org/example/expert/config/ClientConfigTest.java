package org.example.expert.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class ClientConfigTest {
    
    @Test
    @DisplayName("Bean 주입이 정상적으로 생성된다.")
    void restTemplate_bean_created_success() {
        //given
        RestTemplateBuilder builder = new RestTemplateBuilder();
        ClientConfig clientConfig = new ClientConfig();

        //when
        RestTemplate restTemplate = clientConfig.restTemplate(builder);

        //then
        assertThat(restTemplate).isNotNull();
    }
}