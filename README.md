## Getting Started

Welcome to the VS Code Java world. Here is a guideline to help you get started to write Java code in Visual Studio Code.

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.

## Dependency Management

The `JAVA PROJECTS` view allows you to manage your dependencies. More details can be found [here](https://github.com/microsoft/vscode-java-dependency#manage-dependencies).

## Sockets y Threads: Comunicación Cliente-Servidor

En aplicaciones de red en Java, la comunicación entre un cliente y un servidor se realiza a través de **Sockets**. Para manejar múltiples conexiones simultáneas sin que el servidor se bloquee, se hace uso de **Threads** (hilos).

### Tipos de Datos a Enviar entre Servidor y Cliente

A través de un Socket, la información siempre viaja como una secuencia de bytes (`InputStream` / `OutputStream`), pero Java proporciona diferentes clases ("Wrappers") para enviar y recibir datos de manera más estructurada:

1. **Texto o Cadenas (Strings):**
   - **Clases:** `PrintWriter` (para enviar) y `BufferedReader` (para leer).
   - **Uso ideal:** Enviar mensajes de chat, comandos de texto o datos estructurados como JSON/XML.
   - **Ejemplo:** `out.println("Hola servidor");` / `String msj = in.readLine();`

2. **Tipos Primitivos (int, double, boolean, etc.):**
   - **Clases:** `DataOutputStream` (para enviar) y `DataInputStream` (para leer).
   - **Uso ideal:** Enviar datos exactos sin convertirlos a texto, como coordenadas, valores booleanos o tamaños de archivos.
   - **Ejemplo:** `out.writeInt(100);` / `int valor = in.readInt();`

3. **Objetos Java (Serialización):**
   - **Clases:** `ObjectOutputStream` (para enviar) y `ObjectInputStream` (para leer).
   - **Uso ideal:** Enviar instancias completas de clases (ej. una clase `Mensaje` o `Usuario`). La clase debe implementar la interfaz `java.io.Serializable`.
   - **Ejemplo:** `out.writeObject(nuevoUsuario);` / `Usuario obj = (Usuario) in.readObject();`

4. **Archivos o Datos Binarios (Bytes crudos):**
   - **Clases:** `FileInputStream`/`FileOutputStream` combinados con los `InputStream`/`OutputStream` del socket, o usando buffers (`BufferedInputStream`).
   - **Uso ideal:** Transferir imágenes, audios, PDFs o cualquier archivo binario de extremo a extremo.

5. **Listas o Colecciones (`List`, `ArrayList`):**
   Existen principalmente tres formas de enviar una lista o colección:
   - **Serialización de Objetos:** Ya que `ArrayList` implementa `Serializable`, puedes enviarla directamente usando `ObjectOutputStream`, siempre y cuando todos los elementos dentro de la lista también sean serializables.
     *Ejemplo:* `out.writeObject(miLista);` / `List<String> lista = (List<String>) in.readObject();`
   - **Elemento por elemento:** Puedes enviar primero un entero con el tamaño de la lista (`out.writeInt(lista.size());`) y luego iterar la lista enviando cada elemento en un bucle for. El receptor lee el tamaño y usa un bucle para reconstruir la lista.
   - **Formato JSON/XML:** Puedes convertir la lista a un texto JSON (usando librerías como Gson o Jackson) y enviarla como un `String` usando `PrintWriter`, luego el receptor la parsea de vuelta a una lista.

### Formas de usar Sockets y Threads (Arquitecturas)

Existen diferentes formas de implementar la relación entre sockets e hilos, dependiendo de la escala de la aplicación:

#### 1. Servidor Monohilo (Single-threaded)
- **Concepto:** El servidor atiende a un solo cliente a la vez. Mientras procesa la petición del primer cliente, los demás deben esperar.
- **Uso:** Pruebas muy básicas o cuando la conexión dura milisegundos.
- **Implementación:** Bucle `while(true) { Socket client = serverSocket.accept(); procesar(client); }`

#### 2. Servidor Multihilo Clásico (Un Hilo por Cliente - "Thread per Client")
- **Concepto:** Cada vez que el servidor acepta una nueva conexión, crea un nuevo `Thread` exclusivamente para ese cliente.
- **Uso:** Aplicaciones de chat simples, juegos multijugador a pequeña escala, donde las conexiones son continuas.
- **Implementación:**
  ```java
  while (true) {
      Socket clientSocket = serverSocket.accept();
      new Thread(new ClientHandler(clientSocket)).start(); // ClientHandler implementa Runnable
  }
  ```

#### 3. Servidor con Pool de Hilos (Thread Pool)
- **Concepto:** En lugar de crear un hilo nuevo infinitamente por cada cliente (lo cual puede colapsar la memoria del servidor), se usa un "Pool" (piscina) de hilos limitados (ej. 10 hilos). Si llegan 11 clientes, el #11 espera a que un hilo se libere.
- **Uso:** Servidores web y APIs en producción donde se necesita controlar el consumo de recursos.
- **Implementación:** Usando `ExecutorService`.
  ```java
  ExecutorService pool = Executors.newFixedThreadPool(10);
  while (true) {
      Socket clientSocket = serverSocket.accept();
      pool.execute(new ClientHandler(clientSocket));
  }
  ```

#### 4. I/O No Bloqueante (Java NIO)
- **Concepto:** No usa múltiples hilos por conexión. Un solo hilo (llamado `Selector`) puede monitorear cientos o miles de canales (`SocketChannel`) simultáneamente y atender solo a aquellos que tienen datos listos para leer o escribir.
- **Uso:** Servidores de altísimo rendimiento o servidores de juegos masivos (MMORPGs).

#### 5. Multihilo del lado del Cliente
- **Concepto:** El cliente también necesita hilos. Un hilo principal (el de la Interfaz Gráfica o la consola) envía mensajes al servidor, mientras que un hilo secundario en segundo plano está constantemente escuchando los mensajes que llegan del servidor para no "congelar" la aplicación.
- **Implementación:** En el lado cliente, se lanza un `Thread` que ejecuta `in.readLine()` en un bucle infinito recibiendo respuestas, y el hilo principal envía enviando `out.println()`.
