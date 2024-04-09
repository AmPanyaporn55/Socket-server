import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket

fun startServer(port: Int) {
    val serverSocket = ServerSocket(port)
    println("Server is running on port $port")

    while (true) {
        val clientSocket = serverSocket.accept()
        println("Client connected: ${clientSocket.inetAddress.hostAddress}")

        val input = BufferedReader(InputStreamReader(clientSocket.inputStream))
        val output = PrintWriter(clientSocket.outputStream, true)

        val message = input.readLine()
        println("Received from client: $message")

        // Echo the message back to the client
        output.println("Echo: $message")

        clientSocket.close()
    }
}

fun main() {
    startServer(4001)
}
