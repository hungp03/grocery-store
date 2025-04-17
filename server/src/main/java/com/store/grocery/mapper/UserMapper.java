package com.store.grocery.mapper;

import com.store.grocery.domain.User;
import com.store.grocery.dto.response.user.CreateUserResponse;
import com.store.grocery.dto.response.user.UpdateUserResponse;
import com.store.grocery.dto.response.user.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UpdateUserResponse toUpdateUserResponse(User user);
    CreateUserResponse toCreateUserResponse(User user);
    UserResponse toUserResponse(User user);
}
