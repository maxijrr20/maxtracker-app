package com.maxtracker.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Propiedades de la autenticación.
 *
 * @param registroHabilitado permite crear cuentas con {@code POST /api/v1/auth/register}
 *                           (variable {@code AUTH_REGISTRO_HABILITADO}). La app es de uso personal: una vez
 *                           creada la cuenta en producción conviene ponerla en {@code false}.
 */
@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(@DefaultValue("true") boolean registroHabilitado) {
}
