package com.store.grocery.service.impl;

import com.store.grocery.domain.User;
import com.store.grocery.domain.UserToken;
import com.store.grocery.domain.request.user.UpdatePasswordDTO;
import com.store.grocery.domain.request.user.UpdateUserRequest;
import com.store.grocery.domain.request.user.UserStatusDTO;
import com.store.grocery.domain.response.PaginationDTO;
import com.store.grocery.domain.response.user.CreateUserDTO;
import com.store.grocery.domain.response.user.DeviceDTO;
import com.store.grocery.domain.response.user.ResLoginDTO;
import com.store.grocery.domain.response.user.UserDTO;
import com.store.grocery.repository.UserRepository;
import com.store.grocery.repository.UserTokenRepository;
import com.store.grocery.service.CartService;
import com.store.grocery.service.FileService;
import com.store.grocery.service.UserService;
import com.store.grocery.util.SecurityUtil;
import com.store.grocery.util.exception.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;
    private final CartService cartService;
    private final UserTokenRepository userTokenRepository;

    @Override
    public User create(User user) {
        log.info("Creating new user with email: {}", user.getEmail());
        if (this.isExistedEmail(user.getEmail())) {
            log.error("Email already exists: {}", user.getEmail());
            throw new DuplicateResourceException("Email " + user.getEmail() + " đã tồn tại");
        }
        //hash password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(true);
        User savedUser = this.userRepository.save(user);
        log.info("Successfully created user with ID: {}", savedUser.getId());
        return savedUser;
    }

    @Override
    public boolean isExistedEmail(String email) {
        log.debug("Checking if email exists: {}", email);
        return this.userRepository.existsByEmail(email);
    }

    private boolean isExistedId(long id) {
        log.debug("Checking if user ID exists: {}", id);
        return this.userRepository.existsById(id);
    }

    @Override
    public void delete(long id) {
        log.info("Attempting to delete user with ID: {}", id);
        boolean check = this.isExistedId(id);
        if (!check) {
            log.error("User not found with ID: {}", id);
            throw new ResourceInvalidException("Người dùng với id " + id + " không tồn tại");
        }
        this.userRepository.deleteById(id);
        log.info("Successfully deleted user with ID: {}", id);
    }

    @Override
    public CreateUserDTO convertToCreateDTO(User user) {
        log.debug("Converting User to CreateUserDTO for user ID: {}", user.getId());
        CreateUserDTO res = new CreateUserDTO();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setStatus(user.isStatus());
        return res;
    }

    @Override
    public UserDTO convertToUserDTO(User user) {
        log.debug("Converting User to UserDTO for user ID: {}", user.getId());
        UserDTO res = new UserDTO();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setAddress(user.getAddress());
        res.setStatus(user.isStatus());
        res.setPhone(user.getPhone());
        res.setAvatarUrl(user.getAvatarUrl());
        return res;
    }

    @Override
    public User getUserById(long id) {
        log.debug("Fetching user by ID: {}", id);
        return this.userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User không tồn tại"));
    }

    @Override
    public PaginationDTO fetchAllUser(Specification<User> specification, Pageable pageable) {
        log.info("Fetching all users with pagination");
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
        log.debug("Found {} users", userPage.getTotalElements());
        return p;
    }

    @Override
    public void updateStatus(UserStatusDTO reqUser) {
        log.info("Updating status for user ID: {}", reqUser.getId());
        User currentUser = this.getUserById(reqUser.getId());
        if (currentUser != null) {
            currentUser.setStatus(reqUser.isStatus());
            this.userRepository.save(currentUser);
            log.info("Successfully updated status for user ID: {}", reqUser.getId());
        }
    }

    //    @Override
