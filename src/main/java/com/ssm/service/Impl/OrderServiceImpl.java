package com.ssm.service.impl;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ssm.entity.Cart;
import com.ssm.entity.OrderAddress;
import com.ssm.entity.OrderRequest;
import com.ssm.entity.ProductOrder;
import com.ssm.repository.ICartRepository;
import com.ssm.repository.IProductOrderRepository;
import com.ssm.service.IOrderService;
import com.ssm.util.CommonUtil;
import com.ssm.util.OrderStatus;

import jakarta.mail.MessagingException;

@Service
public class OrderServiceImpl implements IOrderService {
	
	@Autowired
	private IProductOrderRepository orderRepository;
	
	@Autowired
	private ICartRepository cartRepository;
	
	@Autowired
	private CommonUtil commonUtil;
	
	@Override
	public void saveOder(Integer userId, OrderRequest orderRequest) throws UnsupportedEncodingException, MessagingException {
		
		List<Cart> carts = cartRepository.findByUserId(userId);
		
		for(Cart cart: carts) {
		
			ProductOrder order = new ProductOrder();
			
			order.setOrderId(UUID.randomUUID().toString());
			order.setOrderDate(LocalDate.now());
			
			order.setProduct(cart.getProduct());			
			order.setPrice(cart.getProduct().getDiscountPrice());
			
			order.setQuantity(cart.getQuantity());
			order.setUser(cart.getUser());
			
			order.setStatus(OrderStatus.IN_PROGRESS.getName());
			order.setPaymentType(orderRequest.getPaymentType());
			
			OrderAddress address = new OrderAddress();
			
			address.setFirstName(orderRequest.getFirstName());
			address.setLastName(orderRequest.getLastName());
			
			address.setEmail(orderRequest.getEmail());
			address.setMobileNo(orderRequest.getMobileNo());
			
			address.setAddress(orderRequest.getAddress());
			address.setCity(orderRequest.getCity());
			
			address.setState(orderRequest.getState());
			address.setPincode(orderRequest.getPincode());
			
			order.setOrderAddress(address);
			
			ProductOrder saveOrder = orderRepository.save(order);
			commonUtil.sendMailForProductOrder(saveOrder, "Success");
			
		}
		
	}

	@Override
	public List<ProductOrder> getOrdersByUser(Integer userId) {
		
	List<ProductOrder> orders= orderRepository.findByUserId(userId);
		
		return orders;
	}

	@Override
	public ProductOrder updateOrdersStatus(Integer id, String status) {
		
		Optional<ProductOrder> findById = orderRepository.findById(id);
		
		if(findById.isPresent())
		{
			ProductOrder productOrder = findById.get();
			productOrder.setStatus(status);
			ProductOrder updateOrder = orderRepository.save(productOrder);
			return updateOrder;
		}
		return null;
	}

	@Override
	public List<ProductOrder> getAllOrders() {
		
		List<ProductOrder> findAll = orderRepository.findAll();
		
		return findAll;
	}

	@Override
	public ProductOrder getOrdersByOrderId(String orderId) {
		
		return orderRepository.findByOrderId(orderId);
		
		
	}

	@Override
	public Page<ProductOrder> getAllOrdersPagiantion(Integer pageNo, Integer pageSize) {
		
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return orderRepository.findAll(pageable);
		
	}
}
