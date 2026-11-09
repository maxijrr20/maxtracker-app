package com.maxtracker.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.maxtracker.api.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** El email se guarda en minúsculas; normaliza también el parámetro. */
    default Optional<Usuario> buscarPorEmail(String email) {
        return findByEmail(Usuario.normalizarEmail(email));
    }

    default boolean existeEmail(String email) {
        return existsByEmail(Usuario.normalizarEmail(email));
    }

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}
