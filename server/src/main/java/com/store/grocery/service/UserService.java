package com.store.grocery.service;

import com.store.grocery.domain.User;
import com.store.grocery.dto.request.user.UpdatePasswordRequest;
import com.store.grocery.dto.request.user.UpdateUserRequest;
import com.store.grocery.dto.request.user.UserRegisterRequest;
import com.store.grocery.dto.request.user.UpdateUserStatusRequest;
import com.store.grocery.dto.response.PaginationResponse;
import com.store.grocery.dto.response.user.CreateUserResponse;
import com.store.grocery.dto.response.user.DeviceResponse;
import com.store.grocery.dto.response.user.UpdateUserResponse;
import com.store.grocery.dto.response.user.UserResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface UserService {
    CreateUserResponse create(UserRegisterRequest user);

    boolean isExistedEmail(String email);

    void delete(long id);
    User findById(long id);
    UserResponse getUserById(long id);

    PaginationResponse fetchAllUser(Specification<User> specification, Pageable pageable);

    void updateStatus(Long id, UpdateUserStatusRequest req);

    User getUserByUsername(String username);

    void updatePassword(String email, String newPassword);

    void checkAccountActive(User user);

    UpdateUserResponse updateUser(UpdateUserRequest request);
    void changePassword(UpdatePasswordRequest dto);
    List<DeviceResponse> getLoggedInDevices(String deviceHash);
    void requestDeactiveAccount();
    void verifyOTPAndDisableAccount(String inputOtp);
}

