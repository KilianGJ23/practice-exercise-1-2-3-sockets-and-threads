package excersice3;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ListClient {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // 1. Pedimos host y puerto por consola
        System.out.print("Ingrese el host del servidor (ej. localhost): ");
        String host = scanner.nextLine();
        
        System.out.print("Ingrese el puerto del servidor (ej. 5000): ");
        int port;
        try {
            port = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("El puerto debe ser un número entero.");
            return;
        }

        // 2. Nos conectamos al ListServer usando el host y puerto especificado
        try (Socket socket = new Socket(host, port);
            DataInputStream in = new DataInputStream(socket.getInputStream())) {

            // Recibimos el tope de la lista, N
            int n = in.readInt();
            
            System.out.print("\nLista recibida:");
            
            // Recibimos los N números
            for (int i = 0; i < n; i++) {
                int value = in.readInt();
                System.out.print(" " + value);
            }
            System.out.println();

        } catch (IOException e) {
            System.err.println("Error en el cliente: " + e.getMessage());
        }
    }
}
