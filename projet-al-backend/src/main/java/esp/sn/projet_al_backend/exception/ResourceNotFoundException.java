package esp.sn.projet_al_backend.exception;

/**
 * Levée lorsqu'une ressource (article, catégorie, utilisateur, jeton...) demandée
 * par son id n'existe pas. Mappée vers un statut HTTP 404 par le GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
