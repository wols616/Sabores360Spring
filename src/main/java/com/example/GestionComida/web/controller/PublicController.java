package com.example.GestionComida.web.controller;

import com.example.GestionComida.domain.entity.Product;
import com.example.GestionComida.repo.ProductRepository;
import com.example.GestionComida.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final ProductRepository productRepo;

    /**
     * Versión pública de /api/client/products/full: devuelve las entidades completas
     * de los productos visibles al cliente, pero NO requiere autenticación.
     */
    @GetMapping("/products/full")
    public ApiResponse<Map<String, Object>> productsFullPublic(
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page
    ) {
        List<Product> base = productRepo.findAll();

        List<Product> filtered = new ArrayList<>();
        for (Product p : base) {
            if (!Boolean.TRUE.equals(p.getIsAvailable())) continue;

            if (category != null) {
                if (p.getCategory() == null || p.getCategory().getId() == null ||
                        !p.getCategory().getId().equals(category)) {
                    continue;
                }
            }

            if (search != null) {
                String s = search.toLowerCase();
                String n = p.getName() == null ? "" : p.getName().toLowerCase();
                if (!n.contains(s)) continue;
            }
            filtered.add(p);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("products", filtered);
        return ApiResponse.ok(body);
    }
}

