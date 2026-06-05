package excersice2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;

public class UpperServer {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // 1. Pedimos al usuario que ingrese el puerto por consola
        System.out.print("Ingrese el puerto a escuchar (ej. 5000): ");
        int port;
        try {
            port = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("El puerto debe ser un número entero.");
            return;
        }

        // 2. Creamos el ServerSocket para escuchar conexiones entrantes en el puerto dado
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor iniciado y escuchando en el puerto: " + port);
            
            // 3. Bucle infinito para aceptar múltiples clientes uno por uno
            while (true) {
                try {
                    // 4. El servidor se bloquea aquí hasta que un cliente se conecta
                    Socket clientSocket = serverSocket.accept();
                    
                    // 5. Procesamos la petición del cliente
                    handleClient(clientSocket);
                } catch (IOException e) {
                    System.err.println("Error al aceptar conexión: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }

    // Método encargado de manejar la comunicación con un cliente específico
    private static void handleClient(Socket clientSocket) {
        try (
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            // --- FASE DE RECEPCIÓN ---
            int msgLen = in.readInt();
            byte[] msgBytes = new byte[msgLen];
            in.readFully(msgBytes); 
            
            String mensaje = new String(msgBytes, StandardCharsets.UTF_8);
            System.out.println("\nRecibido: " + mensaje);
            
            // --- FASE DE TRANSFORMACIÓN ---
            String mensajeTransformado = mensaje.toUpperCase(Locale.ROOT);
            System.out.println("Enviado: " + mensajeTransformado);
            
            // --- FASE DE ENVÍO ---
            byte[] transformedBytes = mensajeTransformado.getBytes(StandardCharsets.UTF_8);
            out.writeInt(transformedBytes.length);
            out.write(transformedBytes);
            
        } catch (IOException e) {
            System.err.println("Error en la comunicación con el cliente: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // Se ignora si falla al cerrar
            }
        }
    }
}
