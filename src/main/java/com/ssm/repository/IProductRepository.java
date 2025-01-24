package com.ssm.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ssm.entity.Product;

public interface IProductRepository extends JpaRepository<Product, Integer> {

	public List<Product> findByIsActiveTrue();
	
	public List<Product> findByProductCategory(String category);
	
	List<Product> findByProductTitleContainingIgnoreCaseOrProductCategoryContainingIgnoreCase(String ch, String ch2);

	public Page<Product> findByIsActiveTrue(Pageable pageable);

	public Page<Product> findByProductCategory(Pageable pageable, String category);

	Page<Product> findByProductTitleContainingIgnoreCaseOrProductCategoryContainingIgnoreCase(String ch, String ch2,
			Pageable pageable);

	Page<Product> findByisActiveTrueAndProductTitleContainingIgnoreCaseOrProductCategoryContainingIgnoreCase(String ch,
			String ch2, Pageable pageable);

}
