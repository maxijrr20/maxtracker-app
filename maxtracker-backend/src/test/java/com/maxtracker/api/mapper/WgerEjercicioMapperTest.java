package com.maxtracker.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.maxtracker.api.config.WgerProperties;
import com.maxtracker.api.dto.EjercicioDto;
import com.maxtracker.api.dto.wger.WgerCategoria;
import com.maxtracker.api.dto.wger.WgerEjercicioInfo;
import com.maxtracker.api.dto.wger.WgerEquipamiento;
import com.maxtracker.api.dto.wger.WgerImagen;
import com.maxtracker.api.dto.wger.WgerMusculo;
import com.maxtracker.api.dto.wger.WgerTraduccion;

class WgerEjercicioMapperTest {

    private static final int ESPANOL = 4;
    private static final int INGLES = 2;

    private final WgerEjercicioMapper mapper = new WgerEjercicioMapper(
            new WgerProperties("https://wger.de/api/v2/", ESPANOL, INGLES, Duration.ofSeconds(5), Duration.ofSeconds(10)));

    @Test
    void usaLaTraduccionEnEspanolCuandoExiste() {
        WgerEjercicioInfo info = ejercicio(List.of(
                new WgerTraduccion(1, "Bench Press", "<p>Push the bar</p>", INGLES),
                new WgerTraduccion(2, "Press de banca", "<p>Empuja la barra</p>", ESPANOL)));

        EjercicioDto dto = mapper.toDto(info);

        assertThat(dto.id()).isEqualTo(192);
        assertThat(dto.nombre()).isEqualTo("Press de banca");
        assertThat(dto.descripcion()).isEqualTo("Empuja la barra");
        assertThat(dto.idioma()).isEqualTo("es");
    }

    @Test
    void usaInglesComoRespaldoSiNoHayTraduccionAlEspanol() {
        WgerEjercicioInfo info = ejercicio(List.of(
                new WgerTraduccion(1, "Bankdrücken", null, 1),
                new WgerTraduccion(2, "Bench Press", "Push the bar", INGLES)));

        EjercicioDto dto = mapper.toDto(info);

        assertThat(dto.nombre()).isEqualTo("Bench Press");
        assertThat(dto.idioma()).isEqualTo("en");
    }

    @Test
    void ignoraTraduccionesConNombreVacio() {
        WgerEjercicioInfo info = ejercicio(List.of(
                new WgerTraduccion(1, "  ", "vacía", ESPANOL),
                new WgerTraduccion(2, "Bench Press", null, INGLES)));

        assertThat(mapper.toDto(info).nombre()).isEqualTo("Bench Press");
    }

    @Test
    void usaUnNombreGenericoSiNoHayTraducciones() {
        EjercicioDto dto = mapper.toDto(ejercicio(null));

        assertThat(dto.nombre()).isEqualTo("Ejercicio #192");
        assertThat(dto.descripcion()).isNull();
        assertThat(dto.idioma()).isNull();
    }

    @Test
    void limpiaEtiquetasHtmlYEntidadesDeLaDescripcion() {
        WgerEjercicioInfo info = ejercicio(List.of(new WgerTraduccion(1, "Sentadilla",
                "<p>Baja&nbsp;lento</p>\n<ul><li>Espalda &amp; core firmes</li></ul>", ESPANOL)));

        assertThat(mapper.toDto(info).descripcion()).isEqualTo("Baja lento Espalda & core firmes");
    }

    @Test
    void mapeaCategoriaMusculosEquipamientoEImagenPrincipal() {
        WgerEjercicioInfo info = new WgerEjercicioInfo(192, "uuid", new WgerCategoria(11, "Chest"),
                List.of(new WgerMusculo(4, "Pectoralis major", "Chest")),
                List.of(new WgerMusculo(5, "Triceps brachii", ""), new WgerMusculo(2, "Anterior deltoid", "Shoulders")),
                List.of(new WgerEquipamiento(1, "Barbell"), new WgerEquipamiento(8, "Bench")),
                List.of(new WgerTraduccion(1, "Press de banca", null, ESPANOL)),
                List.of(new WgerImagen(1, "https://wger.de/media/secundaria.png", false),
                        new WgerImagen(2, "https://wger.de/media/principal.png", true)));

        EjercicioDto dto = mapper.toDto(info);

        assertThat(dto.categoriaId()).isEqualTo(11);
        assertThat(dto.categoria()).isEqualTo("Pecho");
        assertThat(dto.musculosPrincipales()).containsExactly("Chest");
        assertThat(dto.musculosSecundarios()).containsExactly("Triceps brachii", "Shoulders");
        assertThat(dto.equipamiento()).containsExactly("Barbell", "Bench");
        assertThat(dto.imagenUrl()).isEqualTo("https://wger.de/media/principal.png");
    }

    @Test
    void toleraListasNulasYCategoriaDesconocida() {
        WgerEjercicioInfo info = new WgerEjercicioInfo(7, "uuid", new WgerCategoria(99, "Stretching"),
                null, null, null, List.of(new WgerTraduccion(1, "Estiramiento", null, ESPANOL)), null);

        EjercicioDto dto = mapper.toDto(info);

        assertThat(dto.categoria()).isEqualTo("Stretching");
        assertThat(dto.musculosPrincipales()).isEmpty();
        assertThat(dto.musculosSecundarios()).isEmpty();
        assertThat(dto.equipamiento()).isEmpty();
        assertThat(dto.imagenUrl()).isNull();
    }

    private static WgerEjercicioInfo ejercicio(List<WgerTraduccion> traducciones) {
        return new WgerEjercicioInfo(192, "uuid", new WgerCategoria(11, "Chest"),
                List.of(), List.of(), List.of(), traducciones, List.of());
    }
}
