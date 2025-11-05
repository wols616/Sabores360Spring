# API Routes — Resumen para vistas y formularios

Este documento lista las rutas existentes en la API y describe: ruta, método HTTP, qué hace, parámetros (path/query), cuerpo de petición (campos y validaciones) y forma de respuesta. Úsalo para crear formularios y conectar vistas con los endpoints.

Nota: la mayoría de respuestas están envueltas en `ApiResponse` (campo `data`/contenido directo según implementación). Algunos endpoints devuelven `ResponseEntity<byte[]>` (exports) o `Map` (auth/login/register).

---

## /api/auth

### POST /api/auth/login

- Qué hace: Autentica al usuario y devuelve token/info de sesión.
- Método: POST
- Request body (JSON): LoginRequest
  - email (string, required, formato email)
  - password (string, required)
- Response: Map con datos devueltos por `auth.login(...)` + `success: true` (ej. token, user info) — revisar implementación de `AuthService` para el contenido exacto.

### POST /api/auth/register

- Qué hace: Registra un usuario.
- Método: POST
- Request body (JSON): RegisterRequest
  - name (string, required)
  - email (string, required, email)
  - address (string, optional)
  - password (string, optional, minLength=8)
- Response: Map { success: true, userId: <int> }

### POST /api/auth/forgot-password

- Qué hace: Inicia flujo de recuperación (envía email/token).
- Método: POST
- Request body: ForgotPasswordRequest
  - email (string, required, email)
- Response: ApiResponse<Void> (ok)

### POST /api/auth/reset-password

- Qué hace: Resetea contraseña usando token.
- Método: POST
- Request body: ResetPasswordRequest
  - token (string, required)
  - password (string, minLength=8)
- Response: ApiResponse<Void>

### GET/POST /api/auth/logout

- Qué hace: Logout (placeholder; si usas JWT puede no invalidar tokens automáticamente).
- Método: GET o POST
- Request body: ninguno
- Response: ApiResponse<Void>

---

## /api/client (requiere rol CLIENTE)

### GET /api/client/products

- Qué hace: Lista productos disponibles para cliente (DTO simplificado).
- Método: GET
- Query params:
  - category (int, optional)
  - search (string, optional)
  - page (int, default=1)
- Response: ApiResponse { products: [ProductListDto] }
  - ProductListDto:
    - id (int)
    - name (string)
    - description (string)
    - price (decimal)
    - stock (int)
    - isAvailable (boolean)
    - categoryId (int|null)
    - categoryName (string|null)

### POST /api/client/orders

- Qué hace: Crea un pedido desde carrito.
- Método: POST
- Request body: PlaceOrderRequest
  - delivery_address (string, required)
  - payment_method (string, required, allowed: "Tarjeta"|"Efectivo")
  - cart (array, required, no vacío)
    - cart[].id (int, required) — product id
    - cart[].quantity (int, required, >0)
- Response: ApiResponse { order_id: <int> }

### GET /api/client/categories

- Qué hace: Lista todas las categorías (para mostrar en filtros y navegaciones).
- Método: GET
- Query params: ninguno
- Response: ApiResponse { categories: [Category entity] }

### GET /api/client/categories/{id}

- Qué hace: Devuelve detalle de una categoría específica (incluye fields como id, name, description y lista de productos si aplica en tu implementación).
- Método: GET
- Path param: id (int)
- Response: ApiResponse { category: Category entity }

### GET /api/client/products/full

- Qué hace: Lista todos los productos (entidades completas) visibles para el cliente, incluyendo campos como `imageUrl`.
- Método: GET
- Query params:
  - category (int, optional)
  - search (string, optional)
  - page (int, default=1)
- Response: ApiResponse { products: [Product entity] }
  - Product entity incluye: id, category, name, description, price, stock, imageUrl, isAvailable, createdAt, updatedAt

### POST /api/client/cart/details

- Qué hace: Devuelve los detalles completos de los productos que están en el carrito (útil para mostrar imagenes y precios en el carrito). Recibe una lista de product ids y devuelve sus datos.
- Método: POST
- Request body (JSON): { "ids": [1,2,3] } (ver `CartDetailsRequest`)
- Response: ApiResponse { products: [ ProductFullDto ] }
  - ProductFullDto: id, name, description, price, stock, isAvailable, imageUrl, categoryId, categoryName

### GET /api/client/orders

