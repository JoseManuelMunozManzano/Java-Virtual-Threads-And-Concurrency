package com.jmunoz.sec09.controller;

import com.jmunoz.sec09.security.SecurityContext;
import com.jmunoz.sec09.security.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

// @RestController
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);
    private final Supplier<SecurityContext> securityContextSupplier;

    public DocumentController(Supplier<SecurityContext> securityContextSupplier) {
        this.securityContextSupplier = securityContextSupplier;
    }

    // En la vida real, usando Spring, usaríamos esta anotación y por detrás, Spring usaría el objeto
    // SecurityContextSupplier y obtendría la información de conexión del usuario y validaría si el usuario
    // tiene el permiso VIEWER.
    // Como no estamos usando el framework Spring, esto lo tenemos que hacer nosotros.
    // @HasRole(VIEWER)
    public void read() {
        this.validateUserRole(UserRole.VIEWER);
        log.info("reading");
    }

    // @HasRole(EDITOR)
    public void edit() {
        this.validateUserRole(UserRole.EDITOR);
        log.info("editing");
    }

    // @HasRole(ADMIN)
    public void delete() {
        this.validateUserRole(UserRole.ADMIN);
        log.info("deleting");
    }

    private void validateUserRole(UserRole requiredRole) {
        var securityContext = this.securityContextSupplier.get();
        if (!securityContext.hasPermission(requiredRole)) {
            log.error("user {} does not have {} permission", securityContext.userId(), requiredRole);
            throw new SecurityException("Unauthorized access. Required role: " + requiredRole);
        }
    }
}
