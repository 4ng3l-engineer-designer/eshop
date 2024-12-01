package cl.dci.eshop.model;

import cl.dci.eshop.auth.User;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carrito")
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "carrito_id")
    private int id;

    @Column
    private int cantidadProductos;

    @Column
    private int precioTotal;

    // Relación OneToMany con ProductoCarrito
    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductoCarrito> productoCarritos;

    // Relación OneToOne con User
    @OneToOne
    @JoinTable(name = "user_carrito",
            joinColumns = {@JoinColumn(name = "carrito_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")})
    private User user;

    // Constructor sin parámetros
    public Carrito() {
        this.cantidadProductos = 0;
        this.precioTotal = 0;
        this.productoCarritos = new ArrayList<>();
    }

    // Métodos Getter y Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCantidadProductos() {
        return cantidadProductos;
    }

    public void setCantidadProductos(int cantidadProductos) {
        this.cantidadProductos = cantidadProductos;
    }

    public int getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(int precioTotal) {
        this.precioTotal = precioTotal;
    }

    public List<ProductoCarrito> getProductoCarritos() {
        return productoCarritos;
    }

    public void setProductoCarritos(List<ProductoCarrito> productoCarritos) {
        this.productoCarritos = productoCarritos;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Método para agregar un producto al carrito
    public void addProducto(Producto producto) {
        ProductoCarrito pcExistente = null;

        // Buscar si el producto ya existe en el carrito
        for (ProductoCarrito pc : this.productoCarritos) {
            if (pc.getProducto().equals(producto)) {
                pcExistente = pc;
                break;
            }
        }

        if (pcExistente != null) {
            // Si ya existe, incrementar la cantidad
            pcExistente.incrementarCantidad();
        } else {
            // Si no existe, agregar un nuevo ProductoCarrito
            ProductoCarrito pc = new ProductoCarrito(producto, this);
            this.productoCarritos.add(pc);
        }

        // Actualizar los totales
        this.precioTotal += producto.getPrecio();
        this.cantidadProductos++;
    }

    // Método para eliminar un producto del carrito
    public void deleteProducto(Producto producto) {
        ProductoCarrito pcAEliminar = null;

        for (ProductoCarrito pc : this.productoCarritos) {
            if (pc.getProducto().equals(producto)) {
                pcAEliminar = pc;
                break;
            }
        }

        if (pcAEliminar != null) {
            this.productoCarritos.remove(pcAEliminar);
            this.precioTotal -= producto.getPrecio();
            this.cantidadProductos--;
        }
    }

    @Override
    public String toString() {
        return "Carrito{" +
                "id=" + id +
                ", cantidadProductos=" + cantidadProductos +
                ", precioTotal=" + precioTotal +
                '}';
    }
}
