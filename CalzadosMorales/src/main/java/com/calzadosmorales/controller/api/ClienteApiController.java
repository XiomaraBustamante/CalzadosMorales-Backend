package com.calzadosmorales.controller.api;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.calzadosmorales.entity.Cliente;
import com.calzadosmorales.service.ClienteService;

@RestController
@RequestMapping("/api/clientes")
public class ClienteApiController {

    @Autowired
    private ClienteService clienteService;

    // Este endpoint lo llamará el celular usando Volley para sincronizar los clientes
    @GetMapping(value = "/listar", produces = "application/json")
    public ResponseEntity<List<Cliente>> listarClientesParaMovil() {
        try {
            // Llamamos al método de tu servicio. 
            // Si te sale rojo "listar()", bórralo, pon "clienteService." y presiona Ctrl + Espacio para ver cómo se llama en tu proyecto.
            List<Cliente> lista = clienteService.listarTodos();
            
            if (lista == null || lista.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}