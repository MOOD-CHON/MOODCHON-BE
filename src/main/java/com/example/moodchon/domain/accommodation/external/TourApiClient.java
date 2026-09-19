package com.example.moodchon.domain.accommodation.external;

import com.example.moodchon.domain.chonkangs.entity.Region;
import com.example.moodchon.domain.place.entity.PlaceCategory;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private static final int ROOM_MAX_COUNT = 20;
    private static final int LIST_PAGE_SIZE = 100;
    // 전국 숙소가 약 3천 곳이라 30페이지면 전부 훑는다. 무한 루프 방지용 상한이다.
    private static final int LIST_MAX_PAGES = 40;

    // detailInfo2 객실 시설 플래그 -> 화면 표기. 순서가 곧 노출 순서라 LinkedHashMap을 쓴다.
    private static final Map<String, String> ROOM_FACILITY_LABELS = new LinkedHashMap<>();

    static {
        ROOM_FACILITY_LABELS.put("roomaircondition", "에어컨");
        ROOM_FACILITY_LABELS.put("roomtv", "TV");
        ROOM_FACILITY_LABELS.put("roompc", "PC");
        ROOM_FACILITY_LABELS.put("roomsofa", "쇼파");
        ROOM_FACILITY_LABELS.put("roomcable", "케이블설치");
        ROOM_FACILITY_LABELS.put("roominternet", "인터넷");
        ROOM_FACILITY_LABELS.put("roomrefrigerator", "냉장고");
        ROOM_FACILITY_LABELS.put("roomtable", "테이블");
        ROOM_FACILITY_LABELS.put("roomhairdryer", "드라이기");
        ROOM_FACILITY_LABELS.put("roombathfacility", "목욕시설");
        ROOM_FACILITY_LABELS.put("roomtoiletries", "세면도구");
        ROOM_FACILITY_LABELS.put("roomcook", "취사용품");
    }

    private final RestClient restClient;
    private final TourApiProperties properties;
    private final JsonMapper jsonMapper;

    public TourApiClient(TourApiProperties properties, JsonMapper jsonMapper) {
        this.properties = properties;
        this.jsonMapper = jsonMapper;
        this.restClient = RestClient.create();
    }

    // region이 null이면 지역 무관 전국 검색.
    public List<TourApiPlace> searchPlaces(Region region, PlaceCategory category, int numOfRows) {
        String rawResponseBody = requestAreaBasedList(region, TourApiCategoryCode.resolve(category), numOfRows);
        return parseItems(rawResponseBody, this::toPlace);
    }

    // 숙소 추천 후보 풀. arrange=A(제목순)로 앞에서 N개만 가져오면 이름이 'ㄱ'으로 시작하는 곳만
    // 후보가 되므로, 해당 지역의 목록을 끝까지 훑어서 모은다.
    // arrange 값으로는 대표 이미지를 거를 수 없어(totalCount가 동일) 여기서 직접 제외한다.
    public List<TourApiPlace> searchAllPlacesWithImage(Region region, PlaceCategory category) {
        List<TourApiPlace> collected = new ArrayList<>();

        for (int pageNo = 1; pageNo <= LIST_MAX_PAGES; pageNo++) {
            String rawResponseBody = requestAreaBasedList(
                    region, TourApiCategoryCode.resolve(category), LIST_PAGE_SIZE, pageNo);
            List<TourApiPlace> page = parseItems(rawResponseBody, this::toPlace);

            page.stream()
                    .filter(place -> place.thumbnailUrl() != null && !place.thumbnailUrl().isBlank())
                    .forEach(collected::add);

            if (page.size() < LIST_PAGE_SIZE) {
                break;
            }
        }

        return collected;
    }

    public Set<PlaceCategory> syncablePlaceCategories() {
        return TourApiCategoryCode.syncableCategories();
    }

    // PhotoGalleryService1 gallerySearchList1 - 포토코리아 큐레이션 사진을 장소 이름으로 검색한다.
    // 관광지/레포츠는 매칭이 잘 되지만 특정 숙소 상호명·소규모 행사명은 거의 안 걸린다.
    // 실패하거나 못 찾으면(에러가 아니라 정상적인 0건도 포함) null을 반환해 동기화를 막지 않는다.
    public String searchGalleryImage(String keyword) {
        try {
            String rawResponseBody = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("apis.data.go.kr")
                            .path("/B551011/PhotoGalleryService1/gallerySearchList1")
                            .queryParam("serviceKey", properties.serviceKey())
                            .queryParam("MobileOS", "ETC")
                            .queryParam("MobileApp", "moodchon")
                            .queryParam("_type", "json")
                            .queryParam("keyword", keyword)
                            .queryParam("numOfRows", 1)
                            .queryParam("pageNo", 1)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);

            JsonNode root = jsonMapper.readTree(rawResponseBody);
            JsonNode itemNode = root.path("response").path("body").path("items").path("item");
            if (!itemNode.isObject() && !itemNode.isArray()) {
                return null;
            }

            String imageUrl = firstItem(itemNode).path("galWebImageUrl").asString("");
            return imageUrl.isBlank() ? null : imageUrl;
        } catch (RestClientException | JacksonException e) {
            return null;
        }
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

    // detailIntro2 - 장소 유형별(관광지/문화시설/레포츠, 행사/공연/축제, 음식점, 쇼핑) 이용안내·편의시설 원본 필드.
    public TourApiIntroFields fetchIntro(String contentId, PlaceCategory category) {
        try {
            String rawResponseBody = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("apis.data.go.kr")
                            .path("/B551011/KorService2/detailIntro2")
                            .queryParam("serviceKey", properties.serviceKey())
                            .queryParam("MobileOS", "ETC")
                            .queryParam("MobileApp", "moodchon")
                            .queryParam("_type", "json")
                            .queryParam("contentId", contentId)
                            .queryParam("contentTypeId", detailContentTypeId(category))
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);

            JsonNode root = jsonMapper.readTree(rawResponseBody);
            JsonNode item = firstItem(root.path("response").path("body").path("items").path("item"));
            return toIntroFields(item);
        } catch (RestClientException | JacksonException e) {
            throw new CustomException(ErrorCode.TOUR_API_REQUEST_FAILED);
        }
    }

    // TourAPI는 EVENT/PERFORMANCE/FESTIVAL을 전부 15(축제공연행사)로 취급한다.
    private static String detailContentTypeId(PlaceCategory category) {
        return switch (category) {
            case TOURIST_SPOT -> "12";
            case CULTURAL_FACILITY -> "14";
            case EVENT, PERFORMANCE, FESTIVAL -> "15";
            case LEISURE_SPORTS -> "28";
            case SHOPPING -> "38";
            case RESTAURANT -> "39";
            case ACCOMMODATION -> "32";
        };
    }

    private TourApiIntroFields toIntroFields(JsonNode item) {
        return new TourApiIntroFields(
                item.path("restdate").asString(""),
                item.path("usetime").asString(""),
                item.path("useseason").asString(""),
                item.path("parking").asString(""),
                item.path("parkingfee").asString(""),
                item.path("infocenter").asString(""),
                item.path("accomcount").asString(""),
                item.path("chkbabycarriage").asString(""),
                item.path("chkpet").asString(""),
                item.path("chkcreditcard").asString(""),
                item.path("expguide").asString(""),
                item.path("expagerange").asString(""),
                item.path("eventstartdate").asString(""),
                item.path("eventenddate").asString(""),
                item.path("playtime").asString(""),
                item.path("usetimefestival").asString(""),
                item.path("agelimit").asString(""),
                item.path("eventplace").asString(""),
                item.path("program").asString(""),
                item.path("sponsor1").asString(""),
                item.path("sponsor1tel").asString(""),
                item.path("eventhomepage").asString(""),
                item.path("firstmenu").asString(""),
                item.path("treatmenu").asString(""),
                item.path("opentimefood").asString(""),
                item.path("restdatefood").asString(""),
                item.path("packing").asString(""),
                item.path("infocenterfood").asString(""),
                item.path("saleitem").asString(""),
                item.path("opentime").asString(""),
                item.path("restdateshopping").asString(""),
                item.path("parkingshopping").asString(""),
                item.path("shopguide").asString(""),
                item.path("infocentershopping").asString("")
        );
    }

    // detailIntro2 - 숙소(contentTypeId=32) 전용 이용안내·편의시설 원본 필드.
    public TourApiLodgingIntroFields fetchLodgingIntro(String contentId) {
        try {
            String rawResponseBody = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("apis.data.go.kr")
                            .path("/B551011/KorService2/detailIntro2")
                            .queryParam("serviceKey", properties.serviceKey())
                            .queryParam("MobileOS", "ETC")
                            .queryParam("MobileApp", "moodchon")
                            .queryParam("_type", "json")
                            .queryParam("contentId", contentId)
                            .queryParam("contentTypeId", detailContentTypeId(PlaceCategory.ACCOMMODATION))
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);

            JsonNode root = jsonMapper.readTree(rawResponseBody);
            JsonNode item = firstItem(root.path("response").path("body").path("items").path("item"));
            return toLodgingIntroFields(item, fetchPetAccompanyType(contentId));
        } catch (RestClientException | JacksonException e) {
            throw new CustomException(ErrorCode.TOUR_API_REQUEST_FAILED);
        }
    }

    private TourApiLodgingIntroFields toLodgingIntroFields(JsonNode item, String petAccompanyType) {
        return new TourApiLodgingIntroFields(
                item.path("checkintime").asString(""),
                item.path("checkouttime").asString(""),
                item.path("chkcooking").asString(""),
                item.path("barbecue").asString(""),
                item.path("accomcountlodging").asString(""),
                petAccompanyType,
                item.path("bicycle").asString(""),
                item.path("campfire").asString(""),
                item.path("parkinglodging").asString(""),
                item.path("sauna").asString(""),
                item.path("sports").asString(""),
                item.path("reservationurl").asString(""),
                item.path("infocenterlodging").asString(""),
                item.path("roomcount").asString(""),
                item.path("roomtype").asString("")
        );
    }

    // detailPetTour2 - 반려동물 동반여행 정보. 등록된 숙소가 적어 대부분 결과가 없고, 그건 정상이라
    // 예외로 취급하지 않는다 (실패 시에도 null만 반환해 숙소 추천 생성 자체를 막지 않는다).
    private String fetchPetAccompanyType(String contentId) {
        try {
            String rawResponseBody = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("apis.data.go.kr")
                            .path("/B551011/KorService2/detailPetTour2")
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
            JsonNode itemNode = root.path("response").path("body").path("items").path("item");
            if (!itemNode.isObject() && !itemNode.isArray()) {
                return null;
            }

            String type = firstItem(itemNode).path("acmpyTypeCd").asString("");
            return type.isBlank() ? null : type;
        } catch (RestClientException | JacksonException e) {
            return null;
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

    // areaBasedList2 - region이 null이면 areaCode를 아예 안 붙인다. TourAPI에서 실제로 확인한 결과
    // areaCode는 선택 파라미터라, 생략하면 지역 무관 전국 검색이 된다(희망 지역 미선택 시 사용).
    private String requestAreaBasedList(Region region, String contentTypeId, int numOfRows) {
        return requestAreaBasedList(region, contentTypeId, numOfRows, 1);
    }

    private String requestAreaBasedList(Region region, String contentTypeId, int numOfRows, int pageNo) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder
                                .scheme("https")
                                .host("apis.data.go.kr")
                                .path("/B551011/KorService2/areaBasedList2")
                                .queryParam("serviceKey", properties.serviceKey())
                                .queryParam("MobileOS", "ETC")
                                .queryParam("MobileApp", "moodchon")
                                .queryParam("_type", "json")
                                .queryParam("arrange", "A")
                                .queryParam("contentTypeId", contentTypeId)
                                .queryParam("numOfRows", numOfRows)
                                .queryParam("pageNo", pageNo);
                        if (region != null) {
                            uriBuilder.queryParam("areaCode", TourApiRegionCode.resolve(region));
                        }
                        return uriBuilder.build();
                    })
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.TOUR_API_REQUEST_FAILED);
        }
    }

    public List<TourApiPlace> searchByKeyword(String keyword, int numOfRows) {
        try {
            String rawResponseBody = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("apis.data.go.kr")
                            .path("/B551011/KorService2/searchKeyword2")
                            .queryParam("serviceKey", properties.serviceKey())
                            .queryParam("MobileOS", "ETC")
                            .queryParam("MobileApp", "moodchon")
                            .queryParam("_type", "json")
                            .queryParam("arrange", "A")
                            .queryParam("contentTypeId", TourApiCategoryCode.resolve(PlaceCategory.ACCOMMODATION))
                            .queryParam("keyword", keyword)
                            .queryParam("numOfRows", numOfRows)
                            .queryParam("pageNo", 1)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);

            return parseItems(rawResponseBody, this::toPlace);
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.TOUR_API_REQUEST_FAILED);
        }
    }

    // searchKeyword2 - contentTypeId 없이 전 카테고리 대상 검색("일정에 장소 추가하기" 검색).
    public List<TourApiPlaceSearchResult> searchAnyCategory(String keyword, int numOfRows) {
        try {
            String rawResponseBody = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("apis.data.go.kr")
                            .path("/B551011/KorService2/searchKeyword2")
                            .queryParam("serviceKey", properties.serviceKey())
                            .queryParam("MobileOS", "ETC")
                            .queryParam("MobileApp", "moodchon")
                            .queryParam("_type", "json")
                            .queryParam("arrange", "A")
                            .queryParam("keyword", keyword)
                            .queryParam("numOfRows", numOfRows)
                            .queryParam("pageNo", 1)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);

            return parseItems(rawResponseBody, this::toPlaceSearchResult).stream()
                    .filter(result -> result.category() != null)
                    .toList();
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.TOUR_API_REQUEST_FAILED);
        }
    }

    private TourApiPlaceSearchResult toPlaceSearchResult(JsonNode item) {
        return new TourApiPlaceSearchResult(toPlace(item), resolveCategory(item.path("contenttypeid").asString("")));
    }

    // EVENT/PERFORMANCE/FESTIVAL은 TourAPI에서 전부 15(축제공연행사)로 통합되어 있어 FESTIVAL로만 구분한다.
    private static PlaceCategory resolveCategory(String contentTypeId) {
        return switch (contentTypeId) {
            case "12" -> PlaceCategory.TOURIST_SPOT;
            case "14" -> PlaceCategory.CULTURAL_FACILITY;
            case "15" -> PlaceCategory.FESTIVAL;
            case "28" -> PlaceCategory.LEISURE_SPORTS;
            case "32" -> PlaceCategory.ACCOMMODATION;
            case "38" -> PlaceCategory.SHOPPING;
            case "39" -> PlaceCategory.RESTAURANT;
            default -> null;
        };
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

    // detailInfo2 - 숙소(contentTypeId=32) 객실 목록. 등록이 안 된 숙소는 빈 목록을 반환한다.
    public List<TourApiRoom> fetchRooms(String contentId) {
        try {
            String rawResponseBody = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("apis.data.go.kr")
                            .path("/B551011/KorService2/detailInfo2")
                            .queryParam("serviceKey", properties.serviceKey())
                            .queryParam("MobileOS", "ETC")
                            .queryParam("MobileApp", "moodchon")
                            .queryParam("_type", "json")
                            .queryParam("contentId", contentId)
                            .queryParam("contentTypeId", detailContentTypeId(PlaceCategory.ACCOMMODATION))
                            .queryParam("numOfRows", ROOM_MAX_COUNT)
                            .queryParam("pageNo", 1)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);

            return parseItems(rawResponseBody, this::toRoom);
        } catch (RestClientException | JacksonException e) {
            throw new CustomException(ErrorCode.TOUR_API_REQUEST_FAILED);
        }
    }

    private TourApiRoom toRoom(JsonNode item) {
        return new TourApiRoom(
                blankToNull(item.path("roomtitle").asString("")),
                parseIntOrNull(item.path("roomcount").asString("")),
                parseIntOrNull(item.path("roombasecount").asString("")),
                parseIntOrNull(item.path("roommaxcount").asString("")),
                parseIntOrNull(item.path("roomoffseasonminfee1").asString("")),
                parseIntOrNull(item.path("roomoffseasonminfee2").asString("")),
                parseIntOrNull(item.path("roompeakseasonminfee1").asString("")),
                parseIntOrNull(item.path("roompeakseasonminfee2").asString("")),
                blankToNull(item.path("roomimg1").asString("")),
                roomFacilities(item)
        );
    }

    // 객실 시설은 'Y'/'N' 플래그로 내려온다. 화면에 칩으로 나열할 이름만 뽑는다.
    private List<String> roomFacilities(JsonNode item) {
        List<String> facilities = new ArrayList<>();
        ROOM_FACILITY_LABELS.forEach((field, label) -> {
            if ("Y".equalsIgnoreCase(item.path(field).asString(""))) {
                facilities.add(label);
            }
        });
        return facilities;
    }

    private Integer parseIntOrNull(String rawValue) {
        String value = blankToNull(rawValue);
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String blankToNull(String rawValue) {
        return (rawValue == null || rawValue.isBlank()) ? null : rawValue.trim();
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
