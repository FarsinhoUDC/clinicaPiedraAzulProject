package com.piedrazul.configuracion.infrastructure.persistence;

import com.piedrazul.configuracion.domain.ConfiguracionSistema;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracionSistemaRepository extends JpaRepository<ConfiguracionSistema, Long> {
}
