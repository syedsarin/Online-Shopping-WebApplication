package com.ssm.util;

import java.io.UnsupportedEncodingException;
import java.security.Principal;

import org.hibernate.annotations.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.ssm.entity.ProductOrder;
import com.ssm.entity.UserDetail;
import com.ssm.service.IUserDetailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CommonUtil {
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private IUserDetailService userService;  
	
	public Boolean sendMail(String url, String recipentEmail) throws UnsupportedEncodingException, MessagingException {
		
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		
		helper.setFrom("syedsarin05@gmail.com", "Shopping Cart");
		helper.setTo(recipentEmail);
		
		String content = "<p> Hello,</p>" + "<p> You have requested to reset your password.</p>"
						+ "<p>Click on the link below to change your password:<p>" + "<p><a href=\"" + url
						+"\">Change my Password</a></p>";
		
		helper.setSubject("Password Reset");
		helper.setText(content,true);
		mailSender.send(message);
		return true;
	}

	public static String generateUrl(HttpServletRequest request) {
		
			String siteUrl = request.getRequestURL().toString();
			
			
			
			return siteUrl.replace(request.getServletPath(),"");
		
	}
	
	String msg = null;
	
	public boolean sendMailForProductOrder(ProductOrder order, String status) throws MessagingException, UnsupportedEncodingException {
		
		String msg="<p>[[name]],</p>"
				+ "<p>Thank you Order <b>[[orderStatus]]<b>.</p>"
				+"<p><b>Product Details:</b></p>"
				+"<p>Product Name : [[productName]]</p>"
				+"<p>Product Category : [[productCategory]]</p>"
				+"<p>Product Quantity : [[quantity]]</p>"
				+"<p>Product Price : [[productPrice]]</p>"
				+"<p>Payment Type : [[paymentType]]</p>";
		

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		
		helper.setFrom("syedsarin05@gmail.com", "Shopping Cart");
		helper.setTo(order.getOrderAddress().getEmail());
	
		msg=msg.replace("[[name]]",order.getOrderAddress().getFirstName());
		msg=msg.replace("[[orderStatus]]", status);
		msg=msg.replace("[[productName]]", order.getProduct().getProductTitle());
		msg=msg.replace("[[productCategory]]", order.getProduct().getProductCategory());
		msg=msg.replace("[[quantity]]", order.getQuantity().toString());
		msg=msg.replace("[[productPrice]]", order.getPrice().toString());
		msg=msg.replace("[[paymentType]]", order.getPaymentType());
		helper.setSubject("Product Order Status");
		helper.setText(msg,true);
		mailSender.send(message);
		
		return true;
	}
	
	
public UserDetail getLoggedInUserDetails(Principal p) {
		
		String email = p.getName();
		UserDetail userDetail = userService.getUserByEmail(email);
		return userDetail;
	}
	
}
