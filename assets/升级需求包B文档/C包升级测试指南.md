# =====================================
# 升级需求包C - 功能测试指南
# =====================================

## 一、数据库升级步骤

### 1. 连接到MySQL数据库
```powershell
mysql -u root -p
```

### 2. 选择数据库
```sql
USE shop_db;
```

### 3. 执行升级脚本
```sql
source d:/project/rjgcsj/scripts/upgrade_add_payment_delivery_aftersale.sql
```

或者在PowerShell中直接执行：
```powershell
Get-Content d:\project\rjgcsj\scripts\upgrade_add_payment_delivery_aftersale.sql | mysql -u root -p shop_db
```

---

## 二、后端编译和启动

### 1. 编译后端
```powershell
cd d:\project\rjgcsj\backend
mvn clean package -DskipTests
```

### 2. 启动后端服务
```powershell
java -jar target\rjgcsj-backend-1.0.0.jar
```

---

## 三、前端启动

```powershell
cd d:\project\rjgcsj\frontend
npm install   # 首次运行需要
npm run serve
```

---

## 四、功能测试清单

### A. 收货地址管理测试

#### 1. 添加收货地址
- 访问：http://localhost:8081/customer/addresses
- 登录客户账号：customer1 / 123456
- 点击"添加新地址"
- 填写地址信息并保存

#### 2. 设置默认地址
- 在地址列表中点击"设为默认"
- 验证地址卡片显示"默认"标签

#### 3. 编辑和删除地址
- 测试编辑地址功能
- 测试删除地址功能

### B. 订单支付功能测试

#### 1. 创建订单
```powershell
# PowerShell测试命令
Invoke-WebRequest -Uri "http://localhost:8080/api/customers/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"username":"customer1","password":"123456"}' `
  -SessionVariable session

# 添加商品到购物车
Invoke-WebRequest -Uri "http://localhost:8080/api/cart" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"productId":1,"quantity":2}' `
  -WebSession $session

# 查询购物车获取cartId
$cartRes = Invoke-WebRequest -Uri "http://localhost:8080/api/cart" `
  -Method GET `
  -WebSession $session
$cart = $cartRes.Content | ConvertFrom-Json

# 创建订单
$orderBody = @{
  cartIds = @($cart[0].id)
  receiverName = "测试用户"
  receiverPhone = "13800138000"
  shippingAddress = "北京市朝阳区测试地址"
} | ConvertTo-Json

$orderRes = Invoke-WebRequest -Uri "http://localhost:8080/api/orders" `
  -Method POST `
  -ContentType "application/json" `
  -Body $orderBody `
  -WebSession $session
$order = $orderRes.Content | ConvertFrom-Json
Write-Host "订单创建成功，订单ID: $($order.id)"
```

#### 2. 创建支付
```powershell
# 创建支付记录
$paymentBody = @{
  orderId = $order.id
  paymentMethod = "ALIPAY"
} | ConvertTo-Json

$paymentRes = Invoke-WebRequest -Uri "http://localhost:8080/api/payments/create" `
  -Method POST `
  -ContentType "application/json" `
  -Body $paymentBody `
  -WebSession $session
$payment = $paymentRes.Content | ConvertFrom-Json
Write-Host "支付记录创建成功，支付ID: $($payment.id)"
```

#### 3. 执行支付（模拟）
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/payments/$($payment.id)/pay" `
  -Method POST `
  -WebSession $session | Select-Object -ExpandProperty Content | ConvertFrom-Json

Write-Host "支付成功！"
```

#### 4. 前端测试
- 在购物车页面选择商品，点击"结算"
- 在结算页面点击"选择地址"，选择收货地址
- 提交订单后，会弹出成功对话框
- 点击"立即支付"跳转到支付页面
- 选择支付方式（支付宝/微信/银行卡/信用卡）
- 点击"确认支付"完成模拟支付

### C. 物流跟踪功能测试

#### 1. 商家发货并创建物流
```powershell
# 商家登录
Invoke-WebRequest -Uri "http://localhost:8080/api/seller/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"username":"seller1","password":"123456"}' `
  -SessionVariable sellerSession

# 确认订单
Invoke-WebRequest -Uri "http://localhost:8080/api/seller/orders/$($order.id)/confirm" `
  -Method PUT `
  -WebSession $sellerSession

# 备货完成
Invoke-WebRequest -Uri "http://localhost:8080/api/seller/orders/$($order.id)/prepare" `
  -Method PUT `
  -WebSession $sellerSession

