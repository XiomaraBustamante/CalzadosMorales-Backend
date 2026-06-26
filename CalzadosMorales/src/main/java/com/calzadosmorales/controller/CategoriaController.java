package com.calzadosmorales.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.calzadosmorales.entity.Categoria;
import com.calzadosmorales.repository.CategoriaRepository;
import com.calzadosmorales.service.CategoriaService;


@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService service;
    
    @Autowired
    private CategoriaRepository repo; 

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", service.listarTodas());
        model.addAttribute("categoria", new Categoria()); 
        return "categorias";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Categoria categoria, RedirectAttributes flash) {
        

        Categoria posibleDuplicado = repo.findByNombre(categoria.getNombre());

        if (posibleDuplicado != null) {
     
            if (categoria.getId_categoria() == null) {
                flash.addFlashAttribute("error", "Ya existe una categoría llamada '" + categoria.getNombre() + "'.");
                return "redirect:/categorias";
            }
            
           
           
            if (!categoria.getId_categoria().equals(posibleDuplicado.getId_categoria())) {
                flash.addFlashAttribute("error", "No puedes usar el nombre '" + categoria.getNombre() + "' porque ya pertenece a otra categoría.");
                return "redirect:/categorias";
            }
        }


        if(categoria.getId_categoria() == null) {
            categoria.setEstado(true);
        } else {
          
           
            Categoria actual = repo.findById(categoria.getId_categoria()).orElse(null);
            if(actual != null) {
                categoria.setEstado(actual.getEstado());
            }
        }
        
        service.guardar(categoria);
        flash.addFlashAttribute("success", "Categoría guardada correctamente.");
        return "redirect:/categorias";
    }
    
    // LÓGICA DE ACTIVAR / DESACTIVAR 
    @GetMapping("/cambiarEstado/{id}/{estado}")
    public String cambiarEstado(@PathVariable("id") Integer id, @PathVariable("estado") boolean nuevoEstado, RedirectAttributes flash) {
      
        Categoria cat = repo.findById(id).orElse(null);
        if (cat != null) {
            cat.setEstado(nuevoEstado);
            service.guardar(cat);
            String mensaje = nuevoEstado ? "Categoría activada." : "Categoría desactivada.";
            flash.addFlashAttribute("info", mensaje);
        }
        return "redirect:/categorias";
    }
}