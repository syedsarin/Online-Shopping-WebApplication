package com.ssm.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ssm.entity.Category;

public interface ICategoryService {
	
	public Category saveCategory(Category category);
	
	public boolean existCategory(String name);
	
	public List<Category> getAllCategory();
	
	public boolean deleteCategory(Integer id);
	
	public Category getCategoryById(Integer id);

	public List<Category> getAllActiveCategory();
	
	public Page<Category> getAllCategoryPagination(Integer pageNo, Integer pageSize);
}
