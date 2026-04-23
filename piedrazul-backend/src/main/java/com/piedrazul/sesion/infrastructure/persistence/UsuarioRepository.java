package com.piedrazul.sesion.infrastructure.persistence;

import com.piedrazul.sesion.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByNumeroDocumento(String numeroDocumento);

    boolean existsByNumeroDocumento(String numeroDocumento);
}
