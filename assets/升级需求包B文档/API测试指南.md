# API接口测试指南

## 📋 测试准备

### 1. 环境要求
- 后端服务已启动：`http://localhost:xxxx`
- 数据库已运行并完成数据初始化

### 2. 测试工具推荐
- **PowerShell**（Windows命令行）
- **Postman**（图形化工具）
- **浏览器开发者工具**（F12）

---

## 🔧 测试前准备

### 获取认证Cookie

大部分接口需要登录认证，测试前需要先登录获取JSESSIONID。

#### 客户登录
```powershell
# PowerShell命令
Invoke-WebRequest -Uri "http://localhost:8080/api/customers/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"username":"customer1","password":"123456"}' `
  -SessionVariable session

# 查看Cookie
$session.Cookies.GetCookies("http://localhost:8080")
```

#### 商家登录
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/seller/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"username":"seller1","password":"123456"}' `
  -SessionVariable session
```

**提示**：后续命令中使用 `-WebSession $session` 参数来携带认证Cookie。

---

## 🛒 订单管理测试（客户端）

### 测试场景1：创建订单

**步骤1**：添加商品到购物车
```powershell
# 添加商品ID为1的商品，数量2
Invoke-WebRequest -Uri "http://localhost:8080/api/cart" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"productId":1,"quantity":2}' `
  -WebSession $session
```

**步骤2**：查看购物车
```powershell
$cartRes = Invoke-WebRequest -Uri "http://localhost:8080/api/cart" `
  -Method GET `
  -WebSession $session
$cartRes.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

**步骤3**：从购物车创建订单
```powershell
# 假设购物车ID为1、2
$orderBody = @{
  cartIds = @(1, 2)
  receiverName = "张三"
  receiverPhone = "13800138000"
  shippingAddress = "北京市朝阳区xxx街道"
  remark = "请尽快发货"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/api/orders" `
  -Method POST `
  -ContentType "application/json" `
  -Body $orderBody `
  -WebSession $session | Select-Object -ExpandProperty Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

---

### 测试场景2：查询订单

**查询订单列表**
```powershell
$ordersRes = Invoke-WebRequest -Uri "http://localhost:8080/api/orders" `
  -Method GET `
  -WebSession $session
$ordersRes.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

**查询订单详情**
```powershell
# 假设订单ID为1
$orderRes = Invoke-WebRequest -Uri "http://localhost:8080/api/orders/1" `
  -Method GET `
  -WebSession $session
$orderRes.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

---

### 测试场景3：客户取消订单

```powershell
# 取消订单ID为1的订单
Invoke-WebRequest -Uri "http://localhost:8080/api/orders/1/cancel?reason=不想要了" `
  -Method PUT `
  -WebSession $session | Select-Object -ExpandProperty Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

**预期结果**：
- 订单状态变为 `CANCELLED`
- `cancelRole` 为 `CUSTOMER`
- `cancelReason` 为 "不想要了"
- 商品库存自动恢复

**边界测试**：
```powershell
# 尝试取消已发货的订单（应该失败）
Invoke-WebRequest -Uri "http://localhost:8080/api/orders/2/cancel?reason=测试" `
  -Method PUT `
  -WebSession $session
# 预期：400 Bad Request，提示"订单已开始发货，无法取消"
```

---

## 👨‍💼 订单管理测试（商家端）

**先登录商家账号**：
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/seller/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"username":"seller1","password":"123456"}' `
  -SessionVariable sellerSession