//    public UpdateUserDTO convertToUpdateUserDTO(User user) {
//        log.debug("Converting User to UpdateUserDTO for user ID: {}", user.getId());
//        UpdateUserDTO u = new UpdateUserDTO();
//        u.setId(user.getId());
//        u.setEmail(user.getEmail());
//        u.setName(user.getName());
//        u.setPhone(user.getPhone());
//        u.setAddress(user.getAddress());
//        u.setStatus(user.getStatus());
//        u.setAvatarUrl(user.getAvatarUrl());
//        return u;
//    }
    @Override
    public User getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        User u = this.userRepository.findByEmail(username);
        if (u == null) {
            log.error("User not found with username: {}", username);
            throw new UserNotFoundException("User không tồn tại");
        }
        return u;
    }

    @Override
    public void updatePassword(String email, String newPassword) {
        log.info("Resetting password for user with email: {}", email);
        User user = getUserByUsername(email);
        user.setPassword(passwordEncoder.encode(newPassword));
        this.userRepository.save(user);
        log.info("Successfully reset password for user with email: {}", email);
    }

    @Override
    public void checkAccountBanned(User user) {
        log.debug("Checking if account is banned for user ID: {}", user.getId());
        if (!user.isStatus()) {
            log.error("Account is banned for user ID: {}", user.getId());
            throw new AuthException("Tài khoản của bạn đã bị vô hiệu hóa.");
        }
    }

    @Override
    public ResLoginDTO.UserGetAccount updateUser(
            UpdateUserRequest request, MultipartFile avatar) {
        log.info("Updating user profile");
        long uid = SecurityUtil.getUserId();
        User currentUserDB = this.getUserById(uid);

        // Cập nhật thông tin người dùng
        currentUserDB.setName(request.getName());
        currentUserDB.setPhone(request.getPhone());
        currentUserDB.setAddress(request.getAddress());

        // Nếu có avatar mới, lưu ảnh vào server
        if (avatar != null && !avatar.isEmpty()) {
            try {
                log.debug("Updating avatar for user ID: {}", currentUserDB.getId());
                String avatarUrl = fileService.store(avatar, "avatar");
                currentUserDB.setAvatarUrl(avatarUrl);
            } catch (IOException e) {
                log.error("Failed to store avatar file for user ID: {}", currentUserDB.getId(), e);
                throw new StorageException("Failed to store avatar file");
            }
        }
        userRepository.save(currentUserDB);
        log.info("Successfully updated user profile for user ID: {}", currentUserDB.getId());

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

    @Override
    @Transactional
    public void changePassword(UpdatePasswordDTO dto) {
        log.info("Attempting to change password for user");
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            log.error("Password confirmation does not match");
            throw new AuthException("Mật khẩu xác nhận không trùng khớp");
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = this.getUserByUsername(username);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            log.error("Current password is incorrect for user ID: {}", user.getId());
            throw new AuthException("Mật khẩu cũ không đúng");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        log.info("Successfully changed password for user ID: {}", user.getId());
    }

    @Override
    public void storeUserToken(User user, String refreshToken, String deviceInfo, String deviceHash) {
        Optional<UserToken> existingToken = userTokenRepository.findByUserAndDeviceInfo(user, deviceInfo);

        if (existingToken.isPresent()) {
            // Cập nhật refreshToken mới
            UserToken userToken = existingToken.get();
            userToken.setRefreshToken(refreshToken);
            userToken.setDeviceInfo(deviceInfo);
            userTokenRepository.save(userToken);
        } else {
            // Tạo mới UserToken
            UserToken newUserToken = new UserToken();
            newUserToken.setUser(user);
            newUserToken.setRefreshToken(refreshToken);
            newUserToken.setDeviceInfo(deviceInfo);
            newUserToken.setDeviceHash(deviceHash);
            newUserToken.setCreatedAt(Instant.now());
            userTokenRepository.save(newUserToken);
        }
    }

    @Override
    public List<DeviceDTO> getLoggedInDevices(String deviceHash) {
        long userId = SecurityUtil.getUserId();
        List<UserToken> userTokens = userTokenRepository.findByUserId(userId);
        return userTokens.stream()
                .map(token -> new DeviceDTO(token.getDeviceInfo(), token.getCreatedAt(), token.getDeviceHash(), token.getDeviceHash().equals(deviceHash)))
                .toList();
    }

    @Override
    public void deactiveAccount() {

    }

}

