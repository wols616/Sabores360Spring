package com.example.GestionComida.web.controller;

import com.example.GestionComida.domain.entity.Order;
import com.example.GestionComida.domain.entity.OrderItem;
import com.example.GestionComida.domain.entity.User;
import com.example.GestionComida.repo.OrderItemRepository;
import com.example.GestionComida.repo.OrderRepository;
import com.example.GestionComida.repo.ProductRepository;
import com.example.GestionComida.repo.UserRepository;
import com.example.GestionComida.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PublicController {

    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;
    private final OrderItemRepository itemRepo;
    private final UserRepository userRepo;

    /**
     * Cuenta total de productos activos (isAvailable = true).
     * Accesible públicamente (no requiere rol específico).
     */
    @GetMapping("/products/active-count")
    public ApiResponse<Map<String, Object>> activeProductsCount() {
        long count = productRepo.countByIsAvailableTrue();
        Map<String, Object> resp = new HashMap<>();
        resp.put("active_count", count);
        return ApiResponse.ok(resp);
    }

    /**
     * Detalle extendido de un pedido por id.
     * Devuelve: datos del pedido, cliente (id, name, address), total, y lista de items con cantidad, precio unitario y total por item.
     */
    @GetMapping("/orders/{id}/details")
    public ApiResponse<Map<String, Object>> orderDetails(@PathVariable Integer id) {
        Order o = orderRepo.findById(id).orElse(null);
        Map<String, Object> resp = new HashMap<>();
        if (o == null) {
            resp.put("order", null);
            return ApiResponse.ok(resp);
        }

        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("id", o.getId());
        orderMap.put("status", o.getStatus());
        orderMap.put("totalAmount", o.getTotalAmount());
        orderMap.put("deliveryAddress", o.getDeliveryAddress());
        orderMap.put("paymentMethod", o.getPaymentMethod());
        orderMap.put("createdAt", o.getCreatedAt());

        // cliente
        User client = o.getClient();
        if (client != null) {
            Map<String, Object> clientMap = new HashMap<>();
            clientMap.put("id", client.getId());
            clientMap.put("name", client.getName());
            clientMap.put("email", client.getEmail());
            clientMap.put("address", client.getAddress());
            orderMap.put("client", clientMap);
        } else {
            orderMap.put("client", null);
        }

        List<OrderItem> items = itemRepo.findByOrder_Id(o.getId());
        List<Map<String, Object>> itemList = new ArrayList<>();
        for (OrderItem it : items) {
            Map<String, Object> im = new HashMap<>();
            if (it.getProduct() != null) {
                im.put("productId", it.getProduct().getId());
                im.put("productName", it.getProduct().getName());
            } else {
                im.put("productId", null);
                im.put("productName", null);
            }
            im.put("quantity", it.getQuantity());
            im.put("unitPrice", it.getUnitPrice());
            BigDecimal total = it.getUnitPrice() == null ? BigDecimal.ZERO : it.getUnitPrice().multiply(BigDecimal.valueOf(it.getQuantity()));
            im.put("total", total);
            itemList.add(im);
        }

        orderMap.put("items", itemList);
        resp.put("order", orderMap);
        return ApiResponse.ok(resp);
    }
}
