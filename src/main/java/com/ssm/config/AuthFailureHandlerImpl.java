package com.ssm.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.ssm.entity.UserDetail;
import com.ssm.repository.UserDetailRepository;
import com.ssm.service.IUserDetailService;
import com.ssm.util.AppConstant;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthFailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler {

	@Autowired
	private UserDetailRepository userRepository;

	@Autowired
	private IUserDetailService userService;

	
	
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {

		String email = request.getParameter("username");

		UserDetail userDetail = userRepository.findByEmail(email);

		if (userDetail != null) {

			if (userDetail.getIsEnable()) {

				if (userDetail.getAccountNonLocked()) {

					if (userDetail.getFailedAttempt() < AppConstant.ATTEMPT_TIME) {
						userService.increaseFailedAttempt(userDetail);
					} else {
						userService.userAccountLock(userDetail);
						exception = new LockedException("Your account is locked !! failed attempt 3");
					}
				} else {

					if (userService.unlockAccountTimeExpired(userDetail)) {
						exception = new LockedException("Your account is unlocked !! Please try to login");
					} else {
						exception = new LockedException("your account is Locked !! Please try after sometimes");
					}
				}

			} else {
				exception = new LockedException("your account is inactive");
			}
		} else {
			exception = new LockedException("Email & password invalid");
		}

		super.setDefaultFailureUrl("/signin?error");
		super.onAuthenticationFailure(request, response, exception);
	}

}