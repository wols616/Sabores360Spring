// src/main/java/com/example/GestionComida/web/controller/ClientController.java
package com.example.GestionComida.web.controller;

import com.example.GestionComida.domain.entity.Order;
import com.example.GestionComida.domain.entity.OrderItem;
import com.example.GestionComida.domain.entity.Product;
import com.example.GestionComida.domain.entity.User;
import com.example.GestionComida.repo.OrderItemRepository;
import com.example.GestionComida.repo.CategoryRepository;
import com.example.GestionComida.repo.OrderRepository;
import com.example.GestionComida.repo.ProductRepository;
import com.example.GestionComida.service.OrderService;
import com.example.GestionComida.web.ApiResponse;
import com.example.GestionComida.web.dto.ChangeStatusRequest;
import com.example.GestionComida.web.dto.CreateOrderRequest;
import com.example.GestionComida.web.dto.OrderDetailDto;
import com.example.GestionComida.web.dto.OrderItemDto;
import com.example.GestionComida.web.dto.OrderSummaryDto;
import com.example.GestionComida.web.dto.ProductListDto;
import com.example.GestionComida.web.dto.ProductFullDto;
import com.example.GestionComida.web.dto.client.PlaceOrderRequest;
import com.example.GestionComida.web.dto.client.CartDetailsRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENTE')")
public class ClientController {

    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;
    private final OrderItemRepository itemRepo;
        private final CategoryRepository categoryRepo;
    private final OrderService orderService;

    /**
     * Helper para obtener el usuario autenticado desde el contexto de seguridad
     */
    private User getAuthenticatedUser(Authentication auth) {
        return (User) auth.getPrincipal();
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

    // ---------------------------------------
    // Productos (lista para clientes) -> DTO
    // ---------------------------------------
    @GetMapping("/products")
    public ApiResponse<Map<String, Object>> products(
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

        List<ProductListDto> list = new ArrayList<>();
        for (Product p : filtered) {
            Integer catId = (p.getCategory() != null) ? p.getCategory().getId() : null;
            String catName = (p.getCategory() != null) ? p.getCategory().getName() : null;
            list.add(new ProductListDto(
                    p.getId(),
                    p.getName(),
                    p.getDescription(),
                    p.getPrice(),
                    p.getStock(),
                    p.getIsAvailable(),
                    catId,
                    catName
            ));
        }

        Map<String, Object> body = new HashMap<>();
        body.put("products", list);
        return ApiResponse.ok(body);
    }

    // ---------------------------------------
    // Productos (lista completa para cliente) -> ENTIDADES completas (incluye imageUrl)
    // ---------------------------------------
    @GetMapping("/products/full")
    public ApiResponse<Map<String, Object>> productsFull(
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

    // ---------------------------------------
    // Cart: obtener detalles de productos por ids (para mostrar imagenes en el carrito)
    // ---------------------------------------
    @PostMapping("/cart/details")
public ApiResponse<List<ProductFullDto>> cartDetails(@RequestBody @jakarta.validation.Valid CartDetailsRequest req) {
    List<Integer> ids = req.getIds();
    List<Product> products = productRepo.findAllById(ids);

    List<ProductFullDto> list = new ArrayList<>();
    for (Product p : products) {
        Integer catId = (p.getCategory() != null) ? p.getCategory().getId() : null;
        String catName = (p.getCategory() != null) ? p.getCategory().getName() : null;
        list.add(new ProductFullDto(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getStock(),
                p.getIsAvailable(),
                p.getImageUrl(),
                catId,
                catName
        ));
    }

    // Devuelve la lista directamente
    return ApiResponse.ok(list);
}


    // ---------------------------------------
    // Crear pedido desde carrito (cliente)
    // ---------------------------------------
    @PostMapping("/orders")
    public ApiResponse<Map<String, Object>> placeOrder(
            @RequestBody @Valid PlaceOrderRequest req,
            Authentication authentication
    ) {
        User client = getAuthenticatedUser(authentication);
        
        // Cart (PlaceOrderRequest) -> CreateOrderRequest
        List<CreateOrderRequest.Item> items = new ArrayList<>();
        for (PlaceOrderRequest.CartItem c : req.getCart()) {
            items.add(new CreateOrderRequest.Item(c.getId(), c.getQuantity()));
        }

        CreateOrderRequest createReq = new CreateOrderRequest(
                client.getId(),
                req.getDelivery_address(),
                req.getPayment_method(),
                items
        );

        Order o = orderService.createOrder(createReq);

        Map<String, Object> resp = new HashMap<>();
        resp.put("order_id", o.getId());
        return ApiResponse.ok(resp);
    }

    // ---------------------------------------
    // Listado de pedidos del cliente (paginado)
    // -> devuelve resúmenes DTO (no entidades)
    // ---------------------------------------
    @GetMapping("/orders")
    public ApiResponse<Map<String, Object>> myOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "per_page", defaultValue = "20") int perPage
    ) {
        User client = getAuthenticatedUser(authentication);
        List<Order> all = orderRepo.findByClient_Id(client.getId());

        int totalPages = (int) Math.ceil(all.size() / (double) perPage);
        int from = Math.max(0, (page - 1) * perPage);
        int to = Math.min(all.size(), from + perPage);
        List<Order> slice = from >= to ? new ArrayList<Order>() : all.subList(from, to);

        List<OrderSummaryDto> list = new ArrayList<>();
        for (Order o : slice) {
            list.add(new OrderSummaryDto(
                    o.getId(),
                    o.getStatus(),
                    o.getTotalAmount(),
                    o.getCreatedAt()
            ));
        }

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("total_pages", totalPages);

        Map<String, Object> resp = new HashMap<>();
        resp.put("orders", list);
        resp.put("pagination", pagination);
        return ApiResponse.ok(resp);
    }

