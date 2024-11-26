package cl.dci.eshop.controller;

import cl.dci.eshop.model.Producto;
import cl.dci.eshop.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
@RequestMapping("/producto")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    private static final String UPLOAD_DIR = "src/main/resources/static/images/";

    @GetMapping("/new")
    public String newProductoForm(Model model) {
        model.addAttribute("producto", new Producto());
        return "crear-producto";
    }

    @PostMapping("/save")
    public String saveProducto(@ModelAttribute Producto producto, @RequestParam("imagen") MultipartFile imagenFile, RedirectAttributes attributes) {
        try {
            if (!imagenFile.isEmpty()) {
                String imagePath = saveImage(imagenFile);
                if (imagePath == null) {
                    attributes.addFlashAttribute("error", "Error al guardar la imagen");
                    return "redirect:/producto/new";
                }
                producto.setImagenUrl(imagePath);
            }
            productoRepository.save(producto);
            attributes.addFlashAttribute("mensaje", "Producto guardado con éxito");
        } catch (Exception e) {
            attributes.addFlashAttribute("error", "Error al guardar el producto: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/producto/new";
        }
        return "redirect:/admin/productos";
    }

    @PostMapping("/edit/{id}")
    public String updateProducto(@PathVariable Integer id, @ModelAttribute Producto producto, @RequestParam(value = "imagen", required = false) MultipartFile imagenFile, RedirectAttributes attributes) {
        Producto existingProducto = productoRepository.findById(id).orElse(null);
        if (existingProducto != null) {
            existingProducto.setNombre(producto.getNombre());
            existingProducto.setPrecio(producto.getPrecio());
            existingProducto.setStock(producto.getStock()); // Actualizar stock
            existingProducto.setDescripcion(producto.getDescripcion()); // Actualizar descripción
            if (imagenFile != null && !imagenFile.isEmpty()) {
                String imagePath = saveImage(imagenFile);
                existingProducto.setImagenUrl(imagePath);
            }
            productoRepository.save(existingProducto);
            attributes.addFlashAttribute("mensaje", "Producto actualizado con éxito");
        } else {
            attributes.addFlashAttribute("error", "Error: Producto no encontrado");
            return "redirect:/producto/edit/form/{id}";
        }
        return "redirect:/admin/productos";
    }


    @GetMapping("/edit/form/{id}")
    public String editProductoForm(@PathVariable Integer id, Model model) {
        Producto producto = productoRepository.findById(id).orElse(null);
        if (producto != null) {
            model.addAttribute("producto", producto);
            return "admin/producto-update";
        } else {
            return "redirect:/producto/productos";
        }
    }


    @GetMapping("/productos")
    public String listProductos(Model model) {
        model.addAttribute("productos", productoRepository.findAll());
        return "producto";
    }

    private String saveImage(MultipartFile file) {
        if (file.isEmpty()) {
            System.out.println("El archivo está vacío.");
            return null;
        }
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(file.getOriginalFilename());
            if (!Files.exists(filePath)) {
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Imagen guardada en: " + filePath.toString());
                return "/images/" + file.getOriginalFilename();
            } else {
                // Si el archivo ya existe, reutilizar el nombre de archivo existente en lugar de crear uno nuevo.
                System.out.println("Imagen ya existente, reutilizando archivo: " + filePath.toString());
                return "/images/" + file.getOriginalFilename();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    @PostMapping("/delete/{id}")
    public String deleteProducto(@PathVariable Integer id, RedirectAttributes attributes) {
        try {
            if (productoRepository.existsById(id)) {
                productoRepository.deleteById(id);
                attributes.addFlashAttribute("mensaje", "Producto eliminado con éxito");
            } else {
                attributes.addFlashAttribute("error", "Error: Producto no encontrado");
            }
        } catch (Exception e) {
            attributes.addFlashAttribute("error", "Error al eliminar el producto: " + e.getMessage());
            e.printStackTrace();  // Esto imprimirá el stack trace en el log del servidor
        }
        return "redirect:/admin/productos";
    }

}
