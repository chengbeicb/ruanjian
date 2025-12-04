# API接口文档 - 升级需求包B

## 📋 文档说明

本文档提供升级需求包B新增的所有后端API接口说明，供前端开发同学调用。

**基础URL**: `http://localhost:8080`  
**认证方式**: 所有接口需要登录认证（Cookie中的JSESSIONID）

---

## 🛒 订单管理接口（客户端）

### 1. 创建订单

**接口**: `POST /api/orders`  
**描述**: 从购物车创建订单  
**权限**: 客户登录

**请求参数** (JSON Body):
```json
{
  "cartIds": [1, 2, 3],           // 购物车ID列表（必填）
  "receiverName": "张三",          // 收货人姓名（必填）
  "receiverPhone": "13800138000", // 收货人电话（必填）
  "shippingAddress": "北京市朝阳区xxx", // 收货地址（必填）
  "remark": "请尽快发货"            // 备注（可选）
}
```

**响应示例**:
```json
{
  "id": 1,
  "orderNumber": "20251204123456789",
  "customer": {
    "id": 1,
    "username": "customer1",
    "email": "customer1@example.com"
  },
  "status": "PENDING",
  "totalAmount": 299.99,
  "receiverName": "张三",
  "receiverPhone": "13800138000",
  "shippingAddress": "北京市朝阳区xxx",
  "remark": "请尽快发货",
  "createTime": "2025-12-04T12:34:56",
  "updateTime": "2025-12-04T12:34:56",
  "orderItems": [
    {
      "id": 1,
      "product": {
        "id": 1,
        "name": "商品A",
        "price": 99.99
      },
      "quantity": 2,
      "unitPrice": 99.99,
      "subtotal": 199.98
    }
  ]
}
```

---

### 2. 查询订单列表

**接口**: `GET /api/orders`  
**描述**: 获取当前客户的所有订单  
**权限**: 客户登录

**请求参数**: 无

**响应示例**:
```json
[
  {
    "id": 1,
    "orderNumber": "20251204123456789",
    "customer": { ... },
    "status": "PENDING",
    "totalAmount": 299.99,
    "createTime": "2025-12-04T12:34:56",
    "orderItems": [ ... ]
  },
  {
    "id": 2,
    "orderNumber": "20251204234567890",
    "status": "CONFIRMED",
    "totalAmount": 499.99,
    "createTime": "2025-12-04T14:00:00",
    "orderItems": [ ... ]
  }
]
```

---

### 3. 查询订单详情

**接口**: `GET /api/orders/{id}`  
**描述**: 获取指定订单的详细信息  
**权限**: 客户登录（只能查看自己的订单）

**路径参数**:
- `id`: 订单ID

**响应示例**: 同"创建订单"响应

---

### 4. 客户取消订单

**接口**: `PUT /api/orders/{id}/cancel`  
**描述**: 客户取消订单（仅在开始发货前可取消）  
**权限**: 客户登录

**路径参数**:
- `id`: 订单ID

**请求参数** (Query):
- `reason`: 取消原因（必填）

**示例**: `PUT /api/orders/1/cancel?reason=不想要了`

**响应示例**:
```json
{
  "id": 1,
  "orderNumber": "20251204123456789",
  "status": "CANCELLED",
  "cancelTime": "2025-12-04T15:00:00",
  "cancelReason": "不想要了",
  "cancelRole": "CUSTOMER"
}
```

**错误码**:
- `400 Bad Request`: "订单已开始发货，无法取消"

---

## 👨‍💼 订单管理接口（商家端）

### 1. 查询所有订单

**接口**: `GET /api/seller/orders`  
**描述**: 商家查询所有订单  
**权限**: 商家登录

**请求参数** (Query，可选):
- `status`: 订单状态筛选（PENDING/CONFIRMED/PREPARING/SHIPPING/COMPLETED/CANCELLED）

**示例**: `GET /api/seller/orders?status=PENDING`

