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
        
        // Enviamos el archivo y guardamos la respuesta de Cloudinary
        Map<?, ?> respuesta = cloudinary.uploader().upload(archivo.getBytes(), 
                ObjectUtils.asMap("folder", "calzados_morales"));
        
        // Retornamos la URL segura (https) generada para la imagen
        return (String) respuesta.get("secure_url");
    }
    
    
    public boolean eliminarFoto(String urlImagen) {
        if (urlImagen == null || urlImagen.isEmpty()) {
            return false;
        }
        
        try {
            // Ejemplo de URL: https://res.cloudinary.com/tu_cloud/image/upload/v12345678/carpeta/nombre_foto.jpg
            // Necesitamos extraer: "carpeta/nombre_foto" (sin la extensión .jpg)
            
            String[] partesUrl = urlImagen.split("/upload/");
            if (partesUrl.length < 2) return false;
            
            // Tomamos lo que está después de "/upload/" -> "v12345678/carpeta/nombre_foto.jpg"
            String rutaConVersion = partesUrl[1]; 
            
            // Quitamos la versión (v12345678/) cortando en el siguiente slash '/'
            String rutaSinVersion = rutaConVersion.substring(rutaConVersion.indexOf("/") + 1);
            
            // Quitamos la extensión (.jpg, .png, etc.)
            String publicId = rutaSinVersion.substring(0, rutaSinVersion.lastIndexOf("."));
            
            // Ejecutamos la destrucción en Cloudinary
            Map resultado = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            
            // Cloudinary devuelve "result" -> "ok" si se borró con éxito
            return "ok".equals(resultado.get("result"));
            
        } catch (Exception e) {
            System.err.println("Error al eliminar archivo en Cloudinary: " + e.getMessage());
            return false;
        }
    }
}