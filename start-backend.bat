@echo off
title Zero Trust Backend
echo ================================================
echo   Zero Trust Secure Cloud Access - Backend
echo ================================================
echo.

echo Checking for existing process on port 8081...
powershell -NoProfile -Command "$p = Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue; if ($p) { Stop-Process -Id $p.OwningProcess -Force -ErrorAction SilentlyContinue; Write-Host 'Stopped old process.' }"
timeout /t 2 /nobreak >nul

echo Starting backend on http://localhost:8081
echo Press Ctrl+C to stop.
echo OTP will appear below when you log in.
echo.

"C:\Program Files\Java\jdk-25\bin\java.exe" -jar "D:\Finalproject\backend\target\zero-trust-backend-1.0.0.jar" "--spring.profiles.active=dev" "--server.port=8081" "--jwt.secret=dGhpcyBpcyBhIHNlY3VyZSBzZWNyZXQga2V5IGZvciBqd3QgdGVzdGluZzEyMzQ1" "--jwt.expiration=86400000" "--spring.datasource.url=jdbc:mysql://[::1]:3306/zero_trust_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true" "--spring.datasource.username=root" "--spring.datasource.password=MySql@123" "--spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver" "--spring.jpa.hibernate.ddl-auto=update" "--spring.mail.host=smtp.gmail.com" "--spring.mail.port=587" "--spring.mail.username=arssoftech@gmail.com" "--spring.mail.password=YOUR_GMAIL_APP_PASSWORD" "--spring.mail.properties.mail.smtp.auth=true" "--spring.mail.properties.mail.smtp.starttls.enable=true" "--cors.allowed-origins=http://localhost:5500,http://127.0.0.1:5500,http://localhost:3000,null"

pause
