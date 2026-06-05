package excersice1;

// Importaciones necesarias para el Cliente 2
import java.io.DataInputStream;   // Para leer datos primitivos del socket
import java.io.IOException;       // Para manejar errores de entrada/salida
import java.net.Socket;           // Para conectarnos al servidor
import java.util.Scanner;         // Para leer datos del teclado

public class MsgClient2 {

    public static void main(String[] args) {

        // Usamos Scanner para pedirle al usuario los datos de conexion
        Scanner teclado = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("     CLIENTE 2 - Recibir Mensajes       ");
        System.out.println("========================================");

        // Pedimos la IP/host del servidor
        System.out.print("Ingrese la IP del servidor (ej: localhost): ");
        String host = teclado.nextLine();

        // Pedimos el puerto del servidor
        System.out.print("Ingrese el puerto del servidor: ");
        int puerto = Integer.parseInt(teclado.nextLine());

        Socket socket = null;

        try {
            // Nos conectamos al servidor
            socket = new Socket(host, puerto);
            System.out.println("Conectado al servidor en " + host + ":" + puerto);
            System.out.println("----------------------------------------");
            System.out.println("Esperando mensajes...");
            System.out.println("----------------------------------------");

            // Creamos el flujo de entrada para recibir datos del servidor
            DataInputStream entrada = new DataInputStream(socket.getInputStream());

            // Ciclo principal: recibimos mensajes hasta que el servidor indique que termino
            while (true) {
                // Leemos el boolean "ok" del protocolo
                boolean ok = entrada.readBoolean();

                // Leemos la longitud del mensaje
                int longitudMensaje = entrada.readInt();

                // Si la longitud es -1, significa que la conversacion termino
                // (el Cliente 1 escribio "salir")
                if (longitudMensaje == -1) {
                    System.out.println("El Cliente 1 ha terminado la conversacion.");
                    break;
                }

                if (ok && longitudMensaje > 0) {
                    // Caso exitoso: leemos el mensaje transformado
                    byte[] bytesDelMensaje = new byte[longitudMensaje];
                    entrada.readFully(bytesDelMensaje);
                    String mensajeRecibido = new String(bytesDelMensaje, "UTF-8");

                    System.out.println(">>> Mensaje recibido: " + mensajeRecibido);
                } else {
                    // Caso de error: el mensaje no estaba en minusculas
                    System.out.println(">>> [Error] El mensaje del Cliente 1 NO estaba en minusculas (ok=false)");
                }
            }

        } catch (IOException error) {
            System.out.println("Error en el Cliente 2: " + error.getMessage());
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                    System.out.println("Conexion cerrada.");
                } catch (IOException error) {
                    System.out.println("Error al cerrar la conexion: " + error.getMessage());
                }
            }
            teclado.close();
        }
    }
}
