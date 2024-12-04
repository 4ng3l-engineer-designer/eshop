package cl.dci.eshop.controller;

import cl.dci.eshop.auth.User;
import cl.dci.eshop.model.Carrito;
import cl.dci.eshop.model.Producto;
import cl.dci.eshop.model.ProductoCarrito;
import cl.dci.eshop.repository.CarritoRepository;
import cl.dci.eshop.repository.ProductoCarritoRepository;
import cl.dci.eshop.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
@RequestMapping("/api/carrito")
public class CarritoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarritoController.class);

    @Autowired
    private CarritoRepository carritoRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private ProductoCarritoRepository productoCarritoRepository;

    @PostMapping("/crear/{id}")
    public String agregarProducto(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            LOGGER.info("Iniciando proceso para agregar producto al carrito: producto ID {}", id);

            Producto producto = productoRepository.findById(id).orElse(null);
            if (producto == null) {
                LOGGER.error("Producto con ID {} no encontrado", id);
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                return "redirect:/carrito";
            }

            User currentUser = getCurrentUser();
            if (currentUser == null) {
                LOGGER.error("Usuario no autenticado");
                redirectAttributes.addFlashAttribute("error", "Usuario no autenticado");
                return "redirect:/login";
            }

            Carrito carrito = currentUser.getCarrito();
            if (carrito == null) {
                LOGGER.info("El usuario no tiene carrito, creando uno nuevo");
                carrito = new Carrito();
                carrito.setUser(currentUser);
                carrito = carritoRepository.save(carrito);
                currentUser.setCarrito(carrito);
            }

            // Buscar si el producto ya está en el carrito
            ProductoCarrito productoCarrito = productoCarritoRepository.findByCarritoAndProducto(carrito, producto);

            if (productoCarrito == null) {
                // Si no existe, crear nuevo ProductoCarrito
                productoCarrito = new ProductoCarrito(producto, carrito);
                productoCarrito = productoCarritoRepository.save(productoCarrito);
                actualizarTotalesCarrito(carrito);
            } else {
                // Si existe, incrementar cantidad
                productoCarrito.incrementarCantidad();
                productoCarritoRepository.save(productoCarrito);
                actualizarTotalesCarrito(carrito);
            }

            redirectAttributes.addFlashAttribute("mensaje", "Producto agregado al carrito");
            return "redirect:/carrito";

        } catch (Exception e) {
            LOGGER.error("Error al agregar el producto al carrito: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Ha ocurrido un error al agregar el producto al carrito");
            return "redirect:/carrito";
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            ProductoCarrito pc = productoCarritoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("ProductoCarrito no encontrado"));

            Carrito carrito = pc.getCarrito();
            productoCarritoRepository.deleteById(id);
            actualizarTotalesCarrito(carrito);

            redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado del carrito");
            return "redirect:/carrito";
        } catch (Exception e) {
            LOGGER.error("Error al eliminar producto: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el producto");
            return "redirect:/carrito";
        }
    }

    @PostMapping("/incrementar/{id}")
    public String incrementarProducto(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            LOGGER.info("Iniciando proceso para incrementar cantidad de producto: ProductoCarrito ID {}", id);

            ProductoCarrito pc = productoCarritoRepository.findById(id).orElse(null);
            if (pc == null) {
                LOGGER.error("ProductoCarrito con ID {} no encontrado", id);
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado en el carrito");
                return "redirect:/carrito";
            }

            Carrito carrito = pc.getCarrito();
            pc.incrementarCantidad();
            carrito.setPrecioTotal(carrito.getPrecioTotal() + pc.getProducto().getPrecio());
            carrito.setCantidadProductos(carrito.getCantidadProductos() + 1);

            productoCarritoRepository.save(pc);
            carrito = carritoRepository.save(carrito);

            LOGGER.info("Cantidad incrementada exitosamente para ProductoCarrito ID {}", id);
            redirectAttributes.addFlashAttribute("mensaje", "Cantidad incrementada");
            redirectAttributes.addFlashAttribute("carrito", carrito);
            redirectAttributes.addFlashAttribute("prodCars", productoCarritoRepository.findByCarrito(carrito));

        } catch (Exception e) {
            LOGGER.error("Error al incrementar la cantidad del producto: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Ha ocurrido un error al incrementar la cantidad del producto");
        }
        return "redirect:/carrito";
    }

    @PostMapping("/decrementar/{id}")
    public String decrementarProducto(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            LOGGER.info("Iniciando proceso para decrementar cantidad de producto: ProductoCarrito ID {}", id);

            ProductoCarrito pc = productoCarritoRepository.findById(id).orElse(null);
            if (pc == null) {
                LOGGER.error("ProductoCarrito con ID {} no encontrado", id);
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado en el carrito");
                return "redirect:/carrito";
            }

            Carrito carrito = pc.getCarrito();
            if (pc.getCantidad() > 1) {
                pc.decrementarCantidad();
                carrito.setPrecioTotal(carrito.getPrecioTotal() - pc.getProducto().getPrecio());
                carrito.setCantidadProductos(carrito.getCantidadProductos() - 1);
                productoCarritoRepository.save(pc);
            } else {
                carrito.deleteProducto(pc.getProducto());
                carrito.setPrecioTotal(carrito.getPrecioTotal() - pc.getProducto().getPrecio());
                carrito.setCantidadProductos(carrito.getCantidadProductos() - 1);
                productoCarritoRepository.delete(pc);
            }

            carrito = carritoRepository.save(carrito);

            LOGGER.info("Cantidad decrementada exitosamente para ProductoCarrito ID {}", id);
            redirectAttributes.addFlashAttribute("mensaje", "Cantidad decrementada");
            redirectAttributes.addFlashAttribute("carrito", carrito);
            redirectAttributes.addFlashAttribute("prodCars", productoCarritoRepository.findByCarrito(carrito));

        } catch (Exception e) {
            LOGGER.error("Error al decrementar la cantidad del producto: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Ha ocurrido un error al decrementar la cantidad del producto");
        }
        return "redirect:/carrito";
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        LOGGER.warn("Usuario actual no es una instancia de User");
        return null;
    }

    private void actualizarTotalesCarrito(Carrito carrito) {
        List<ProductoCarrito> productos = productoCarritoRepository.findByCarrito(carrito);
        int cantidadTotal = 0;
        int precioTotal = 0;

        for (ProductoCarrito pc : productos) {
            cantidadTotal += pc.getCantidad();
            precioTotal += pc.getProducto().getPrecio() * pc.getCantidad();
        }

        carrito.setCantidadProductos(cantidadTotal);
        carrito.setPrecioTotal(precioTotal);
        carritoRepository.save(carrito);
    }
}