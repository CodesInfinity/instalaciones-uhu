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
 * SERVICIO DE AUTENTICACIÓN Y SEGURIDAD
 * * <p>Componente de la capa de servicio encargado de verificar la identidad de los usuarios.</p>
 * * <p><strong>Responsabilidades:</strong></p>
 * <ul>
 * <li>Validación de credenciales (Email/Password).</li>
 * <li>Abstracción de la lógica de encriptación (Hashing).</li>
 * <li>Interacción segura con la base de datos para consultas de login.</li>
 * </ul>
 * * @author agustinrodriguez
 * @version 2.0
 */
public class AuthService {
    
    /**
     * Referencia al contexto de persistencia JPA.
     * <p><strong>Nota de Arquitectura:</strong> En este diseño ligero, el <code>EntityManager</code> 
     * se inyecta manualmente desde el Controlador (Servlet) que instancia este servicio, 
     * en lugar de usar inyección de dependencias automática (CDI/EJB).</p>
     */
    public EntityManager em;
    
    

    /**
     * Verifica las credenciales de un usuario contra la base de datos.
     * * <p>El flujo del proceso es:</p>
     * <ol>
     * <li>Recibe la contraseña en texto plano.</li>
     * <li>Aplica el algoritmo de hash (MD5).</li>
     * <li>Busca un usuario que coincida exactamente con el email y el hash generado.</li>
     * </ol>
     * * @param email Correo electrónico del usuario (identificador único).
     * @param password Contraseña en texto plano tal como la ingresó el usuario.
     * @return El objeto {@link Usuario} si las credenciales son válidas, o <code>null</code> si no hay coincidencia.
     */
    public Usuario autenticarPorEmail(String email, String password) {
        try {
            // 1. Encriptar la contraseña entrante para compararla con la almacenada
            String hashedPassword = hashPassword(password);
            
            // 2. Consulta segura usando parámetros (Previene SQL Injection)
            TypedQuery<Usuario> query = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.email = :email AND u.password = :password", 
                Usuario.class
            );
            query.setParameter("email", email);
            query.setParameter("password", hashedPassword);
            
            List<Usuario> resultados = query.getResultList();
            
            // 3. Retorno de resultado (Usuario encontrado o null)
            return resultados.isEmpty() ? null : resultados.get(0);
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
        
    /**
     * Genera un hash criptográfico de la contraseña utilizando el algoritmo MD5.
     * * <p><strong>⚠️ ADVERTENCIA DE SEGURIDAD CRÍTICA:</strong></p>
     * <p>El uso de MD5 para contraseñas se considera <strong>INSEGURO</strong> en aplicaciones modernas por las siguientes razones:</p>
     * <ul>
     * <li><strong>Colisiones:</strong> Es matemáticamente posible generar el mismo hash con dos textos diferentes.</li>
     * <li><strong>Velocidad:</strong> Es demasiado rápido, lo que facilita ataques de fuerza bruta (Brute Force) y tablas arcoíris (Rainbow Tables).</li>
     * <li><strong>Falta de Salt:</strong> Esta implementación no utiliza "Salting", por lo que dos usuarios con la misma contraseña tendrán el mismo hash.</li>
     * </ul>
     * <p><em>Se recomienda migrar a algoritmos lentos como BCrypt, Argon2 o PBKDF2 en futuras versiones.</em></p>
     * * @param password La contraseña en texto plano.
     * @return String Cadena hexadecimal en mayúsculas que representa el hash.
     * @throws NoSuchAlgorithmException Si el proveedor de seguridad de Java no soporta MD5.
     */
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());
        byte[] digest = md.digest();
        String myHash = DatatypeConverter.printHexBinary(digest).toUpperCase();
        return myHash;
    }
    
}