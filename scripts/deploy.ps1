# 完整部署流程
Write-Host "1. 构建应用..."
mvn clean package -f "$PSScriptRoot\..\backend\pom.xml"

Write-Host "2. 复制构建文件..."
Copy-Item -Path "$PSScriptRoot\..\backend\target\*.jar" -Destination "$PSScriptRoot\..\assets\rjgcsj-backend-1.0.0.jar" -Force

Write-Host "3. 健康检查..."
Write-Host "请手动启动后端服务后执行健康检查"
Write-Host "命令: curl http://localhost:8080/health"