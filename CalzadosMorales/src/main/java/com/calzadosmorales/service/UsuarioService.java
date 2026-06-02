package com.calzadosmorales.service;

import java.util.List;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder; // CAMBIADO
import org.springframework.stereotype.Service;

import com.calzadosmorales.entity.Rol;
import com.calzadosmorales.entity.Usuario;
import com.calzadosmorales.repository.RolRepository;
import com.calzadosmorales.repository.UsuarioRepository;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepo;
    
    @Autowired
    private RolRepository rolRepo;

    @Autowired
    private PasswordEncoder encoder; 

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("--- PROCESO DE LOGIN PARA: " + username + " ---");
        
        Usuario u = usuarioRepo.findByUsuario(username);
        
        if (u == null) {
            System.out.println("--- ERROR: EL USUARIO NO EXISTE EN LA BASE DE DATOS ---");
            throw new UsernameNotFoundException("Usuario no encontrado");
        }
        
        System.out.println("CLAVE CARGADA DESDE DB: " + u.getClave());


        String nombreRol = "ROLE_" + u.getRol().getId_rol();
        System.out.println("ROL ASIGNADO: " + nombreRol);

        return User.builder()
            .username(u.getUsuario())
            .password(u.getClave()) 
            .disabled(!u.getEstado())
            .accountExpired(false)
            .credentialsExpired(false)
            .accountLocked(false)
            .authorities(Collections.singletonList(new SimpleGrantedAuthority(nombreRol)))
            .build();
    }

    public void guardarUsuario(Usuario usuario) {
      
        String claveProcesada = encoder.encode(usuario.getClave());
        usuario.setClave(claveProcesada); 
        usuarioRepo.save(usuario);
    }

    public List<Usuario> listarUsuarios() { return usuarioRepo.findAll(); }
    public Usuario buscarUsuarioPorId(Integer id) { return usuarioRepo.findById(id).orElse(null); }
    public List<Rol> listarRoles() { return rolRepo.findAll(); }
}