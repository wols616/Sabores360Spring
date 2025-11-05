package com.example.GestionComida.web.controller;

import com.example.GestionComida.domain.entity.Order;
import com.example.GestionComida.domain.entity.Product;
import com.example.GestionComida.domain.entity.User;
import com.example.GestionComida.repo.OrderRepository;
import com.example.GestionComida.repo.ProductRepository;
import com.example.GestionComida.repo.CategoryRepository;
import com.example.GestionComida.service.OrderService;
import com.example.GestionComida.service.ProductService;
import com.example.GestionComida.web.ApiResponse;
import com.example.GestionComida.web.dto.AssignSellerRequest;
import com.example.GestionComida.web.dto.ChangeStatusRequest;
import com.example.GestionComida.web.dto.seller.AvailabilityRequest;
import com.example.GestionComida.web.dto.seller.BulkStockRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDEDOR')")
public class SellerController {
    private final ProductRepository productRepo;
    private final ProductService productService;
    private final CategoryRepository categoryRepo;
    private final OrderRepository orderRepo;
    private final OrderService orderService;

    /**
     * Helper para obtener el usuario autenticado desde el contexto de seguridad
     */
    private User getAuthenticatedUser(Authentication auth) {
        return (User) auth.getPrincipal();
    }

    @GetMapping("/dashboard")
    public ApiResponse<Object> dashboard(Authentication authentication){
        User seller = getAuthenticatedUser(authentication);
        List<Order> orders = orderRepo.findBySeller_Id(seller.getId());
        long pending = 0L;
        for (Order o : orders) {
            String st = o.getStatus();
            if ("Confirmado".equals(st) || "En preparación".equals(st)) {
                pending++;
            }
        }
        List<Order> recent = orders.stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .limit(10)
                .collect(Collectors.toList());

        Map<String,Object> resp = new HashMap<String,Object>();
        resp.put("pending", pending);
        resp.put("recent_orders", recent);
        return ApiResponse.ok(resp);
    }

        @GetMapping("/categories")
        public ApiResponse<Map<String, Object>> categories() {
            List<com.example.GestionComida.domain.entity.Category> categories = categoryRepo.findAll();
            Map<String, Object> resp = new HashMap<String, Object>();
            resp.put("categories", categories);
            return ApiResponse.ok(resp);
        }

        @GetMapping("/categories/{id}")
        public ApiResponse<Map<String, Object>> categoryDetail(@PathVariable Integer id) {
            com.example.GestionComida.domain.entity.Category c = categoryRepo.findById(id).orElse(null);
            Map<String, Object> resp = new HashMap<String, Object>();
            resp.put("category", c);
            return ApiResponse.ok(resp);
        }

    @GetMapping("/products/stock")
    public ApiResponse<Object> stockList(){
        List<Product> products = productRepo.findAll();
        Map<String,Object> resp = new HashMap<String,Object>();
        resp.put("products", products);
        return ApiResponse.ok(resp);
    }

    @GetMapping("/products")
    public ApiResponse<Object> products(){
        Map<String,Object> resp = new HashMap<String,Object>();
        resp.put("products", productRepo.findAll());
        return ApiResponse.ok(resp);
    }

    @PostMapping("/products/{id}/availability")
    public ApiResponse<Void> availability(@PathVariable Integer id, @RequestBody @Valid AvailabilityRequest req){
        Product p = productService.get(id);
        p.setIsAvailable(req.getAvailable());
        productRepo.save(p);
        return ApiResponse.ok();
    }

    @PostMapping("/products/{id}/stock")
    public ApiResponse<Void> stock(@PathVariable Integer id, @RequestBody Map<String,Integer> body){
        Integer stock = body.get("stock");
        productService.adjustStock(id, stock == null ? 0 : stock, null);
        return ApiResponse.ok();
    }

    @PostMapping("/products/stocks")
    public ApiResponse<Void> stocks(@RequestBody @Valid BulkStockRequest req){
        // BulkStockRequest.Item tiene getId() y getStock()
        for (BulkStockRequest.Item i : req.getItems()) {
            productService.adjustStock(i.getId(), i.getStock(), null);
        }
        return ApiResponse.ok();
    }

    @PostMapping("/products/bulk-update")
    public ApiResponse<Void> bulkUpdate(){
        // TODO: implementar cambios masivos adicionales si tu frontend lo requiere
        return ApiResponse.ok();
    }

    @GetMapping("/orders")
    public ApiResponse<Object> sellerOrders(Authentication authentication){
        User seller = getAuthenticatedUser(authentication);
        Map<String,Object> resp = new HashMap<String,Object>();
        resp.put("orders", orderRepo.findBySeller_Id(seller.getId()));
        return ApiResponse.ok(resp);
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<Object> sellerOrderDetail(@PathVariable Integer id){
        Map<String,Object> resp = new HashMap<String,Object>();
        resp.put("order", orderRepo.findById(id).orElse(null));
        return ApiResponse.ok(resp);
    }

    @PostMapping("/orders/{id}/status")
    public ApiResponse<Void> sellerChangeStatus(@PathVariable Integer id, @RequestBody @Valid ChangeStatusRequest req){
        orderService.changeStatus(id, req);
        return ApiResponse.ok();
    }

    @PostMapping("/orders/{id}/assign")
    public ApiResponse<Void> assign(@PathVariable Integer id, @RequestBody @Valid AssignSellerRequest req){
        orderService.assignSeller(id, req);
        return ApiResponse.ok();
    }
}
