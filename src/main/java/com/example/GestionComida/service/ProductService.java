package com.example.GestionComida.service;

import com.example.GestionComida.domain.entity.Category;
import com.example.GestionComida.domain.entity.Product;
import com.example.GestionComida.error.BadRequestException;
import com.example.GestionComida.error.NotFoundException;
import com.example.GestionComida.repo.CategoryRepository;
import com.example.GestionComida.repo.ProductRepository;
import com.example.GestionComida.web.dto.admin.CreateProductRequest;
import com.example.GestionComida.web.dto.admin.UpdateProductRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;

    public Product get(Integer id){
        return productRepo.findById(id).orElseThrow(() -> new NotFoundException("Producto no encontrado"));
    }

    @Transactional
    public Product adjustStock(Integer id, int newStock, Boolean available){
        if (newStock < 0) throw new BadRequestException("Stock no puede ser negativo");
        Product p = get(id);
        p.setStock(newStock);
        if (available != null) p.setIsAvailable(available);
        if (p.getStock() == 0) p.setIsAvailable(false);
        return productRepo.save(p);
    }

    @Transactional
    public Product createProduct(CreateProductRequest req) {
        Category category = categoryRepo.findById(req.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));

        Product product = Product.builder()
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .stock(req.getStock())
                .category(category)
                .imageUrl(req.getImageUrl())
                .isAvailable(req.getIsAvailable() != null ? req.getIsAvailable() : true)
                .build();

        return productRepo.save(product);
    }

    @Transactional
    public Product updateProduct(Integer id, UpdateProductRequest req) {
        Product product = get(id);

        if (req.getName() != null) {
            product.setName(req.getName());
        }
        if (req.getDescription() != null) {
            product.setDescription(req.getDescription());
        }
        if (req.getPrice() != null) {
            product.setPrice(req.getPrice());
        }
        if (req.getStock() != null) {
            if (req.getStock() < 0) {
                throw new BadRequestException("Stock no puede ser negativo");
            }
            product.setStock(req.getStock());
        }
        if (req.getCategoryId() != null) {
            Category category = categoryRepo.findById(req.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
            product.setCategory(category);
        }
        if (req.getImageUrl() != null) {
            product.setImageUrl(req.getImageUrl());
        }
        if (req.getIsAvailable() != null) {
            product.setIsAvailable(req.getIsAvailable());
        }

        return productRepo.save(product);
    }

    @Transactional
    public void toggleProductAvailability(Integer id) {
        Product product = get(id);
        product.setIsAvailable(!product.getIsAvailable());
        productRepo.save(product);
    }
}
