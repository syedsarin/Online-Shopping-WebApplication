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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ssm.entity.Category;
import com.ssm.entity.Product;
import com.ssm.entity.ProductOrder;
import com.ssm.entity.UserDetail;
import com.ssm.service.ICartService;
import com.ssm.service.ICategoryService;
import com.ssm.service.IOrderService;
import com.ssm.service.IProductService;
import com.ssm.service.IUserDetailService;
import com.ssm.util.CommonUtil;
import com.ssm.util.OrderStatus;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private ICategoryService categoryService;

	@Autowired
	private IProductService productService;
	
	@Autowired
	private IUserDetailService userService;
	
	@Autowired
	private ICartService cartService;
	
	@Autowired
	private IOrderService orderService;
	
	@Autowired
	private CommonUtil commonUtil;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@ModelAttribute
	public void getUserDetails(Principal p, Map<String, Object> map) {
		if(p!=null) {
			String email = p.getName();
			UserDetail userDetail = userService.getUserByEmail(email);
			map.put("user", userDetail);
		}
		

		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		map.put("categorys", allActiveCategory);
	}
	
	
	@GetMapping("/")
	public String index() {
		return "admin/index";
	}

	@GetMapping("/loadAddProducts")
	public String loadAddProducts(Model model) {

		List<Category> categories = categoryService.getAllCategory();
		model.addAttribute("categories", categories);
		return "admin/add_products";
	}

	@GetMapping("/category")
	public String category(Map<String, Object> map,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
		// cat.addAttribute("categories", categoryService.getAllCategory());
		
		Page<Category> page = categoryService.getAllCategoryPagination(pageNo, pageSize);
		
		List<Category> categories= page.getContent();

		map.put("categories", categories);
		map.put("pageNo", page.getNumber());
		
		map.put("totalElements", page.getTotalElements());
		map.put("totalPages", page.getTotalPages());
		map.put("isFirst", page.isFirst());
		map.put("isLast", page.isLast());
		
		return "admin/category";
	}

	@PostMapping("/saveCategory")
