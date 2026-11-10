import { http } from './http'
import type { AuthResponse, Credenciales } from './types'

export async function login(credenciales: Credenciales): Promise<AuthResponse> {
  const { data } = await http.post<AuthResponse>('/auth/login', credenciales)
  return data
}

export async function registrar(credenciales: Credenciales): Promise<AuthResponse> {
  const { data } = await http.post<AuthResponse>('/auth/register', credenciales)
  return data
}
