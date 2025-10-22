# 完整部署流程
Write-Host "1. 环境检查..."
.\scripts\env-check.ps1

Write-Host "2. 构建应用..."
mvn clean package

Write-Host "3. 构建Docker镜像..."
docker build -t myapp:latest .

Write-Host "4. 启动服务..."
docker-compose up -d

Write-Host "5. 健康检查..."
Start-Sleep -Seconds 30
curl http://localhost:8080/health