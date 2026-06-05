package excersice2;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ListServer {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // 1. Pedimos los parámetros por consola
        System.out.print("Ingrese el puerto a escuchar (ej. 5000): ");
        int port;
        int n;
        try {
            port = Integer.parseInt(scanner.nextLine());
            System.out.print("Ingrese el número N (debe ser mayor a 10 y menor a 20): ");
            n = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("Los valores ingresados deben ser números enteros.");
            return;
        }

        // 2. Validamos la regla obligatoria: N debe estar entre 11 y 19 (inclusive)
        if (n <= 10 || n >= 20) {
            System.err.println("Error: N debe ser mayor a 10 y menor de 20");
            return;
        }

        // 3. Creamos el socket de servidor para recibir conexiones en el puerto indicado
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("ListServer activo en el puerto " + port + ". Generando hasta " + n);
            
            // 4. Bucle infinito para atender solicitudes (una por una)
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleClient(clientSocket, n);
                } catch (IOException e) {
                    System.err.println("Error al aceptar conexión: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket, int n) {
        try (DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {
            
            // Envía primero N
            out.writeInt(n);
            
            // Luego se envían de forma ordenada todos los enteros del 1 al N
            for (int i = 1; i <= n; i++) {
                out.writeInt(i);
            }
            
        } catch (IOException e) {
            System.err.println("Error en la comunicación con el cliente: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}
