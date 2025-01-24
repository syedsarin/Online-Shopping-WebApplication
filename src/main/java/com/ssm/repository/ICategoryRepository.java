package com.ssm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssm.entity.Category;
import com.ssm.entity.Product;

public interface ICategoryRepository extends JpaRepository<Category, Integer>{

	public boolean existsByName(String name);
	
	public List<Category> findByIsActiveTrue();
	

}
