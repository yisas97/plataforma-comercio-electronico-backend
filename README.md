# Inventory Management System - Backend
Este proyecto es un backend RESTful para un sistema de gestión de inventario desarrollado con Spring Boot 17. Proporciona endpoints para operaciones CRUD (Crear, Leer, Actualizar, Eliminar) para gestionar productos en un inventario.
### Tecnologías Utilizadas

Java 17
Spring Boot 3.2.0
Spring Data JPA
H2 Database (base de datos embebida en memoria)
Maven
Lombok

### Características

Operaciones CRUD completas para productos
Base de datos H2 embebida
Validación de datos de entrada
Manejo de errores
Configuración CORS para integración con frontend

### Estructura del Proyecto
```
src/main/java/com/inventory/app/
├── config/
│   └── CorsConfig.java
├── controller/
│   └── ProductController.java
├── model/
│   └── Product.java
├── repository/
│   └── ProductRepository.java
├── service/
│   └── ProductService.java
└── InventoryApplication.java
```
### Prerequisitos

Java 17 o superior
Maven 3.6 o superior

### Cómo Iniciar la Aplicación

#### Clonar el repositorio

```
git clone https://github.com/tu-usuario/inventory-backend.git
cd inventory-backend
```

#### Compilar el proyecto

```
mvn clean install
```

#### Ejecutar la aplicación

```
mvn spring-boot:run
```

La aplicación se iniciará en http://localhost:8080

Acceder a la consola H2 (opcional)

Para ver la base de datos en el navegador, visita:
Copiarhttp://localhost:8080/h2-console
Datos de conexión:

JDBC URL: jdbc:h2:mem:inventorydb
Username: sa
Password: password

### Endpoints API
#### Obtener todos los productos
```
GET /api/products
```

#### Obtener un producto por ID

```
GET /api/products/{id}
```

#### Buscar productos por nombre
```
GET /api/products/search?name={nombre}
```

#### Filtrar productos por categoría
```
GET /api/products/category/{categoria}
```

#### Crear un nuevo producto
```
POST /api/products

Content-Type: application/json

{
"name": "Nombre del producto",
"price": 99.99,
"description": "Descripción del producto",
"quantity": 100,
"category": "Categoría",
"sku": "CODIGO-PROD"
}
```

#### Actualizar un producto existente
```
PUT /api/products/{id}
Content-Type: application/json

{
"name": "Nombre actualizado",
"price": 89.99,
"description": "Descripción actualizada",
"quantity": 50,
"category": "Categoría nueva",
"sku": "CODIGO-NUEVO"
}
```
#### Eliminar un producto por ID
```
DELETE /api/products/{id}
```

### Ejemplos de Datos para Pruebas
Aquí hay algunos ejemplos de JSON que puedes usar para probar la API con herramientas como Postman o cURL:
#### Ejemplo 1: Smartphone

```json
{
  "name": "Smartphone Galaxy S23",
  "price": 899.99,
  "description": "Último modelo de smartphone con cámara de 108MP y 256GB de almacenamiento",
  "quantity": 50,
  "category": "Electrónica",
  "sku": "SMGS23-BLK-256"
}
```

#### Ejemplo 2: Laptop
```json
{
"name": "MacBook Pro 14",
"price": 1999.99,
"description": "Laptop con chip M2 Pro, 16GB RAM y 512GB SSD",
"quantity": 25,
"category": "Computadoras",
"sku": "MBP14-M2-512"
}
```

### Script cURL para Cargar Datos de Prueba
Aquí hay un script que puedes usar para cargar rápidamente los datos de prueba:


```bash
#!/bin/bash

# URL base de la API
API_URL="http://localhost:8080/api/products"

# Crear productos de ejemplo
echo "Creando productos de ejemplo..."

# Producto 1
curl -X POST $API_URL \
-H "Content-Type: application/json" \
-d '{
"name": "Smartphone Galaxy S23",
"price": 899.99,
"description": "Último modelo de smartphone con cámara de 108MP y 256GB de almacenamiento",
"quantity": 50,
"category": "Electrónica",
"sku": "SMGS23-BLK-256"
}'
echo -e "\n"

# Producto 2
curl -X POST $API_URL \
-H "Content-Type: application/json" \
-d '{
"name": "MacBook Pro 14",
"price": 1999.99,
"description": "Laptop con chip M2 Pro, 16GB RAM y 512GB SSD",
"quantity": 25,
"category": "Computadoras",
"sku": "MBP14-M2-512"
}'
echo -e "\n"

# Producto 3
curl -X POST $API_URL \
-H "Content-Type: application/json" \
-d '{
"name": "Café Premium Orgánico",
"price": 12.50,
"description": "Café de especialidad, tostado medio, origen Colombia, 500g",
"quantity": 100,
"category": "Alimentación",
"sku": "CAF-COL-500G"
}'
echo -e "\n"

# Producto 4
curl -X POST $API_URL \
-H "Content-Type: application/json" \
-d '{
"name": "Escritorio de Roble",
"price": 349.99,
"description": "Escritorio de madera de roble con cajones y soporte para monitor",
"quantity": 15,
"category": "Muebles",
"sku": "ESC-ROB-150"
}'
echo -e "\n"

# Producto 5
curl -X POST $API_URL \
-H "Content-Type: application/json" \
-d '{
"name": "Chaqueta Impermeable",
"price": 79.99,
"description": "Chaqueta ligera impermeable con capucha, talla M",
"quantity": 35,
"category": "Ropa",
"sku": "CHAQ-IMP-M-AZL"
}'
echo -e "\n"

echo "Datos de prueba cargados correctamente."

```
Guarda este script como load-test-data.sh, dale permisos de ejecución (chmod +x load-test-data.sh) y ejecútalo después de iniciar la aplicación.
Esto solo para ejecutar prueba en local, no se recomienda para producción.


Integración con Frontend
Este backend está diseñado para trabajar con un frontend desarrollado en Astro utilizando TypeScript. La configuración CORS ya está configurada para permitir solicitudes desde http://localhost:4321 (puerto predeterminado de Astro).