**响应示例**: 同客户订单列表

---

### 2. 确认订单

**接口**: `PUT /api/seller/orders/{id}/confirm`  
**描述**: 商家确认订单（PENDING → CONFIRMED）  
**权限**: 商家登录

**路径参数**:
- `id`: 订单ID

**响应示例**: 订单对象（status="CONFIRMED"）

---

### 3. 备货完成

**接口**: `PUT /api/seller/orders/{id}/prepare`  
**描述**: 商家备货完成（CONFIRMED → PREPARING）  
**权限**: 商家登录

**路径参数**:
- `id`: 订单ID

**响应示例**: 订单对象（status="PREPARING"）

---

### 4. 开始发货

**接口**: `PUT /api/seller/orders/{id}/ship`  
**描述**: 商家开始发货（PREPARING → SHIPPING）  
**权限**: 商家登录

**路径参数**:
- `id`: 订单ID

**响应示例**: 订单对象（status="SHIPPING"）

---

### 5. 交易完成

**接口**: `PUT /api/seller/orders/{id}/complete`  
**描述**: 商家标记交易完成（SHIPPING → COMPLETED）  
**权限**: 商家登录

**路径参数**:
- `id`: 订单ID

**响应示例**: 订单对象（status="COMPLETED"）

---

### 6. 商家取消订单

**接口**: `PUT /api/seller/orders/{id}/cancel`  
**描述**: 商家取消订单（仅在交易完成前可取消）  
**权限**: 商家登录

**路径参数**:
- `id`: 订单ID

**请求参数** (Query):
- `reason`: 取消原因（必填）

**示例**: `PUT /api/seller/orders/1/cancel?reason=库存不足`

**响应示例**: 订单对象（status="CANCELLED", cancelRole="SELLER"）

---

## ❤️ 收藏功能接口

### 1. 查询收藏列表

**接口**: `GET /api/favorites`  
**描述**: 获取当前客户的收藏列表  
**权限**: 客户登录

**响应示例**:
```json
[
  {
    "id": 1,
    "customer": {
      "id": 1,
      "username": "customer1"
    },
    "product": {
      "id": 10,
      "name": "商品A",
      "price": 99.99,
      "image": "http://example.com/product.jpg"
    },
    "createTime": "2025-12-04T10:00:00"
  }
]
```

---

### 2. 添加收藏

**接口**: `POST /api/favorites/{productId}`  
**描述**: 收藏指定商品  
**权限**: 客户登录

**路径参数**:
- `productId`: 商品ID

**响应示例**:
```json
{
  "id": 1,
  "customer": { ... },
  "product": { ... },
  "createTime": "2025-12-04T10:00:00"
}
```

**错误码**:
- `400 Bad Request`: "该商品已收藏"

---

### 3. 取消收藏

**接口**: `DELETE /api/favorites/{productId}`  
**描述**: 取消收藏指定商品  
**权限**: 客户登录

**路径参数**:
- `productId`: 商品ID

**响应**: 
- `200 OK`: 删除成功（无返回内容）

---

### 4. 检查是否收藏

**接口**: `GET /api/favorites/check/{productId}`  
**描述**: 检查指定商品是否已收藏  
**权限**: 客户登录

**路径参数**:
- `productId`: 商品ID

**响应示例**:
```json
{
  "favorited": true
}
```

---

## 🛍️ 购物车功能接口

### 1. 查询购物车

**接口**: `GET /api/cart`  
**描述**: 获取当前客户的购物车列表  
**权限**: 客户登录

**响应示例**:
```json
[
  {
    "id": 1,
    "customer": {
      "id": 1,
      "username": "customer1"
    },
    "product": {
      "id": 10,
      "name": "商品A",
      "price": 99.99,
      "stock": 50
    },
    "quantity": 2,
    "createTime": "2025-12-04T09:00:00",
    "updateTime": "2025-12-04T09:30:00"
  }
]
```

---

### 2. 添加到购物车

