package com.ssm.service;

import java.util.List;

import org.antlr.v4.runtime.misc.IntegerList;

import com.ssm.entity.Cart;

public interface ICartService {
	
	public Cart saveCart(Integer productId, Integer userId);
	
	public List<Cart> getCartByUser(Integer userId);
	
	public Integer getCountCart(Integer userId);

	public void updateQuantity(String sy, Integer cid);
}
