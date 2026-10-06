package digitales_hausbuch_backend.exception;

import digitales_hausbuch_backend.householdobject.HouseholdObjectNotFoundException;
import digitales_hausbuch_backend.maintenance.InvalidRecurrenceException;
import digitales_hausbuch_backend.maintenance.MaintenanceTaskNotFoundException;
import digitales_hausbuch_backend.property.PropertyNotFoundException;
import digitales_hausbuch_backend.room.RoomNotFoundException;
import digitales_hausbuch_backend.user.EmailAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.HashMap;
import java.util.Map;

/**
 * Central exception handler for REST API errors.
 *
 * <p>Maps domain-specific exceptions and validation errors to appropriate
 * HTTP status codes and consistent JSON error responses.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex) {

        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(
            BadCredentialsException ex) {

        Map<String, String> response = new HashMap<>();
        response.put(
                "message",
                "E-Mail-Adresse oder Passwort ist ungültig."
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(PropertyNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePropertyNotFound(
            PropertyNotFoundException ex) {

        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleRoomNotFound(
            RoomNotFoundException ex) {

        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(HouseholdObjectNotFoundException.class)
    public ResponseEntity<Map<String, String>>
    handleHouseholdObjectNotFound(
            HouseholdObjectNotFoundException ex) {

        Map<String, String> response =
                new HashMap<>();

        response.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(MaintenanceTaskNotFoundException.class)
    public ResponseEntity<Map<String, String>>
    handleMaintenanceTaskNotFound(
            MaintenanceTaskNotFoundException ex) {

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "message",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(InvalidRecurrenceException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRecurrence(
            InvalidRecurrenceException ex) {

        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}
