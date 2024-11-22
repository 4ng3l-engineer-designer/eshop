package cl.dci.eshop;

import cl.dci.eshop.auth.User;
import cl.dci.eshop.model.Carrito;
import cl.dci.eshop.repository.CarritoRepository;
import cl.dci.eshop.repository.UserRepository;
import cl.dci.eshop.security.ApplicationUserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository, CarritoRepository carritoRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String password = passwordEncoder.encode("password");

            // Verificar si los usuarios ya existen antes de crearlos
            createOrUpdateUser(userRepository, carritoRepository, "customer1", password, ApplicationUserRole.CUSTOMER);
            createOrUpdateUser(userRepository, carritoRepository, "customer2", password, ApplicationUserRole.CUSTOMER);
            createOrUpdateUser(userRepository, carritoRepository, "customer3", password, ApplicationUserRole.CUSTOMER);
            createOrUpdateUser(userRepository, carritoRepository, "admin", password, ApplicationUserRole.ADMIN);

            System.out.println("Usuarios y carritos creados o actualizados exitosamente");
        };
    }

    private void createOrUpdateUser(UserRepository userRepository, CarritoRepository carritoRepository, String username, String password, ApplicationUserRole role) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        User user;
        if (existingUser.isPresent()) {
            // Actualizar el usuario existente si es necesario
            user = existingUser.get();
            user.setPassword(password);
            user.setRole(role);
        } else {
            // Crear un nuevo usuario si no existe
            user = new User(username, password, role);
            userRepository.save(user);

            // Crear un carrito para el nuevo usuario
            Carrito carrito = new Carrito();
            carrito.setUser(user);
            carritoRepository.save(carrito);
        }
    }
}
