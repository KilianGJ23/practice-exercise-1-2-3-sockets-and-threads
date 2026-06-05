package excersice2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class UpperClient {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // 1. Solicitamos los datos interactivos por terminal
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
        
        System.out.print("Ingrese el mensaje a enviar: ");
        String mensaje = scanner.nextLine();

        // 2. Se establece la conexión con el servidor (host y puerto) y se abren los flujos
        try (Socket socket = new Socket(host, port);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream())) {

            // --- FASE DE ENVÍO AL SERVIDOR ---
            byte[] msgBytes = mensaje.getBytes(StandardCharsets.UTF_8);
            out.writeInt(msgBytes.length);
            out.write(msgBytes);

            // --- FASE DE RECEPCIÓN DE RESPUESTA ---
            int replyLen = in.readInt();
            byte[] replyBytes = new byte[replyLen];
            in.readFully(replyBytes);

            String replyMensaje = new String(replyBytes, StandardCharsets.UTF_8);
            System.out.println("\nRecibido del servidor: " + replyMensaje);

        } catch (IOException e) {
            System.err.println("Error en el cliente: " + e.getMessage());
        }
    }
}
