import { createBrowserRouter, RouterProvider } from 'react-router-dom'

import { AuthProvider } from './auth/AuthProvider'
import { RequiereAuth, SoloInvitados } from './auth/guards'
import { AuthLayout } from './components/templates/AuthLayout'
import { MainLayout } from './components/templates/MainLayout'
import { DashboardPage } from './pages/DashboardPage'
import { EntrenamientoPage } from './pages/EntrenamientoPage'
import { LoginPage } from './pages/LoginPage'
import { NotFoundPage } from './pages/NotFoundPage'
import { RegisterPage } from './pages/RegisterPage'
import { RutinaDetallePage } from './pages/RutinaDetallePage'
import { RutinasPage } from './pages/RutinasPage'

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
          { path: '/rutinas/:id', element: <RutinaDetallePage /> },
          { path: '/entrenamiento', element: <EntrenamientoPage /> },
        ],
      },
    ],
  },
  { path: '*', element: <NotFoundPage /> },
])

export default function App() {
  return (
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  )
}
