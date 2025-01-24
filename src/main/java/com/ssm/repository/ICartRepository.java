package com.ssm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssm.entity.Cart;

public interface ICartRepository extends JpaRepository<Cart, Integer> {

	public Cart findByProductIdAndUserId(Integer productId, Integer userId);

	public Integer countByUserId(Integer userId);
	
	public List<Cart> findByUserId(Integer userId);
}
