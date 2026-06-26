package com.calzadosmorales.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.calzadosmorales.entity.Material;
import com.calzadosmorales.repository.MaterialRepository;
import com.calzadosmorales.service.MaterialService;



@Controller
@RequestMapping("/materiales")
public class MaterialController {

    @Autowired
    private MaterialService service;
    
    @Autowired
    private MaterialRepository repo;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("materiales", service.listarTodos());
        model.addAttribute("material", new Material());
        return "materiales"; 
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Material material, RedirectAttributes flash) {
 
        Material posibleDuplicado = repo.findByNombre(material.getNombre());

        if (posibleDuplicado != null) {
            if (material.getId_material() == null) {
                flash.addFlashAttribute("error", "Ya existe el material '" + material.getNombre() + "'.");
                return "redirect:/materiales";
            }
            if (!material.getId_material().equals(posibleDuplicado.getId_material())) {
                flash.addFlashAttribute("error", "No puedes usar '" + material.getNombre() + "' porque ya existe.");
                return "redirect:/materiales";
            }
        }


        if(material.getId_material() == null) {
            material.setEstado(true);
        } else {
            Material actual = service.buscarPorId(material.getId_material());
            if(actual != null) {
                material.setEstado(actual.getEstado());
            }
        }
        
        service.guardar(material);
        flash.addFlashAttribute("success", "Material guardado correctamente.");
        return "redirect:/materiales";
    }

    @GetMapping("/cambiarEstado/{id}/{estado}")
    public String cambiarEstado(@PathVariable("id") Integer id, @PathVariable("estado") boolean nuevoEstado, RedirectAttributes flash) {
        Material material = service.buscarPorId(id);
        if (material != null) {
            material.setEstado(nuevoEstado);
            service.guardar(material);
            String mensaje = nuevoEstado ? "Material activado." : "Material desactivado.";
            flash.addFlashAttribute("info", mensaje);
        }
        return "redirect:/materiales";
    }
}