package com.ssm.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ssm.entity.UserDetail;
import com.ssm.repository.UserDetailRepository;
import com.ssm.service.IUserDetailService;
import com.ssm.util.AppConstant;

@Service
public class UserDetailsServiceImpl implements IUserDetailService {

    @Autowired
    private UserDetailRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetail saveUser(UserDetail user) {
        user.setRole("ROLE_USER");
        user.setIsEnable(true);
        user.setAccountNonLocked(true);

        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);
        return userRepository.save(user);
    }

    @Override
    public UserDetail getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<UserDetail> getUsers(String role) {
        return userRepository.findByRole(role);
    }

    @Override
    public Boolean updateAccountStatus(Integer id, Boolean status) {
        Optional<UserDetail> findByUser = userRepository.findById(id);
        if (findByUser.isPresent()) {
            UserDetail userDetail = findByUser.get();
            userDetail.setIsEnable(status);
            userRepository.save(userDetail);
            return true;
        }
        return false;
    }

    @Override
    public void increaseFailedAttempt(UserDetail user) {
        Integer attempt = user.getFailedAttempt() + 1;
        user.setFailedAttempt(attempt);
        userRepository.save(user);
    }

    @Override
    public void userAccountLock(UserDetail user) {
        user.setAccountNonLocked(false);
        user.setLockTime(new Date());
        userRepository.save(user);
    }

    @Override
    public Boolean unlockAccountTimeExpired(UserDetail user) {
        if (user.getLockTime() == null) {
            return false;
        }

        long lockTime = user.getLockTime().getTime();
        long unlockTime = lockTime + AppConstant.UNLOCK_DURATION_TIME;
        long currentTime = System.currentTimeMillis();

        if (unlockTime < currentTime) {
            user.setAccountNonLocked(true);
            user.setFailedAttempt(0);
            user.setLockTime(null);
            userRepository.save(user);
            return true;
        }

        return false;
    }

    @Override
    public void resetAttempt(Integer userId) {
        // Optional: You can implement logic to reset attempts
    }

    @Override
    public void userUpdateUserResetToken(String email, String resetToken) {
        UserDetail user = userRepository.findByEmail(email);
        user.setResetToken(resetToken);
        userRepository.save(user);
    }

    @Override
    public UserDetail getUserByToken(String token) {
        return userRepository.findByresetToken(token);
    }

    @Override
    public UserDetail updateUser(UserDetail user) {
        return userRepository.save(user);
    }

    @Override
    public UserDetail updateUserProfile(UserDetail user, MultipartFile img) {
        UserDetail dbUser = userRepository.findById(user.getId()).orElse(null);

        if (!ObjectUtils.isEmpty(dbUser)) {
            dbUser.setUserName(user.getUserName());
            dbUser.setMobileNumber(user.getMobileNumber());
            dbUser.setAddress(user.getAddress());
            dbUser.setCity(user.getCity());
            dbUser.setState(user.getState());
            dbUser.setPincode(user.getPincode());

            if (!img.isEmpty()) {
                try {
                    // ✅ Save to external folder, safe in JAR deployments
                    String uploadDir = System.getProperty("user.home") + "/app-images/profile_img";
                    File folder = new File(uploadDir);

                    if (!folder.exists()) {
                        folder.mkdirs(); // create folder if not exists
                    }

                    String fileName = img.getOriginalFilename();
                    Path filePath = Paths.get(uploadDir + File.separator + fileName);

                    Files.copy(img.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    // Save image name to DB
                    dbUser.setProfileImage(fileName);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            dbUser = userRepository.save(dbUser);
        }

        return dbUser;
    }

    @Override
    public UserDetail saveAdmin(UserDetail user) {
        user.setRole("ROLE_ADMIN");
        user.setIsEnable(true);
        user.setAccountNonLocked(true);

        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);
        return userRepository.save(user);
    }

    @Override
    public Boolean existsEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
