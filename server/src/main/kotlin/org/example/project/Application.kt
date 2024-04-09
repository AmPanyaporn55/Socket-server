package org.example.project

import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket

fun main() {
    runBlocking {
        launch {
            startServer()
        }

        delay(1000)

        startClient()
    }
}

fun startServer() {
    val serverSocket = ServerSocket(4003)
    println("Server is running on port 4003")

    while (true) {
        val clientSocket = serverSocket.accept()
        println("Client connected: ${clientSocket.inetAddress.hostAddress}")

        val reader = BufferedReader(InputStreamReader(clientSocket.inputStream))
        val writer = PrintWriter(clientSocket.outputStream, true)

        val message = reader.readLine()
        println("Message from client: $message")

        writer.println("Echo: $message")

        clientSocket.close()
    }
}

suspend fun startClient() {
    val selectorManager = ActorSelectorManager(Dispatchers.IO)
    val socket = aSocket(selectorManager).tcp().connect("127.0.0.1", 4003)

    val receiveChannel = socket.openReadChannel()
    val sendChannel = socket.openWriteChannel(autoFlush = true)

    sendChannel.writeStringUtf8("Hello from the client!\n")

    val response = receiveChannel.readUTF8Line(1000)
    println(response)

    socket.close()
    selectorManager.close()
}

