package com.jmunoz.sec09.security;

public record SecurityContext(Integer userId,
                              UserRole role) {

    public boolean hasPermission(UserRole requiredRole) {
        return this.role.ordinal() <= requiredRole.ordinal();
    }
}

/*
    Esta es la forma en la que funciona.
    En el enum UserRole, el ordinal de cada elemento es el valor que ocupa. ADMIN es 0, EDITOR es 1...
    Por eso abajo, en el return se indica 0 (ADMIN) < 1 (EDITOR) - true

    var securityContext = new SecurityContext(1, ADMIN);
    ...
    ...
    securityContext.hasPermission(EDITOR) -> true

    return 0 < 1 - true
*/