    // ---------------------------------------
    // Detalle de un pedido -> DTO completo
    // ---------------------------------------
    @GetMapping("/orders/{id}")
    public ApiResponse<Map<String, Object>> orderDetail(@PathVariable Integer id) {
        Order o = orderRepo.findById(id).orElse(null);
        if (o == null) {
            Map<String, Object> r = new HashMap<>();
            r.put("order", null);
            return ApiResponse.ok(r);
        }

        List<OrderItem> entityItems = itemRepo.findByOrder_Id(id);

        List<OrderItemDto> items = new ArrayList<>();
        for (OrderItem it : entityItems) {
            items.add(new OrderItemDto(
                    it.getProduct().getId(),
                    it.getProduct().getName(),
                    it.getQuantity(),
                    it.getUnitPrice()
            ));
        }

        OrderDetailDto dto = new OrderDetailDto(
                o.getId(),
                o.getStatus(),
                o.getTotalAmount(),
                o.getDeliveryAddress(),
                o.getPaymentMethod(),
                o.getCreatedAt(),
                items
        );

        Map<String, Object> resp = new HashMap<>();
        resp.put("order", dto);
        return ApiResponse.ok(resp);
    }

    // ---------------------------------------
    // Cancelar pedido (cliente)
    // ---------------------------------------
    @PostMapping("/orders/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Integer id) {
        orderService.changeStatus(id, new ChangeStatusRequest("Cancelado", "Solicitud de cliente"));
        return ApiResponse.ok();
    }

    // ---------------------------------------
    // Reordenar: crear un nuevo pedido con
    // los mismos ítems que uno anterior
    // ---------------------------------------
    @PostMapping("/orders/{id}/reorder")
    public ApiResponse<Map<String, Object>> reorder(@PathVariable Integer id) {
        Order old = orderRepo.findById(id).orElse(null);
        if (old == null) {
            Map<String, Object> resp0 = new HashMap<>();
            resp0.put("order_id", 0);
            return ApiResponse.ok(resp0);
        }

        List<OrderItem> oldItems = itemRepo.findByOrder_Id(old.getId());
        List<CreateOrderRequest.Item> items = new ArrayList<>();
        for (OrderItem oi : oldItems) {
            items.add(new CreateOrderRequest.Item(oi.getProduct().getId(), oi.getQuantity()));
        }

        CreateOrderRequest req = new CreateOrderRequest(
                old.getClient().getId(),
                old.getDeliveryAddress(),
                old.getPaymentMethod(),
                items
        );

        Order n = orderService.createOrder(req);

        Map<String, Object> resp = new HashMap<>();
        resp.put("order_id", n.getId());
        return ApiResponse.ok(resp);
    }

    // ---------------------------------------
    // Stats del perfil del cliente
    // ---------------------------------------
    @GetMapping("/profile/stats")
    public ApiResponse<Map<String, Object>> profileStats(Authentication authentication) {
        User client = getAuthenticatedUser(authentication);
        List<Order> orders = orderRepo.findByClient_Id(client.getId());

        int totalOrders = orders.size();
        BigDecimal spent = BigDecimal.ZERO;
        for (Order o : orders) {
            if (o.getTotalAmount() != null) {
                spent = spent.add(o.getTotalAmount());
            }
        }

        // Categoría favorita por cantidad total pedida
        Map<String, Integer> qtyByCategory = new HashMap<>();
        for (Order o : orders) {
            List<OrderItem> its = itemRepo.findByOrder_Id(o.getId());
            for (OrderItem oi : its) {
                String catName = (oi.getProduct() != null && oi.getProduct().getCategory() != null)
                        ? oi.getProduct().getCategory().getName()
                        : null;
                if (catName != null) {
                    Integer current = qtyByCategory.get(catName);
                    qtyByCategory.put(catName, (current == null ? 0 : current) + oi.getQuantity());
                }
            }
        }

        String favoriteCategory = null;
        int max = -1;
        for (Map.Entry<String, Integer> e : qtyByCategory.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                favoriteCategory = e.getKey();
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("total_orders", totalOrders);
        stats.put("total_spent", spent);
        stats.put("favorite_category", favoriteCategory);

        Map<String, Object> resp = new HashMap<>();
        resp.put("stats", stats);
        return ApiResponse.ok(resp);
    }

    // ---------------------------------------
    // Últimos pedidos (resúmenes DTO)
    // ---------------------------------------
    @GetMapping("/orders/recent")
    public ApiResponse<Map<String, Object>> recent(Authentication authentication) {
        User client = getAuthenticatedUser(authentication);
        List<Order> list = orderRepo.findByClient_Id(client.getId());
        // ordenar por createdAt desc
        Collections.sort(list, new Comparator<Order>() {
            @Override
            public int compare(Order a, Order b) {
                if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            }
        });

        List<Order> top = list.size() > 5 ? list.subList(0, 5) : list;

        List<OrderSummaryDto> dtos = new ArrayList<>();
        for (Order o : top) {
            dtos.add(new OrderSummaryDto(
                    o.getId(),
                    o.getStatus(),
                    o.getTotalAmount(),
                    o.getCreatedAt()
            ));
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("orders", dtos);
        return ApiResponse.ok(resp);
    }

    // ---------------------------------------
    // Favoritos (placeholder)
    // ---------------------------------------
    @GetMapping("/favorites")
    public ApiResponse<Map<String, Object>> favorites() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("products", new ArrayList<Object>()); // TODO: implementar favoritos
        return ApiResponse.ok(resp);
    }

    // ---------------------------------------
    // Placeholders de perfil
    // ---------------------------------------
    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile() {
        return ApiResponse.ok(); // TODO
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword() {
        return ApiResponse.ok(); // TODO
    }
}
