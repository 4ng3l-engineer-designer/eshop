package cl.dci.eshop.model;

import javax.persistence.*;

@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column
    private String nombre;

    @Column
    private int precio;

    @Column
    private String imagenUrl;

    @Column
    private int stock; // Campo para la cantidad disponible en stock

    @Column(length = 500)
    private String descripcion; // Campo para la descripción del producto

    // Constructor por defecto
    public Producto() {
    }

    // Constructor con parámetros
    public Producto(String nombre, int precio, String imagenUrl, int stock, String descripcion) {
        this.nombre = nombre;
        this.precio = precio;
        this.imagenUrl = imagenUrl;
        this.stock = stock;
        this.descripcion = descripcion;
    }

    // Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getPrecio() {
        return precio;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", imagenUrl='" + imagenUrl + '\'' +
                ", stock=" + stock +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