```

### 测试场景4：商家订单流程

**步骤1**：查询所有订单
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/seller/orders" `
  -Method GET `
  -WebSession $sellerSession | Select-Object -ExpandProperty Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

**步骤2**：确认订单（PENDING → CONFIRMED）
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/seller/orders/1/confirm" `
  -Method PUT `
  -WebSession $sellerSession | Select-Object -ExpandProperty Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

**步骤3**：备货完成（CONFIRMED → PREPARING）
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/seller/orders/1/prepare" `
  -Method PUT `
  -WebSession $sellerSession | Select-Object -ExpandProperty Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

**步骤4**：开始发货（PREPARING → SHIPPING）
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/seller/orders/1/ship" `
  -Method PUT `
  -WebSession $sellerSession | Select-Object -ExpandProperty Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

**步骤5**：交易完成（SHIPPING → COMPLETED）
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/seller/orders/1/complete" `
  -Method PUT `
  -WebSession $sellerSession | Select-Object -ExpandProperty Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

---

### 测试场景5：商家取消订单

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/seller/orders/2/cancel?reason=库存不足" `
  -Method PUT `
  -WebSession $sellerSession | Select-Object -ExpandProperty Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

**预期结果**：
- 订单状态变为 `CANCELLED`
- `cancelRole` 为 `SELLER`
- `cancelReason` 为 "库存不足"

---

## ❤️ 收藏功能测试

**先登录客户账号**：
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/customers/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"username":"customer1","password":"123456"}' `
  -SessionVariable session
```

### 测试场景6：收藏商品

**添加收藏**
```powershell
# 收藏商品ID为10的商品
Invoke-WebRequest -Uri "http://localhost:8080/api/favorites/10" `
  -Method POST `
  -WebSession $session | Select-Object -ExpandProperty Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

**查询收藏列表**
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/favorites" `
  -Method GET `
  -WebSession $session | Select-Object -ExpandProperty Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

**检查是否收藏**
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/favorites/check/10" `
  -Method GET `
  -WebSession $session | Select-Object -ExpandProperty Content | ConvertFrom-Json
# 预期：{"favorited": true}
```

**取消收藏**
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/favorites/10" `
  -Method DELETE `
  -WebSession $session
# 预期：200 OK
```

---

## 🛍️ 购物车功能测试

### 测试场景7：购物车操作

**添加商品到购物车**
```powershell
$cartBody = @{
  productId = 10
  quantity = 3
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/api/cart" `
  -Method POST `
  -ContentType "application/json" `
  -Body $cartBody `
  -WebSession $session | Select-Object -ExpandProperty Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

**查询购物车**
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/cart" `
  -Method GET `
  -WebSession $session | Select-Object -ExpandProperty Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

**更新购物车数量**
```powershell
# 假设购物车ID为1，更新数量为5
Invoke-WebRequest -Uri "http://localhost:8080/api/cart/1?quantity=5" `
  -Method PUT `
  -WebSession $session | Select-Object -ExpandProperty Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

**从购物车移除**
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/cart/1" `
  -Method DELETE `
  -WebSession $session
# 预期：200 OK
```

**清空购物车**
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/cart/clear" `
  -Method DELETE `
  -WebSession $session
# 预期：200 OK
```

---

### 测试场景8：购物车转收藏

**步骤1**：添加多件商品到购物车
```powershell
# 添加商品1
Invoke-WebRequest -Uri "http://localhost:8080/api/cart" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"productId":10,"quantity":1}' `
  -WebSession $session

# 添加商品2
Invoke-WebRequest -Uri "http://localhost:8080/api/cart" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"productId":11,"quantity":2}' `
  -WebSession $session
```

**步骤2**：购物车转收藏
```powershell
$moveBody = @{
  cartIds = @(1, 2)
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/api/cart/move-to-favorites" `
  -Method POST `
  -ContentType "application/json" `
  -Body $moveBody `
  -WebSession $session | Select-Object -ExpandProperty Content | ConvertFrom-Json
# 预期：{"message": "成功转移 2 件商品到收藏"}
```

**步骤3**：验证购物车已清空
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/cart" `
  -Method GET `
  -WebSession $session | Select-Object -ExpandProperty Content
# 预期：[]（空数组）
```

**步骤4**：验证收藏列表有商品
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/favorites" `
  -Method GET `
  -WebSession $session | Select-Object -ExpandProperty Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
# 预期：包含商品10和11
```

---

## 🧪 综合测试场景

### 完整流程：从浏览商品到订单完成

```powershell
# 1. 客户登录
Invoke-WebRequest -Uri "http://localhost:8080/api/customers/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"username":"customer1","password":"123456"}' `
  -SessionVariable session

# 2. 添加商品到购物车
Invoke-WebRequest -Uri "http://localhost:8080/api/cart" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"productId":1,"quantity":2}' `
  -WebSession $session