# 发货（自动创建物流信息）
$shipBody = @{
  logisticsCompany = "顺丰速运"
  logisticsNumber = "SF1234567890"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/api/seller/orders/$($order.id)/ship" `
  -Method PUT `
  -ContentType "application/json" `
  -Body $shipBody `
  -WebSession $sellerSession

Write-Host "发货成功，物流信息已创建"
```

#### 2. 查询物流信息
```powershell
# 客户查询物流
$logisticsRes = Invoke-WebRequest -Uri "http://localhost:8080/api/logistics/order/$($order.id)" `
  -Method GET `
  -WebSession $session
$logistics = $logisticsRes.Content | ConvertFrom-Json

# 查询物流跟踪
Invoke-WebRequest -Uri "http://localhost:8080/api/logistics/$($logistics.id)/tracks" `
  -Method GET `
  -WebSession $session | Select-Object -ExpandProperty Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

#### 3. 模拟物流进度更新
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/logistics/$($logistics.id)/simulate" `
  -Method POST `
  -WebSession $session

Write-Host "物流进度已更新"
```

#### 4. 前端测试
- 在订单列表中找到已发货的订单
- 点击"查看物流"按钮
- 查看物流时间轴，显示物流进度

### D. 售后服务功能测试

#### 1. 完成订单（必须先完成才能申请售后）
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/seller/orders/$($order.id)/complete" `
  -Method PUT `
  -WebSession $sellerSession

Write-Host "订单已完成"
```

#### 2. 客户申请售后
```powershell
# 获取订单明细ID
$orderDetail = Invoke-WebRequest -Uri "http://localhost:8080/api/orders/$($order.id)" `
  -Method GET `
  -WebSession $session
$orderData = $orderDetail.Content | ConvertFrom-Json
$orderItemId = $orderData.orderItems[0].id

# 创建售后申请
$afterSaleBody = @{
  orderItemId = $orderItemId
  serviceType = "REFUND"
  reason = "质量问题"
  description = "商品存在质量问题，申请退款"
  imageUrls = @("http://example.com/image1.jpg", "http://example.com/image2.jpg")
} | ConvertTo-Json

$afterSaleRes = Invoke-WebRequest -Uri "http://localhost:8080/api/after-sales" `
  -Method POST `
  -ContentType "application/json" `
  -Body $afterSaleBody `
  -WebSession $session
$afterSale = $afterSaleRes.Content | ConvertFrom-Json
Write-Host "售后申请成功，售后单号: $($afterSale.afterSaleNumber)"
```

#### 3. 商家处理售后
```powershell
# 商家查看售后列表
Invoke-WebRequest -Uri "http://localhost:8080/api/seller/after-sales" `
  -Method GET `
  -WebSession $sellerSession | Select-Object -ExpandProperty Content | ConvertFrom-Json

# 同意售后申请
$processBody = @{
  status = "APPROVED"
  sellerReply = "已同意您的售后申请，我们将尽快处理"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/api/seller/after-sales/$($afterSale.id)/process" `
  -Method PUT `
  -ContentType "application/json" `
  -Body $processBody `
  -WebSession $sellerSession

# 完成售后
$completeBody = @{
  result = "REFUNDED"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/api/seller/after-sales/$($afterSale.id)/complete" `
  -Method PUT `
  -ContentType "application/json" `
  -Body $completeBody `
  -WebSession $sellerSession | Select-Object -ExpandProperty Content | ConvertFrom-Json

Write-Host "售后处理完成"
```

#### 4. 前端测试
- 访问：http://localhost:8081/customer/after-sales
- 查看售后列表
- 在已完成的订单中，点击申请售后
- 填写售后信息并提交

---

## 五、快速完整测试流程

执行以下PowerShell脚本可以完整测试整个流程：

```powershell
# 设置变量
$baseUrl = "http://localhost:8080"

# 1. 客户登录
Write-Host "1. 客户登录..." -ForegroundColor Green
Invoke-WebRequest -Uri "$baseUrl/api/customers/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"username":"customer1","password":"123456"}' `
  -SessionVariable session | Out-Null

# 2. 添加收货地址
Write-Host "2. 添加收货地址..." -ForegroundColor Green
$addressBody = @{
  receiverName = "测试用户"
  receiverPhone = "13800138000"
  province = "北京市"
  city = "朝阳区"
  district = "朝阳区"
  detailAddress = "测试街道123号"
  isDefault = $true
} | ConvertTo-Json

Invoke-WebRequest -Uri "$baseUrl/api/addresses" `
  -Method POST `
  -ContentType "application/json" `
  -Body $addressBody `
  -WebSession $session | Out-Null

# 3. 添加商品到购物车并创建订单
Write-Host "3. 创建订单..." -ForegroundColor Green
Invoke-WebRequest -Uri "$baseUrl/api/cart" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"productId":1,"quantity":1}' `
  -WebSession $session | Out-Null

$cartRes = Invoke-WebRequest -Uri "$baseUrl/api/cart" -Method GET -WebSession $session
$cart = $cartRes.Content | ConvertFrom-Json

$orderBody = @{
  cartIds = @($cart[0].id)
  receiverName = "测试用户"
  receiverPhone = "13800138000"
  shippingAddress = "北京市朝阳区测试街道123号"
} | ConvertTo-Json

$orderRes = Invoke-WebRequest -Uri "$baseUrl/api/orders" `
  -Method POST `
  -ContentType "application/json" `
  -Body $orderBody `
  -WebSession $session
$order = $orderRes.Content | ConvertFrom-Json
Write-Host "订单创建成功，ID: $($order.id)" -ForegroundColor Yellow