public String saveCategory(@ModelAttribute("category") Category category, @RequestParam("file") MultipartFile file,
                           HttpSession session) throws IOException {

    String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
    category.setImageName(imageName);

    Boolean existCategory = categoryService.existCategory(category.getName());

    if (existCategory) {
        session.setAttribute("errorMsg", "Category Name Already Exists");
    } else {
        Category saveCategory = categoryService.saveCategory(category);
        if (ObjectUtils.isEmpty(saveCategory)) {
            session.setAttribute("Error", "Not Saved Internal Server Error");
        } else {
            // ✅ Use absolute external path
            String uploadDir = "/uploads/category_img";
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) {
                uploadFolder.mkdirs(); // create folder if not exists
            }

            Path path = Paths.get(uploadDir + File.separator + file.getOriginalFilename());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            session.setAttribute("successMsg", "Saved Successfully");
        }
    }

    return "redirect:/admin/category";
}


	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable Integer id, HttpSession session) {

		boolean deleteCategory = categoryService.deleteCategory(id);
		if (deleteCategory) {
			session.setAttribute("successMsg", "Category Delete Success");
		} else {
			session.setAttribute("errorMsg", "Category Not Deleted");

		}

		return "redirect:/admin/category";
	}

	@GetMapping("/loadeditCategory/{id}")
	public String loadEditCategory(@PathVariable Integer id, Model model) {
		model.addAttribute("category", categoryService.getCategoryById(id));

		return "admin/edit_category";
	}

	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			HttpSession session) throws IOException {

		Category oldCategory = categoryService.getCategoryById(category.getProductId());
		String imageName = file.isEmpty() ? oldCategory.getImageName() : file.getOriginalFilename();

		if (!ObjectUtils.isEmpty(category)) {

			oldCategory.setName(category.getName());
			oldCategory.setIsActive(category.getIsActive());
			oldCategory.setImageName(imageName);
		}

		Category updateCategory = categoryService.saveCategory(oldCategory);

		if (!ObjectUtils.isEmpty(updateCategory)) {

			if (!file.isEmpty()) {
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
						+ file.getOriginalFilename());
				System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

			}
			session.setAttribute("successMsg", "Category Update Successfully");
		} else {
			session.setAttribute("errorMsg", "Something Went Wrong");

		}
		return "redirect:/admin/loadeditCategory/" + category.getProductId();
	}

	@PostMapping("/saveProduct")
	public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image,
			HttpSession session) throws IOException {

		String imageName = image.isEmpty() ? "default.jpg" : image.getOriginalFilename();

		product.setImageName(imageName);
		product.setDiscount(0);
		product.setDiscountPrice(product.getProductPrice());
		Product saveProduct = productService.saveProduct(product);

		if (!ObjectUtils.isEmpty(saveProduct)) {

			File saveFile = new ClassPathResource("static/img").getFile();

			Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator
					+ image.getOriginalFilename());
			System.out.println(path);
			Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

			session.setAttribute("successMsg", "Product Saved Successfully");
		} else {
			session.setAttribute("errorMsg", "Something Went Wrong");

		}
		return "redirect:/admin/loadAddProducts";
	}

	@GetMapping("/loadProducts")
	public String viewLoadProduct(Map<String,Object> map,
			@RequestParam(defaultValue = "") String ch,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
		
		/*List<Product> products=null;	
		
		if(ch!=null && ch.length()>0) {
			
			products = productService.searchProduct(ch);
		} 
		else {
			products = productService.getAllProduct();
		}
		*/
		
		Page<Product> page=null;	
		
		if(ch!=null && ch.length()>0) {
			
			page = productService.searchProductPagination(pageNo, pageSize, ch);
		} 
		else {
			page = productService.getAllProductsPagination(pageNo,pageSize);
		}
		
		
	
		map.put("products",page.getContent());
		
		
		map.put("pageNo", page.getNumber());
		
		map.put("totalElements", page.getTotalElements());
		map.put("totalPages", page.getTotalPages());
		map.put("isFirst", page.isFirst());
		map.put("isLast", page.isLast());
			
		return "admin/view_products";
	}

	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable Integer id, HttpSession session) {

		Boolean deleteProduct = productService.deleteProduct(id);

		if (deleteProduct) {
			session.setAttribute("successMsg", "Product Delted Successfully");

		} else {
			session.setAttribute("errorMsg", "Somethig Went Wrong");
		}

		return "redirect:/admin/loadProducts";

	}

	@GetMapping("/editProduct/{id}")
	public String editProduct(@PathVariable Integer id, Model model) {

		Product productById = productService.getProductById(id);

		model.addAttribute("product", productById);
		model.addAttribute("categories", categoryService.getAllCategory());

		return "admin/edit_products";
	}

	@PostMapping("/updateProduct")
	public String editProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image , HttpSession session, Model model) throws IOException {
		
		if(product.getDiscount()<0 || product.getDiscount()>100) {
			session.setAttribute("errorMsg", "Invalid Discount");

		}
		else{
		
		Product updateProduct = productService.updateProduct(product,image);
			
			if(!ObjectUtils.isEmpty(updateProduct)) {
				
				session.setAttribute("successMsg", "Product Updated Successfully");
				
			}
			else {
				
				session.setAttribute("errorMsg", "Somethig Went Wrong");

			}
		}
		
				return "redirect:/admin/editProduct/"+product.getId();
	}
	
	@GetMapping("/users")
	public String getAllUsers(Map<String, Object> map, @RequestParam Integer type) {
		
		List<UserDetail> users=null;
		
		if(type==1) {
			
			users = userService.getUsers("ROLE_USER");
		}
		else {
			
			users = userService.getUsers("ROLE_ADMIN");
			
		}
		
		map.put("userType", type);
		map.put("users", users);
		
		return "admin/users";
		
	}
	
	@GetMapping("/updateStatus")
	public String updateUserAccountStatus(@RequestParam Boolean status, @RequestParam Integer id,@RequestParam Integer type, HttpSession session) {
		
	Boolean f=userService.updateAccountStatus(id,status);
		
	if(f) {
		session.setAttribute("successMsg", "Account Status Updated Successfully");

	}
	else {
		session.setAttribute("errorMsg", "Somethig Went Wrong");

	}
	
		return "redirect:/admin/users?type="+type;
	}
	
	
	@GetMapping("/orders")
	public String getAllOrders(Map<String, Object> map,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
		
		/*List<ProductOrder> allOrders = orderService.getAllOrders();
		map.put("orders", allOrders);
		map.put("srch", false);
		*/
		Page<ProductOrder> page = orderService.getAllOrdersPagiantion(pageNo, pageSize);
		map.put("orders", page);
		map.put("srch", false);		
		
		map.put("pageNo", page.getNumber());
		map.put("pageSize", pageSize);
		map.put("totalElements", page.getTotalElements());
		map.put("totalPages", page.getTotalPages());
		map.put("isFirst", page.isFirst());
		map.put("isLast", page.isLast());
		
		return "admin/orders";
		
	}
	
	@PostMapping("/update-order-status")
	public String updtaOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {
		
		OrderStatus[] values = OrderStatus.values();
		String status=null;
		
		for(OrderStatus orderStatus: values) {
			
			
			if(orderStatus.getId().equals(st)) {
				
				status= orderStatus.getName();
			}
		}
		ProductOrder updateOrder = orderService.updateOrdersStatus(id, status);
		
		try {
			commonUtil.sendMailForProductOrder(updateOrder, status);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(!ObjectUtils.isEmpty(updateOrder)) {
			session.setAttribute("successMsg", "Product Status Updated");

		}
		else {
			session.setAttribute("errorMsg", "Status Not Updated");

		}
		return "redirect:/admin/orders";
	}
	
	@GetMapping("/search-order")
	public String searchProduct(@RequestParam String orderId, Map<String, Object> map, HttpSession session,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize){
		
		if(orderId!=null && orderId.length()>0) {
			
		
		
		ProductOrder order = orderService.getOrdersByOrderId(orderId.trim());
		
			if(ObjectUtils.isEmpty(order)) {
				session.setAttribute("errorMsg", "Incorrect Order Id");
				map.put("orderDetails", null);
			}
			else {
				map.put("orderDetails", order);
			}
			map.put("srch", true);
		}
		else {
			/*List<ProductOrder> allOrders = orderService.getAllOrders();			
			map.put("orders", allOrders);
			map.put("srch", false);*/
			
			Page<ProductOrder> page = orderService.getAllOrdersPagiantion(pageNo,pageSize);			
			map.put("orders", page);
			map.put("srch", false);
									
			map.put("pageNo", page.getNumber());
			map.put("pageSize", pageSize);
			map.put("totalElements", page.getTotalElements());
			map.put("totalPages", page.getTotalPages());
			map.put("isFirst", page.isFirst());
			map.put("isLast", page.isLast());
			
		}
		return"admin/orders";
	}
	
	@GetMapping("/add_admin")
	public String loadAdminAdd() {
		
		
		
		return"admin/add_admin";
	}
	
	
	@PostMapping("/register-admin")
	public String registerAdmin(@ModelAttribute UserDetail user, @RequestParam("img") MultipartFile file,
			HttpSession session) throws IOException {

		String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
		user.setProfileImage(imageName);
		UserDetail saveUser = userService.saveAdmin(user);

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
		return "redirect:/admin/add_admin";
	}
	
	@GetMapping("/profile")
	public String profile() {
		
		return"/admin/profile";
	}
	
	@PostMapping("/update-Profile")
	public String updateProfile(@ModelAttribute UserDetail user, @RequestParam MultipartFile img, HttpSession session) {
		
		UserDetail userProfile = userService.updateUserProfile(user, img);
		if(ObjectUtils.isEmpty(userProfile)) {
			
			session.setAttribute("errorMsg", "Profile Not Updated");
			
		}
		else {
			
			session.setAttribute("successMsg", "Profile Updated");
			
		}
		return "redirect:/admin/profile";
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p, HttpSession session) {
		
		UserDetail loggedInUserDetails = commonUtil.getLoggedInUserDetails(p);
		
	boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());
			
		if(matches) {
			
		String encodePassword = passwordEncoder.encode(newPassword);
		loggedInUserDetails.setPassword(encodePassword);
		UserDetail updateUser = userService.updateUser(loggedInUserDetails);
		if(ObjectUtils.isEmpty(updateUser)) {
			
			session.setAttribute("errorMsg", "Password not updated || Error in Server");
		}
		else {
			session.setAttribute("successMsg", "Password Updated");

		}
		} 
		else {
			session.setAttribute("errorMsg", "Current Password is incorect");

		}
	
		return"redirect:/admin/profile";
	}

	
	}
