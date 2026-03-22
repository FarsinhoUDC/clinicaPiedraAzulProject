package com.piedrazul.configuracion.application;

import com.piedrazul.configuracion.domain.ConfiguracionSistema;
import com.piedrazul.configuracion.domain.DisponibilidadMedico;
import com.piedrazul.configuracion.dto.*;
import com.piedrazul.configuracion.infrastructure.persistence.ConfiguracionSistemaRepository;
import com.piedrazul.configuracion.infrastructure.persistence.DisponibilidadMedicoRepository;
import com.piedrazul.medicos.application.MedicoService;
import com.piedrazul.medicos.domain.Medico;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfiguracionService {

    private final ConfiguracionSistemaRepository configuracionSistemaRepository;
    private final DisponibilidadMedicoRepository disponibilidadMedicoRepository;
    private final MedicoService medicoService;

    @Transactional
    public ConfiguracionSistema guardarConfiguracionSistema(ConfiguracionSistemaRequest request) {
        ConfiguracionSistema config = configuracionSistemaRepository.findAll()
                .stream().findFirst()
                .orElseGet(ConfiguracionSistema::new);
        config.setVentanaSemanas(request.getVentanaSemanas());
        return configuracionSistemaRepository.save(config);
    }

    @Transactional(readOnly = true)
    public ConfiguracionSistema obtenerConfiguracionSistema() {
        return configuracionSistemaRepository.findAll().stream().findFirst()
                .orElseGet(() -> { ConfiguracionSistema c = new ConfiguracionSistema(); c.setVentanaSemanas(4); return c; });
    }

    @Transactional
    public DisponibilidadMedicoResponse guardarDisponibilidad(DisponibilidadMedicoRequest request) {
        Medico medico = medicoService.obtenerPorId(request.getMedicoId());
        DisponibilidadMedico disp = disponibilidadMedicoRepository
                .findByMedicoId(request.getMedicoId())
                .orElseGet(DisponibilidadMedico::new);

        disp.setMedico(medico);
        disp.setDiasSemana(request.getDiasSemana());
        disp.setHoraInicio(request.getHoraInicio());
        disp.setHoraFin(request.getHoraFin());
        disp.setIntervaloMinutos(request.getIntervaloMinutos());

        return toResponse(disponibilidadMedicoRepository.save(disp));
    }

    @Transactional(readOnly = true)
    public List<DisponibilidadMedicoResponse> listarDisponibilidades() {
        return disponibilidadMedicoRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    private DisponibilidadMedicoResponse toResponse(DisponibilidadMedico d) {
        Medico m = d.getMedico();
        return DisponibilidadMedicoResponse.builder()
                .id(d.getId()).medicoId(m.getId())
                .nombreMedico(m.getNombres() + " " + m.getApellidos())
                .diasSemana(d.getDiasSemana())
                .horaInicio(d.getHoraInicio()).horaFin(d.getHoraFin())
                .intervaloMinutos(d.getIntervaloMinutos())
                .build();
    }
}
