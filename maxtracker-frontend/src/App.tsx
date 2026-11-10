import { createBrowserRouter, RouterProvider } from 'react-router-dom'

import { AuthProvider } from './auth/AuthProvider'
import { RequiereAuth, SoloInvitados } from './auth/guards'
import { ActualizacionAviso } from './components/organisms/ActualizacionAviso'
import { AuthLayout } from './components/templates/AuthLayout'
import { MainLayout } from './components/templates/MainLayout'
import { DashboardPage } from './pages/DashboardPage'
import { EntrenamientoPage } from './pages/EntrenamientoPage'
import { LoginPage } from './pages/LoginPage'
import { NotFoundPage } from './pages/NotFoundPage'
import { PerfilPage } from './pages/PerfilPage'
import { RegisterPage } from './pages/RegisterPage'
import { RutinaDetallePage } from './pages/RutinaDetallePage'
import { RutinaFormPage } from './pages/RutinaFormPage'
import { RutinasPage } from './pages/RutinasPage'
import { PreferenciasProvider } from './preferencias/PreferenciasProvider'

const router = createBrowserRouter([
  {
    // Invitados: /login y /register (con sesión activa redirigen al inicio o a la ruta pedida).
    element: <SoloInvitados />,
    children: [
      {
        element: <AuthLayout />,
        children: [
          { path: '/login', element: <LoginPage /> },
          { path: '/register', element: <RegisterPage /> },
        ],
      },
    ],
  },
  {
    // Autenticadas: sin sesión (o tras un 401) redirigen a /login.
    element: <RequiereAuth />,
    children: [
      {
        element: <MainLayout />,
        children: [
          { path: '/', element: <DashboardPage /> },
          { path: '/rutinas', element: <RutinasPage /> },
          { path: '/rutinas/nueva', element: <RutinaFormPage /> },
          { path: '/rutinas/:id', element: <RutinaDetallePage /> },
          { path: '/rutinas/:id/editar', element: <RutinaFormPage /> },
          { path: '/entrenamiento', element: <EntrenamientoPage /> },
          { path: '/perfil', element: <PerfilPage /> },
        ],
      },
    ],
  },
  { path: '*', element: <NotFoundPage /> },
])

export default function App() {
  return (
    <PreferenciasProvider>
      <AuthProvider>
        <RouterProvider router={router} />
        <ActualizacionAviso />
      </AuthProvider>
    </PreferenciasProvider>
  )
}
