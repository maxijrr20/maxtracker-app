package com.maxtracker.api.service.impl;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.maxtracker.api.dto.EjercicioDto;
import com.maxtracker.api.exception.EjercicioNoEncontradoException;
import com.maxtracker.api.model.Ejercicio;
import com.maxtracker.api.repository.EjercicioRepository;
import com.maxtracker.api.service.EjercicioService;

/**
 * Obtiene la entidad {@link Ejercicio} para una serie (ver §4.3 del informe):
 * <ul>
 *   <li>por ID local: una copia de wger o un personalizado <b>del mismo usuario</b>;</li>
 *   <li>por ID de wger: la copia local si ya existe; si no, se consulta wger y se guarda la copia.</li>
 * </ul>
 */
@Component
class ResolutorEjercicio {

    private static final int LARGO_NOMBRE = 150;
    private static final int LARGO_CATEGORIA = 50;

    private final EjercicioRepository ejercicioRepository;
    private final EjercicioService catalogo;
    private final TransactionTemplate nuevaTransaccion;

    ResolutorEjercicio(EjercicioRepository ejercicioRepository, EjercicioService catalogo,
            PlatformTransactionManager transactionManager) {
        this.ejercicioRepository = ejercicioRepository;
        this.catalogo = catalogo;
        this.nuevaTransaccion = new TransactionTemplate(transactionManager);
        this.nuevaTransaccion.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /** @throws EjercicioNoEncontradoException si no existe o es un personalizado de otro usuario */
    Ejercicio porIdLocal(long ejercicioId, long usuarioId) {
        return ejercicioRepository.findAccesible(ejercicioId, usuarioId)
                .orElseThrow(() -> new EjercicioNoEncontradoException(ejercicioId));
    }

    /**
     * @throws EjercicioNoEncontradoException                    si wger no tiene ese ejercicio
     * @throws com.maxtracker.api.exception.WgerApiException si wger no está disponible (y no hay copia local)
     */
    Ejercicio porWgerId(long wgerId) {
        return ejercicioRepository.findByWgerId(wgerId).orElseGet(() -> crearCopia(wgerId));
    }

    /**
     * La copia se inserta en su propia transacción: si otra petición la creó a la vez, la violación de
     * {@code uk_ejercicio_wger} no invalida la transacción que registra la serie y basta con releerla.
     */
    private Ejercicio crearCopia(long wgerId) {
        EjercicioDto datos = catalogo.obtenerPorId(wgerId);
        String nombre = datos.nombre() == null || datos.nombre().isBlank()
                ? "Ejercicio wger #" + wgerId
                : datos.nombre();
        try {
            Long id = nuevaTransaccion.execute(estado -> ejercicioRepository.saveAndFlush(
                    Ejercicio.desdeWger(wgerId, recortar(nombre, LARGO_NOMBRE),
                            recortar(datos.categoria(), LARGO_CATEGORIA))).getId());
            return ejercicioRepository.getReferenceById(id);
        } catch (DataIntegrityViolationException ex) {
            return ejercicioRepository.findByWgerId(wgerId).orElseThrow(() -> ex);
        }
    }

    private static String recortar(String texto, int largo) {
        return texto == null || texto.length() <= largo ? texto : texto.substring(0, largo);
    }
}
