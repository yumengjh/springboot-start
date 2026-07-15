package com.yumg.starter.modules.navigation.api.dto;

import com.yumg.starter.entities.NavigationMenu;
import com.yumg.starter.entities.NavigationMenuType;
import java.util.List;

public record NavigationMenuResponse(
        String id, String parentId, String code, String title, String routePath,
        String componentKey, String icon, int sortOrder, NavigationMenuType menuType,
        String requiredPermission, boolean visible, boolean enabled, boolean keepAlive,
        boolean systemManaged, List<NavigationMenuResponse> children) {

    public static NavigationMenuResponse from(NavigationMenu menu,
            List<NavigationMenuResponse> children) {
        return new NavigationMenuResponse(menu.getId(), menu.getParentId(), menu.getCode(),
                menu.getTitle(), menu.getRoutePath(), menu.getComponentKey(), menu.getIcon(),
                menu.getSortOrder(), menu.getMenuType(), menu.getRequiredPermission(),
                menu.isVisible(), menu.isEnabled(), menu.isKeepAlive(), menu.isSystemManaged(),
                children);
    }
}
