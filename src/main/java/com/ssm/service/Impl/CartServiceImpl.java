package com.ssm.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.ssm.entity.Cart;
import com.ssm.entity.Product;
import com.ssm.entity.UserDetail;
import com.ssm.repository.ICartRepository;
import com.ssm.repository.IProductRepository;
import com.ssm.repository.UserDetailRepository;
import com.ssm.service.ICartService;

@Service
public class CartServiceImpl implements ICartService {

	@Autowired
	private ICartRepository cartRepository;
	
	@Autowired
	private UserDetailRepository userRepository;
	
	@Autowired
	private IProductRepository productRepository;
	
	@Override
	public Cart saveCart(Integer productId, Integer userId) {
		
		UserDetail userDetail = userRepository.findById(userId).get();	
		
		Product product = productRepository.findById(productId).get();
		
		Cart cartStatus = cartRepository.findByProductIdAndUserId(productId, userId);
		Cart cart =null;
		if(ObjectUtils.isEmpty(cartStatus)) {
			
			cart = new Cart();
			cart.setProduct(product);
			cart.setUser(userDetail);
			cart.setQuantity(1);
			cart.setTotalPrice(1 * product.getDiscountPrice());
		} else {
			
			cart=cartStatus;
			cart.setQuantity(cart.getQuantity()+1);
			cart.setTotalPrice(cart.getQuantity() * cart.getProduct().getDiscountPrice());
		}
		
		Cart saveCart = cartRepository.save(cart);
		return saveCart;
	}

	@Override
	public List<Cart> getCartByUser(Integer userId) {
		
		List<Cart> carts = cartRepository.findByUserId(userId);
		
		Double totalOrderPrice = 0.0;
		List<Cart> updateCarts = new ArrayList<Cart>();
		for(Cart c:carts) {
			Double totalPrice = (c.getProduct().getDiscountPrice()*c.getQuantity());
			c.setTotalPrice(totalPrice);
			totalOrderPrice+=totalPrice;
			c.setTotalOrderPrice(totalOrderPrice);
			updateCarts.add(c);
		}
		
		return updateCarts;
	}

	@Override
	public Integer getCountCart(Integer userId) {
		
		Integer countByUserId = cartRepository.countByUserId(userId);
		
		
		return countByUserId;
	}

	@Override
	public void updateQuantity(String sy, Integer cid) {
		
		Cart cart = cartRepository.findById(cid).get();
		Integer updateQuantity=0;
		if(sy.equalsIgnoreCase("de")) {
			
			updateQuantity = cart.getQuantity()-1;
			
			if(updateQuantity<=0) {
				
				cartRepository.delete(cart);
				 
			} 
			else {
				cart.setQuantity(updateQuantity);
				cartRepository.save(cart);

			}
		}
		else {
		updateQuantity = cart.getQuantity()+1;
		cart.setQuantity(updateQuantity);
		cartRepository.save(cart);
		}
			
	}

}
