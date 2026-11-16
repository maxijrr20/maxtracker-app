package com.maxtracker.api.service.impl;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maxtracker.api.dto.constancia.ConstanciaResponse;
import com.maxtracker.api.dto.constancia.PlanEntrenamientoRequest;
import com.maxtracker.api.dto.constancia.PlanEntrenamientoResponse;
import com.maxtracker.api.model.PlanEntrenamiento;
import com.maxtracker.api.repository.PlanEntrenamientoRepository;
import com.maxtracker.api.repository.SesionEntrenamientoRepository;
import com.maxtracker.api.repository.UsuarioRepository;
import com.maxtracker.api.service.ConstanciaService;
import com.maxtracker.api.service.constancia.CalculadoraConstancia;
import com.maxtracker.api.service.constancia.CalculadoraConstancia.PlanVigente;
import com.maxtracker.api.service.constancia.CalculadoraConstancia.Rachas;

import lombok.RequiredArgsConstructor;

/** Carga los días entrenados y el historial de planes, y delega los cálculos en {@link CalculadoraConstancia}. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaConstanciaService implements ConstanciaService {

    private final SesionEntrenamientoRepository sesionRepository;
    private final PlanEntrenamientoRepository planRepository;
    private final UsuarioRepository usuarioRepository;
    private final Clock clock;

    @Override
    public ConstanciaResponse consultar(long usuarioId, YearMonth mes) {
        LocalDate hoy = LocalDate.now(clock);
        YearMonth consultado = mes != null ? mes : YearMonth.from(hoy);
        Set<LocalDate> entrenados = new HashSet<>(sesionRepository.findFechasEntrenadas(usuarioId));
        List<PlanEntrenamiento> historial = planRepository.findByUsuarioIdOrderByVigenteDesdeAsc(usuarioId);
        List<PlanVigente> planes = historial.stream()
                .map(p -> new PlanVigente(p.getVigenteDesde(), p.getDias()))
                .toList();

        Rachas rachas = CalculadoraConstancia.rachas(entrenados, planes, hoy);
        LocalDate inicio = CalculadoraConstancia.inicioHistorial(entrenados, planes, hoy);
        return new ConstanciaResponse(
                consultado.toString(),
                CalculadoraConstancia.calendario(consultado, entrenados, planes, hoy),
                CalculadoraConstancia.resumen(consultado.atDay(1), consultado.atEndOfMonth(), entrenados, planes, hoy),
                CalculadoraConstancia.resumen(inicio, hoy, entrenados, planes, hoy),
                rachas != null ? rachas.actual() : null,
                rachas != null ? rachas.mejor() : null,
                historial.isEmpty() ? null : aResponse(historial.getLast()));
    }

    @Override
    public Optional<PlanEntrenamientoResponse> planVigente(long usuarioId) {
        return planRepository.findFirstByUsuarioIdOrderByVigenteDesdeDesc(usuarioId)
                .map(JpaConstanciaService::aResponse);
    }

    @Override
    @Transactional
    public PlanEntrenamientoResponse guardarPlan(long usuarioId, PlanEntrenamientoRequest request) {
        LocalDate hoy = LocalDate.now(clock);
        List<DayOfWeek> dias = request.dias().stream().map(DayOfWeek::of).toList();
        Optional<PlanEntrenamiento> vigente = planRepository.findFirstByUsuarioIdOrderByVigenteDesdeDesc(usuarioId);

        if (vigente.isPresent() && vigente.get().mismosDias(dias)) {
            return aResponse(vigente.get()); // sin cambios: no crea historial nuevo
        }
        PlanEntrenamiento plan;
        if (vigente.isPresent() && vigente.get().getVigenteDesde().isEqual(hoy)) {
            // Segundo cambio en el mismo día: reemplaza el de hoy (UNIQUE usuario + vigente_desde).
            plan = vigente.get();
            plan.setDias(dias);
        } else {
            plan = new PlanEntrenamiento(usuarioRepository.getReferenceById(usuarioId), dias, hoy);
        }
        return aResponse(planRepository.saveAndFlush(plan));
    }

    private static PlanEntrenamientoResponse aResponse(PlanEntrenamiento plan) {
        List<Integer> dias = plan.getDias().stream().map(DayOfWeek::getValue).sorted().toList();
        return new PlanEntrenamientoResponse(dias, plan.getVigenteDesde());
    }
}
