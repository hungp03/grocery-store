package com.store.grocery.dto.response.user;

import com.store.grocery.domain.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.store.grocery.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class UserLoginResponse {
    private String accessToken;
    private UserLogin user;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserLogin{
        private long id;
        private String email;
        private String name;
        private Role role;

        public static UserLogin from(User user) {
            return new UserLogin(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getRole()
            );
        }

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserGetAccount{
        private long id;
        private String email;
        private String name;
        private boolean status;
        private String phone;
        private String address;
        private String avatarUrl;
        private Role role;

        public static UserGetAccount from(User user) {
            return new UserGetAccount(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.isStatus(),
                    user.getPhone(),
                    user.getAddress(),
                    user.getAvatarUrl(),
                    user.getRole()
            );
        }

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInsideToken{
        private long id;
        private String email;
        private String name;
    }
}
