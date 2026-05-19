# Auth API

Base URL: http://localhost:8080

Response format
{
  "code": 0,
  "message": "OK",
  "data": {}
}

## Send code
POST /api/auth/send-code

Body
{
  "email": "user@example.com",
  "purpose": "REGISTER" | "RESET" | "CHANGE_EMAIL"
}

## Register
POST /api/auth/register

Body
{
  "email": "user@example.com",
  "password": "password123",
  "code": "123456",
  "nickname": "optional"
}

## Login
POST /api/auth/login

Body
{
  "email": "user@example.com",
  "password": "password123",
  "rememberMe": true
}

## Reset password
POST /api/auth/reset-password

Body
{
  "email": "user@example.com",
  "code": "123456",
  "newPassword": "newpass123"
}

## Logout
POST /api/auth/logout

Headers
Authorization: Bearer <token>

## Get profile
GET /api/users/me

Headers
Authorization: Bearer <token>

## Update profile
PUT /api/users/me/profile

Body
{
  "nickname": "newname",
  "avatarUrl": "/uploads/20260509/xxx.png"
}

## Update email
PUT /api/users/me/email

Body
{
  "newEmail": "new@example.com",
  "code": "123456"
}

## Update password
PUT /api/users/me/password

Body
{
  "oldPassword": "oldpass",
  "newPassword": "newpass"
}

## Upload image
POST /api/files/upload

FormData
file=<image>