# 3. 创建订单
$orderBody = @{
  cartIds = @(1)
  receiverName = "测试用户"
  receiverPhone = "13800138000"
  shippingAddress = "测试地址"
} | ConvertTo-Json

$orderRes = Invoke-WebRequest -Uri "http://localhost:8080/api/orders" `
  -Method POST `
  -ContentType "application/json" `
  -Body $orderBody `
  -WebSession $session
$order = $orderRes.Content | ConvertFrom-Json
$orderId = $order.id
Write-Host "订单创建成功，订单ID: $orderId"

# 4. 商家登录
Invoke-WebRequest -Uri "http://localhost:8080/api/seller/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"username":"seller1","password":"123456"}' `
  -SessionVariable sellerSession

# 5. 商家确认订单
Invoke-WebRequest -Uri "http://localhost:8080/api/seller/orders/$orderId/confirm" `
  -Method PUT `
  -WebSession $sellerSession

# 6. 商家备货完成
Invoke-WebRequest -Uri "http://localhost:8080/api/seller/orders/$orderId/prepare" `
  -Method PUT `
  -WebSession $sellerSession

# 7. 商家开始发货
Invoke-WebRequest -Uri "http://localhost:8080/api/seller/orders/$orderId/ship" `
  -Method PUT `
  -WebSession $sellerSession

# 8. 商家交易完成
$finalOrder = Invoke-WebRequest -Uri "http://localhost:8080/api/seller/orders/$orderId/complete" `
  -Method PUT `
  -WebSession $sellerSession
$finalOrder.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10

Write-Host "订单流程测试完成！订单状态：COMPLETED"
```

---

## ⚠️ 常见错误排查

### 1. 401 Unauthorized（未登录）
**原因**：未登录或Session过期  
**解决**：重新执行登录命令获取Session

### 2. 400 Bad Request - "商品库存不足"
**原因**：购物车数量超过商品库存  
**解决**：检查商品库存，减少购物车数量

### 3. 400 Bad Request - "订单已开始发货，无法取消"
**原因**：订单状态已经是SHIPPING  
**解决**：这是正常的业务逻辑限制

### 4. 403 Forbidden
**原因**：无权限访问（如客户访问商家接口）  
**解决**：使用正确的账号登录

### 5. 404 Not Found
**原因**：订单/商品/购物车记录不存在  
**解决**：检查ID是否正确

---

## 📊 测试清单

- [ ] 客户登录
- [ ] 添加商品到购物车
- [ ] 查询购物车
- [ ] 更新购物车数量
- [ ] 从购物车创建订单
- [ ] 查询订单列表
- [ ] 客户取消订单（发货前）
- [ ] 客户尝试取消已发货订单（应失败）
- [ ] 商家登录
- [ ] 商家查询订单
- [ ] 商家确认订单
- [ ] 商家备货完成
- [ ] 商家开始发货
- [ ] 商家交易完成
- [ ] 商家取消订单
- [ ] 添加收藏
- [ ] 查询收藏列表
- [ ] 检查是否收藏
- [ ] 取消收藏
- [ ] 购物车转收藏
- [ ] 清空购物车

---

## 🔍 验证数据库变更

测试完成后，可以查询数据库验证数据是否正确：

```sql
-- 查询订单表
SELECT * FROM orders ORDER BY create_time DESC;

-- 查询订单明细
SELECT * FROM order_items WHERE order_id = 1;

-- 查询收藏表
SELECT * FROM favorites;

-- 查询购物车
SELECT * FROM shopping_cart;

-- 检查商品库存是否正确扣减
SELECT id, name, stock FROM products WHERE id IN (1, 10, 11);
```

---

**测试文档版本**: v1.0  
**更新时间**: 2025-12-04  
**维护人员**: 后端开发团队
