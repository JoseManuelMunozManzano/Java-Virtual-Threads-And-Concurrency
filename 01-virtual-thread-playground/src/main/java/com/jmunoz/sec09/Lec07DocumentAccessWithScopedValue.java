package com.jmunoz.sec09;

import com.jmunoz.sec09.controller.DocumentController;
import com.jmunoz.sec09.security.scopedvalue.AuthenticationService;
import com.jmunoz.sec09.security.scopedvalue.SecurityContextHolder;
import com.jmunoz.util.CommonUtils;

import java.time.Duration;

public class Lec07DocumentAccessWithScopedValue {

    // Indicamos quién puede proveer (supplier) SecurityContext.
    // Lo puede proveer SecurityContextHolder.
    private static final DocumentController documentController = new DocumentController(SecurityContextHolder::getContext);

    static void main() {
        // Error: El password `test` es erróneo.
//        documentAccessWorkflow(1, "test");

        // Error: Solo tenemos 3 userId (del 1 al 3) Ver Map en AuthenticationService.
//        documentAccessWorkflow(4, "password");

        // UserId 3 tiene permisos VIEWER. No puede editar.
        documentAccessWorkflow(3, "password");

        // UserId 2 tiene permisos EDITOR. No puede borrar.
//        documentAccessWorkflow(2, "password");

        // UserId 1 tiene permisos ADMIN. Puede hacerlo to-do.
//        documentAccessWorkflow(1, "password");

        // Comportamiento cuando dos thread diferentes intentan ejecutar con dos roles diferentes.
//        Thread.ofVirtual().name("admin").start(() -> documentAccessWorkflow(1, "password"));
//        Thread.ofVirtual().name("editor").start(() -> documentAccessWorkflow(2, "password"));

        // Para que el main thread no termine inmediatamente, ya que virtual threads son daemon threads.
//        CommonUtils.sleep(Duration.ofSeconds(1));
    }

    private static void documentAccessWorkflow(Integer userId, String password) {
        AuthenticationService.loginAndExecute(userId, password, () -> {
            documentController.read();

            // Se elevan los privilegios del rol del usuario (como Run as Administrator en Windows)
            // para que un rol que no puede editar ni borrar pueda hacerlo.
            AuthenticationService.runAsAdmin(() -> {
                documentController.edit();
                documentController.delete();
            });

            // Aquí los privilegios son los que tuviera el rol del usuario al principio, es decir, el ámbito admin terminó.
            documentController.delete();
        });
    }
}
