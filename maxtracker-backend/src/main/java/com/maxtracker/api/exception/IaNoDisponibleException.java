package com.maxtracker.api.exception;

/**
 * El análisis con IA no se pudo hacer: el proveedor no está configurado, falló, agotó la cuota gratuita o no
 * respondió a tiempo. Se responde como 503 (el resto de la app sigue funcionando).
 */
public class IaNoDisponibleException extends RuntimeException {

    private IaNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public static IaNoDisponibleException noConfigurada() {
        return new IaNoDisponibleException(
                "El análisis con IA no está configurado en el servidor (falta GEMINI_API_KEY)", null);
    }

    public static IaNoDisponibleException cuotaAgotada(Throwable causa) {
        return new IaNoDisponibleException(
                "Se alcanzó el límite de uso gratuito del servicio de IA. Intenta nuevamente en unos minutos", causa);
    }

    public static IaNoDisponibleException fallo(Throwable causa) {
        return new IaNoDisponibleException("El servicio de IA no está disponible. Intenta nuevamente en unos minutos",
                causa);
    }

    public static IaNoDisponibleException respuestaInvalida(String motivo) {
        return new IaNoDisponibleException("El servicio de IA no pudo generar un análisis válido (" + motivo + ")",
                null);
    }
}
