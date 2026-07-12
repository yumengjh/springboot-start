package com.yumg.starter.modules.users.application;

import com.yumg.starter.common.api.ApiException;
import com.yumg.starter.entities.User;
import com.yumg.starter.modules.auth.application.TokenService;
import com.yumg.starter.modules.auth.infrastructure.UserRepository;
import com.yumg.starter.modules.users.api.dto.ChangePasswordRequest;
import com.yumg.starter.modules.users.api.dto.CurrentUserResponse;
import com.yumg.starter.modules.users.api.dto.UpdateProfileRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;
    public UserProfileService(UserRepository users, PasswordEncoder passwordEncoder, TokenService tokens) {
        this.users = users; this.passwordEncoder = passwordEncoder; this.tokens = tokens;
    }
    @Transactional(readOnly = true)
    public CurrentUserResponse current(String userId) {
        return CurrentUserResponse.from(find(userId));
    }
    @Transactional
    public CurrentUserResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = find(userId); user.changeDisplayName(request.displayName().trim());
        return CurrentUserResponse.from(user);
    }
    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = find(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw ApiException.unauthorized();
        }
        user.changePasswordHash(passwordEncoder.encode(request.newPassword()));
        tokens.revokeAllForUser(userId);
    }
    private User find(String userId) { return users.findById(userId).orElseThrow(ApiException::notFound); }
}
