package com.example.moodchon.domain.recommendation.service;

import com.example.moodchon.domain.place.GeoUtils;
import com.example.moodchon.domain.recommendation.entity.TransportMode;

// 확정 숙소 기준 이동수단/소요시간 추정. RecommendedItineraryGenerationService의 산출 방식과 동일하다.
final class TravelEstimator {

    private static final double WALK_THRESHOLD_KM = 1.0;
    private static final double WALK_SPEED_KMH = 4.0;
    private static final double CAR_SPEED_KMH = 30.0;

    private TravelEstimator() {
    }

    record Estimate(TransportMode transportMode, int travelMinutes) {
    }

    static Estimate from(double fromLat, double fromLng, double toLat, double toLng) {
        double distanceKm = GeoUtils.distanceKm(fromLat, fromLng, toLat, toLng);
        TransportMode transportMode = distanceKm <= WALK_THRESHOLD_KM ? TransportMode.WALK : TransportMode.CAR;
        double speedKmh = transportMode == TransportMode.WALK ? WALK_SPEED_KMH : CAR_SPEED_KMH;
        int travelMinutes = (int) Math.max(1, Math.round(distanceKm / speedKmh * 60));
        return new Estimate(transportMode, travelMinutes);
    }
}
