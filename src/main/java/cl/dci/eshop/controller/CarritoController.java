package cl.dci.eshop.controller;

import cl.dci.eshop.auth.User;
import cl.dci.eshop.model.Carrito;
import cl.dci.eshop.model.Producto;
import cl.dci.eshop.model.ProductoCarrito;
import cl.dci.eshop.repository.CarritoRepository;
import cl.dci.eshop.repository.ProductoCarritoRepository;
import cl.dci.eshop.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    // Agregar un producto al carrito
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

            // Verificar si el producto ya está en el carrito
            ProductoCarrito productoCarritoExistente = productoCarritoRepository.findByCarritoAndProducto(carrito, producto);
            if (productoCarritoExistente != null) {
                LOGGER.info("Producto ya existente en el carrito, incrementando cantidad");
                productoCarritoExistente.incrementarCantidad();
                productoCarritoRepository.save(productoCarritoExistente);
                carrito.setPrecioTotal(carrito.getPrecioTotal() + productoCarritoExistente.getProducto().getPrecio());
                carrito.setCantidadProductos(carrito.getCantidadProductos() + 1);
                carritoRepository.save(carrito);
                redirectAttributes.addFlashAttribute("mensaje", "Cantidad de producto incrementada en el carrito");
            } else {
                LOGGER.info("Agregando nuevo producto al carrito");
                ProductoCarrito pc = new ProductoCarrito(producto, carrito);
                productoCarritoRepository.save(pc);
                // Agregar el Producto (no ProductoCarrito) al carrito
                carrito.addProducto(producto);  // Aquí cambiamos de pc a producto
                carrito.setPrecioTotal(carrito.getPrecioTotal() + pc.getProducto().getPrecio());
                carrito.setCantidadProductos(carrito.getCantidadProductos() + 1);
                carritoRepository.save(carrito);
                redirectAttributes.addFlashAttribute("mensaje", "Producto agregado al carrito");
            }
        } catch (Exception e) {
            LOGGER.error("Error al agregar el producto al carrito: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Ha ocurrido un error al agregar el producto al carrito");
            return "redirect:/carrito";
        }

        return "redirect:/carrito";
    }

    // Eliminar un producto del carrito
    @PreAuthorize("hasAuthority('carrito:manage')")
    @PostMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            LOGGER.info("Iniciando proceso para eliminar producto del carrito: ProductoCarrito ID {}", id);

            ProductoCarrito pc = productoCarritoRepository.findById(id).orElse(null);
            if (pc == null) {
                LOGGER.error("ProductoCarrito con ID {} no encontrado", id);
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado en el carrito");
                return "redirect:/carrito";
            }

            Carrito carrito = pc.getCarrito();
            if (carrito == null) {
                LOGGER.error("Carrito no encontrado para el usuario");
                redirectAttributes.addFlashAttribute("error", "Carrito no encontrado");
                return "redirect:/carrito";
            }

            Producto producto = pc.getProducto();
            LOGGER.info("Eliminando producto del carrito: Producto ID {}", producto.getId());

            carrito.deleteProducto(producto);
            carrito.setPrecioTotal(carrito.getPrecioTotal() - (producto.getPrecio() * pc.getCantidad()));
            carrito.setCantidadProductos(carrito.getCantidadProductos() - pc.getCantidad());

            productoCarritoRepository.delete(pc);
            carritoRepository.save(carrito);

            LOGGER.info("Producto eliminado exitosamente del carrito");
            redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado del carrito");
        } catch (Exception e) {
            LOGGER.error("Error al eliminar el producto del carrito: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Ha ocurrido un error al eliminar el producto del carrito");
            return "redirect:/carrito";
        }

        return "redirect:/carrito";
    }

    // Incrementar cantidad del producto en el carrito
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
            carritoRepository.save(carrito);

            LOGGER.info("Cantidad incrementada exitosamente para ProductoCarrito ID {}", id);
            redirectAttributes.addFlashAttribute("mensaje", "Producto incrementado en el carrito");
        } catch (Exception e) {
            LOGGER.error("Error al incrementar la cantidad del producto: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Ha ocurrido un error al incrementar la cantidad del producto");
            return "redirect:/carrito";
        }
        return "redirect:/carrito";
    }

    // Decrementar cantidad del producto en el carrito
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
            } else {
                // Eliminar producto si la cantidad es 1
                carrito.deleteProducto(pc.getProducto());
                carrito.setPrecioTotal(carrito.getPrecioTotal() - pc.getProducto().getPrecio());
                carrito.setCantidadProductos(carrito.getCantidadProductos() - 1);
                productoCarritoRepository.delete(pc);
            }

            // Guardar cambios
            productoCarritoRepository.save(pc);
            carritoRepository.save(carrito);

            LOGGER.info("Cantidad decrementada exitosamente para ProductoCarrito ID {}", id);
            redirectAttributes.addFlashAttribute("mensaje", "Producto decrementado en el carrito");
        } catch (Exception e) {
            LOGGER.error("Error al decrementar la cantidad del producto: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Ha ocurrido un error al decrementar la cantidad del producto");
            return "redirect:/carrito";
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
}
