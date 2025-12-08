package com.jmunoz.sec09.security.threadlocal;

import com.jmunoz.sec09.security.SecurityContext;
import com.jmunoz.sec09.security.UserRole;

// Fuera de este paquete, solo se le permite al thread ejecutar el méto-do getContext()
// para obtener información del usuario conectado.
public class SecurityContextHolder {

    // Si no hay usuario conectado, en vez de devolver null, devolvemos ANONYMOUS.
    // Y el ThreadLocal que creamos, ya lo hacemos con este valor por defecto (en un supplier).
    private static final SecurityContext ANONYMOUS_CONTEXT = new SecurityContext(0, UserRole.ANONYMOUS);
    private static final ThreadLocal<SecurityContext> contextHolder = ThreadLocal.withInitial(() -> ANONYMOUS_CONTEXT);

    // package private
    static void setContext(SecurityContext securityContext) {
        contextHolder.set(securityContext);
    }

    static void clear() {
        contextHolder.remove();
    }

    // public
    public static SecurityContext getContext() {
        return contextHolder.get();
    }
}
