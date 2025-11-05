// src/main/java/com/example/GestionComida/service/OrderService.java
package com.example.GestionComida.service;

import com.example.GestionComida.domain.entity.*;
import com.example.GestionComida.error.BadRequestException;
import com.example.GestionComida.error.NotFoundException;
import com.example.GestionComida.repo.*;
import com.example.GestionComida.web.dto.AssignSellerRequest;
import com.example.GestionComida.web.dto.ChangeStatusRequest;
import com.example.GestionComida.web.dto.CreateOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.GestionComida.service.OrderStatusValidator.canTransition;
import static com.example.GestionComida.service.OrderStatusValidator.ensureValid;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepo;
    private final OrderItemRepository itemRepo;
    private final OrderStatusHistoryRepository historyRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;

    // Estados desde los que, si se cancela, se retorna stock
    private static final Set<String> REFUNDABLE_STATUSES =
            new HashSet<String>(Arrays.asList("Pendiente", "Confirmado", "En preparación"));

    @Transactional
    public Order createOrder(CreateOrderRequest req){
        User client = userRepo.findById(req.getClientId())
                .orElseThrow(() -> new NotFoundException("Cliente no existe"));
        if (req.getItems() == null || req.getItems().isEmpty())
            throw new BadRequestException("El pedido debe tener ítems");

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<OrderItem>();

        for (CreateOrderRequest.Item it : req.getItems()){
            Product p = productRepo.findById(it.getProductId())
                    .orElseThrow(() -> new NotFoundException("Producto no existe: " + it.getProductId()));
            if (!Boolean.TRUE.equals(p.getIsAvailable()) || p.getStock() < it.getQuantity()){
                throw new BadRequestException("Sin stock/disponible: " + p.getName());
            }

            BigDecimal line = p.getPrice().multiply(BigDecimal.valueOf(it.getQuantity()));
            total = total.add(line);

            // descontar stock
            p.setStock(p.getStock() - it.getQuantity());
            if (p.getStock() == 0) p.setIsAvailable(false);
            productRepo.save(p);

            items.add(OrderItem.builder()
                    .product(p)
                    .quantity(it.getQuantity())
                    .unitPrice(p.getPrice())
                    .build());
        }

        Order order = Order.builder()
                .client(client)
                .seller(null)
                .deliveryAddress(req.getDeliveryAddress())
                .paymentMethod(req.getPaymentMethod())
                .status("Pendiente")
                .totalAmount(total)
                .build();

        order = orderRepo.save(order);
        for (OrderItem oi : items) oi.setOrder(order);
        itemRepo.saveAll(items);

        historyRepo.save(OrderStatusHistory.builder()
                .order(order).status("Pendiente").notes("Creación de pedido").build());

        return order;
    }

    @Transactional
    public Order changeStatus(Integer orderId, ChangeStatusRequest req){
        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado"));

        String from = o.getStatus();
        String to = req.getNewStatus();

        ensureValid(to);
        if (!canTransition(from, to)) {
            throw new BadRequestException("Transición no permitida: " + from + " -> " + to);
        }

        o.setStatus(to);
        o = orderRepo.save(o);

        historyRepo.save(OrderStatusHistory.builder()
                .order(o).status(to).notes(req.getNotes()).build());

        // Si se cancela desde estados tempranos, reponer stock de cada ítem
        if ("Cancelado".equals(to) && REFUNDABLE_STATUSES.contains(from)) {
            List<OrderItem> itemsOfOrder = itemRepo.findByOrder_Id(o.getId());
            for (OrderItem it : itemsOfOrder) {
                Product p = it.getProduct();
                p.setStock(p.getStock() + it.getQuantity());
                p.setIsAvailable(true);
                productRepo.save(p);
            }
        }
        return o;
    }

    @Transactional
    public Order assignSeller(Integer orderId, AssignSellerRequest req){
        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado"));
        User seller = userRepo.findById(req.getSellerId())
                .orElseThrow(() -> new NotFoundException("Vendedor no existe"));
        o.setSeller(seller);
        return orderRepo.save(o);
    }
}
