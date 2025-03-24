package com.app.webnongsan.service;

import com.app.webnongsan.domain.User;
import com.app.webnongsan.domain.request.UpdatePasswordDTO;
import com.app.webnongsan.domain.request.UserStatusDTO;
import com.app.webnongsan.domain.response.PaginationDTO;
import com.app.webnongsan.domain.response.user.CreateUserDTO;
import com.app.webnongsan.domain.response.user.ResLoginDTO;
import com.app.webnongsan.domain.response.user.UpdateUserDTO;
import com.app.webnongsan.domain.response.user.UserDTO;
import com.app.webnongsan.repository.UserRepository;
import com.app.webnongsan.util.SecurityUtil;
import com.app.webnongsan.util.exception.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;
    private final CartService cartService;

    public User create(User user) {
        if (this.isExistedEmail(user.getEmail())) {
            throw new DuplicateResourceException("Email " + user.getEmail() + " đã tồn tại");
        }
        //hash password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);
        return this.userRepository.save(user);
    }

    public boolean isExistedEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public boolean isExistedId(long id) {
        return this.userRepository.existsById(id);
    }

    public void delete(long id) {
        boolean check = this.isExistedId(id);
        if (!check) {
            throw new ResourceInvalidException("Người dùng với id " + id + " không tồn tại");
        }
        this.userRepository.deleteById(id);
    }

    public CreateUserDTO convertToCreateDTO(User user) {
        CreateUserDTO res = new CreateUserDTO();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setStatus(user.getStatus());
        return res;
    }

    public UserDTO convertToUserDTO(User user) {
        UserDTO res = new UserDTO();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setAddress(user.getAddress());
        res.setStatus(user.getStatus());
        res.setPhone(user.getPhone());
        res.setAvatarUrl(user.getAvatarUrl());
        return res;
    }

    public User getUserById(long id) {
        return this.userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User không tồn tại"));
    }

    public PaginationDTO fetchAllUser(Specification<User> specification, Pageable pageable) {
        Page<User> userPage = this.userRepository.findAll(pageable);

        PaginationDTO p = new PaginationDTO();
        PaginationDTO.Meta meta = new PaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(userPage.getTotalPages());
        meta.setTotal(userPage.getTotalElements());

        p.setMeta(meta);

        // remove sensitive data like password
        List<UserDTO> listUser = userPage.getContent()
                .stream().map(this::convertToUserDTO)
                .collect(Collectors.toList());

        p.setResult(listUser);
        return p;
    }

    public void updateStatus(UserStatusDTO reqUser) {
        User currentUser = this.getUserById(reqUser.getId());
        if (currentUser != null) {
            currentUser.setStatus(reqUser.getStatus());
            this.userRepository.save(currentUser);
        }
    }

    public UpdateUserDTO convertToUpdateUserDTO(User user) {
        UpdateUserDTO u = new UpdateUserDTO();
        u.setId(user.getId());
        u.setEmail(user.getEmail());
        u.setName(user.getName());
        u.setPhone(user.getPhone());
        u.setAddress(user.getAddress());
        u.setStatus(user.getStatus());
        u.setAvatarUrl(user.getAvatarUrl());
        return u;
    }

    public User getUserByUsername(String username) {
        User u = this.userRepository.findByEmail(username);
        if (u == null) {
            throw new UserNotFoundException("User không tồn tại");
        }
        return u;
    }

    public void updateUserToken(String token, String email) {
        User currentUser = this.getUserByUsername(email);
        currentUser.setRefreshToken(token);
        this.userRepository.save(currentUser);

    }

    public User getUserByRFTokenAndEmail(String email, String token) {
        return this.userRepository.findByEmailAndRefreshToken(email, token);
    }

    public void resetPassword(String email, String newPassword) {
        User user = getUserByUsername(email);
        user.setPassword(passwordEncoder.encode(newPassword));
        this.userRepository.save(user);
    }

    public void checkAccountBanned(User user) {
        if (user != null && user.getStatus() == 0) {
            throw new AuthException("Tài khoản của bạn đã bị khóa.");
        }
    }

    public ResLoginDTO.UserGetAccount updateUser(
            String name, String phone, String address, MultipartFile avatar) {

        long uid = SecurityUtil.getUserId();
        User currentUserDB = this.getUserById(uid);

        // Cập nhật thông tin người dùng
        currentUserDB.setName(name);
//        currentUserDB.setEmail(email);
        currentUserDB.setPhone(phone);
        currentUserDB.setAddress(address);

        // Nếu có avatar mới, lưu ảnh vào server
        if (avatar != null && !avatar.isEmpty()) {
            try {
                String avatarUrl = fileService.store(avatar, "avatar");
                currentUserDB.setAvatarUrl(avatarUrl);
            } catch (IOException e) {
                throw new StorageException("Failed to store avatar file");
            }
        }
        userRepository.save(currentUserDB);
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                currentUserDB.getId(),
                currentUserDB.getEmail(),
                currentUserDB.getName(),
                currentUserDB.getRole());

        ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();
        userGetAccount.setUser(userLogin);
        userGetAccount.setCartLength(cartService.countProductInCart(currentUserDB.getId()));
        return userGetAccount;
    }

    @Transactional
    public void changePassword(UpdatePasswordDTO dto) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())){
            throw new AuthException("Mật khẩu xác nhận không trùng khớp");
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = this.getUserByUsername(username);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new AuthException("Mật khẩu cũ không đúng");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }
}

