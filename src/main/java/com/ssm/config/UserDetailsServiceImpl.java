package com.ssm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.ssm.entity.UserDetail;
import com.ssm.repository.UserDetailRepository;

public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	UserDetailRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		UserDetail user = userRepository.findByEmail(username);
		
		if(user==null){
			throw new UsernameNotFoundException("user not found");
		}
		return new CustomUser(user);
	}

}