- Qué hace: Lista pedidos del cliente (paginado, devuelve resúmenes DTO).
- Método: GET
- Query params:
  - page (int, default=1)
  - per_page (int, default=20)
- Response: ApiResponse { orders: [OrderSummaryDto], pagination: { page, total_pages } }
  - OrderSummaryDto: id, status, totalAmount, createdAt

### GET /api/client/orders/{id}

- Qué hace: Detalle de pedido (DTO completo).
- Método: GET
- Path param: id (int)
- Response: ApiResponse { order: OrderDetailDto }
  - OrderDetailDto: id, status, totalAmount, deliveryAddress, paymentMethod, createdAt, items: [OrderItemDto]
  - OrderItemDto: productId, productName, quantity, unitPrice

### POST /api/client/orders/{id}/cancel

- Qué hace: Cancela el pedido (cliente).
- Método: POST
- Path param: id (int)
- Response: ApiResponse<Void>

### POST /api/client/orders/{id}/reorder

- Qué hace: Reordenar — crea nuevo pedido con ítems del anterior.
- Método: POST
- Path param: id (int)
- Response: ApiResponse { order_id: <int> }

### GET /api/client/profile/stats

- Qué hace: Estadísticas del perfil del cliente.
- Método: GET
- Response: ApiResponse { stats: { total_orders (int), total_spent (decimal), favorite_category (string|null) } }

### GET /api/client/orders/recent

- Qué hace: Últimos pedidos (resúmenes).
- Método: GET
- Response: ApiResponse { orders: [OrderSummaryDto] }

### GET /api/client/favorites

- Qué hace: Placeholder para favoritos.
- Método: GET
- Response: ApiResponse { products: [] }

### PUT /api/client/profile

- Qué hace: Placeholder para actualizar perfil.
- Método: PUT
- Request: TBD
- Response: ApiResponse<Void>

### POST /api/client/change-password

- Qué hace: Placeholder para cambiar contraseña.
- Método: POST
- Request: TBD
- Response: ApiResponse<Void>

---

## /api/seller (requiere rol VENDEDOR)

### GET /api/seller/dashboard

- Qué hace: Dashboard del vendedor — número de pedidos pendientes y últimas órdenes.
- Método: GET
- Response: ApiResponse { pending: <int>, recent_orders: [Order entity] }
  - Nota: recent_orders devuelve entidades `Order` completas; para vistas frontend usa los campos necesarios (id, status, totalAmount, createdAt, etc.)

### GET /api/seller/products/stock

- Qué hace: Lista productos (detalle de stock).
- Método: GET
- Response: ApiResponse { products: [Product entity] }

### GET /api/seller/products

- Qué hace: Lista productos generales (seller).
- Método: GET
- Response: ApiResponse { products: [Product entity] }

### GET /api/seller/categories

- Qué hace: Lista todas las categorías (para que el vendedor pueda filtrar/organizar su catálogo).
- Método: GET
- Query params: ninguno
- Response: ApiResponse { categories: [Category entity] }

### GET /api/seller/categories/{id}

- Qué hace: Devuelve detalle de una categoría específica (id, name, description, productos relacionados si aplica).
- Método: GET
- Path param: id (int)
- Response: ApiResponse { category: Category entity }

### POST /api/seller/products/{id}/availability

- Qué hace: Cambiar disponibilidad de producto.
- Método: POST
- Path param: id (int)
- Request body: AvailabilityRequest
  - available (boolean, required)
- Response: ApiResponse<Void>

### POST /api/seller/products/{id}/stock

- Qué hace: Ajustar stock de un producto (single).
- Método: POST
- Path param: id (int)
- Request body: JSON simple: { "stock": <int> } (map)
- Response: ApiResponse<Void>

### POST /api/seller/products/stocks

- Qué hace: Actualización masiva de stock.
- Método: POST
- Request body: BulkStockRequest
  - items (array, required)
    - items[].id (int, required)
    - items[].stock (int, required)
- Response: ApiResponse<Void>

### POST /api/seller/products/bulk-update

- Qué hace: Placeholder para actualizaciones masivas.
- Método: POST
- Response: ApiResponse<Void>

### GET /api/seller/orders

- Qué hace: Lista pedidos asignados al vendedor.
- Método: GET
- Response: ApiResponse { orders: [Order entity] }

### GET /api/seller/orders/{id}

- Qué hace: Detalle de pedido (entidad) para vendedor.
- Método: GET
- Path param: id (int)
- Response: ApiResponse { order: Order entity }

