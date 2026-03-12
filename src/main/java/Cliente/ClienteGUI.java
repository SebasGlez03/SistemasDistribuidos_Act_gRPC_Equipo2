/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Cliente;

import com.mycompany.grpccarritocompras.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Chino
 */
public class ClienteGUI extends JFrame {
    private ManagedChannel channel;
    private ShoppingServiceGrpc.ShoppingServiceBlockingStub stub;
    
    private JTable tablaCatalogo, tablaCarrito;
    private DefaultTableModel modeloCatalogo, modeloCarrito;
    private List<Product> carritoLocal = new ArrayList<>();

    public ClienteGUI() {
        setTitle("Punto de Venta - Seguridad Industrial Obregon");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(2, 1, 10, 10));

        channel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build();
        stub = ShoppingServiceGrpc.newBlockingStub(channel);

        JPanel panelCatalogo = new JPanel(new BorderLayout());
        panelCatalogo.setBorder(BorderFactory.createTitledBorder("Catalogo de Productos"));
        
        modeloCatalogo = new DefaultTableModel(new String[]{"ID", "Producto", "Precio ($)", "Stock Servidor"}, 0);
        tablaCatalogo = new JTable(modeloCatalogo);
        panelCatalogo.add(new JScrollPane(tablaCatalogo), BorderLayout.CENTER);
        
        JButton btnAgregar = new JButton("Agregar al Carrito");
        panelCatalogo.add(btnAgregar, BorderLayout.SOUTH);

        JPanel panelCarrito = new JPanel(new BorderLayout());
        panelCarrito.setBorder(BorderFactory.createTitledBorder("Mi Carrito"));
        
        modeloCarrito = new DefaultTableModel(new String[]{"ID", "Producto", "Precio ($)", "Cantidad a Comprar"}, 0);
        tablaCarrito = new JTable(modeloCarrito);
        panelCarrito.add(new JScrollPane(tablaCarrito), BorderLayout.CENTER);
        
        JPanel panelBotonesCarrito = new JPanel();
        JButton btnQuitar = new JButton("Quitar Producto");
        JButton btnComprar = new JButton("Finalizar Compra");
        panelBotonesCarrito.add(btnQuitar);
        panelBotonesCarrito.add(btnComprar);
        panelCarrito.add(panelBotonesCarrito, BorderLayout.SOUTH);

        add(panelCatalogo);
        add(panelCarrito);

        cargarCatalogo();

        btnAgregar.addActionListener(e -> {
            int fila = tablaCatalogo.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un producto del catalogo primero.");
                return;
            }
            
            String id = (String) modeloCatalogo.getValueAt(fila, 0);
            String nombre = (String) modeloCatalogo.getValueAt(fila, 1);
            double precio = (double) modeloCatalogo.getValueAt(fila, 2);
            int stockDisponible = (int) modeloCatalogo.getValueAt(fila, 3);
            
            if (stockDisponible <= 0) {
                JOptionPane.showMessageDialog(this, "Producto agotado. Ya no hay stock.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String cantStr = JOptionPane.showInputDialog(this, "Cuantos " + nombre + " vas a llevar?");
            if (cantStr != null && !cantStr.isEmpty()) {
                try {
                    int cantidad = Integer.parseInt(cantStr);
                    
                    if (cantidad > stockDisponible) {
                        JOptionPane.showMessageDialog(this, "Solo hay " + stockDisponible + " unidades disponibles.", "Aviso", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    
                    if (cantidad > 0) {
                        Product p = Product.newBuilder().setId(id).setName(nombre).setPrice(precio).setQuantity(cantidad).build();
                        carritoLocal.add(p);
                        modeloCarrito.addRow(new Object[]{id, nombre, precio, cantidad});
                    } else {
                        JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a 0.");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Ingresa un numero valido.");
                }
            }
        });

        btnQuitar.addActionListener(e -> {
            int fila = tablaCarrito.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un producto de tu carrito para quitarlo.");
                return;
            }
            carritoLocal.remove(fila);
            modeloCarrito.removeRow(fila);
        });

        btnComprar.addActionListener(e -> {
            if (carritoLocal.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El carrito esta vacio.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            OrderRequest request = OrderRequest.newBuilder().addAllItems(carritoLocal).build();
            OrderResponse response = stub.processOrder(request);
            
            if (response.getSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Exito", JOptionPane.INFORMATION_MESSAGE);
                carritoLocal.clear();
                modeloCarrito.setRowCount(0);
                cargarCatalogo(); 
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Error en la compra", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void cargarCatalogo() {
        modeloCatalogo.setRowCount(0);
        CatalogResponse response = stub.getCatalog(Empty.newBuilder().build());
        for (Product p : response.getProductsList()) {
            modeloCatalogo.addRow(new Object[]{p.getId(), p.getName(), p.getPrice(), p.getQuantity()});
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClienteGUI().setVisible(true));
    }
}