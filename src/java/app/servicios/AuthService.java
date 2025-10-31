/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package app.servicios;

import app.modelos.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import jakarta.xml.bind.DatatypeConverter;
/**
 *
 * @author agustinrodriguez
 */
public class AuthService {
    
    public EntityManager em;
    
    public Usuario autenticarPorEmail(String email, String password) {
        try {
            String hashedPassword = hashPassword(password);
            
            TypedQuery<Usuario> query = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.email = :email AND u.password = :password", 
                Usuario.class
            );
            query.setParameter("email", email);
            query.setParameter("password", hashedPassword);
            
            List<Usuario> resultados = query.getResultList();
            return resultados.isEmpty() ? null : resultados.get(0);
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());
        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest).toUpperCase();
    }
}