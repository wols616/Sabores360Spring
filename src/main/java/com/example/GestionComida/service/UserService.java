package com.example.GestionComida.service;

import com.example.GestionComida.domain.entity.Role;
import com.example.GestionComida.domain.entity.User;
import com.example.GestionComida.error.BadRequestException;
import com.example.GestionComida.error.NotFoundException;
import com.example.GestionComida.repo.RoleRepository;
import com.example.GestionComida.repo.UserRepository;
import com.example.GestionComida.web.dto.admin.CreateUserRequest;
import com.example.GestionComida.web.dto.admin.UpdateUserRequest;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;

    @Transactional
    public User register(String name, String email, String passwordHash, Integer roleId){
        if (userRepo.existsByEmail(email)) throw new BadRequestException("email_exists");
        Role role = roleRepo.findById(roleId).orElseThrow(() -> new NotFoundException("Rol no existe"));
        User u = User.builder().name(name).email(email).passwordHash(passwordHash).role(role).isActive(true).build();
        return userRepo.save(u);
    }

    @Transactional
    public User activate(Integer userId, boolean active){
        User u = userRepo.findById(userId).orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        u.setIsActive(active);
        return userRepo.save(u);
    }

    @Transactional
    public User createUser(CreateUserRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new BadRequestException("El email ya está registrado");
        }

        Role role = roleRepo.findById(req.getRoleId())
                .orElseThrow(() -> new NotFoundException("Rol no encontrado"));

        String passwordHash = BCrypt.hashpw(req.getPassword(), BCrypt.gensalt());

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .passwordHash(passwordHash)
                .address(req.getAddress())
                .role(role)
                .isActive(req.getIsActive() != null ? req.getIsActive() : true)
                .build();

        return userRepo.save(user);
    }

    @Transactional
    public User updateUser(Integer id, UpdateUserRequest req) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (req.getName() != null) {
            user.setName(req.getName());
        }
        if (req.getEmail() != null && !req.getEmail().equals(user.getEmail())) {
            if (userRepo.existsByEmail(req.getEmail())) {
                throw new BadRequestException("El email ya está registrado");
            }
            user.setEmail(req.getEmail());
        }
        if (req.getPassword() != null && !req.getPassword().isEmpty()) {
            String passwordHash = BCrypt.hashpw(req.getPassword(), BCrypt.gensalt());
            user.setPasswordHash(passwordHash);
        }
        if (req.getAddress() != null) {
            user.setAddress(req.getAddress());
        }
        if (req.getRoleId() != null) {
            Role role = roleRepo.findById(req.getRoleId())
                    .orElseThrow(() -> new NotFoundException("Rol no encontrado"));
            user.setRole(role);
        }
        if (req.getIsActive() != null) {
            user.setIsActive(req.getIsActive());
        }

        return userRepo.save(user);
    }
}
