package excersice1;

// Importaciones necesarias para el servidor
import java.io.DataInputStream;   // Para leer datos primitivos del socket
import java.io.DataOutputStream;  // Para escribir datos primitivos al socket
import java.io.IOException;       // Para manejar errores de entrada/salida
import java.net.ServerSocket;     // Para crear el servidor que escucha conexiones
import java.net.Socket;           // Para manejar la conexion con cada cliente
import java.util.Locale;          // Para usar Locale.ROOT en las transformaciones
import java.util.Scanner;         // Para leer datos del teclado

public class MsgServer {

    // =====================================================================
    // Variables compartidas entre los hilos del Cliente 1 y Cliente 2.
    // "volatile" hace que los cambios de un hilo sean visibles para el otro.
    // =====================================================================

    // Aqui se guarda el mensaje transformado que va a recibir el Cliente 2
    private static volatile String mensajeTransformado = null;

    // Indica si el mensaje del Cliente 1 era valido (estaba en minusculas)
    private static volatile boolean mensajeValido = false;

    // Indica si hay un nuevo mensaje listo para enviar al Cliente 2
    private static volatile boolean mensajeListo = false;

    // Indica si el Cliente 1 ya termino de enviar mensajes (escribio "salir")
    private static volatile boolean finConversacion = false;

    // Objeto candado para sincronizar los hilos con wait() y notify()
    private static final Object candado = new Object();

    // =====================================================================
    // Metodo que maneja todo lo del Cliente 1 (lee mensajes en un ciclo).
    // Lo separamos en un metodo en vez de usar "new Runnable() anonimo"
    // para que Java NO genere archivos .class extra como MsgServer$1.class
    // =====================================================================
    private static void atenderCliente1(Socket socketCliente1) {
        try {
            // Creamos el flujo para leer datos del Cliente 1
            DataInputStream entrada = new DataInputStream(socketCliente1.getInputStream());

            // Ciclo que lee mensajes mientras el Cliente 1 no se desconecte
            while (true) {
                // Leemos la longitud del mensaje (protocolo: primero int)
                int longitudMensaje = entrada.readInt();

                // Leemos los bytes del mensaje
                byte[] bytesDelMensaje = new byte[longitudMensaje];
                entrada.readFully(bytesDelMensaje);

                // Convertimos los bytes a String
                String mensaje = new String(bytesDelMensaje, "UTF-8");

                // Si el mensaje es "salir", terminamos el ciclo
                if (mensaje.equals("salir")) {
                    System.out.println("[Servidor] Cliente 1 ha salido de la conversacion.");
                    // Avisamos al hilo del Cliente 2 que ya termino
                    synchronized (candado) {
                        finConversacion = true;
                        mensajeListo = true;
                        candado.notify();
                    }
                    break; // Salimos del ciclo while
                }

                System.out.println("[Servidor] Mensaje recibido: \"" + mensaje + "\"");

                // Validamos que el mensaje este en minusculas
                boolean esMinusculas = mensaje.equals(mensaje.toLowerCase(Locale.ROOT));

                // Actualizamos las variables compartidas de forma segura
                synchronized (candado) {
                    if (esMinusculas) {
                        // Transformamos a mayusculas
                        mensajeTransformado = mensaje.toUpperCase(Locale.ROOT);
                        mensajeValido = true;
                        System.out.println("[Servidor] Transformado a: \"" + mensajeTransformado + "\"");
                    } else {
                        mensajeValido = false;
                        mensajeTransformado = null;
                        System.out.println("[Servidor] Mensaje NO estaba en minusculas. Enviando ok=false.");
                    }

                    // Avisamos al hilo del Cliente 2 que hay un mensaje nuevo
                    mensajeListo = true;
                    candado.notify();
                }
            }

            socketCliente1.close();

        } catch (IOException error) {
            System.out.println("[Servidor] Cliente 1 se desconecto: " + error.getMessage());
            // Si el Cliente 1 se desconecta de golpe, avisamos al Cliente 2
            synchronized (candado) {
                finConversacion = true;
                mensajeListo = true;
                candado.notify();
            }
        }
    }

