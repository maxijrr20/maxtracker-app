package com.maxtracker.api.dto.metricas;

import java.math.BigDecimal;
import java.util.List;

/**
 * Panel de inicio. El volumen es la suma de peso × repeticiones de las series (kg); las series con peso 0
 * (peso corporal) no suman volumen. Semana = lunes a domingo, en la zona horaria de la app.
 *
 * @param volumenSemanal una entrada por semana, de la más antigua a la actual, incluidas las semanas sin series
 * @param records        récord de peso de cada ejercicio, del más reciente al más antiguo
 */
public record ResumenMetricasResponse(
        PeriodoDto semanaActual,
        PeriodoDto semanaAnterior,
        PeriodoDto mesActual,
        PeriodoDto mesAnterior,
        List<VolumenSemanalDto> volumenSemanal,
        List<RecordPersonalDto> records) {

    public ResumenMetricasResponse {
        volumenSemanal = List.copyOf(volumenSemanal);
        records = List.copyOf(records);
    }

    /** Totales de un periodo. */
    public record PeriodoDto(BigDecimal volumenKg, int sesiones, int series) {
    }
}
