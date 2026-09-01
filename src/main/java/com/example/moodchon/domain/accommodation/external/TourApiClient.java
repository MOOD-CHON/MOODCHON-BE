package com.example.moodchon.domain.accommodation.external;

import com.example.moodchon.domain.chonkangs.entity.Region;
import com.example.moodchon.domain.place.entity.PlaceCategory;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

// 한국관광공사 TourAPI 4.0 areaBasedList2 / detailCommon2 / detailImage2. 실제 서비스키로 호출 검증 완료.
@Component
public class TourApiClient {

    private final RestClient restClient;
    private final TourApiProperties properties;
    private final JsonMapper jsonMapper;

    public TourApiClient(TourApiProperties properties, JsonMapper jsonMapper) {
        this.properties = properties;
        this.jsonMapper = jsonMapper;
        this.restClient = RestClient.create();
    }

    public List<TourApiPlace> searchPlaces(Region region, PlaceCategory category, int numOfRows) {
        String rawResponseBody = requestAreaBasedList(region, TourApiCategoryCode.resolve(category), numOfRows);
        return parseItems(rawResponseBody, this::toPlace);
    }

    public Set<PlaceCategory> syncablePlaceCategories() {
        return TourApiCategoryCode.syncableCategories();
    }

    public String fetchOverview(String contentId) {
        try {
            String rawResponseBody = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("apis.data.go.kr")
                            .path("/B551011/KorService2/detailCommon2")
                            .queryParam("serviceKey", properties.serviceKey())
                            .queryParam("MobileOS", "ETC")
                            .queryParam("MobileApp", "moodchon")
                            .queryParam("_type", "json")
                            .queryParam("contentId", contentId)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);

            JsonNode root = jsonMapper.readTree(rawResponseBody);
            JsonNode itemNode = firstItem(root.path("response").path("body").path("items").path("item"));
            return itemNode.path("overview").asString("");
        } catch (RestClientException | JacksonException e) {
            throw new CustomException(ErrorCode.TOUR_API_REQUEST_FAILED);
        }
    }

    public List<String> fetchImages(String contentId) {
        try {
            String rawResponseBody = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("apis.data.go.kr")
                            .path("/B551011/KorService2/detailImage2")
                            .queryParam("serviceKey", properties.serviceKey())
                            .queryParam("MobileOS", "ETC")
                            .queryParam("MobileApp", "moodchon")
                            .queryParam("_type", "json")
                            .queryParam("contentId", contentId)
                            .queryParam("imageYN", "Y")
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);

            JsonNode root = jsonMapper.readTree(rawResponseBody);
            JsonNode itemNode = root.path("response").path("body").path("items").path("item");

            List<String> imageUrls = new ArrayList<>();
            if (itemNode.isArray()) {
                itemNode.forEach(item -> imageUrls.add(item.path("originimgurl").asString("")));
            } else if (itemNode.isObject()) {
                imageUrls.add(itemNode.path("originimgurl").asString(""));
            }
            return imageUrls.stream().filter(url -> !url.isBlank()).toList();
        } catch (RestClientException | JacksonException e) {
            throw new CustomException(ErrorCode.TOUR_API_REQUEST_FAILED);
        }
    }

    private String requestAreaBasedList(Region region, String contentTypeId, int numOfRows) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("apis.data.go.kr")
                            .path("/B551011/KorService2/areaBasedList2")
                            .queryParam("serviceKey", properties.serviceKey())
                            .queryParam("MobileOS", "ETC")
                            .queryParam("MobileApp", "moodchon")
                            .queryParam("_type", "json")
                            .queryParam("arrange", "A")
                            .queryParam("contentTypeId", contentTypeId)
                            .queryParam("areaCode", TourApiRegionCode.resolve(region))
                            .queryParam("numOfRows", numOfRows)
                            .queryParam("pageNo", 1)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.TOUR_API_REQUEST_FAILED);
        }
    }

    private <T> List<T> parseItems(String rawResponseBody, java.util.function.Function<JsonNode, T> mapper) {
        try {
            JsonNode root = jsonMapper.readTree(rawResponseBody);
            JsonNode itemNode = root.path("response").path("body").path("items").path("item");

            List<T> results = new ArrayList<>();
            if (itemNode.isArray()) {
                itemNode.forEach(item -> results.add(mapper.apply(item)));
            } else if (itemNode.isObject()) {
                results.add(mapper.apply(itemNode));
            }
            return results;
        } catch (JacksonException e) {
            throw new CustomException(ErrorCode.TOUR_API_REQUEST_FAILED);
        }
    }

    private JsonNode firstItem(JsonNode itemNode) {
        return itemNode.isArray() ? itemNode.path(0) : itemNode;
    }

    private TourApiPlace toPlace(JsonNode item) {
        return new TourApiPlace(
                item.path("contentid").asString(""),
                item.path("title").asString(""),
                item.path("addr1").asString(""),
                item.path("mapx").asDouble(0),
                item.path("mapy").asDouble(0),
                item.path("firstimage").asString(null)
        );
    }
}