# 4. 支付
Write-Host "4. 执行支付..." -ForegroundColor Green
$paymentBody = @{
  orderId = $order.id
  paymentMethod = "ALIPAY"
} | ConvertTo-Json

$paymentRes = Invoke-WebRequest -Uri "$baseUrl/api/payments/create" `
  -Method POST `
  -ContentType "application/json" `
  -Body $paymentBody `
  -WebSession $session
$payment = $paymentRes.Content | ConvertFrom-Json

Invoke-WebRequest -Uri "$baseUrl/api/payments/$($payment.id)/pay" `
  -Method POST `
  -WebSession $session | Out-Null
Write-Host "支付成功！" -ForegroundColor Yellow

# 5. 商家处理订单
Write-Host "5. 商家处理订单..." -ForegroundColor Green
Invoke-WebRequest -Uri "$baseUrl/api/seller/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"username":"seller1","password":"123456"}' `
  -SessionVariable sellerSession | Out-Null

Invoke-WebRequest -Uri "$baseUrl/api/seller/orders/$($order.id)/confirm" -Method PUT -WebSession $sellerSession | Out-Null
Invoke-WebRequest -Uri "$baseUrl/api/seller/orders/$($order.id)/prepare" -Method PUT -WebSession $sellerSession | Out-Null

$shipBody = '{"logisticsCompany":"顺丰速运","logisticsNumber":"SF1234567890"}'
Invoke-WebRequest -Uri "$baseUrl/api/seller/orders/$($order.id)/ship" `
  -Method PUT `
  -ContentType "application/json" `
  -Body $shipBody `
  -WebSession $sellerSession | Out-Null
Write-Host "订单已发货！" -ForegroundColor Yellow

Invoke-WebRequest -Uri "$baseUrl/api/seller/orders/$($order.id)/complete" -Method PUT -WebSession $sellerSession | Out-Null
Write-Host "订单已完成！" -ForegroundColor Yellow

# 6. 查询物流
Write-Host "6. 查询物流信息..." -ForegroundColor Green
$logisticsRes = Invoke-WebRequest -Uri "$baseUrl/api/logistics/order/$($order.id)" -Method GET -WebSession $session
$logistics = $logisticsRes.Content | ConvertFrom-Json
Write-Host "物流公司: $($logistics.logisticsCompany), 单号: $($logistics.logisticsNumber)" -ForegroundColor Yellow

# 7. 申请售后
Write-Host "7. 申请售后..." -ForegroundColor Green
$orderDetail = Invoke-WebRequest -Uri "$baseUrl/api/orders/$($order.id)" -Method GET -WebSession $session
$orderData = $orderDetail.Content | ConvertFrom-Json
$orderItemId = $orderData.orderItems[0].id

$afterSaleBody = @{
  orderItemId = $orderItemId
  serviceType = "REFUND"
  reason = "质量问题"
  description = "商品存在质量问题"
  imageUrls = @()
} | ConvertTo-Json

$afterSaleRes = Invoke-WebRequest -Uri "$baseUrl/api/after-sales" `
  -Method POST `
  -ContentType "application/json" `
  -Body $afterSaleBody `
  -WebSession $session
$afterSale = $afterSaleRes.Content | ConvertFrom-Json
Write-Host "售后申请成功，单号: $($afterSale.afterSaleNumber)" -ForegroundColor Yellow

Write-Host "`n✓ 所有功能测试完成！" -ForegroundColor Green
```

---

## 六、验证数据库

```sql
-- 查看订单支付状态
SELECT id, order_number, payment_status, payment_method, payment_time, logistics_company, logistics_number 
FROM orders 
ORDER BY create_time DESC LIMIT 5;

-- 查看支付记录
SELECT * FROM payments ORDER BY create_time DESC LIMIT 5;

-- 查看物流信息
SELECT * FROM logistics_info ORDER BY create_time DESC LIMIT 5;

-- 查看物流跟踪
SELECT * FROM logistics_tracks ORDER BY create_time DESC LIMIT 10;

-- 查看收货地址
SELECT * FROM delivery_addresses ORDER BY create_time DESC;

-- 查看售后申请
SELECT * FROM after_sales ORDER BY create_time DESC LIMIT 5;
```

---

## 七、前端访问地址

- 首页：http://localhost:8081/
- 客户登录：http://localhost:8081/customer/login
- 收货地址管理：http://localhost:8081/customer/addresses
- 购物车：http://localhost:8081/customer/cart
- 订单列表：http://localhost:8081/customer/orders
- 售后列表：http://localhost:8081/customer/after-sales

---

## 八、注意事项

1. **数据库升级**：首次运行前必须执行数据库升级脚本
2. **支付功能**：这是模拟支付，点击按钮即完成支付，没有真实的第三方对接
3. **物流功能**：物流信息也是模拟的，可以手动调用simulate接口更新物流进度
4. **售后功能**：只有已完成（COMPLETED）的订单才能申请售后
5. **订单流程**：订单必须先支付才能发货

---

**测试文档版本**: v1.0  
**创建时间**: 2026-01-08
