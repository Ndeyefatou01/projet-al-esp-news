package esp.sn.projet_al_backend.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        LocalDateTime horodatage,
        int statut,
        String erreur,
        String message,
        List<String> details
) {
    public ErrorResponse(int statut, String erreur, String message) {
        this(LocalDateTime.now(), statut, erreur, message, null);
    }

    public ErrorResponse(int statut, String erreur, String message, List<String> details) {
        this(LocalDateTime.now(), statut, erreur, message, details);
    }
}
