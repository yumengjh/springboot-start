package com.yumg.starter.modules.users.application;

import com.yumg.starter.modules.auth.infrastructure.UserRepository;
import java.util.Collection;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read-only user data exported for other modules without exposing the repository. */
@Service
public class UserLookupService {
    private final UserRepository users;

    public UserLookupService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public Map<String, String> displayNamesById(Collection<String> userIds) {
        return users.findAllById(userIds).stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        user -> user.getId(), user -> user.getDisplayName(), (left, right) -> left));
    }
}
