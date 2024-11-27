package cl.dci.eshop.model;

import javax.persistence.*;

@Entity
@Table(name = "producto_carrito")
public class ProductoCarrito {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "carrito_id")
    private Carrito carrito;

    @Column
    private Integer cantidad;  // Cambiado de int a Integer para permitir null

    public ProductoCarrito() {
        this.cantidad = 1;  // Asegurar que cantidad está inicializada por defecto
    }

    public ProductoCarrito(Producto producto, Carrito carrito) {
        this.producto = producto;
        this.carrito = carrito;
        this.cantidad = 1;  // Asegurar que cantidad está inicializada en el constructor
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Carrito getCarrito() {
        return carrito;
    }

    public void setCarrito(Carrito carrito) {
        this.carrito = carrito;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public void incrementarCantidad() {
        if (this.cantidad == null) {
            this.cantidad = 1;
        } else {
            this.cantidad++;
        }
    }

    public int decrementarCantidad() {
        if (this.cantidad == null || this.cantidad <= 0) {
            this.cantidad = 0;
        } else {
            this.cantidad--;
        }
        return this.cantidad;  // Asegura que el método ahora retorna un int
    }
}
