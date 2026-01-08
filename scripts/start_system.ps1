# =====================================
# 系统启动脚本
# =====================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  电商系统 - 升级版启动脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = "d:\project\rjgcsj"

# 1. 检查数据库是否运行
Write-Host "1. 检查MySQL服务..." -ForegroundColor Green
$mysqlService = Get-Service -Name "MySQL*" -ErrorAction SilentlyContinue
if ($mysqlService -and $mysqlService.Status -eq "Running") {
    Write-Host "   ✓ MySQL服务正在运行" -ForegroundColor Yellow
} else {
    Write-Host "   ✗ MySQL服务未运行，请先启动MySQL" -ForegroundColor Red
    exit 1
}

# 2. 询问是否需要升级数据库
Write-Host ""
$upgradeDb = Read-Host "2. 是否需要执行数据库升级？(y/n)"
if ($upgradeDb -eq 'y') {
    Write-Host "   正在执行数据库升级..." -ForegroundColor Yellow
    $sqlScript = "$projectRoot\scripts\upgrade_add_payment_delivery_aftersale.sql"
    
    if (Test-Path $sqlScript) {
        # 提示输入数据库密码
        $dbPassword = Read-Host "   请输入MySQL root密码" -AsSecureString
        $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($dbPassword)
        $plainPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
        
        try {
            Get-Content $sqlScript | mysql -u root -p$plainPassword shop_db 2>&1 | Out-Null
            Write-Host "   ✓ 数据库升级完成" -ForegroundColor Yellow
        } catch {
            Write-Host "   ✗ 数据库升级失败: $_" -ForegroundColor Red
        }
    } else {
        Write-Host "   ✗ 找不到升级脚本" -ForegroundColor Red
    }
}

# 3. 编译后端
Write-Host ""
$compileBackend = Read-Host "3. 是否需要重新编译后端？(y/n)"
if ($compileBackend -eq 'y') {
    Write-Host "   正在编译后端..." -ForegroundColor Yellow
    Set-Location "$projectRoot\backend"
    
    $compileProcess = Start-Process -FilePath "mvn" -ArgumentList "clean", "package", "-DskipTests" -NoNewWindow -PassThru -Wait
    
    if ($compileProcess.ExitCode -eq 0) {
        Write-Host "   ✓ 后端编译完成" -ForegroundColor Yellow
    } else {
        Write-Host "   ✗ 后端编译失败" -ForegroundColor Red
        exit 1
    }
}

# 4. 启动后端
Write-Host ""
Write-Host "4. 启动后端服务..." -ForegroundColor Green
Set-Location "$projectRoot\backend"

$jarFile = "target\rjgcsj-backend-1.0.0.jar"
if (Test-Path $jarFile) {
    Write-Host "   后端服务正在启动，请稍候..." -ForegroundColor Yellow
    Start-Process -FilePath "java" -ArgumentList "-jar", $jarFile -WindowStyle Normal
    Write-Host "   ✓ 后端服务已在新窗口启动" -ForegroundColor Yellow
    Write-Host "   访问地址: http://localhost:8080" -ForegroundColor Cyan
} else {
    Write-Host "   ✗ 找不到jar文件，请先编译" -ForegroundColor Red
    exit 1
}

# 5. 等待后端启动
Write-Host ""
Write-Host "5. 等待后端服务就绪..." -ForegroundColor Green
$maxRetries = 30
$retryCount = 0
$backendReady = $false

while ($retryCount -lt $maxRetries) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/api/products" -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200 -or $response.StatusCode -eq 401) {
            $backendReady = $true
            break
        }
    } catch {
        # 继续等待
    }
    
    Start-Sleep -Seconds 2
    $retryCount++
    Write-Host "   等待中... ($retryCount/$maxRetries)" -ForegroundColor Yellow
}

if ($backendReady) {
    Write-Host "   ✓ 后端服务已就绪" -ForegroundColor Yellow
} else {
    Write-Host "   ⚠ 后端服务启动超时，但可能正在启动中" -ForegroundColor Yellow
}

# 6. 启动前端
Write-Host ""
$startFrontend = Read-Host "6. 是否启动前端开发服务器？(y/n)"
if ($startFrontend -eq 'y') {
    Write-Host "   前端服务正在启动..." -ForegroundColor Yellow
    Set-Location "$projectRoot\frontend"
    
    # 检查node_modules
    if (-not (Test-Path "node_modules")) {
        Write-Host "   首次运行，正在安装依赖..." -ForegroundColor Yellow
        npm install
    }
    
    Start-Process -FilePath "npm" -ArgumentList "run", "serve" -WindowStyle Normal
    Write-Host "   ✓ 前端服务已在新窗口启动" -ForegroundColor Yellow
    Write-Host "   访问地址: http://localhost:8081" -ForegroundColor Cyan
}

# 7. 完成
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  系统启动完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "访问地址：" -ForegroundColor Green
Write-Host "  - 后端API: http://localhost:8080" -ForegroundColor White
Write-Host "  - 前端界面: http://localhost:8081" -ForegroundColor White
Write-Host ""
Write-Host "测试账号：" -ForegroundColor Green
Write-Host "  客户: customer1 / 123456" -ForegroundColor White
Write-Host "  商家: seller1 / 123456" -ForegroundColor White
Write-Host ""
Write-Host "新增功能：" -ForegroundColor Green
Write-Host "  ✓ 收货地址管理" -ForegroundColor White
Write-Host "  ✓ 订单在线支付（模拟）" -ForegroundColor White
Write-Host "  ✓ 物流跟踪查询" -ForegroundColor White
Write-Host "  ✓ 售后服务申请" -ForegroundColor White
Write-Host ""
Write-Host "详细测试指南: $projectRoot\assets\升级需求包B文档\C包升级测试指南.md" -ForegroundColor Cyan
Write-Host ""
