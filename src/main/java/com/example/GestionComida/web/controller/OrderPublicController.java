package com.example.GestionComida.web.controller;

import com.example.GestionComida.domain.entity.Order;
import com.example.GestionComida.domain.entity.OrderItem;
import com.example.GestionComida.domain.entity.User;
import com.example.GestionComida.repo.OrderItemRepository;
import com.example.GestionComida.repo.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderPublicController {

    private final OrderRepository orderRepo;
    private final OrderItemRepository itemRepo;

    /**
     * GET /api/orders/{id}/details
     * Devuelve detalle público de un pedido (items y cliente limitado).
     * Response: { success: true, order: { id, status, totalAmount, deliveryAddress, paymentMethod, createdAt, client:{id,name,email,address}, items:[{productId,productName,quantity,unitPrice,total}] } | null }
     */
    @GetMapping("/orders/{id}/details")
    public Map<String, Object> orderDetailsPublic(@PathVariable Integer id) {
        Order o = orderRepo.findById(id).orElse(null);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);

        if (o == null) {
            resp.put("order", null);
            return resp;
        }

        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("id", o.getId());
        orderMap.put("status", o.getStatus());
        orderMap.put("totalAmount", o.getTotalAmount());
        orderMap.put("deliveryAddress", o.getDeliveryAddress());
        orderMap.put("paymentMethod", o.getPaymentMethod());
        orderMap.put("createdAt", o.getCreatedAt());

        User c = o.getClient();
        if (c != null) {
            Map<String, Object> client = new HashMap<>();
            client.put("id", c.getId());
            client.put("name", c.getName());
            client.put("email", c.getEmail());
            client.put("address", c.getAddress());
            orderMap.put("client", client);
        } else {
            orderMap.put("client", null);
        }

        List<OrderItem> items = itemRepo.findByOrder_Id(o.getId());
        List<Map<String, Object>> itemsList = new ArrayList<>();
        for (OrderItem it : items) {
            Map<String, Object> itMap = new HashMap<>();
            itMap.put("productId", it.getProduct().getId());
            itMap.put("productName", it.getProduct().getName());
            itMap.put("quantity", it.getQuantity());
            itMap.put("unitPrice", it.getUnitPrice());
            // total = unitPrice * quantity
            BigDecimal total = it.getUnitPrice() == null ? BigDecimal.ZERO : it.getUnitPrice().multiply(new BigDecimal(it.getQuantity()));
            itMap.put("total", total);
            itemsList.add(itMap);
        }

        orderMap.put("items", itemsList);

        resp.put("order", orderMap);
        return resp;
    }
}

