package com.calzadosmorales.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.calzadosmorales.entity.PersonaNatural;
import com.calzadosmorales.entity.PersonaJuridica;
import com.calzadosmorales.entity.Cliente;
import com.calzadosmorales.service.ClienteService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteService service;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("personasNaturales", service.listarPersonasNaturales());
        model.addAttribute("personasJuridicas", service.listarPersonasJuridicas());
        model.addAttribute("personaNatural", new PersonaNatural());
        model.addAttribute("personaJuridica", new PersonaJuridica());
        return "clientes";
    }

  

    @PostMapping("/guardarNatural")
    public String guardarPersonaNatural(
            @Valid @ModelAttribute("personaNatural") PersonaNatural persona,
            BindingResult result,
            Model model,
            RedirectAttributes flash) {

        if (result.hasErrors()) {
            model.addAttribute("personasNaturales", service.listarPersonasNaturales());
            model.addAttribute("personasJuridicas", service.listarPersonasJuridicas());
            model.addAttribute("personaJuridica", new PersonaJuridica());
            model.addAttribute("modalOpenNatural", true);
            return "clientes";
        }

       
        if (persona.getId_cliente() != null) {
           
            PersonaNatural original = service.buscarPersonaNaturalPorId(persona.getId_cliente());
            if (original != null) {
                persona.setEstado(original.getEstado()); 
                persona.setFecha_registro(original.getFecha_registro()); 
            }
        }
       

    
        PersonaNatural existeDni = service.buscarPorDni(persona.getDni());
        if (existeDni != null && !existeDni.getId_cliente().equals(persona.getId_cliente())) {
            flash.addFlashAttribute("error", "El DNI " + persona.getDni() + " ya existe.");
            return "redirect:/clientes";
        }
        if (validarComunes(persona, flash)) return "redirect:/clientes";

        service.guardarPersonaNatural(persona);
        flash.addFlashAttribute("success", "Persona Natural guardada correctamente.");
        return "redirect:/clientes";
    }


    @PostMapping("/guardarJuridica")
    public String guardarPersonaJuridica(
            @Valid @ModelAttribute("personaJuridica") PersonaJuridica persona,
            BindingResult result,
            Model model,
            RedirectAttributes flash) {

        if (result.hasErrors()) {
            model.addAttribute("personasNaturales", service.listarPersonasNaturales());
            model.addAttribute("personasJuridicas", service.listarPersonasJuridicas());
            model.addAttribute("personaNatural", new PersonaNatural());
            model.addAttribute("modalOpenJuridica", true);
            return "clientes";
        }


        if (persona.getId_cliente() != null) {
            PersonaJuridica original = service.buscarPersonaJuridicaPorId(persona.getId_cliente());
            if (original != null) {
                persona.setEstado(original.getEstado()); 
                persona.setFecha_registro(original.getFecha_registro());
            }
        }
      

        PersonaJuridica existeRuc = service.buscarPorRuc(persona.getRuc());
        if (existeRuc != null && !existeRuc.getId_cliente().equals(persona.getId_cliente())) {
            flash.addFlashAttribute("error", "El RUC " + persona.getRuc() + " ya existe.");
            return "redirect:/clientes";
        }
        PersonaJuridica existeRazon = service.buscarPorRazonSocial(persona.getRazonSocial());
        if (existeRazon != null && !existeRazon.getId_cliente().equals(persona.getId_cliente())) {
            flash.addFlashAttribute("error", "La Razón Social ya existe.");
            return "redirect:/clientes";
        }
        if (validarComunes(persona, flash)) return "redirect:/clientes";

        service.guardarPersonaJuridica(persona);
        flash.addFlashAttribute("success", "Empresa guardada correctamente.");
        return "redirect:/clientes";
    }

    private boolean validarComunes(Cliente cliente, RedirectAttributes flash) {
        Cliente existeEmail = service.buscarPorEmail(cliente.getEmail());
        if (existeEmail != null && !existeEmail.getId_cliente().equals(cliente.getId_cliente())) {
            flash.addFlashAttribute("error", "El Email ya está registrado.");
            return true;
        }
        Cliente existeTelefono = service.buscarPorTelefono(cliente.getTelefono());
        if (existeTelefono != null && !existeTelefono.getId_cliente().equals(cliente.getId_cliente())) {
            flash.addFlashAttribute("error", "El Teléfono ya está registrado.");
            return true;
        }
        return false;
    }

 
    @GetMapping("/cambiarEstado/{id}/{estado}/{tipo}")
    public String cambiarEstado(
            @PathVariable("id") Integer id, 
            @PathVariable("estado") boolean nuevoEstado,
            @PathVariable("tipo") String tipo,
            RedirectAttributes flash) {
        
        if (tipo.equals("natural")) {
            PersonaNatural p = service.buscarPersonaNaturalPorId(id);
            if (p != null) {
                p.setEstado(nuevoEstado);
                service.guardarPersonaNatural(p);
            }
        } else {
            PersonaJuridica p = service.buscarPersonaJuridicaPorId(id);
            if (p != null) {
                p.setEstado(nuevoEstado);
                service.guardarPersonaJuridica(p);
            }
        }
        
        String accion = nuevoEstado ? "activado" : "desactivado";
     
        flash.addFlashAttribute("success", "Cliente " + accion + " correctamente.");
        return "redirect:/clientes";
    }
}