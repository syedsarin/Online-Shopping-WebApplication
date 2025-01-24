package com.ssm.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.ssm.entity.OrderRequest;
import com.ssm.entity.ProductOrder;

public interface IOrderService {

 	
	public void saveOder(Integer userId, OrderRequest orderRequest) throws Exception;
	
	public List<ProductOrder> getOrdersByUser(Integer userId);
	
	public ProductOrder updateOrdersStatus(Integer id, String status);
	
	public List<ProductOrder> getAllOrders();

	public ProductOrder getOrdersByOrderId(String orderId);
	
	public Page<ProductOrder> getAllOrdersPagiantion(Integer pageNo, Integer pageSize);

}