### POST /api/seller/orders/{id}/status

- Qué hace: Cambiar estado del pedido (vendedor).
- Método: POST
- Path param: id (int)
- Request body: ChangeStatusRequest
  - newStatus (string, required) — ejemplos: 'Confirmado','En preparación','En camino','Entregado','Cancelado'
  - notes (string, optional)
- Response: ApiResponse<Void>

### POST /api/seller/orders/{id}/assign

- Qué hace: Asignar vendedor a pedido.
- Método: POST
- Path param: id (int)
- Request body: AssignSellerRequest
  - sellerId (int, required)
- Response: ApiResponse<Void>

---

## /api/admin (requiere rol ADMINISTRADOR)

### GET /api/admin/dashboard

- Qué hace: Estadísticas generales (counts, low stock, recent orders).
- Método: GET
- Response: ApiResponse { orders_count, users_count, products_count, low_stock_count, recent_orders }

### GET /api/admin/orders/stats

- Qué hace: Conteo de pedidos por estado.
- Método: GET
- Response: ApiResponse { stats: { statusName: count, ... } }

### GET /api/admin/vendors

- Qué hace: Lista de vendedores (filtrado por rol).
- Método: GET
- Response: ApiResponse { vendors: [User entity] }

### GET /api/admin/orders

- Qué hace: Lista de pedidos con filtros y paginación.
- Método: GET
- Query params:
  - page (int)
  - per_page (int)
  - status (string, optional)
  - date_from (YYYY-MM-DD, optional)
  - date_to (YYYY-MM-DD, optional)
  - vendor_id (int, optional)
  - search (string, optional)
- Response: ApiResponse { orders: [Order entity], pagination: { page, total_pages } }

### GET /api/admin/orders/{id}

- Qué hace: Detalle de pedido.
- Método: GET
- Path param: id (int)
- Response: ApiResponse { order: Order entity }

### PUT /api/admin/orders/{id}/status

- Qué hace: Placeholder; el admin puede usar otro flujo.
- Método: PUT
- Response: ApiResponse<Void>

### GET /api/admin/orders/export

- Qué hace: Exporta pedidos a Excel (octet-stream).
- Método: GET
- Response: ResponseEntity<byte[]> con archivo `pedidos.xlsx`

### DELETE /api/admin/orders/{id}

- Qué hace: Elimina un pedido.
- Método: DELETE
- Path param: id (int)
- Response: ApiResponse<Void>

### GET /api/admin/products/stats

- Qué hace: Estadísticas de productos (totales, low stock, inactive).
- Método: GET
- Response: ApiResponse { total, low_stock_count, inactive }

### GET /api/admin/categories

- Qué hace: Lista categorías.
- Método: GET
- Response: ApiResponse { categories: [Category entity] }

### GET /api/admin/categories/{id}

- Qué hace: Devuelve detalle de una categoría por id.
- Método: GET
- Path param: id (int)
- Response: ApiResponse { category: Category entity }

### GET /api/admin/roles

- Qué hace: Lista todos los roles disponibles (útil para formularios de creación/edición de usuario).
- Método: GET
- Query params: ninguno
- Response: ApiResponse { roles: [Role entity] }

### GET /api/admin/roles/{id}

- Qué hace: Devuelve detalle de un rol por id.
- Método: GET
- Path param: id (int)
- Response: ApiResponse { role: Role entity }

### POST /api/admin/categories

- Qué hace: Crea una nueva categoría.
- Método: POST
- Request body (JSON): CreateCategoryRequest
  - name (string, required)
  - description (string, optional)
- Response: ApiResponse { id, name }

### PUT /api/admin/categories/{id}

- Qué hace: Actualiza una categoría existente (campos opcionales).
- Método: PUT
- Path param: id (int)
- Request body (JSON): UpdateCategoryRequest
  - name (string, optional)
  - description (string, optional)
- Response: ApiResponse { id, name }

### DELETE /api/admin/categories/{id}

- Qué hace: Elimina una categoría.
- Método: DELETE
- Path param: id (int)
- Response: ApiResponse<Void>

### GET /api/admin/products

- Qué hace: Lista de productos (admin) con filtros y paginación.
- Método: GET
- Query params:
  - page, per_page, category, search, low_stock (boolean)
- Response: ApiResponse { products: [Product entity], pagination }

