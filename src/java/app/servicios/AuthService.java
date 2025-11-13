package app.servicios;

import app.modelos.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import jakarta.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;

/**
 * SERVICIO DE AUTENTICACIÓN
 * 
 * Centraliza la lógica de autenticación de usuarios
 * Responsable de verificar credenciales y encriptación de contraseñas
 * 
 * @author agustinrodriguez
 * @version 2.0 - Refactorizado y comentado
 */
public class AuthService {
    
    // Inyección manual del EntityManager (asignada desde el controlador)
    public EntityManager em;
    
    /**
     * MÉTODO: autenticarPorEmail
     * Verifica las credenciales de un usuario por email y contraseña
     * 
     * @param email Email del usuario
     * @param password Contraseña en texto plano
     * @return Usuario si la autenticación es exitosa, null si falla
     */
    public Usuario autenticarPorEmail(String email, String password) {
        try {
            // Encriptar la contraseña para comparar con la BD
            String hashedPassword = hashPassword(password);
            
            // Consultar usuario con email y contraseña
            TypedQuery<Usuario> query = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.email = :email AND u.password = :password", 
                Usuario.class
            );
            query.setParameter("email", email);
            query.setParameter("password", hashedPassword);
            
            List<Usuario> resultados = query.getResultList();
            
            // Retornar usuario si existe, null si no
            return resultados.isEmpty() ? null : resultados.get(0);
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
        
    /**
     * MÉTODO PRIVADO: hashPassword
     * Encripta una contraseña usando el algoritmo MD5
     * 
     * ⚠️ ADVERTENCIA: MD5 es considerado INSECURE para contraseñas porque:
     * - Es vulnerable a ataques de colisión
     * - Es muy rápido (permite brute force attacks)
     * - No usa salt (misma contraseña = mismo hash siempre)
     * 
     * @param password La contraseña en texto plano a encriptar
     * @return String El hash MD5 de la contraseña en mayúsculas
     * @throws NoSuchAlgorithmException Si el algoritmo MD5 no está disponible en el sistema
     */
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());
        byte[] digest = md.digest();
        String myHash = DatatypeConverter.printHexBinary(digest).toUpperCase();
        return myHash;
    }
    
}
