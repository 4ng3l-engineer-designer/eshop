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
        Producto producto = productoRepository.findById(id).orElse(null);
        if (producto == null) {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
            return "redirect:/catalogo";
        }
        Carrito carrito = getCurrentUser().getCarrito();
        carrito.addProducto(producto);

        ProductoCarrito pc = new ProductoCarrito(producto, carrito);
        productoCarritoRepository.save(pc);
        carritoRepository.save(carrito);

        redirectAttributes.addFlashAttribute("mensaje", "Producto agregado al carrito");
        return "redirect:/carrito";
    }

    @PreAuthorize("hasAuthority('carrito:manage')")
    @PostMapping("/{id}")
    public String eliminarProducto(@PathVariable int id, RedirectAttributes redirectAttributes) {
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
        ProductoCarrito pc = productoCarritoRepository.findById(id).orElse(null);
        if (pc == null) {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado en el carrito");
            return "redirect:/carrito";
        }
        pc.incrementarCantidad();
        productoCarritoRepository.save(pc);
        redirectAttributes.addFlashAttribute("mensaje", "Producto incrementado en el carrito");
        System.out.println("Cantidad incrementada para producto ID " + id + ": " + pc.getCantidad());
        return "redirect:/carrito";
    }

    @PostMapping("/decrementar/{id}")
    public String decrementarProducto(@PathVariable int id, RedirectAttributes redirectAttributes) {
        ProductoCarrito pc = productoCarritoRepository.findById(id).orElse(null);
        if (pc == null) {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado en el carrito");
            return "redirect:/carrito";
        }
        if (pc.getCantidad() > 1) {
            pc.decrementarCantidad();
            productoCarritoRepository.save(pc);
            redirectAttributes.addFlashAttribute("mensaje", "Producto decrementado en el carrito");
            System.out.println("Cantidad decrementada para producto ID " + id + ": " + pc.getCantidad());
        } else {
            redirectAttributes.addFlashAttribute("error", "No se puede decrementar más la cantidad");
        }
        return "redirect:/carrito";
    }
}
