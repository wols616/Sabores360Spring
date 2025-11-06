package com.example.GestionComida.service;

import com.example.GestionComida.domain.entity.PasswordReset;
import com.example.GestionComida.domain.entity.Role;
import com.example.GestionComida.domain.entity.User;
import com.example.GestionComida.error.BadRequestException;
import com.example.GestionComida.error.NotFoundException;
import com.example.GestionComida.repo.PasswordResetRepository;
import com.example.GestionComida.repo.RoleRepository;
import com.example.GestionComida.repo.UserRepository;
import com.example.GestionComida.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordResetRepository resetRepo;
    private final JwtUtil jwtUtil;
    @Autowired(required = false)
    private JavaMailSender mailSender;

    public Map<String, Object> login(String email, String password) {
        // Buscar usuario por email usando el nuevo método
        User u = userRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("invalid_credentials"));

        try {
            if (u.getPasswordHash() == null || !BCrypt.checkpw(password, u.getPasswordHash())) {
                throw new NotFoundException("invalid_credentials");
            }
        } catch (IllegalArgumentException ex) {
            // BCrypt throws IllegalArgumentException for invalid hash formats (e.g. placeholder values).
            // Map to invalid credentials to avoid 500 and leaking internals.
            throw new NotFoundException("invalid_credentials");
        }

        String roleName = (u.getRole() != null && u.getRole().getName() != null)
                ? u.getRole().getName()
                : "Cliente";

        String roleKey;
        String rn = roleName.toLowerCase(Locale.ROOT);
        if ("administrador".equals(rn)) {
            roleKey = "admin";
        } else if ("vendedor".equals(rn)) {
            roleKey = "seller";
        } else {
            roleKey = "client";
        }

        // Generar token JWT real
        String token = jwtUtil.generateToken(u.getId(), u.getEmail(), roleName);

        Map<String, Object> userMap = new HashMap<String, Object>();
        userMap.put("id", u.getId());
        userMap.put("name", u.getName());
        userMap.put("email", u.getEmail());
        userMap.put("role", roleKey);

        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("token", token);
        resp.put("user", userMap);
        return resp;
    }

    @Transactional
    public int register(String name, String email, String address, String password) {
        if (userRepo.existsByEmail(email)) {
            throw new BadRequestException("email_exists");
        }

        // Rol Cliente (crear si no existe)
        List<Role> roles = roleRepo.findAll();
        Role clientRole = null;
        for (Role r : roles) {
            if (r.getName() != null && r.getName().equalsIgnoreCase("Cliente")) {
                clientRole = r;
                break;
            }
        }
        if (clientRole == null) {
            clientRole = roleRepo.save(Role.builder().name("Cliente").build());
        }

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());

        User u = User.builder()
                .name(name)
                .email(email)
                .address(address)
                .passwordHash(hash)
                .role(clientRole)
                .isActive(true)
                .build();

        return userRepo.save(u).getId();
    }

    @Transactional
    public void forgotPassword(String email) {
        // Buscar usuario por email
        User u = userRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("email_not_found"));
        // Generar token y persistir en la tabla password_resets
        String token = UUID.randomUUID().toString();
        PasswordReset pr = PasswordReset.builder()
                .email(u.getEmail())
                .token(token)
                .build();
        resetRepo.save(pr);

        // Enviar correo SMTP con el token si hay JavaMailSender configurado
        if (mailSender != null) {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                String from = "no-reply@localhost";
                // If application property spring.mail.default-from is set, Spring's JavaMailSender
                // may automatically use it; here we set a sensible default.
                msg.setFrom(from);
                msg.setTo(u.getEmail());
                msg.setSubject("Recuperación de contraseña - GestionComida");
                StringBuilder body = new StringBuilder();
                body.append("Hola ").append(u.getName() == null ? "" : u.getName()).append(",\n\n");
                body.append("Se ha solicitado un restablecimiento de contraseña para tu cuenta.\n\n");
                body.append("Tu código de recuperación es:\n\n");
                body.append(token).append("\n\n");
                body.append("Si no solicitaste este correo, ignora este mensaje.\n\n");
                body.append("Saludos,\n");
                body.append("GestionComida");
                msg.setText(body.toString());
                mailSender.send(msg);
            } catch (Exception ex) {
                // Si falla el envío, dejar el token persistido pero reportar error para que el caller lo maneje.
                throw new RuntimeException("mail_send_failed");
            }
        } else {
            // No hay JavaMailSender configurado; token queda persistido but no email will be sent.
            // Log a message to console for debugging during development.
            System.err.println("[AuthService] JavaMailSender not configured - skipped sending reset email to " + u.getEmail());
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        List<PasswordReset> prs = resetRepo.findAll();
        PasswordReset pr = null;
        for (PasswordReset x : prs) {
            if (x.getToken() != null && x.getToken().equals(token)) {
                pr = x;
                break;
            }
        }
        if (pr == null) {
            throw new BadRequestException("invalid_token");
        }

        // Buscar usuario por email guardado en el token
        User u = userRepo.findByEmail(pr.getEmail())
                .orElseThrow(() -> new NotFoundException("email_not_found"));

        String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        u.setPasswordHash(hash);
        userRepo.save(u);

        resetRepo.delete(pr);
    }
}
