package com.yumg.starter.modules.navigation.infrastructure;

import com.yumg.starter.entities.NavigationMenu;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NavigationMenuRepository extends JpaRepository<NavigationMenu, String> {
    List<NavigationMenu> findAllByOrderBySortOrderAscCodeAsc();
    Optional<NavigationMenu> findByCode(String code);
    boolean existsByParentId(String parentId);
}
