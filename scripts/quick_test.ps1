# =====================================
# 快速功能测试脚本
# =====================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  升级需求包C - 快速功能测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080"

try {
    # 1. 客户登录
    Write-Host "1. 客户登录..." -ForegroundColor Green
    $loginResponse = Invoke-WebRequest -Uri "$baseUrl/api/customers/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body '{"username":"customer1","password":"123456"}' `
        -SessionVariable session
    Write-Host "   ✓ 登录成功" -ForegroundColor Yellow

    # 2. 添加收货地址
    Write-Host ""
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
    Write-Host "   ✓ 收货地址添加成功" -ForegroundColor Yellow

    # 3. 添加商品到购物车
    Write-Host ""
    Write-Host "3. 添加商品到购物车..." -ForegroundColor Green
    Invoke-WebRequest -Uri "$baseUrl/api/cart" `
        -Method POST `
        -ContentType "application/json" `
        -Body '{"productId":1,"quantity":1}' `
        -WebSession $session | Out-Null
    Write-Host "   ✓ 商品已加入购物车" -ForegroundColor Yellow

    # 4. 创建订单
    Write-Host ""
    Write-Host "4. 创建订单..." -ForegroundColor Green
    $cartRes = Invoke-WebRequest -Uri "$baseUrl/api/cart" -Method GET -WebSession $session
    $cart = $cartRes.Content | ConvertFrom-Json

    if ($cart.Length -eq 0) {
        throw "购物车为空"
    }

    $orderBody = @{
        cartIds = @($cart[0].id)
        receiverName = "测试用户"
        receiverPhone = "13800138000"
        shippingAddress = "北京市朝阳区测试街道123号"
        remark = "测试订单"
    } | ConvertTo-Json

    $orderRes = Invoke-WebRequest -Uri "$baseUrl/api/orders" `
        -Method POST `
        -ContentType "application/json" `
        -Body $orderBody `
        -WebSession $session
    $order = $orderRes.Content | ConvertFrom-Json
    Write-Host "   ✓ 订单创建成功" -ForegroundColor Yellow
    Write-Host "   订单ID: $($order.id)" -ForegroundColor Cyan
    Write-Host "   订单号: $($order.orderNumber)" -ForegroundColor Cyan

    # 5. 创建支付
    Write-Host ""
    Write-Host "5. 创建支付..." -ForegroundColor Green
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
    Write-Host "   ✓ 支付记录创建成功" -ForegroundColor Yellow
    Write-Host "   支付ID: $($payment.id)" -ForegroundColor Cyan
    Write-Host "   支付单号: $($payment.paymentNumber)" -ForegroundColor Cyan

    # 6. 执行支付
    Write-Host ""
    Write-Host "6. 执行支付..." -ForegroundColor Green
    Invoke-WebRequest -Uri "$baseUrl/api/payments/$($payment.id)/pay" `
        -Method POST `
        -WebSession $session | Out-Null
    Write-Host "   ✓ 支付成功！" -ForegroundColor Yellow

    # 7. 商家登录
    Write-Host ""
    Write-Host "7. 商家登录..." -ForegroundColor Green
    Invoke-WebRequest -Uri "$baseUrl/api/seller/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body '{"username":"seller1","password":"123456"}' `
        -SessionVariable sellerSession | Out-Null
    Write-Host "   ✓ 商家登录成功" -ForegroundColor Yellow

    # 8. 商家确认订单
    Write-Host ""
    Write-Host "8. 商家处理订单..." -ForegroundColor Green
    Invoke-WebRequest -Uri "$baseUrl/api/seller/orders/$($order.id)/confirm" `
        -Method PUT `
        -WebSession $sellerSession | Out-Null
    Write-Host "   ✓ 订单已确认" -ForegroundColor Yellow

    Invoke-WebRequest -Uri "$baseUrl/api/seller/orders/$($order.id)/prepare" `
        -Method PUT `
        -WebSession $sellerSession | Out-Null
    Write-Host "   ✓ 备货完成" -ForegroundColor Yellow

    # 9. 发货
    Write-Host ""
    Write-Host "9. 商家发货..." -ForegroundColor Green
    $shipBody = @{
        logisticsCompany = "顺丰速运"
        logisticsNumber = "SF" + (Get-Date -Format "yyyyMMddHHmmss")
    } | ConvertTo-Json

    Invoke-WebRequest -Uri "$baseUrl/api/seller/orders/$($order.id)/ship" `
        -Method PUT `
        -ContentType "application/json" `
        -Body $shipBody `
        -WebSession $sellerSession | Out-Null
    Write-Host "   ✓ 订单已发货，物流信息已创建" -ForegroundColor Yellow

    # 10. 查询物流
    Write-Host ""
    Write-Host "10. 查询物流信息..." -ForegroundColor Green
    $logisticsRes = Invoke-WebRequest -Uri "$baseUrl/api/logistics/order/$($order.id)" `
        -Method GET `
        -WebSession $session
    $logistics = $logisticsRes.Content | ConvertFrom-Json
    Write-Host "   ✓ 物流信息获取成功" -ForegroundColor Yellow
    Write-Host "   物流公司: $($logistics.logisticsCompany)" -ForegroundColor Cyan
    Write-Host "   物流单号: $($logistics.logisticsNumber)" -ForegroundColor Cyan
    Write-Host "   当前状态: $($logistics.currentStatus)" -ForegroundColor Cyan

    # 11. 模拟物流进度更新
    Write-Host ""
    Write-Host "11. 模拟物流进度更新..." -ForegroundColor Green
    Invoke-WebRequest -Uri "$baseUrl/api/logistics/$($logistics.id)/simulate" `
        -Method POST `
        -WebSession $session | Out-Null
    Write-Host "   ✓ 物流进度已更新" -ForegroundColor Yellow

    # 12. 完成订单
    Write-Host ""
    Write-Host "12. 完成订单..." -ForegroundColor Green
    Invoke-WebRequest -Uri "$baseUrl/api/seller/orders/$($order.id)/complete" `
        -Method PUT `
        -WebSession $sellerSession | Out-Null
    Write-Host "   ✓ 订单已完成" -ForegroundColor Yellow

    # 13. 申请售后
    Write-Host ""
    Write-Host "13. 客户申请售后..." -ForegroundColor Green
    $orderDetail = Invoke-WebRequest -Uri "$baseUrl/api/orders/$($order.id)" `
        -Method GET `
        -WebSession $session
    $orderData = $orderDetail.Content | ConvertFrom-Json
    $orderItemId = $orderData.orderItems[0].id

    $afterSaleBody = @{
        orderItemId = $orderItemId
        serviceType = "REFUND"
        reason = "质量问题"
        description = "商品存在质量问题，申请退款"
        imageUrls = @()
    } | ConvertTo-Json

    $afterSaleRes = Invoke-WebRequest -Uri "$baseUrl/api/after-sales" `
        -Method POST `
        -ContentType "application/json" `
        -Body $afterSaleBody `
        -WebSession $session
    $afterSale = $afterSaleRes.Content | ConvertFrom-Json
    Write-Host "   ✓ 售后申请成功" -ForegroundColor Yellow
    Write-Host "   售后单号: $($afterSale.afterSaleNumber)" -ForegroundColor Cyan
    Write-Host "   服务类型: $($afterSale.serviceType)" -ForegroundColor Cyan

    # 14. 商家处理售后
    Write-Host ""
    Write-Host "14. 商家处理售后..." -ForegroundColor Green
    $processBody = @{
        status = "APPROVED"
        sellerReply = "已同意您的售后申请，我们将尽快处理"
    } | ConvertTo-Json

    Invoke-WebRequest -Uri "$baseUrl/api/seller/after-sales/$($afterSale.id)/process" `
        -Method PUT `
        -ContentType "application/json" `
        -Body $processBody `
        -WebSession $sellerSession | Out-Null
    Write-Host "   ✓ 售后申请已同意" -ForegroundColor Yellow

    # 15. 完成售后
    $completeBody = @{
        result = "REFUNDED"
    } | ConvertTo-Json

    Invoke-WebRequest -Uri "$baseUrl/api/seller/after-sales/$($afterSale.id)/complete" `
        -Method PUT `
        -ContentType "application/json" `
        -Body $completeBody `
        -WebSession $sellerSession | Out-Null
    Write-Host "   ✓ 售后处理完成" -ForegroundColor Yellow

    # 完成
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  ✓ 所有功能测试完成！" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "测试结果总结：" -ForegroundColor Green
    Write-Host "  • 收货地址管理：通过" -ForegroundColor White
    Write-Host "  • 订单在线支付：通过" -ForegroundColor White
    Write-Host "  • 物流信息跟踪：通过" -ForegroundColor White
    Write-Host "  • 售后服务申请：通过" -ForegroundColor White
    Write-Host ""
    Write-Host "可以访问前端界面查看完整功能：" -ForegroundColor Cyan
    Write-Host "  http://localhost:8081" -ForegroundColor White
    Write-Host ""

} catch {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "  ✗ 测试失败" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "错误信息: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "请检查：" -ForegroundColor Yellow
    Write-Host "  1. 后端服务是否正在运行 (http://localhost:8080)" -ForegroundColor White
    Write-Host "  2. 数据库是否已升级" -ForegroundColor White
    Write-Host "  3. 测试数据是否正确（productId=1是否存在）" -ForegroundColor White
    Write-Host ""
}
