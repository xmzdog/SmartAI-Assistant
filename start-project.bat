@echo off
echo ========================================
echo  SmartAI Assistant 项目启动脚本
echo ========================================

echo.
echo 正在启动后端服务...
echo ----------------------------------------
start "SmartAI Backend" cmd /k "mvn spring-boot:run -pl dev-fetch-app"

echo.
echo 等待5秒让后端启动...
timeout /t 5 /nobreak > nul

echo.
echo 正在启动前端服务...
echo ----------------------------------------
cd smartai-frontend
echo 更新前端依赖...
call npm install
echo 启动前端开发服务器...
start "SmartAI Frontend" cmd /k "npm run dev"

echo.
echo ========================================
echo  项目启动完成！
echo  后端地址: http://localhost:8090
echo  前端地址: http://localhost:3000
echo ========================================

pause