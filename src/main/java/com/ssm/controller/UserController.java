package com.ssm.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ssm.entity.Cart;
import com.ssm.entity.Category;
import com.ssm.entity.OrderRequest;
import com.ssm.entity.ProductOrder;
import com.ssm.entity.UserDetail;
import com.ssm.service.ICartService;
import com.ssm.service.ICategoryService;
import com.ssm.service.IOrderService;
import com.ssm.service.IUserDetailService;
import com.ssm.util.CommonUtil;
import com.ssm.util.OrderStatus;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private IUserDetailService userService;

	@Autowired
	private ICategoryService categoryService;

	@Autowired
	private ICartService cartService;

	@Autowired
	private IOrderService orderService;

	@Autowired
	private CommonUtil commonUtill;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@GetMapping("/")
	public String home() {
		return "user/home";
	}

	@ModelAttribute
	public void getUserDetails(Principal p, Map<String, Object> map) {
		if (p != null) {
			String email = p.getName();
			UserDetail userDetail = userService.getUserByEmail(email);
			map.put("user", userDetail);
			Integer countCart = cartService.getCountCart(userDetail.getId());
			map.put("countCart", countCart);
		}
		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		map.put("category", allActiveCategory);
	}

	@GetMapping("/addCart")
	public String addToCart(@RequestParam Integer pid, @RequestParam Integer uid, HttpSession session) {
		Cart saveCart = cartService.saveCart(pid, uid);
		if (ObjectUtils.isEmpty(saveCart)) {
			session.setAttribute("errorMsg", "Product add to Cart Failed");
		} else {
			session.setAttribute("successMsg", "Product added to cart");
		}
		return "redirect:/product/" + pid;
	}

	@GetMapping("/cart")
	public String loadCartPage(Principal p, Model m) {
		UserDetail user = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getCartByUser(user.getId());
		m.addAttribute("carts", carts);
		if (!carts.isEmpty()) {
			Double totalOrderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
			m.addAttribute("totalOrderPrice", totalOrderPrice);
		}
		return "/user/cart";
	}

	@GetMapping("/cartQuantityUpdate")
	public String updateCartQuantity(@RequestParam String sy, @RequestParam Integer cid) {
		cartService.updateQuantity(sy, cid);
		return "redirect:/user/cart";
	}

	private UserDetail getLoggedInUserDetails(Principal p) {
		String email = p.getName();
		return userService.getUserByEmail(email);
	}

	@GetMapping("/orders")
	public String orderPage(Principal p, Model m) {
		UserDetail user = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getCartByUser(user.getId());
		m.addAttribute("carts", carts);
		if (!carts.isEmpty()) {
			Double orderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
			Double totalOrderPrice = orderPrice + 250 + 100;
			m.addAttribute("OrderPrice", orderPrice);
			m.addAttribute("totalOrderPrice", totalOrderPrice);
		}
		return "/user/order";
	}

	@PostMapping("/save-order")
	public String saveOrder(@ModelAttribute OrderRequest request, Principal p) throws Exception {
		UserDetail user = getLoggedInUserDetails(p);
		orderService.saveOder(user.getId(), request);
		return "redirect:/user/success";
	}

	@GetMapping("/success")
	public String loadSuccess() {
		return "/user/success";
	}

	@GetMapping("/user-orders")
	public String myOrder(Map<String, Object> map, Principal p) {
		UserDetail loginUser = getLoggedInUserDetails(p);
		List<ProductOrder> orders = orderService.getOrdersByUser(loginUser.getId());
		map.put("orders", orders);
		return "/user/my_orders";
	}

	@GetMapping("/update-status")
	public String updtaOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session)
			throws UnsupportedEncodingException, MessagingException {

		OrderStatus[] values = OrderStatus.values();
		String status = null;
		for (OrderStatus orderStatus : values) {
			if (orderStatus.getId().equals(st)) {
				status = orderStatus.getName();
			}
		}
		ProductOrder updateOrder = orderService.updateOrdersStatus(id, status);
		commonUtill.sendMailForProductOrder(updateOrder, status);

		if (ObjectUtils.isEmpty(updateOrder)) {
			session.setAttribute("successMsg", "Product Status Updated");
		} else {
			session.setAttribute("errorMsg", "Status Not Updated");
		}
		return "redirect:/user/user-orders";
	}

	@GetMapping("/profile")
	public String profile() {
		return "/user/profile";
	}

	@PostMapping("/update-Profile")
	public String updateProfile(@ModelAttribute UserDetail user,
								@RequestParam MultipartFile img,
								HttpSession session) {
		try {
			if (!img.isEmpty()) {
				String uploadDir = System.getProperty("user.home") + "/app-images";
				File folder = new File(uploadDir);
				if (!folder.exists()) {
					folder.mkdirs();
				}
				String fileName = img.getOriginalFilename();
				File filePath = new File(folder, fileName);
				img.transferTo(filePath);
				user.setProfileImage(fileName); // Save only name or full path as needed
			}
			UserDetail updatedUser = userService.updateUser(user);
			if (ObjectUtils.isEmpty(updatedUser)) {
				session.setAttribute("errorMsg", "Profile Not Updated");
			} else {
				session.setAttribute("successMsg", "Profile Updated");
			}
		} catch (IOException e) {
			e.printStackTrace();
			session.setAttribute("errorMsg", "Error saving profile image");
		}
		return "redirect:/user/profile";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword,
								 @RequestParam String currentPassword,
								 Principal p,
								 HttpSession session) {

		UserDetail loggedInUserDetails = getLoggedInUserDetails(p);
		boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());

		if (matches) {
			String encodePassword = passwordEncoder.encode(newPassword);
			loggedInUserDetails.setPassword(encodePassword);
			UserDetail updateUser = userService.updateUser(loggedInUserDetails);
			if (ObjectUtils.isEmpty(updateUser)) {
				session.setAttribute("errorMsg", "Password not updated || Error in Server");
			} else {
				session.setAttribute("successMsg", "Password Updated");
			}
		} else {
			session.setAttribute("errorMsg", "Current Password is incorrect");
		}
		return "redirect:/user/profile";
	}
}
