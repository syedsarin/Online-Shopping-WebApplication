package com.ssm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssm.entity.ProductOrder;

public interface IProductOrderRepository extends JpaRepository<ProductOrder, Integer>{

	List<ProductOrder> findByUserId(Integer userId);

	ProductOrder findByOrderId(String orderId);

}
