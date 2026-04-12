package sn.edu.ept.user_service.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ProfilNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProfilNotFound(
            ProfilNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, "Profil non trouvé", ex.getMessage());
    }

    @ExceptionHandler(ProfilAlreadyExistException.class)
    public ResponseEntity<Map<String, Object>> handleProfilAlreadyExists(
            ProfilAlreadyExistException ex) {
        return buildError(HttpStatus.CONFLICT, "Profil déjà existant", ex.getMessage());
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, Object>> handleExpiredJwtException(ExpiredJwtException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, "Token expiré", 
            "Votre session a expiré. Veuillez vous reconnecter pour obtenir un nouveau token.");
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, Object>> handleJwtException(JwtException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, "Token invalide", 
            "Le token fourni est invalide ou a été blacklisté. Veuillez vous reconnecter.");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, "Erreur d'authentification", 
            "Vous devez être authentifié pour accéder à cette ressource.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
        return buildError(HttpStatus.FORBIDDEN, "Accès refusé", 
            "Vous n'avez pas les permissions nécessaires pour accéder à cette ressource.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Erreur de validation");
        body.put("message", "Les données fournies sont invalides");
        body.put("validationErrors", errors);
        
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildError(HttpStatus.BAD_REQUEST, "Requête invalide", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Erreur interne du serveur", ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne du serveur", 
            "Une erreur inattendue s'est produite. Veuillez réessayer plus tard.");
    }

    private ResponseEntity<Map<String, Object>> buildError(
            HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
