package com.calzadosmorales.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.calzadosmorales.entity.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    
    Cliente findByEmail(String email);
    
    Cliente findByTelefono(String telefono);

   
    @Query("SELECT c FROM Cliente c WHERE c.id_cliente IN " +
           "(SELECT pn.id_cliente FROM PersonaNatural pn WHERE pn.dni = :numDocumento) OR " +
           "c.id_cliente IN (SELECT pj.id_cliente FROM PersonaJuridica pj WHERE pj.ruc = :numDocumento)")
    Optional<Cliente> findByNumDocumento(@Param("numDocumento") String numDocumento);
}