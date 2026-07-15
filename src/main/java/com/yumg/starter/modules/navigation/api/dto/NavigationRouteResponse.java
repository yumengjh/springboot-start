package com.yumg.starter.modules.navigation.api.dto;

import java.util.List;

public record NavigationRouteResponse(
        String code, String path, String componentKey, String title, String icon,
        int rank, boolean keepAlive, String requiredPermission,
        List<NavigationRouteResponse> children) {
}
