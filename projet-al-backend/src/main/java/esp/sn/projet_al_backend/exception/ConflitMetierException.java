package esp.sn.projet_al_backend.exception;

/**
 * Levée lorsqu'une règle de gestion empêche l'opération demandée
 * (ex : login déjà utilisé, nom de catégorie déjà existant...).
 * Mappée vers un statut HTTP 409 (Conflict) par le GlobalExceptionHandler.
 */
public class ConflitMetierException extends RuntimeException {

    public ConflitMetierException(String message) {
        super(message);
    }
}