    // =====================================================================
    // Metodo que maneja todo lo del Cliente 2 (envia mensajes en un ciclo).
    // Igual lo separamos para evitar archivos .class extra.
    // =====================================================================
    private static void atenderCliente2(Socket socketCliente2) {
        try {
            // Creamos el flujo para escribir datos al Cliente 2
            DataOutputStream salida = new DataOutputStream(socketCliente2.getOutputStream());

            // Ciclo que envia mensajes mientras la conversacion no termine
            while (true) {
                // Esperamos a que haya un mensaje listo
                synchronized (candado) {
                    while (!mensajeListo) {
                        try {
                            candado.wait();
                        } catch (InterruptedException e) {
                            System.out.println("[Servidor] Hilo Cliente 2 interrumpido.");
                        }
                    }
                    // Reseteamos la bandera para el proximo mensaje
                    mensajeListo = false;
                }

                // Si la conversacion termino, avisamos al Cliente 2 y salimos
                if (finConversacion) {
                    // Enviamos una senal especial: ok=false y longitud=-1
                    // para que el Cliente 2 sepa que la conversacion termino
                    salida.writeBoolean(false);
                    salida.writeInt(-1); // -1 significa "fin de conversacion"
                    salida.flush();
                    break;
                }

                // Enviamos el resultado al Cliente 2 segun el protocolo
                salida.writeBoolean(mensajeValido);

                if (mensajeValido) {
                    byte[] bytesTransformados = mensajeTransformado.getBytes("UTF-8");
                    salida.writeInt(bytesTransformados.length);
                    salida.write(bytesTransformados);
                } else {
                    salida.writeInt(0);
                }
                salida.flush();
            }

            socketCliente2.close();

        } catch (IOException error) {
            System.out.println("[Servidor] Cliente 2 se desconecto: " + error.getMessage());
        }
    }

    public static void main(String[] args) {

        // Usamos Scanner para pedirle al usuario el puerto por teclado
        Scanner teclado = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("           SERVIDOR DE MENSAJES         ");
        System.out.println("========================================");
        System.out.print("Ingrese el puerto para el servidor: ");
        int puerto = teclado.nextInt();

        ServerSocket serverSocket = null;

        try {
            // Creamos el ServerSocket en el puerto que eligio el usuario
            serverSocket = new ServerSocket(puerto);
            System.out.println("Servidor iniciado en el puerto: " + puerto);
            System.out.println("Esperando que se conecte el Cliente 1...");

            // Aceptamos la conexion del Cliente 1 (el que envia mensajes)
            Socket socketCliente1 = serverSocket.accept();
            System.out.println("Cliente 1 conectado desde: " + socketCliente1.getInetAddress());
            System.out.println("Esperando que se conecte el Cliente 2...");

            // Aceptamos la conexion del Cliente 2 (el que recibe mensajes)
            Socket socketCliente2 = serverSocket.accept();
            System.out.println("Cliente 2 conectado desde: " + socketCliente2.getInetAddress());
            System.out.println("Ambos clientes conectados. La conversacion puede comenzar.");
            System.out.println("----------------------------------------");

            // Creamos los hilos pasandoles los metodos que definimos arriba.
            // Usamos lambdas simples ( () -> metodo() ) que NO generan .class extra.
            Thread hiloCliente1 = new Thread(() -> atenderCliente1(socketCliente1));
            Thread hiloCliente2 = new Thread(() -> atenderCliente2(socketCliente2));

            // Iniciamos ambos hilos
            hiloCliente1.start();
            hiloCliente2.start();

            // Esperamos a que ambos terminen
            hiloCliente1.join();
            hiloCliente2.join();

            System.out.println("[Servidor] Conversacion finalizada. Servidor cerrado.");

        } catch (IOException error) {
            System.out.println("Error en el servidor: " + error.getMessage());
        } catch (InterruptedException error) {
            System.out.println("El servidor fue interrumpido: " + error.getMessage());
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException error) {
                    System.out.println("Error al cerrar el ServerSocket: " + error.getMessage());
                }
            }
            teclado.close();
        }
    }
}
