package com.yumg.starter.modules.navigation.api;

import com.yumg.starter.modules.navigation.api.dto.NavigationMenuRequest;
import com.yumg.starter.modules.navigation.api.dto.NavigationMenuResponse;
import com.yumg.starter.modules.navigation.api.dto.NavigationRouteResponse;
import com.yumg.starter.modules.navigation.application.NavigationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/navigation")
@Tag(name = "Navigation")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class NavigationController {

    private final NavigationService navigation;

    public NavigationController(NavigationService navigation) {
        this.navigation = navigation;
    }

    @GetMapping("/routes")
    public List<NavigationRouteResponse> routes(@AuthenticationPrincipal Jwt jwt) {
        List<String> permissions = jwt.getClaimAsStringList("permissions");
        return navigation.routes(permissions == null ? Set.of() : Set.copyOf(permissions));
    }

    @GetMapping("/menus")
    @PreAuthorize("hasAuthority('system:menu:read')")
    public List<NavigationMenuResponse> menus() {
        return navigation.list();
    }

    @PostMapping("/menus")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('system:menu:write')")
    public NavigationMenuResponse create(@Valid @RequestBody NavigationMenuRequest request) {
        return navigation.create(request);
    }

    @PutMapping("/menus/{id}")
    @PreAuthorize("hasAuthority('system:menu:write')")
    public NavigationMenuResponse update(@PathVariable @Pattern(regexp = "[0-9a-fA-F-]{36}") String id,
            @Valid @RequestBody NavigationMenuRequest request) {
        return navigation.update(id, request);
    }

    @DeleteMapping("/menus/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('system:menu:write')")
    public void delete(@PathVariable @Pattern(regexp = "[0-9a-fA-F-]{36}") String id) {
        navigation.delete(id);
    }
}
