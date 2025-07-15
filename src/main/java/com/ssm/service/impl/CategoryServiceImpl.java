package com.ssm.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.ssm.entity.Category;
import com.ssm.repository.ICategoryRepository;
import com.ssm.service.ICategoryService;

@Service
public class CategoryServiceImpl implements ICategoryService {

	@Autowired
	private ICategoryRepository categoryRepository;
	@Override
	public Category saveCategory(Category category) {
		Category save = categoryRepository.save(category);
		return save;
	}
	
	@Override
	public boolean existCategory(String name) {
		return categoryRepository.existsByName(name);
		
	}

	@Override
	public List<Category> getAllCategory() {
		
		return categoryRepository.findAll();
	}

	@Override
	public boolean deleteCategory(Integer id) {
		Category category = categoryRepository.findById(id).orElse(null);
		if(!ObjectUtils.isEmpty(category)) {
			categoryRepository.delete(category);
			return true;
		}
		
		return false;
	}

	@Override
	public Category getCategoryById(Integer id) {
		
		Category category = categoryRepository.findById(id).orElse(null);
		
		 return category;
	}

	@Override
	public List<Category> getAllActiveCategory() {
		
		List<Category> categories = categoryRepository.findByIsActiveTrue();
			
		return categories;
	}

	@Override
	public Page<Category> getAllCategoryPagination(Integer pageNo, Integer pageSize) {
		
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return categoryRepository.findAll(pageable);
		
	}

	
	}
