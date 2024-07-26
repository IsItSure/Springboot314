package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.entities.Role;
import ru.kata.spring.boot_security.demo.entities.User;
import ru.kata.spring.boot_security.demo.repository.RoleRepository;
import ru.kata.spring.boot_security.demo.repository.UserRepository;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userServiceImpl;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Autowired
    public AdminController(UserService userServiceImpl, RoleRepository roleRepository, UserRepository userRepository) {
        this.userServiceImpl = userServiceImpl;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String userList(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserDetails user = userServiceImpl.loadUserByUsername(username);
        List<User> users = userServiceImpl.getUsers();
        List<Role> roles = roleRepository.findAll();
        model.addAttribute("user", user);
        model.addAttribute("users", users);
        model.addAttribute("roles", roles);
        return "user-list";
    }

    @PostMapping("/saveUser")
    public String saveUser(@ModelAttribute("user") User user,
                           @RequestParam(value = "roleIds", required = false) List<Long> roleIds) {
        if (roleIds != null) {
            Set<Role> roles = roleRepository.findAllById(roleIds).stream().collect(Collectors.toSet());
            user.setRoles(roles);
        }
        if (user.getId() == null) {
            User existingUser = userRepository.findByUsername(user.getUsername());
            if (existingUser != null) {
                return "redirect:/admin/err";
            }
            userServiceImpl.saveUser(user);
        } else {
            userServiceImpl.updateUser(user);
        }
        return "redirect:/admin/";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam("id") Long id) {
        userServiceImpl.deleteUser(id);
        return "redirect:/admin/";
    }
}
