// src/main/java/com/example/GestionComida/web/controller/AdminController.java
package com.example.GestionComida.web.controller;

import com.example.GestionComida.domain.entity.Category;
import com.example.GestionComida.error.NotFoundException;
import com.example.GestionComida.domain.entity.Order;
import com.example.GestionComida.domain.entity.Product;
import com.example.GestionComida.domain.entity.Role;
import com.example.GestionComida.domain.entity.User;
import com.example.GestionComida.repo.CategoryRepository;
import com.example.GestionComida.repo.OrderRepository;
import com.example.GestionComida.repo.ProductRepository;
import com.example.GestionComida.repo.RoleRepository;
import com.example.GestionComida.repo.UserRepository;
import com.example.GestionComida.service.ExportService;
import com.example.GestionComida.service.ProductService;
import com.example.GestionComida.service.ReportService;
import com.example.GestionComida.service.UserService;
import com.example.GestionComida.web.ApiResponse;
import com.example.GestionComida.web.dto.admin.CreateProductRequest;
import com.example.GestionComida.web.dto.admin.CreateUserRequest;
import com.example.GestionComida.web.dto.admin.UpdateProductRequest;
import com.example.GestionComida.web.dto.admin.UpdateUserRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminController {
    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;
    private final RoleRepository roleRepo;
    private final ReportService reports;
    private final ProductService productService;
    private final UserService userService;
    private final ExportService exportService;

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard() {
        long orders = orderRepo.count();
        long users = userRepo.count();
        long products = productRepo.count();
        long lowStock = 0L;
        List<Product> allProducts = productRepo.findAll();
        for (Product p : allProducts) {
            if (p.getStock() != null && p.getStock() <= 5) lowStock++;
        }

        List<Order> recent = orderRepo.findAll().stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .limit(10)
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("orders_count", orders);
        data.put("users_count", users);
        data.put("products_count", products);
        data.put("low_stock_count", lowStock);
        data.put("recent_orders", recent);

        return ApiResponse.ok(data);
    }

    @GetMapping("/orders/stats")
    public ApiResponse<Map<String, Object>> orderStats() {
        Map<String, Long> stats = orderRepo.findAll().stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));

        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("stats", stats);
        return ApiResponse.ok(resp);
    }

    @GetMapping("/vendors")
    public ApiResponse<Map<String, Object>> vendors() {
        List<User> list = userRepo.findAll().stream()
                .filter(u -> u.getRole() != null && u.getRole().getName() != null
                        && u.getRole().getName().equalsIgnoreCase("Vendedor"))
                .collect(Collectors.toList());

        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("vendors", list);
        return ApiResponse.ok(resp);
    }

    @GetMapping("/orders")
    public ApiResponse<Map<String, Object>> listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "per_page", defaultValue = "20") int perPage,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date_from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date_to,
            @RequestParam(required = false, name = "vendor_id") Integer vendorId,
            @RequestParam(required = false) String search
    ) {
        List<Order> all = orderRepo.findAll().stream()
                .filter(o -> status == null || (o.getStatus() != null && o.getStatus().equalsIgnoreCase(status)))
                .filter(o -> vendorId == null || (o.getSeller() != null && Objects.equals(o.getSeller().getId(), vendorId)))
                .filter(o -> date_from == null || !o.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate().isBefore(date_from))
                .filter(o -> date_to == null || !o.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate().isAfter(date_to))
                .filter(o -> {
                    if (search == null) return true;
                    boolean byId = String.valueOf(o.getId()).contains(search);
                    boolean byClient = o.getClient() != null && o.getClient().getName() != null
                            && o.getClient().getName().toLowerCase().contains(search.toLowerCase());
                    return byId || byClient;
                })
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil(all.size() / (double) perPage);
        int from = Math.max(0, (page - 1) * perPage);
        int to = Math.min(all.size(), from + perPage);
        List<Order> slice = from >= to ? new ArrayList<Order>() : all.subList(from, to);

        Map<String, Object> pagination = new HashMap<String, Object>();
        pagination.put("page", page);
        pagination.put("total_pages", totalPages);

        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("orders", slice);
        resp.put("pagination", pagination);
        return ApiResponse.ok(resp);
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<Map<String, Object>> orderDetail(@PathVariable Integer id) {
        Order o = orderRepo.findById(id).orElse(null);

        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("order", o);
        return ApiResponse.ok(resp);
    }

    @PutMapping("/orders/{id}/status")
    public ApiResponse<Void> updateStatus() {
        // El frontend admin usa otro flujo; puedes reusar /seller/orders/{id}/status
        return ApiResponse.ok();
    }

    @GetMapping("/orders/export")
    public ResponseEntity<byte[]> exportOrders() {
        try {
            byte[] excelData = exportService.exportOrdersToExcel();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "pedidos.xlsx");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/orders/{id}")
    public ApiResponse<Void> deleteOrder(@PathVariable Integer id) {
        orderRepo.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/products/stats")
    public ApiResponse<Map<String, Object>> productStats() {
        List<Product> all = productRepo.findAll();
        long low = 0L;
        long inactive = 0L;
        for (Product p : all) {
            if (p.getStock() != null && p.getStock() <= 5) low++;
            if (!Boolean.TRUE.equals(p.getIsAvailable())) inactive++;
        }

        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("total", all.size());
        resp.put("low_stock_count", low);
        resp.put("inactive", inactive);
        return ApiResponse.ok(resp);
    }

    @GetMapping("/categories")
    public ApiResponse<Map<String, Object>> categories() {
        List<Category> categories = categoryRepo.findAll();
        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("categories", categories);
        return ApiResponse.ok(resp);
    }

    @GetMapping("/categories/{id}")
    public ApiResponse<Map<String, Object>> categoryDetail(@PathVariable Integer id) {
        Category c = categoryRepo.findById(id).orElse(null);
        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("category", c);
        return ApiResponse.ok(resp);
    }

    @PostMapping("/categories")
    public ApiResponse<Map<String, Object>> createCategory(@RequestBody @jakarta.validation.Valid com.example.GestionComida.web.dto.admin.CreateCategoryRequest req) {
        Category c = Category.builder()
                .name(req.getName())
                .description(req.getDescription())
                .build();
        Category saved = categoryRepo.save(c);
        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("id", saved.getId());
        resp.put("name", saved.getName());
        return ApiResponse.ok(resp);
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<Map<String, Object>> updateCategory(@PathVariable Integer id, @RequestBody @jakarta.validation.Valid com.example.GestionComida.web.dto.admin.UpdateCategoryRequest req) {
        Category c = categoryRepo.findById(id).orElseThrow(() -> new NotFoundException("category_not_found"));
        if (req.getName() != null) c.setName(req.getName());
        if (req.getDescription() != null) c.setDescription(req.getDescription());
        Category saved = categoryRepo.save(c);
        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("id", saved.getId());
        resp.put("name", saved.getName());
        return ApiResponse.ok(resp);
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Integer id) {
        categoryRepo.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/roles")
    public ApiResponse<Map<String, Object>> roles() {
        List<Role> list = roleRepo.findAll();
        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("roles", list);
        return ApiResponse.ok(resp);
    }

    @GetMapping("/roles/{id}")
    public ApiResponse<Map<String, Object>> roleDetail(@PathVariable Integer id) {
        Role r = roleRepo.findById(id).orElse(null);
        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("role", r);
        return ApiResponse.ok(resp);
    }

    @GetMapping("/products")
    public ApiResponse<Map<String, Object>> adminProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "per_page", defaultValue = "20") int perPage,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, name = "low_stock") Boolean lowStock
    ) {
        List<Product> all = productRepo.findAll().stream()
                .filter(p -> category == null || (p.getCategory() != null && Objects.equals(p.getCategory().getId(), category)))
                .filter(p -> search == null || (p.getName() != null && p.getName().toLowerCase().contains(search.toLowerCase())))
                .filter(p -> lowStock == null || (p.getStock() != null && p.getStock() <= 5))
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil(all.size() / (double) perPage);
        int from = Math.max(0, (page - 1) * perPage);
        int to = Math.min(all.size(), from + perPage);
        List<Product> slice = from >= to ? new ArrayList<Product>() : all.subList(from, to);

        Map<String, Object> pagination = new HashMap<String, Object>();
        pagination.put("page", page);
        pagination.put("total_pages", totalPages);

        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("products", slice);
        resp.put("pagination", pagination);
        return ApiResponse.ok(resp);
    }

    @GetMapping("/products/{id}")
    public ApiResponse<Map<String, Object>> productDetail(@PathVariable Integer id) {
        Product p = productRepo.findById(id).orElse(null);
        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("product", p);
        return ApiResponse.ok(resp);
    }

    @PostMapping("/products")
    public ApiResponse<Map<String, Object>> createProduct(@RequestBody @Valid CreateProductRequest req) {
        Product product = productService.createProduct(req);
        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("id", product.getId());
        resp.put("name", product.getName());
        return ApiResponse.ok(resp);
    }

    @PutMapping("/products/{id}")
    public ApiResponse<Map<String, Object>> updateProduct(@PathVariable Integer id, @RequestBody @Valid UpdateProductRequest req) {
        Product product = productService.updateProduct(id, req);
        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("id", product.getId());
        resp.put("name", product.getName());
        return ApiResponse.ok(resp);
    }

    @PostMapping("/products/{id}/toggle-status")
    public ApiResponse<Void> toggleProduct(@PathVariable Integer id) {
        productService.toggleProductAvailability(id);
        return ApiResponse.ok();
    }

    @DeleteMapping("/products/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable Integer id) {
        productRepo.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/products/export")
    public ResponseEntity<byte[]> exportProducts() {
        try {
            byte[] excelData = exportService.exportProductsToExcel();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "productos.xlsx");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/reports")
    public ApiResponse<Map<String, Object>> reports(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date_from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date_to
    ) {
        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("sales_by_day", reports.ventasPorDia(date_from, date_to));
        resp.put("sales_by_seller", reports.ventasPorVendedor(date_from, date_to));
        resp.put("top_products", reports.topProductos(date_from, date_to, 10));
        return ApiResponse.ok(resp);
    }

    @GetMapping("/reports/export")
    public ResponseEntity<byte[]> exportReports(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date_from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date_to
    ) {
        try {
            byte[] excelData = exportService.exportReportsToExcel(date_from, date_to);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "reportes.xlsx");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/users")
    public ApiResponse<Map<String, Object>> users(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "per_page", defaultValue = "20") int perPage
    ) {
        List<User> all = userRepo.findAll();
        int totalPages = (int) Math.ceil(all.size() / (double) perPage);
        int from = Math.max(0, (page - 1) * perPage);
        int to = Math.min(all.size(), from + perPage);
        List<User> slice = from >= to ? new ArrayList<User>() : all.subList(from, to);

        Map<String, Object> pagination = new HashMap<String, Object>();
        pagination.put("page", page);
        pagination.put("total_pages", totalPages);

        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("users", slice);
        resp.put("pagination", pagination);
        return ApiResponse.ok(resp);
    }

    @PostMapping("/users")
    public ApiResponse<Map<String, Object>> createUser(@RequestBody @Valid CreateUserRequest req) {
        User user = userService.createUser(req);
        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("id", user.getId());
        resp.put("name", user.getName());
        resp.put("email", user.getEmail());
        return ApiResponse.ok(resp);
    }

    @PutMapping("/users/{id}")
    public ApiResponse<Map<String, Object>> updateUser(@PathVariable Integer id, @RequestBody @Valid UpdateUserRequest req) {
        User user = userService.updateUser(id, req);
        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("id", user.getId());
        resp.put("name", user.getName());
        resp.put("email", user.getEmail());
        return ApiResponse.ok(resp);
    }

    @PostMapping("/users/{id}/status")
    public ApiResponse<Void> toggleUserStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        User u = userRepo.findById(id).orElse(null);
        if (u != null) {
            String status = body.get("status");
            boolean active = status != null && "active".equalsIgnoreCase(status);
            u.setIsActive(active);
            userRepo.save(u);
        }
        return ApiResponse.ok();
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Integer id) {
        userRepo.deleteById(id);
        return ApiResponse.ok();
    }
}
