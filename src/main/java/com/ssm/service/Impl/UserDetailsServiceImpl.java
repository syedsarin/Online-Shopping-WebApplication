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
import org.springframework.core.io.ClassPathResource;
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
	UserDetailRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Override
	public UserDetail saveUser(UserDetail user) {
		
		user.setRole("ROLE_USER");
		user.setIsEnable(true);
		user.setAccountNonLocked(true);
		user.getFailedAttempt();
		
		String encodePassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodePassword);
		UserDetail saveUser = userRepository.save(user);
		
		return saveUser;
	}

	@Override
	public UserDetail getUserByEmail(String email) {
		
		return userRepository.findByEmail(email);
	}

	@Override
	public List<UserDetail> getUsers(String role) {

	List<UserDetail> findByRole = userRepository.findByRole(role);	
	
		return findByRole;
	}

	@Override
	public Boolean updateAccountStatus(Integer id, Boolean status) {
		
		Optional<UserDetail> findByUser = userRepository.findById(id);
		
		if(findByUser.isPresent()) {
			
			UserDetail userDetail = findByUser.get();
			userDetail.setIsEnable(status);
			userRepository.save(userDetail);
			return true;
			}
		else {
		
		return false;
	}
	}

	@Override
	public void increaseFailedAttempt(UserDetail user) {

		Integer attempt = user.getFailedAttempt()+1;
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
	        return false; // Account is not locked, so no need to unlock
	    }
		
		long lockTime = user.getLockTime().getTime();
		long unlockTime = lockTime + AppConstant.UNLOCK_DURATION_TIME;
		long currentTime = System.currentTimeMillis();
		if(unlockTime < currentTime) {
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

		
	}

	@Override
	public void userUpdateUserResetToken(String email, String resetToken) {

		UserDetail findByEmail = userRepository.findByEmail(email);
		findByEmail.setResetToken(resetToken);
		userRepository.save(findByEmail);
		
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
	public UserDetail updateUserProfile(UserDetail user,  MultipartFile img) {
		
		UserDetail dbUser = userRepository.findById(user.getId()).get();
		
		if(!img.isEmpty()) {
			dbUser.setProfileImage(img.getOriginalFilename());
		}
		
		if(!ObjectUtils.isEmpty(dbUser)) {
			
			dbUser.setUserName(user.getUserName());
			dbUser.setMobileNumber(user.getMobileNumber());
			dbUser.setAddress(user.getAddress());
			dbUser.setCity(user.getCity());
			dbUser.setState(user.getState());
			dbUser.setPincode(user.getPincode());
			dbUser = userRepository.save(dbUser);
			}
		try {
		if(!img.isEmpty()) {
			File saveFile = new ClassPathResource("static/img").getFile();
			File profileImgFolder = new File(saveFile, "profile_img");

			// Create the directory if it does not exist
			if (!profileImgFolder.exists()) {
				profileImgFolder.mkdirs(); // This will create the folder if it doesn't exist
			}

			Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
					+ img.getOriginalFilename());
			System.out.println(path);
			Files.copy(img.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);


		} 
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return dbUser;
	}

	@Override
	public UserDetail saveAdmin(UserDetail user) {
		
		user.setRole("ROLE_ADMIN");
		user.setIsEnable(true);
		user.setAccountNonLocked(true);
		user.getFailedAttempt();
		
		String encodePassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodePassword);
		UserDetail saveAdmin = userRepository.save(user);
		

		
		return saveAdmin;
	}

	@Override
	public Boolean existsEmail(String email) {
		
		Boolean existsByEmail = userRepository.existsByEmail(email);
		
		return existsByEmail;
	}

	
}
