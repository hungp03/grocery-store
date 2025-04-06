package com.store.grocery.service;

import com.store.grocery.domain.User;
import com.store.grocery.domain.request.user.UpdatePasswordDTO;
import com.store.grocery.domain.request.user.UpdateUserRequest;
import com.store.grocery.domain.request.user.UserStatusDTO;
import com.store.grocery.domain.response.PaginationDTO;
import com.store.grocery.domain.response.user.CreateUserDTO;
import com.store.grocery.domain.response.user.DeviceDTO;
import com.store.grocery.domain.response.user.ResLoginDTO;
import com.store.grocery.domain.response.user.UserDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    User create(User user);

    boolean isExistedEmail(String email);

    void delete(long id);

    CreateUserDTO convertToCreateDTO(User user);

    UserDTO convertToUserDTO(User user);

    User getUserById(long id);

    PaginationDTO fetchAllUser(Specification<User> specification, Pageable pageable);

    void updateStatus(UserStatusDTO reqUser);

    //    UpdateUserDTO convertToUpdateUserDTO(User user);
    User getUserByUsername(String username);

    void updatePassword(String email, String newPassword);

    void checkAccountBanned(User user);

    void updateUser(UpdateUserRequest request, MultipartFile avatar);

    void changePassword(UpdatePasswordDTO dto);

    void storeUserToken(User user, String refreshToken, String deviceInfo, String deviceHash);

    List<DeviceDTO> getLoggedInDevices(String deviceHash);

    void requestDeactiveAccount();
    void verifyOTPAndDisableAccount(String inputOtp);
}

