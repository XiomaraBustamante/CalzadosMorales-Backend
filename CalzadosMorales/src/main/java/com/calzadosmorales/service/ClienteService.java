package com.calzadosmorales.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.calzadosmorales.entity.PersonaNatural;
import com.calzadosmorales.entity.PersonaJuridica;
import com.calzadosmorales.repository.ClienteRepository;
import com.calzadosmorales.repository.PersonaNaturalRepository;
import com.calzadosmorales.repository.PersonaJuridicaRepository;
import com.calzadosmorales.entity.Cliente;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;
    
    @Autowired
    private PersonaNaturalRepository personaNaturalRepository;
    
    @Autowired
    private PersonaJuridicaRepository personaJuridicaRepository;


    
    public List<PersonaNatural> listarPersonasNaturales() {
        return personaNaturalRepository.findAll();
    }
    
    public void guardarPersonaNatural(PersonaNatural personaNatural) {
        personaNaturalRepository.save(personaNatural);
    }
    
    public PersonaNatural buscarPersonaNaturalPorId(Integer id) {
        return personaNaturalRepository.findById(id).orElse(null);
    }
    
    public PersonaNatural buscarPorDni(String dni) {
        return personaNaturalRepository.findByDni(dni);
    }

  
    
    public List<PersonaJuridica> listarPersonasJuridicas() {
        return personaJuridicaRepository.findAll();
    }
    
    public void guardarPersonaJuridica(PersonaJuridica personaJuridica) {
        personaJuridicaRepository.save(personaJuridica);
    }
    
    public PersonaJuridica buscarPersonaJuridicaPorId(Integer id) {
        return personaJuridicaRepository.findById(id).orElse(null);
    }
    
    public PersonaJuridica buscarPorRuc(String ruc) {
        return personaJuridicaRepository.findByRuc(ruc);
    }
    
    
    public PersonaJuridica buscarPorRazonSocial(String razonSocial) {
        return personaJuridicaRepository.findByRazonSocial(razonSocial);
    }


    public Cliente buscarPorEmail(String email) {
        return clienteRepository.findByEmail(email);
    }
    
    public Cliente buscarPorTelefono(String telefono) {
        return clienteRepository.findByTelefono(telefono);
    }
    
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll(); 
    }
    

    public Cliente buscarPorId(Integer id) {
  
        return clienteRepository.findById(id).orElse(null);
    }
}