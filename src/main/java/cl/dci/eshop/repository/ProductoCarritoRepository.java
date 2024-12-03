package cl.dci.eshop.repository;

import cl.dci.eshop.model.Carrito;
import cl.dci.eshop.model.Producto;
import cl.dci.eshop.model.ProductoCarrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoCarritoRepository extends JpaRepository<ProductoCarrito, Integer> {
    List<ProductoCarrito> findByCarrito(Carrito carrito);
    List<ProductoCarrito> findByProducto(Producto producto);
    ProductoCarrito findByCarritoAndProducto(Carrito carrito, Producto producto);
}
