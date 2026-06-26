package com.calzadosmorales.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String subirFoto(MultipartFile archivo) throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }
        
       
        Map<?, ?> respuesta = cloudinary.uploader().upload(archivo.getBytes(), 
                ObjectUtils.asMap("folder", "calzados_morales"));
       
        return (String) respuesta.get("secure_url");
    }
    
    
    public boolean eliminarFoto(String urlImagen) {
        if (urlImagen == null || urlImagen.isEmpty()) {
            return false;
        }
        
        try {
           
            
            String[] partesUrl = urlImagen.split("/upload/");
            if (partesUrl.length < 2) return false;
            
            
            String rutaConVersion = partesUrl[1]; 
           
            String rutaSinVersion = rutaConVersion.substring(rutaConVersion.indexOf("/") + 1);
            
            
            String publicId = rutaSinVersion.substring(0, rutaSinVersion.lastIndexOf("."));
            
          
            Map resultado = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            
          
            return "ok".equals(resultado.get("result"));
            
        } catch (Exception e) {
            System.err.println("Error al eliminar archivo en Cloudinary: " + e.getMessage());
            return false;
        }
    }
}