package com.store.grocery.mapper;

import com.store.grocery.domain.User;
import com.store.grocery.dto.response.user.CreateUserResponse;
import com.store.grocery.dto.response.user.UpdateUserResponse;
import com.store.grocery.dto.response.user.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UpdateUserResponse toUpdateUserResponse(User user) {
        if (user == null) return null;
        UpdateUserResponse dto = new UpdateUserResponse();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setStatus(user.isStatus());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setAvatarUrl(user.getAvatarUrl());
        return dto;
    }

    public CreateUserResponse toCreateUserResponse(User user) {
        if (user == null) return null;

        CreateUserResponse dto = new CreateUserResponse();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setStatus(user.isStatus());
        return dto;
    }

    public UserResponse toUserResponse(User user) {
        if (user == null) return null;

        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setStatus(user.isStatus());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setAvatarUrl(user.getAvatarUrl());
        return dto;
    }
}
