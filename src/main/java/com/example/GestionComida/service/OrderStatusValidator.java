// src/main/java/com/example/GestionComida/service/OrderStatusValidator.java
package com.example.GestionComida.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class OrderStatusValidator {
    private OrderStatusValidator(){}

    public static final Set<String> VALID_STATUSES;
    public static final Map<String, Set<String>> ALLOWED_TRANSITIONS;

    static {
        // Estados válidos
        Set<String> vs = new HashSet<String>();
        vs.add("Pendiente");
        vs.add("Confirmado");
        vs.add("En preparación");
        vs.add("En camino");
        vs.add("Entregado");
        vs.add("Cancelado");
        VALID_STATUSES = Collections.unmodifiableSet(vs);

        // Transiciones permitidas
        Map<String, Set<String>> map = new HashMap<String, Set<String>>();
        map.put("Pendiente", unmodifiableSet("Confirmado", "Cancelado"));
        map.put("Confirmado", unmodifiableSet("En preparación", "Cancelado"));
        map.put("En preparación", unmodifiableSet("En camino", "Cancelado"));
        map.put("En camino", unmodifiableSet("Entregado", "Cancelado"));
        map.put("Entregado", Collections.<String>emptySet());
        map.put("Cancelado", Collections.<String>emptySet());
        ALLOWED_TRANSITIONS = Collections.unmodifiableMap(map);
    }

    private static Set<String> unmodifiableSet(String... items){
        Set<String> s = new HashSet<String>();
        for (String it : items) s.add(it);
        return Collections.unmodifiableSet(s);
    }

    public static boolean canTransition(String from, String to){
        Set<String> next = ALLOWED_TRANSITIONS.containsKey(from)
                ? ALLOWED_TRANSITIONS.get(from)
                : Collections.<String>emptySet();
        return next.contains(to);
    }

    public static void ensureValid(String status){
        if (!VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Estado inválido: " + status);
        }
    }
}
