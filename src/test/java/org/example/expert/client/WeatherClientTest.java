package org.example.expert.client;

import org.example.expert.client.dto.WeatherDto;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class WeatherClientTest {

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    WeatherClient weatherClient;

    @Test
    @DisplayName("HTTP 상태 코드가 200이 아니면 예외 발생")
    void get_today_weather_status_is_not_ok_fail() {
        //given
        ResponseEntity<WeatherDto[]> responseEntity =
                new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        given(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                .willReturn(responseEntity);

        //when & then
        assertThatThrownBy(() -> weatherClient.getTodayWeather())
                .isInstanceOf(ServerException.class)
                .hasMessage("날씨 데이터를 가져오는데 실패했습니다. 상태 코드: " + HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @Test
    @DisplayName("응답 body가 null이면 예외 발생")
    void get_today_weather_body_is_null_fail() {
        //given
        ResponseEntity<WeatherDto[]> responseEntity =
                new ResponseEntity<>(null, HttpStatus.OK);

        given(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                .willReturn(responseEntity);

        //when & then
        assertThatThrownBy(() -> weatherClient.getTodayWeather())
                .isInstanceOf(ServerException.class)
                .hasMessage("날씨 데이터가 없습니다.");
    }

    @Test
    @DisplayName("응답 body가 비어있으면 예외 발생")
    void get_today_weather_body_is_empty_fail() {
        //given
        ResponseEntity<WeatherDto[]> responseEntity =
                new ResponseEntity<>(new WeatherDto[]{}, HttpStatus.OK);

        given(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                .willReturn(responseEntity);

        //when & then
        assertThatThrownBy(() -> weatherClient.getTodayWeather())
                .isInstanceOf(ServerException.class)
                .hasMessage("날씨 데이터가 없습니다.");
    }

    @Test
    @DisplayName("오늘에 해당하는 날씨 데이터를 찾을 수 없으면 예외 발생")
    void get_today_weather_is_not_found_fail() {
        //given
        WeatherDto weatherDto = new WeatherDto("03-04", "맑음");

        ResponseEntity<WeatherDto[]> responseEntity =
                new ResponseEntity<>(new WeatherDto[]{weatherDto}, HttpStatus.OK);

        given(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                .willReturn(responseEntity);

        //when & then
        assertThatThrownBy(() -> weatherClient.getTodayWeather())
                .isInstanceOf(ServerException.class)
                .hasMessage("오늘에 해당하는 날씨 데이터를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("오늘 날씨의 데이터를 정상적으로 반환")
    void get_today_weather_success() {
        //given
        WeatherDto weatherDto = new WeatherDto("03-06", "맑음");

        ResponseEntity<WeatherDto[]> responseEntity =
                new ResponseEntity<>(new WeatherDto[]{weatherDto}, HttpStatus.OK);

        given(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                .willReturn(responseEntity);

        //when
        String todayWeather = weatherClient.getTodayWeather();

        //then
        assertThat(todayWeather).isEqualTo("맑음");

    }
}