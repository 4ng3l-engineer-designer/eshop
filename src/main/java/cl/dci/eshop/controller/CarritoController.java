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

@Controller
@RequestMapping("/api/carrito")
public class CarritoController {

    @Autowired
    private CarritoRepository carritoRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private ProductoCarritoRepository productoCarritoRepository;

    @PostMapping("/crear/{id}")
    public String agregarProducto(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            Producto producto = productoRepository.findById(id).orElse(null);
            if (producto == null) {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                return "redirect:/carrito";
            }

            User currentUser = getCurrentUser();
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "Usuario no autenticado");
                return "redirect:/login";
            }

            Carrito carrito = currentUser.getCarrito();
            if (carrito == null) {
                // Crear un nuevo carrito si el usuario no tiene uno
                carrito = new Carrito();
                carrito.setUser(currentUser);
                carrito = carritoRepository.save(carrito);
                currentUser.setCarrito(carrito);
            }

            // Verificar si el producto ya está en el carrito
            ProductoCarrito productoCarritoExistente = productoCarritoRepository.findByCarritoAndProducto(carrito, producto);
            if (productoCarritoExistente != null) {
                // Si el producto ya está en el carrito, incrementar la cantidad
                productoCarritoExistente.incrementarCantidad();
                productoCarritoRepository.save(productoCarritoExistente);
                redirectAttributes.addFlashAttribute("mensaje", "Cantidad de producto incrementada en el carrito");
            } else {
                // Si el producto no está en el carrito, agregarlo
                ProductoCarrito pc = new ProductoCarrito(producto, carrito);
                productoCarritoRepository.save(pc);
                carrito.addProducto(producto);
                carritoRepository.save(carrito);
                redirectAttributes.addFlashAttribute("mensaje", "Producto agregado al carrito");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ha ocurrido un error al agregar el producto al carrito");
            return "redirect:/carrito";
        }

        return "redirect:/carrito";
    }

    @PreAuthorize("hasAuthority('carrito:manage')")
    @PostMapping("/{id}")
    public String eliminarProducto(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            ProductoCarrito pc = productoCarritoRepository.findById(id).orElse(null);
            if (pc == null) {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado en el carrito");
                return "redirect:/carrito";
            }
            Carrito carrito = getCurrentUser().getCarrito();
            Producto producto = pc.getProducto();

            carrito.deleteProducto(producto);
            productoCarritoRepository.delete(pc);
            carritoRepository.save(carrito);

            redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado del carrito");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ha ocurrido un error al eliminar el producto del carrito");
            return "redirect:/carrito";
        }

        return "redirect:/carrito";
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        return null;
    }

    @PostMapping("/incrementar/{id}")
    public String incrementarProducto(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            ProductoCarrito pc = productoCarritoRepository.findById(id).orElse(null);
            if (pc == null) {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado en el carrito");
                return "redirect:/carrito";
            }
            pc.incrementarCantidad();
            productoCarritoRepository.save(pc);
            redirectAttributes.addFlashAttribute("mensaje", "Producto incrementado en el carrito");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ha ocurrido un error al incrementar la cantidad del producto");
            return "redirect:/carrito";
        }
        return "redirect:/carrito";
    }

    @PostMapping("/decrementar/{id}")
    public String decrementarProducto(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            ProductoCarrito pc = productoCarritoRepository.findById(id).orElse(null);
            if (pc == null) {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado en el carrito");
                return "redirect:/carrito";
            }
            if (pc.getCantidad() > 1) {
                pc.decrementarCantidad();
                productoCarritoRepository.save(pc);
                redirectAttributes.addFlashAttribute("mensaje", "Producto decrementado en el carrito");
            } else {
                productoCarritoRepository.delete(pc);
                Carrito carrito = getCurrentUser().getCarrito();
                carrito.deleteProducto(pc.getProducto());
                carritoRepository.save(carrito);
                redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado del carrito");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ha ocurrido un error al decrementar la cantidad del producto");
            return "redirect:/carrito";
        }
        return "redirect:/carrito";
    }
}
