package com.ssm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssm.entity.Cart;

public interface ICartRepository extends JpaRepository<Cart, Integer> {

	public Cart findByProductIdAndUserId(Integer productId, Integer userId);

	public Integer countByUserId(Integer userId);
	
	public List<Cart> findByUserId(Integer userId);

	
    // ✅ Add this method to remove cart entries referencing a product before deletion
    @Modifying
    @Transactional
    @Query("DELETE FROM Cart c WHERE c.product.id = :productId")
    void deleteByProductId(@Param("productId") Integer productId);

}
