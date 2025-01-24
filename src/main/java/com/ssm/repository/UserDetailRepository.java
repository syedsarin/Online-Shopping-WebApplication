package com.ssm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssm.entity.UserDetail;

public interface UserDetailRepository extends JpaRepository<UserDetail, Integer> {

	public UserDetail findByEmail(String email);
	
	public List<UserDetail> findByRole(String role);
	
	public UserDetail findByresetToken(String token);
	
	public Boolean existsByEmail(String email);
	
}
