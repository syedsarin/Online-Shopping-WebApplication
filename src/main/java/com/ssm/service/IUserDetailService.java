package com.ssm.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.ssm.entity.UserDetail;

public interface IUserDetailService {
	
	public UserDetail saveUser(UserDetail user);
	
	public UserDetail getUserByEmail(String email);
	
	public List<UserDetail> getUsers(String role);

	public Boolean updateAccountStatus(Integer id, Boolean status);
	
	public void increaseFailedAttempt(UserDetail user); 
	
	public void userAccountLock(UserDetail user);
	
	public Boolean unlockAccountTimeExpired(UserDetail user);
	
	public void resetAttempt(Integer userId);

	public void userUpdateUserResetToken(String email, String resetToken);
	
	public UserDetail getUserByToken(String token);
	
	public UserDetail updateUser(UserDetail user);
	
	public UserDetail updateUserProfile(UserDetail user, MultipartFile img);
	
	public UserDetail saveAdmin(UserDetail user);
	
	public Boolean existsEmail(String email);

}
