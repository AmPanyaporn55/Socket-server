package org.example.project

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import java.net.Inet4Address
import java.net.ServerSocket
import java.net.Socket

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ServerUI()
        }
    }
}

@Composable
fun ServerUI() {
    var messagesToSend by remember { mutableStateOf("") }
    var clientIp by remember { mutableStateOf("") }
    var receivedMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val serverIp = getCurrentWifiIpAddress(context) ?: "IP not available"
    val serverPort = "4005"

    val backgroundColor = Color(0xFF001F3F)
    val textColor = Color(0xFFBFA100)
    val buttonColor = Color(0xFF0074D9)
    val buttonTextColor = Color.White

    LaunchedEffect(true) {
        setupServer { message ->
            receivedMessage = message
        }
    }

    Surface(color = backgroundColor, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Server IP: $serverIp\n\n", style = MaterialTheme.typography.h6.copy(color = textColor))
            Text("Port: $serverPort\n", style = MaterialTheme.typography.h6.copy(color = textColor))

            OutlinedTextField(
                value = clientIp,
                onValueChange = { clientIp = it },
                label = { Text("Client IP", color = textColor) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = textColor,
                    unfocusedBorderColor = textColor,
                    textColor = textColor
                )
            )

            OutlinedTextField(
                value = messagesToSend,
                onValueChange = { messagesToSend = it },
                label = { Text("Message to send", color = textColor) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = textColor,
                    unfocusedBorderColor = textColor,
                    textColor = textColor
                )
            )

            Button(
                onClick = {
                    if (clientIp.isNotBlank() && messagesToSend.isNotBlank()) {
                        sendMessageToClient(messagesToSend, clientIp, serverPort.toInt())
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = buttonColor)
            ) {
                Text("Send to Client", color = buttonTextColor)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .background(Color.LightGray)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("Messages from Client:", style = MaterialTheme.typography.subtitle1.copy(color = textColor))
                    if (receivedMessage.isNotEmpty()) {
                        Text(receivedMessage, style = MaterialTheme.typography.body1.copy(color = textColor))
                    }
                }
            }
        }
    }
}

private fun sendMessageToClient(message: String, ipAddress: String, port: Int) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            Socket(ipAddress, port).use { socket ->
                socket.getOutputStream().write(message.toByteArray())
                socket.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun getCurrentWifiIpAddress(context: Context): String? {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val wifiNetwork = connectivityManager.allNetworks.firstOrNull { network ->
        connectivityManager.getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    } ?: return null

    val linkProperties = connectivityManager.getLinkProperties(wifiNetwork) ?: return null
    return linkProperties.linkAddresses.firstOrNull { it.address is Inet4Address }?.address?.hostAddress
}

private suspend fun setupServer(onMessageReceived: (String) -> Unit) {
    withContext(Dispatchers.IO) {
        ServerSocket(4005).use { serverSocket ->
            while (true) {
                val clientSocket = serverSocket.accept()
                CoroutineScope(Dispatchers.IO).launch {
                    clientSocket.getInputStream().bufferedReader().use { reader ->
                        val message = reader.readLine()
                        onMessageReceived(message)
                    }
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
        ServerUI()
}