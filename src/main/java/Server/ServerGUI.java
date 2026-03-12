/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
/**
 *
 * @author Chino
 */
public class ServerGUI extends JFrame {
    private JTable tablaInventario;
    private DefaultTableModel modeloTabla;

    public ServerGUI() {
        setTitle("Servidor - Inventario de Seguridad Industrial");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

       
        String[] columnas = {"ID", "Producto", "Precio ($)", "Stock Disponible"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaInventario = new JTable(modeloTabla);

        add(new JScrollPane(tablaInventario), BorderLayout.CENTER);
    }

 
    public void actualizarTabla(Map<String, com.mycompany.grpccarritocompras.Product> inventario) {
                modeloTabla.setRowCount(0);
        
        for (com.mycompany.grpccarritocompras.Product p : inventario.values()) {
            Object[] fila = {p.getId(), p.getName(), p.getPrice(), p.getQuantity()};
            modeloTabla.addRow(fila);
        }
    }
}
