package com.jmunoz.sec09.security.scopedvalue;

import com.jmunoz.sec09.security.SecurityContext;
import com.jmunoz.sec09.security.UserRole;

// Fuera de este paquete, solo se le permite al thread ejecutar el méto-do getContext()
// para obtener información del usuario conectado.
public class SecurityContextHolder {

    // Si no hay usuario conectado, en vez de devolver null, devolvemos ANONYMOUS.
    private static final SecurityContext ANONYMOUS_CONTEXT = new SecurityContext(0, UserRole.ANONYMOUS);
    // Creamos un ScopedValue
    private static final ScopedValue<SecurityContext> CONTEXT = ScopedValue.newInstance();

    // package private
    static ScopedValue<SecurityContext> getScopedValue() {
        return CONTEXT;
    }

    // public
    public static SecurityContext getContext() {
        return CONTEXT.orElse(ANONYMOUS_CONTEXT);
    }
}