**接口**: `POST /api/cart`  
**描述**: 添加商品到购物车  
**权限**: 客户登录

**请求参数** (JSON Body):
```json
{
  "productId": 10,  // 商品ID（必填）
  "quantity": 2     // 数量（必填，必须>0）
}
```

**响应示例**: 购物车对象

**错误码**:
- `400 Bad Request`: "商品库存不足"

---

### 3. 更新购物车数量

**接口**: `PUT /api/cart/{cartId}`  
**描述**: 更新购物车商品数量  
**权限**: 客户登录

**路径参数**:
- `cartId`: 购物车ID

**请求参数** (Query):
- `quantity`: 新的数量（必填，必须>0）

**示例**: `PUT /api/cart/1?quantity=5`

**响应示例**: 更新后的购物车对象

---

### 4. 从购物车移除

**接口**: `DELETE /api/cart/{cartId}`  
**描述**: 从购物车移除指定商品  
**权限**: 客户登录

**路径参数**:
- `cartId`: 购物车ID

**响应**: 
- `200 OK`: 删除成功

---

### 5. 购物车转收藏

**接口**: `POST /api/cart/move-to-favorites`  
**描述**: 批量将购物车商品转为收藏  
**权限**: 客户登录

**请求参数** (JSON Body):
```json
{
  "cartIds": [1, 2, 3]  // 购物车ID列表（必填）
}
```

**响应示例**:
```json
{
  "message": "成功转移 2 件商品到收藏，1 件已收藏跳过"
}
```

---

### 6. 清空购物车

**接口**: `DELETE /api/cart/clear`  
**描述**: 清空当前客户的购物车  
**权限**: 客户登录

**响应**: 
- `200 OK`: 清空成功

---

## 📊 订单状态说明

| 状态 | 说明 | 可流转到 | 客户可取消 | 商家可取消 |
|------|------|----------|------------|------------|
| PENDING | 客户下单 | CONFIRMED, CANCELLED | ✅ | ✅ |
| CONFIRMED | 商家确认 | PREPARING, CANCELLED | ✅ | ✅ |
| PREPARING | 备货完成 | SHIPPING, CANCELLED | ✅ | ✅ |
| SHIPPING | 开始发货 | COMPLETED, CANCELLED | ❌ | ✅ |
| COMPLETED | 交易完成 | - | ❌ | ❌ |
| CANCELLED | 已取消 | - | ❌ | ❌ |

---

## 🔐 认证说明

所有接口都需要登录认证，通过 Spring Security 的 Cookie-Based Session 认证：

1. 客户登录: `POST /api/customers/login`
2. 商家登录: `POST /api/seller/login`
3. 登录成功后会在Cookie中设置JSESSIONID
4. 后续请求自动携带Cookie进行认证

---

## ⚠️ 错误码说明

| HTTP状态码 | 说明 |
|-----------|------|
| 200 | 成功 |
| 400 | 请求参数错误（如库存不足、状态不允许等） |
| 401 | 未登录或登录已过期 |
| 403 | 无权限（如访问他人订单） |
| 404 | 资源不存在（如订单不存在） |
| 500 | 服务器内部错误 |

---

## 📝 使用示例

### 完整下单流程（前端调用示例）

```javascript
// 1. 添加商品到购物车
await fetch('/api/cart', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ productId: 10, quantity: 2 })
});

// 2. 查询购物车
const cartRes = await fetch('/api/cart');
const cart = await cartRes.json();

// 3. 创建订单
const orderRes = await fetch('/api/orders', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    cartIds: cart.map(item => item.id),
    receiverName: "张三",
    receiverPhone: "13800138000",
    shippingAddress: "北京市朝阳区xxx"
  })
});
const order = await orderRes.json();

// 4. 查询订单列表
const ordersRes = await fetch('/api/orders');
const orders = await ordersRes.json();
```

---

**文档版本**: v1.0  
**更新时间**: 2025-12-04  
**维护人员**: 后端开发团队
