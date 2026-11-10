package com.maxtracker.api.dto.analisis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maxtracker.api.service.ia.OpcionesAnalisis.Enfoque;
import com.maxtracker.api.service.ia.OpcionesAnalisis.UnidadPeso;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

/**
 * Qué analizar. El historial lo arma el servidor a partir de las series del usuario (no se recibe del cliente:
 * así no se puede alterar y el prompt queda acotado). Todos los campos son opcionales:
 * <ul>
 *   <li>{@code rutinaId}: los ejercicios de esa rutina, con sus objetivos.</li>
 *   <li>{@code sesionId}: los ejercicios hechos en esa sesión (p. ej. al finalizarla).</li>
 *   <li>ninguno: todos los ejercicios entrenados en el periodo.</li>
 * </ul>
 *
 * @param dias       periodo de historial a considerar hacia atrás desde hoy (7–90, por defecto 28)
 * @param enfoque    FUERZA, HIPERTROFIA o MANTENIMIENTO; sin él, análisis general
 * @param unidadPeso KG (por defecto) o LB: unidad en la que la IA expresa las cargas
 */
public record AnalisisRutinaRequest(
        @Positive(message = "debe ser un ID positivo")
        Long rutinaId,

        @Positive(message = "debe ser un ID positivo")
        Long sesionId,

        @Min(value = 7, message = "debe estar entre 7 y 90")
        @Max(value = 90, message = "debe estar entre 7 y 90")
        Integer dias,

        Enfoque enfoque,

        UnidadPeso unidadPeso) {

    public static final int DIAS_POR_DEFECTO = 28;

    public static AnalisisRutinaRequest general() {
        return new AnalisisRutinaRequest(null, null, null, null, null);
    }

    public int diasOPorDefecto() {
        return dias != null ? dias : DIAS_POR_DEFECTO;
    }

    @JsonIgnore
    @AssertTrue(message = "indica como máximo uno de rutinaId o sesionId")
    public boolean isAlcance() {
        return rutinaId == null || sesionId == null;
    }
}
