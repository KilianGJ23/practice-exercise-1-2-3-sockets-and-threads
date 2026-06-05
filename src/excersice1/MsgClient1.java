package excersice1;

// Importaciones necesarias para el Cliente 1
import java.io.DataOutputStream;  // Para escribir datos primitivos al socket
import java.io.IOException;       // Para manejar errores de entrada/salida
import java.net.Socket;           // Para conectarnos al servidor
import java.util.Scanner;         // Para leer datos del teclado

public class MsgClient1 {

    public static void main(String[] args) {

        // Usamos Scanner para pedirle al usuario los datos de conexion
        Scanner teclado = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("     CLIENTE 1 - Enviar Mensajes        ");
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
            System.out.println("Escribe mensajes en minusculas para enviar.");
            System.out.println("Escribe \"salir\" para terminar.");
            System.out.println("----------------------------------------");

            // Creamos el flujo de salida para enviar datos al servidor
            DataOutputStream salida = new DataOutputStream(socket.getOutputStream());

            // Ciclo principal: el usuario escribe mensajes hasta que escriba "salir"
            while (true) {
                System.out.print("Tu mensaje: ");
                String mensaje = teclado.nextLine();

                // Convertimos el mensaje a bytes UTF-8
                byte[] bytesDelMensaje = mensaje.getBytes("UTF-8");

                // Enviamos siguiendo el protocolo: primero la longitud, luego los bytes
                salida.writeInt(bytesDelMensaje.length);
                salida.write(bytesDelMensaje);
                salida.flush();

                // Si el usuario escribio "salir", terminamos el ciclo
                if (mensaje.equals("salir")) {
                    System.out.println("Desconectando...");
                    break;
                }

                System.out.println("Mensaje enviado al servidor.");
            }

        } catch (IOException error) {
            System.out.println("Error en el Cliente 1: " + error.getMessage());
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
