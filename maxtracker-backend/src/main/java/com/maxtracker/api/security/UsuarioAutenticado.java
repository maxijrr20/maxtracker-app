package com.maxtracker.api.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.maxtracker.api.model.Usuario;

/**
 * Principal de Spring Security. Expone el {@code id} para que los controladores puedan filtrar
 * los datos por dueño con {@code @AuthenticationPrincipal UsuarioAutenticado usuario}.
 */
public record UsuarioAutenticado(Long id, String email, String passwordHash) implements UserDetails {

    private static final List<GrantedAuthority> AUTORIDADES = List.of(new SimpleGrantedAuthority("ROLE_USER"));

    public static UsuarioAutenticado desde(Usuario usuario) {
        return new UsuarioAutenticado(usuario.getId(), usuario.getEmail(), usuario.getPasswordHash());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AUTORIDADES;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String toString() {
        return "UsuarioAutenticado[id=" + id + ", email=" + email + "]";
    }
}
