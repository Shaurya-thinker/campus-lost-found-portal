/**
 * Authentication types and contracts matching Spring Boot AuthController DTOs
 */

export type Role = 'STUDENT' | 'ADMIN'

export interface AuthUser {
  id: number
  collegeUid: string
  name: string
  email: string
  role: Role
}

export interface AuthResponse {
  token: string
  tokenType: string
  expiresIn: number
  user: AuthUser
}

export interface LoginPayload {
  collegeUid: string
  password: string
}

export interface RegisterPayload {
  collegeUid: string
  name: string
  email: string
  password: string
}

