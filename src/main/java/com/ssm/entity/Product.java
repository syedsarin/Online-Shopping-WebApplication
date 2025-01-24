package com.ssm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(length = 500)
	private String productTitle;
	
	@Column(length = 500)
	private String productDescription;
	
	private String productCategory;
	
	private Double productPrice;
	
	private Integer stock;
	
	private String imageName;
	
	private Integer discount;
	
	private Double discountPrice;
	
	private Boolean isActive;
}
