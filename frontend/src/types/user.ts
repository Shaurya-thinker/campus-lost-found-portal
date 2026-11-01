/**
 * User types and payloads matching Spring Boot UserRegistrationRequest and UserResponse DTOs
 */

export interface User {
  id: number
  name: string
  email: string
}

export interface UserRegistrationPayload {
  name: string
  email: string
}

