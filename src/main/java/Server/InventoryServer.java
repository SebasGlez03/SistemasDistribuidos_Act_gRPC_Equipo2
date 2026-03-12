/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
/**
 *
 * @author Chino
 */
public class InventoryServer {
    public static void main(String[] args) throws IOException, InterruptedException {
  
        ServerGUI gui = new ServerGUI();
        gui.setVisible(true);

    
        int port = 9090;
        Server server = ServerBuilder.forPort(port)
                .addService(new ShoppingServiceImpl(gui))
                .build()
                .start();

        System.out.println("Servidor iniciado en el puerto " + port);
        

        server.awaitTermination();
    }
}
