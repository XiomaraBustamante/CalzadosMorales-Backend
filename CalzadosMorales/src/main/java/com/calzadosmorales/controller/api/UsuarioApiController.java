package com.calzadosmorales.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.calzadosmorales.entity.Usuario;
import com.calzadosmorales.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioApiController {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private PasswordEncoder encoder;

    
    @PostMapping(value = "/login", produces = "application/json")
    public ResponseEntity<?> loginParaMovil(@RequestBody LoginRequest loginReq) {
        try {
            
            Usuario u = usuarioRepo.findByUsuario(loginReq.getUsuario());
            
           
            if (u != null && encoder.matches(loginReq.getClave(), u.getClave())) {
                if (!u.getEstado()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("{\"message\": \"El usuario está desactivado\"}");
                }
                
                return ResponseEntity.ok(u);
            }
            
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"message\": \"Usuario o clave incorrectos\"}");
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"message\": \"Error en el servidor central\"}");
        }
    }
}