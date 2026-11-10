package com.maxtracker.api.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.maxtracker.api.config.SecurityConfig;

/**
 * Para tests {@code @WebMvcTest}: carga la {@link SecurityConfig} real (rutas públicas/protegidas,
 * CSRF, CORS, errores 401/403) con {@link JwtService} y {@link UsuarioDetailsService} simulados,
 * así el slice no necesita base de datos ni {@code JWT_SECRET}.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({SecurityConfig.class, SecurityProblemHandler.class})
@MockitoBean(types = {JwtService.class, UsuarioDetailsService.class})
public @interface ConSeguridadJwt {
}
