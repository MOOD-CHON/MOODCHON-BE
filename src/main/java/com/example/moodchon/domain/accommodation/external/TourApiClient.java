package com.example.moodchon.domain.accommodation.external;

import com.example.moodchon.domain.chonkangs.entity.Region;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

// 한국관광공사 TourAPI 4.0 areaBasedList2 (숙박: contentTypeId=32).
// 실제 서비스키/네트워크 환경에서 응답 스펙 재확인 필요.
@Component
public class TourApiClient {

    private static final String ACCOMMODATION_CONTENT_TYPE_ID = "32";

    private final RestClient restClient;
    private final TourApiProperties properties;
    private final JsonMapper jsonMapper;

    public TourApiClient(TourApiProperties properties, JsonMapper jsonMapper) {
        this.properties = properties;
        this.jsonMapper = jsonMapper;
        this.restClient = RestClient.create();
    }

    public List<TourApiAccommodation> searchAccommodations(Region region, int numOfRows) {
        try {
            String rawResponseBody = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("apis.data.go.kr")
                            .path("/B551011/KorService2/areaBasedList2")
                            .queryParam("serviceKey", properties.serviceKey())
                            .queryParam("MobileOS", "ETC")
                            .queryParam("MobileApp", "moodchon")
                            .queryParam("_type", "json")
                            .queryParam("arrange", "A")
                            .queryParam("listYN", "Y")
                            .queryParam("contentTypeId", ACCOMMODATION_CONTENT_TYPE_ID)
                            .queryParam("areaCode", TourApiRegionCode.resolve(region))
                            .queryParam("numOfRows", numOfRows)
                            .queryParam("pageNo", 1)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);

            return parseAccommodations(rawResponseBody);
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.TOUR_API_REQUEST_FAILED);
        }
    }

    private List<TourApiAccommodation> parseAccommodations(String rawResponseBody) {
        try {
            JsonNode root = jsonMapper.readTree(rawResponseBody);
            JsonNode itemNode = root.path("response").path("body").path("items").path("item");

            List<TourApiAccommodation> accommodations = new ArrayList<>();
            if (itemNode.isArray()) {
                itemNode.forEach(item -> accommodations.add(toAccommodation(item)));
            } else if (itemNode.isObject()) {
                accommodations.add(toAccommodation(itemNode));
            }
            return accommodations;
        } catch (JacksonException e) {
            throw new CustomException(ErrorCode.TOUR_API_REQUEST_FAILED);
        }
    }

    private TourApiAccommodation toAccommodation(JsonNode item) {
        return new TourApiAccommodation(
                item.path("contentid").asString(""),
                item.path("title").asString(""),
                item.path("addr1").asString(""),
                item.path("mapx").asDouble(0),
                item.path("mapy").asDouble(0),
                item.path("firstimage").asString(null)
        );
    }
}
