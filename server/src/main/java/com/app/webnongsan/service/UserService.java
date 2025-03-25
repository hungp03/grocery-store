package com.app.webnongsan.service;

import com.app.webnongsan.domain.User;
import com.app.webnongsan.domain.request.UpdatePasswordDTO;
import com.app.webnongsan.domain.request.UserStatusDTO;
import com.app.webnongsan.domain.response.PaginationDTO;
import com.app.webnongsan.domain.response.user.CreateUserDTO;
import com.app.webnongsan.domain.response.user.ResLoginDTO;
import com.app.webnongsan.domain.response.user.UserDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

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

    void updateUserToken(String token, String email);

    User getUserByRFTokenAndEmail(String email, String token);

    void updatePassword(String email, String newPassword);

    void checkAccountBanned(User user);

    ResLoginDTO.UserGetAccount updateUser(String name, String phone, String address, MultipartFile avatar);

    void changePassword(UpdatePasswordDTO dto);
}

