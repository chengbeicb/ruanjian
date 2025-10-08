@echo off
REM 测试卖家登录API
curl -X POST -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}" http://localhost:8080/seller/login

REM 测试使用Basic认证访问受保护的资源
curl -u admin:admin123 http://localhost:8080/seller/me

pause