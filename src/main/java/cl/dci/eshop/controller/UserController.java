package cl.dci.eshop.controller;

import cl.dci.eshop.auth.User;
import cl.dci.eshop.model.Carrito;
import cl.dci.eshop.repository.CarritoRepository;
import cl.dci.eshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static cl.dci.eshop.security.ApplicationUserRole.*;

@Controller
@RequestMapping("/api/usuario")
public class UserController {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    public UserController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @PreAuthorize("hasAuthority('usuario:write')")
    @PostMapping("/crear")
    public String crearUsuario(@ModelAttribute("usuario") User usuario) {

        Carrito c1 = new Carrito();
        c1.setUser(usuario);
        System.out.println("USUARIO: " + usuario);
        String password = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(password);
        userRepository.save(usuario);
        carritoRepository.save(c1);
        return "redirect:/admin/productos";
    }

    @PostMapping("/registrar")
    public String registrarUsuario(@ModelAttribute("usuario") User usuario) {

        Carrito c1 = new Carrito();
        c1.setUser(usuario);
        System.out.println("USUARIO: " + usuario);
        String password = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(password);
        usuario.setRole(CUSTOMER);
        userRepository.save(usuario);
        carritoRepository.save(c1);
        System.out.println("USUARIO: " + usuario);
        return "redirect:/";
    }
}
