package com.maxtracker.api.mapper;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import com.maxtracker.api.config.WgerProperties;
import com.maxtracker.api.dto.EjercicioDto;
import com.maxtracker.api.dto.wger.WgerCategoria;
import com.maxtracker.api.dto.wger.WgerEjercicioInfo;
import com.maxtracker.api.dto.wger.WgerImagen;
import com.maxtracker.api.dto.wger.WgerMusculo;
import com.maxtracker.api.dto.wger.WgerTraduccion;

/** Convierte los DTOs externos de wger al DTO propio {@link EjercicioDto}. */
@Component
public class WgerEjercicioMapper {

    private static final Pattern ETIQUETAS_HTML = Pattern.compile("<[^>]+>");
    private static final Pattern ESPACIOS = Pattern.compile("\\s+");

    /** Categorías de wger (vienen en inglés) traducidas al español. */
    private static final Map<String, String> CATEGORIAS_ES = Map.of(
            "Abs", "Abdominales",
            "Arms", "Brazos",
            "Back", "Espalda",
            "Calves", "Pantorrillas",
            "Cardio", "Cardio",
            "Chest", "Pecho",
            "Legs", "Piernas",
            "Shoulders", "Hombros");

    private final int idiomaEspanol;
    private final int idiomaRespaldo;

    public WgerEjercicioMapper(WgerProperties properties) {
        this.idiomaEspanol = properties.idiomaEspanol();
        this.idiomaRespaldo = properties.idiomaRespaldo();
    }

    public EjercicioDto toDto(WgerEjercicioInfo info) {
        Optional<WgerTraduccion> traduccion = buscarTraduccion(info, idiomaEspanol)
                .or(() -> buscarTraduccion(info, idiomaRespaldo))
                .or(() -> traduccionesValidas(info).findFirst());

        String nombre = traduccion.map(t -> t.name().strip()).orElse("Ejercicio #" + info.id());
        String descripcion = traduccion.map(WgerTraduccion::description).map(this::limpiarHtml).orElse(null);
        String idioma = traduccion.map(t -> codigoIdioma(t.language())).orElse(null);

        WgerCategoria categoria = info.category();

        return new EjercicioDto(
                info.id(),
                nombre,
                descripcion,
                idioma,
                categoria != null ? categoria.id() : null,
                categoria != null ? CATEGORIAS_ES.getOrDefault(categoria.name(), categoria.name()) : null,
                nombresMusculos(info.muscles()),
                nombresMusculos(info.musclesSecondary()),
                nombres(info.equipment(), e -> e.name()),
                imagenPrincipal(info.images()));
    }

    private Optional<WgerTraduccion> buscarTraduccion(WgerEjercicioInfo info, int idioma) {
        return traduccionesValidas(info).filter(t -> t.language() == idioma).findFirst();
    }

    private java.util.stream.Stream<WgerTraduccion> traduccionesValidas(WgerEjercicioInfo info) {
        return listaSegura(info.translations()).stream()
                .filter(Objects::nonNull)
                .filter(t -> t.name() != null && !t.name().isBlank());
    }

    private String codigoIdioma(int idioma) {
        if (idioma == idiomaEspanol) {
            return "es";
        }
        if (idioma == idiomaRespaldo) {
            return "en";
        }
        return "otro";
    }

    /** Quita etiquetas HTML, decodifica entidades y normaliza espacios. */
    String limpiarHtml(String html) {
        if (html == null) {
            return null;
        }
        String sinEtiquetas = ETIQUETAS_HTML.matcher(html).replaceAll(" ");
        String texto = ESPACIOS.matcher(HtmlUtils.htmlUnescape(sinEtiquetas).replace(' ', ' '))
                .replaceAll(" ")
                .strip();
        return texto.isEmpty() ? null : texto;
    }

    private List<String> nombresMusculos(List<WgerMusculo> musculos) {
        return nombres(musculos, m -> m.nameEn() != null && !m.nameEn().isBlank() ? m.nameEn() : m.name());
    }

    private static <T> List<String> nombres(List<T> elementos, Function<T, String> extractor) {
        return listaSegura(elementos).stream()
                .filter(Objects::nonNull)
                .map(extractor)
                .filter(n -> n != null && !n.isBlank())
                .map(String::strip)
                .toList();
    }

    private static String imagenPrincipal(List<WgerImagen> imagenes) {
        return listaSegura(imagenes).stream()
                .filter(Objects::nonNull)
                .filter(i -> i.image() != null && !i.image().isBlank())
                .min(Comparator.comparing(i -> !i.isMain()))
                .map(WgerImagen::image)
                .orElse(null);
    }

    private static <T> List<T> listaSegura(List<T> lista) {
        return lista != null ? lista : List.of();
    }
}
