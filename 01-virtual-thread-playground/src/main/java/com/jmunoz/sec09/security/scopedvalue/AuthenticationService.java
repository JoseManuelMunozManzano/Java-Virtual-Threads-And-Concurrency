package com.jmunoz.sec09.security.scopedvalue;

import com.jmunoz.sec09.security.SecurityContext;
import com.jmunoz.sec09.security.UserRole;

import java.util.Map;

public class AuthenticationService {

    private static final String VALID_PASSWORD = "password";
    private static final Map<Integer, UserRole> USER_ROLES = Map.of(
            1, UserRole.ADMIN,
            2, UserRole.EDITOR,
            3, UserRole.VIEWER
    );

    public static void loginAndExecute(Integer userId, String password, Runnable runnable) {
        // Para mantenerlo simple, todos los roles de usuario tienen el mismo password.
        if (!VALID_PASSWORD.equals(password)) {
            throw new SecurityException("Invalid password for user id: " + userId);
        }

        // Este es el usuario actualmente conectado.
        // Si no existe el UserRole, por defecto será ANONYMOUS.
        var securityContext = new SecurityContext(userId, USER_ROLES.getOrDefault(userId, UserRole.ANONYMOUS));
        // bind
        ScopedValue.where(SecurityContextHolder.getScopedValue(), securityContext)
                .run(runnable);
    }

    // Se añade la posibilidad de elevar los privilegios del rol del usuario (como Run as Administrator en Windows)
    public static void runAsAdmin(Runnable runnable) {
        // Si el usuario se ha conectado, existe valor para la key.
        var securityContext = SecurityContextHolder.getScopedValue()
                .orElseThrow(() -> new SecurityException("User must login"));
        // Aquí indicamos el nuevo valor del rol como ADMIN.
        var elevatedContext = new SecurityContext(securityContext.userId(), UserRole.ADMIN);
        ScopedValue.where(SecurityContextHolder.getScopedValue(), elevatedContext)
                .run(runnable);
    }
}
