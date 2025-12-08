package com.jmunoz.sec09.security.threadlocal;

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

        try {
            // Este es el usuario actualmente conectado.
            // Si no existe el UserRole, por defecto será ANONYMOUS.
            var securityContext = new SecurityContext(userId, USER_ROLES.getOrDefault(userId, UserRole.ANONYMOUS));
            // Aquí es donde pasamos al ThreadLocal el valor de securityContext.
            SecurityContextHolder.setContext(securityContext);
            // Ejecutamos el runnable.
            runnable.run();
        } finally {
            // Eliminamos ThreadLocal.
            SecurityContextHolder.clear();
        }
    }
}