### GET /api/admin/products/{id}

- Qué hace: Detalle de producto.
- Método: GET
- Path param: id
- Response: ApiResponse { product: Product entity }

### POST /api/admin/products

- Qué hace: Crear producto.
- Método: POST
- Request body: CreateProductRequest
  - name (string, required)
  - description (string, optional)
  - price (decimal, required, positive)
  - stock (int, required)
  - categoryId (int, required)
  - imageUrl (string, optional)
  - isAvailable (boolean, optional, default true)
- Response: ApiResponse { id, name }

### PUT /api/admin/products/{id}

- Qué hace: Actualizar producto.
- Método: PUT
- Path param: id
- Request body: UpdateProductRequest (campos opcionales)
  - name, description, price (positive), stock, categoryId, imageUrl, isAvailable
- Response: ApiResponse { id, name }

### POST /api/admin/products/{id}/toggle-status

- Qué hace: Alternar disponibilidad.
- Método: POST
- Path param: id
- Response: ApiResponse<Void>

### DELETE /api/admin/products/{id}

- Qué hace: Eliminar producto.
- Método: DELETE
- Path param: id
- Response: ApiResponse<Void>

### GET /api/admin/products/export

- Qué hace: Exporta productos a Excel.
- Método: GET
- Response: ResponseEntity<byte[]> con archivo `productos.xlsx`

### GET /api/admin/reports

- Qué hace: Genera reportes (ventas por día, por vendedor, top productos)
- Método: GET
- Query params:
  - date_from (YYYY-MM-DD)
  - date_to (YYYY-MM-DD)
- Response: ApiResponse { sales_by_day, sales_by_seller, top_products }

### GET /api/admin/reports/export

- Qué hace: Exporta reportes a Excel entre fechas.
- Método: GET
- Query params: date_from, date_to
- Response: ResponseEntity<byte[]> con archivo `reportes.xlsx`

### GET /api/admin/users

- Qué hace: Lista usuarios (paginado).
- Método: GET
- Query params: page, per_page
- Response: ApiResponse { users: [User entity], pagination }

### POST /api/admin/users

- Qué hace: Crear usuario.
- Método: POST
- Request body: CreateUserRequest
  - name (string, required)
  - email (string, required, email)
  - password (string, required)
  - address (string, optional)
  - roleId (int, required)
  - isActive (boolean, optional)
- Response: ApiResponse { id, name, email }

### PUT /api/admin/users/{id}

- Qué hace: Actualizar usuario.
- Método: PUT
- Request body: UpdateUserRequest (campos opcionales)
  - name, email (email), password, address, roleId, isActive
- Response: ApiResponse { id, name, email }

### POST /api/admin/users/{id}/status

- Qué hace: Cambiar estado activo/inactivo de usuario.
- Método: POST
- Path param: id
- Request body: simple map { status: string } — si status == "active" marca activo, si no, inactivo.
- Response: ApiResponse<Void>

### DELETE /api/admin/users/{id}

- Qué hace: Eliminar usuario.
- Método: DELETE
- Path param: id
- Response: ApiResponse<Void>

---

## Observaciones importantes para las vistas y formularios

- Autenticación/Autorización: endpoints están protegidos por roles (`@PreAuthorize`). Asegúrate de que el frontend incluya el token (o la cookie de sesión) y que muestres vistas/acciones según el rol.
- Validaciones: revisa las anotaciones `@NotNull`, `@NotBlank`, `@Size`, `@Pattern` en los DTOs para validar formularios en el frontend antes de enviar.
- Formatos de fecha: los endpoints que aceptan fechas esperan `YYYY-MM-DD` (ISO date) en query params.
- Exports: los endpoints `/export` devuelven archivos binarios; desde el frontend usa `fetch` con response.arrayBuffer() y descarga con blob.
- Respuestas: la mayoría devuelve un `ApiResponse` (estructura genérica). Para construir formularios y mostrar resultados fíjate en la sección `Response:` de cada ruta.

---

Si quieres, hago lo siguiente a continuación:

- 1. Genero automáticamente formularios HTML/JS (o componentes) para las rutas que me indiques (cliente/administrador/vendedor), o
- 2. Extraigo ejemplos de payloads JSON (ejemplos válidos) para cada endpoint para usar en Postman/Insomnia.

Dime cuál prefieres y lo genero (puedo crear archivos .html/.js o un conjunto de JSON examples en `docs/`).
