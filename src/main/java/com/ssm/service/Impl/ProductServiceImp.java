package com.ssm.service.impl;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ssm.entity.Product;
import com.ssm.repository.ICartRepository;
import com.ssm.repository.IProductOrderRepository;
import com.ssm.repository.IProductRepository;
import com.ssm.service.IProductService;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ssm.entity.Product;
import com.ssm.repository.IProductRepository;
import com.ssm.service.IProductService;

@Service
public class ProductServiceImp implements IProductService {

	@Autowired
	IProductRepository productRepository;
	@Autowired
	private ICartRepository cartRepository;

	@Autowired
	private IProductOrderRepository productOrderRepository;

	
	@Override
	public Product saveProduct(Product product) {
		Product save = productRepository.save(product);
		return save;
	}

	@Override
	public List<Product> getAllProduct() {
		
		List<Product> findAll = productRepository.findAll();
		return findAll;
	}

	@Transactional
	@Override
	public Boolean deleteProduct(Integer id) {
	    Product product = productRepository.findById(id).orElse(null);
	    if (!ObjectUtils.isEmpty(product)) {

	        // 🧹 Step 1: Delete related cart entries
	        cartRepository.deleteByProductId(id);

	        // 🧹 Step 2: Delete related product_order entries
	        productOrderRepository.deleteByProductId(id);

	        // 🗑️ Step 3: Now delete product
	        productRepository.delete(product);
	        return true;
	    }
	    return false;
	}


	@Override
	public Product getProductById(Integer id) {
		
		Product product = productRepository.findById(id).orElse(null);
		
		return product;
	}

	@Override
public Product updateProduct(Product product, MultipartFile image) throws IOException {
    
    Product dbProduct = getProductById(product.getId());
    String imageName = image.isEmpty() ? dbProduct.getImageName() : image.getOriginalFilename();	

    dbProduct.setProductTitle(product.getProductTitle());
    dbProduct.setProductDescription(product.getProductDescription());
    dbProduct.setProductCategory(product.getProductCategory());
    dbProduct.setProductPrice(product.getProductPrice());
    dbProduct.setStock(product.getStock());
    dbProduct.setIsActive(product.getIsActive());
    dbProduct.setImageName(imageName);
    
    dbProduct.setDiscount(product.getDiscount());
    Double discount = product.getProductPrice() * (product.getDiscount() / 100.0);
    Double finalPrice = product.getProductPrice() - discount;
    dbProduct.setDiscountPrice(finalPrice);
    
    Product updateProduct = productRepository.save(dbProduct);

    if (!ObjectUtils.isEmpty(updateProduct)) {
        if (!image.isEmpty()) {
            // ✅ External upload directory (not inside classpath)
            String uploadDir = "uploads/product_img";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            Path path = Paths.get(uploadDir, image.getOriginalFilename());
            System.out.println("Saving file to: " + path);
            Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        }
        return dbProduct;
    }

    return null;
}

	@Override
	public List<Product> getAllActiveProducts(String category) {
		
		List<Product> activeProducts=null;
		
		if(ObjectUtils.isEmpty(category)) {
			
			activeProducts = productRepository.findByIsActiveTrue();
		}
		else{
			activeProducts= productRepository.findByProductCategory(category);
		}
		
		return activeProducts;
	}

	@Override
	public List<Product> searchProduct(String ch) {
		
		
	return productRepository.findByProductTitleContainingIgnoreCaseOrProductCategoryContainingIgnoreCase(ch, ch);
		
		
	}

	@Override
	public Page<Product> getAllActiveProductPagination(Integer pageNo, Integer pageSize, String category) {
		
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		Page<Product> pageProduct = null; 
		if(ObjectUtils.isEmpty(category)) {
			
		pageProduct = productRepository.findByIsActiveTrue(pageable);
		}
		else{
			pageProduct= productRepository.findByProductCategory(pageable, category);
		}
		
		return pageProduct;
	}

	@Override
	public Page<Product> searchProductPagination(Integer pageNo, Integer pageSize, String ch) {
		
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return productRepository.findByProductTitleContainingIgnoreCaseOrProductCategoryContainingIgnoreCase(ch, ch, pageable);
		
		
	}

	@Override
	public Page<Product> getAllProductsPagination(Integer pageNo, Integer pageSize) {
		
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return productRepository.findAll(pageable);
		
	}

	@Override
	public Page<Product> searchActiveProductPagination(Integer pageNo, Integer pageSize, String category, String ch) {
		
		Page<Product> pageProduct = null;
		
		
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		pageProduct = productRepository.findByisActiveTrueAndProductTitleContainingIgnoreCaseOrProductCategoryContainingIgnoreCase(ch, ch, pageable);
		
		
			/*if(ObjectUtils.isEmpty(category)) {
				
			pageProduct = productRepository.findByIsActiveTrue(pageable);
			}
			else{
				pageProduct= productRepository.findByProductCategory(pageable, category);
			}*/
		
		return pageProduct;
	
	}
}
