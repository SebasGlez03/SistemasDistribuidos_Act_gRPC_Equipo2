/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import com.mycompany.grpccarritocompras.*;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Chino
 */
public class ShoppingServiceImpl extends ShoppingServiceGrpc.ShoppingServiceImplBase {
    
        private final ConcurrentHashMap<String, Product> inventario = new ConcurrentHashMap<>();
    private final ServerGUI gui;

    public ShoppingServiceImpl(ServerGUI gui) {
        this.gui = gui;
        cargarInventarioInicial();
        this.gui.actualizarTabla(inventario);     }

    private void cargarInventarioInicial() {
        agregarProducto("EXT-01", "Extintor PQS 4.5kg", 650.0, 15);
        agregarProducto("CAS-01", "Casco de Seguridad", 120.0, 50);
        agregarProducto("CHAL-01", "Chaleco Reflejante con Logo", 85.0, 100);
        agregarProducto("SEN-01", "Senalamiento Ruta Evacuacion", 45.0, 200);
    }

    private void agregarProducto(String id, String nombre, double precio, int cantidad) {
        Product p = Product.newBuilder().setId(id).setName(nombre).setPrice(precio).setQuantity(cantidad).build();
        inventario.put(id, p);
    }

    @Override
    public void getCatalog(Empty request, StreamObserver<CatalogResponse> responseObserver) {
        CatalogResponse.Builder responseBuilder = CatalogResponse.newBuilder();
 
        responseBuilder.addAllProducts(inventario.values());
        
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void processOrder(OrderRequest request, StreamObserver<OrderResponse> responseObserver) {
               if (request.getItemsCount() == 0) {
            enviarRespuesta(responseObserver, false, "Error: El carrito está vacio.");
            return;
        }

                synchronized (this) {
         
            for (Product itemCliente : request.getItemsList()) {
                if (itemCliente.getQuantity() <= 0 || itemCliente.getPrice() <= 0) {
                    enviarRespuesta(responseObserver, false, "Error: Cantidad o precio invalido en " + itemCliente.getName());
                    return;
                }
                
                Product itemServidor = inventario.get(itemCliente.getId());
                if (itemServidor == null || itemServidor.getQuantity() < itemCliente.getQuantity()) {
                    enviarRespuesta(responseObserver, false, "Error: Stock insuficiente para " + itemCliente.getName());
                    return;
                }
            }

      
            for (Product itemCliente : request.getItemsList()) {
                Product itemActual = inventario.get(itemCliente.getId());
                int nuevoStock = itemActual.getQuantity() - itemCliente.getQuantity();
                
                // Actualizamos el producto en el mapa
                Product productoActualizado = itemActual.toBuilder().setQuantity(nuevoStock).build();
                inventario.put(itemCliente.getId(), productoActualizado);
            }
            
      
            gui.actualizarTabla(inventario);
        }

        enviarRespuesta(responseObserver, true, "¡Compra procesada con exito!");
    }

    private void enviarRespuesta(StreamObserver<OrderResponse> observer, boolean exito, String mensaje) {
        OrderResponse response = OrderResponse.newBuilder().setSuccess(exito).setMessage(mensaje).build();
        observer.onNext(response);
        observer.onCompleted();
    }
}
