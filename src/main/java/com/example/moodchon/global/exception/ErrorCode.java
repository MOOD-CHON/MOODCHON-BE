package com.example.moodchon.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "허용되지 않은 HTTP 메서드입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "서버 내부 오류가 발생했습니다."),
    ITINERARY_ALREADY_COMMITTED(HttpStatus.CONFLICT, "C005", "이미 담긴 추천 일정은 다시 생성할 수 없습니다."),
    AI_GENERATION_FAILED(HttpStatus.BAD_GATEWAY, "C006", "추천 일정 생성에 실패했습니다. 잠시 후 다시 시도해주세요."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "C007", "여행 날짜는 오늘 이후, 최대 6박 7일까지만 선택할 수 있습니다."),
    INVALID_MOOD_SELECTION(HttpStatus.BAD_REQUEST, "C008", "무드 이미지를 정확히 3개 선택해야 합니다."),
    INVALID_INVITE_CODE(HttpStatus.NOT_FOUND, "C009", "유효하지 않은 초대 코드예요. 다시 확인해주세요."),
    CHONKANG_FULL(HttpStatus.CONFLICT, "C010", "해당 촌캉스는 참여 인원이 모두 찼어요. 촌캉스는 최대 6명까지 참여할 수 있어요."),
    ALREADY_JOINED(HttpStatus.CONFLICT, "C011", "이미 참여한 촌캉스입니다."),
    TOUR_API_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "C012", "숙소 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요."),
    MOOD_NOT_DECIDED(HttpStatus.CONFLICT, "C013", "무드가 아직 결정되지 않아 숙소를 추천할 수 없습니다."),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "A002", "접근 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}