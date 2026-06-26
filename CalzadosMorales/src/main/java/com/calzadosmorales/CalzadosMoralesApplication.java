package com.calzadosmorales;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync; // 🌟 NUEVO IMPORT
import java.util.TimeZone;

@SpringBootApplication
@EnableAsync // 🌟 OBLIGATORIO: Activa la ejecución en segundo plano para el EmailService
public class CalzadosMoralesApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"));
    }

    public static void main(String[] args) {
        SpringApplication.run(CalzadosMoralesApplication.class, args);
    }
}