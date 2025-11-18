# 启动后端服务
Write-Host "启动后端服务..."
Start-Process java -ArgumentList "-jar", "$PSScriptRoot\..\assets\rjgcsj-backend-1.0.0.jar" -WorkingDirectory "$PSScriptRoot\.."
Write-Host "后端服务已启动，端口8080"

# 启动前端开发服务器（可选）
# cd $PSScriptRoot\..\frontend
# npm run serve