package com.ssm.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hibernate.sql.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ssm.entity.Category;
import com.ssm.entity.Product;
import com.ssm.entity.UserDetail;
import com.ssm.repository.IProductRepository;
import com.ssm.service.ICartService;
import com.ssm.service.ICategoryService;
import com.ssm.service.IProductService;
import com.ssm.service.IUserDetailService;
import com.ssm.util.CommonUtil;

import io.micrometer.common.util.StringUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

	@Autowired
	ICategoryService categoryService;

	@Autowired
	IProductService productService;

	@Autowired
	IUserDetailService userService;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private ICartService cartService;

	@ModelAttribute
	public void getUserDetails(Principal p, Map<String, Object> map) {

		if (p != null) {
			String email = p.getName();
			UserDetail userDetail = userService.getUserByEmail(email);
			map.put("user", userDetail);
			Integer countCart = cartService.getCountCart(userDetail.getId());
			map.put("countCart", countCart);

		} else {
			map.put("user", null);
		}

		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		map.put("categorys", allActiveCategory);
	}

	@GetMapping("/")
	public String home(Map<String,Object> map) {

		List<Category> allActiveCategory = categoryService.getAllActiveCategory().stream()
				.sorted((c1,c2)->c2.getProductId().compareTo(c1.getProductId())).limit(6).toList();
		List<Product> allActiveProduct = productService.getAllActiveProducts("").stream()
				.sorted((p1,p2)->p2.getId().compareTo(p1.getId())).limit(8).toList();
		map.put("category", allActiveCategory);
		map.put("products", allActiveProduct);
		return "index";
	}

	@GetMapping("/register")
	public String register() {

		return "register";
	}

	@GetMapping("/signin")
	public String login() {
		return "login";
	}

	@GetMapping("/products")
	public String products(Map<String, Object> map,
			@RequestParam(value = "category", defaultValue = "") String category, 
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo, 
			@RequestParam(name = "pageSize",defaultValue = "9") Integer pageSize,
			@RequestParam(defaultValue = "") String ch) {

		List<Category> categories = categoryService.getAllActiveCategory();
		map.put("paramValue", category);
		map.put("categories", categories);
		
		/*List<Product> products = productService.getAllActiveProducts(category);
		map.put("products", products);
		*/
		Page<Product> page = null;
		
		if(StringUtils.isEmpty(ch)) {
			
			page = productService.getAllActiveProductPagination(pageNo, pageSize, category);
		}
		else {
			page=productService.searchActiveProductPagination(pageNo,pageSize,category,ch);
		}
		
		
		List<Product> products= page.getContent();

		map.put("products", products);
		map.put("pageNo", page.getNumber());
		map.put("productsSize", products.size());
		
		map.put("totalElements", page.getTotalElements());
		map.put("totalPages", page.getTotalPages());
		map.put("isFirst", page.isFirst());
		map.put("isLast", page.isLast());
		
		return "product";
	}

	@GetMapping("/product/{id}")
	public String product(@PathVariable Integer id, Map<String, Object> map) {

		Product productById = productService.getProductById(id);
		map.put("product", productById);
		return "view_product";
	}

	@PostMapping("/register")
	public String registerUser(@ModelAttribute UserDetail user, @RequestParam("img") MultipartFile file,
			HttpSession session) throws IOException {
		
		Boolean existstEmail = userService.existsEmail(user.getEmail());
		
		if(existstEmail) {
			
			session.setAttribute("errorMsg", "Email Id already Exists.");
		}
		else {
			
			String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
			user.setProfileImage(imageName);
			UserDetail saveUser = userService.saveUser(user);

			
			if (!ObjectUtils.isEmpty(saveUser)) {
				if (!file.isEmpty()) {

					File saveFile = new ClassPathResource("static/img").getFile();
					File profileImgFolder = new File(saveFile, "profile_img");

					// Create the directory if it does not exist
					if (!profileImgFolder.exists()) {
						profileImgFolder.mkdirs(); // This will create the folder if it doesn't exist
					}

					Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
							+ file.getOriginalFilename());
					System.out.println(path);
					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				}
				session.setAttribute("successMsg", "Regsiter Successful");
			} else {
				session.setAttribute("errorMsg", "Something Went Wrong");

			}

		}
		

		return "redirect:/register";
	}

	// Forgot Password Code

	@GetMapping("/forgot-pass")
	public String showForgotPassword() {

		return "forgot_password";
	}

	@PostMapping("/forgot-pass")
	public String processForgotPassword(@RequestParam String email, HttpSession session, HttpServletRequest request)
			throws UnsupportedEncodingException, MessagingException {

		UserDetail userByEmail = userService.getUserByEmail(email);

		if (ObjectUtils.isEmpty(userByEmail)) {

			session.setAttribute("errorMsg", "Invalid Email");
		} else {

			String resetToken = UUID.randomUUID().toString();
			userService.userUpdateUserResetToken(email, resetToken);

			// Generate URL
			// http://localhost:8080/reset-pass?token=4003eea3-c5ba-4cec-8892-1deb6c68fb8d

			String url = commonUtil.generateUrl(request) + "/reset-pass?token=" + resetToken;

			Boolean sendMail = commonUtil.sendMail(url, email);

			if (sendMail) {
				session.setAttribute("successMsg", "Please Check Your mail.. Password reset link Sent");

			} else {
				session.setAttribute("errorMsg", "Something Wrong on Server");

			}
		}

		return "redirect:/forgot-pass";
	}

	// Reset Password

	@GetMapping("/reset-pass")
	public String showResetPassword(@RequestParam String token, HttpSession session, Map<String, Object> map) {

		UserDetail userByToken = userService.getUserByToken(token);

		if (ObjectUtils.isEmpty(userByToken)) {

			map.put("errorMsg", "Your Link is Invalid or Expired.");
			return "message";
		}

		map.put("token", token);
		return "reset_password";
	}

	@PostMapping("/reset-pass")
	public String resetPassword(@RequestParam String token, @RequestParam String password, HttpSession session,
			Map<String, Object> map) {

		UserDetail userByToken = userService.getUserByToken(token);

		if (userByToken == null) {

			map.put("errorMsg", "Your Link is Invalid or Expired.");
			return "message";
		} else {
			userByToken.setPassword(passwordEncoder.encode(password));
			userByToken.setResetToken(null);
			userService.updateUser(userByToken);
			session.setAttribute("successMsg", "Password Has been Changed Successfully.");
			map.put("msg", "Password Has been Changed Successfully.");

			return "message";
		}

	}
	
	@GetMapping("/search")
	public String searchProduct(@RequestParam String ch, Map<String, Object> map){
		
		List<Product> searchProducts = productService.searchProduct(ch);
		
		map.put("products", searchProducts);
		
		List<Category> categories = categoryService.getAllActiveCategory();
		map.put("categories", categories);

		
		return"product";
	}
}
