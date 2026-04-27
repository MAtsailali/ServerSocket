import java.io.*
import java.net.ServerSocket
import java.net.Socket

fun main() {
    val port = 1234
    val server = ServerSocket(port)
    println("🚀 Servidor de fitxers actiu al port $port...")

    while (true) {
        try {
            val clientSocket = server.accept()
            println("📱 Client connectat des de: ${clientSocket.inetAddress.hostAddress}")
            handleClient(clientSocket)
        } catch (e: Exception) {
            println("❌ Error en la connexió: ${e.message}")
        }
    }
}

fun handleClient(socket: Socket) {
    try {
        val input = DataInputStream(socket.getInputStream())
        val output = DataOutputStream(socket.getOutputStream())

        val accio = input.readUTF()
        val userId = input.readUTF()
        val fileName = input.readUTF()

        val directory = File("uploads/$userId")
        if (!directory.exists()) directory.mkdirs()

        val file = File(directory, fileName)

        when (accio) {
            "PUJAR" -> {
                try {
                    val mida = input.readLong()
                    println("📥 Rebent fitxer: $fileName ($mida bytes)")

                    // Aseguramos que el directorio padre existe (por si fileName tiene subcarpetas)
                    file.parentFile?.mkdirs()

                    FileOutputStream(file).use { fos ->
                        val buffer = ByteArray(8192)
                        var totalLlegit = 0L
                        while (totalLlegit < mida) {
                            val aLlegir = minOf(buffer.size.toLong(), mida - totalLlegit).toInt()
                            val llegitsNow = input.read(buffer, 0, aLlegir)
                            if (llegitsNow == -1) break
                            fos.write(buffer, 0, llegitsNow)
                            totalLlegit += llegitsNow
                        }
                        fos.flush()
                    }
                    println("✅ Fitxer guardat correctament a: ${file.absolutePath}")
                } catch (e: Exception) {
                    println("❌ Error en PUJAR: ${e.message}")
                }
            }

            "BAIXAR" -> {
                if (file.exists()) {
                    println("📤 Enviant fitxer: ${file.name} (${file.length()} bytes)")

                    output.writeBoolean(true)
                    output.writeLong(file.length())
                    output.flush()

                    file.inputStream().use { fis ->
                        val buffer = ByteArray(8192)
                        var bytesLeidos: Int
                        while (fis.read(buffer).also { bytesLeidos = it } != -1) {
                            output.write(buffer, 0, bytesLeidos)
                        }
                    }
                    output.flush()
                    // SIN shutdownOutput() — dejamos que el cliente cierre primero
                    Thread.sleep(500) // damos tiempo al cliente para leer todos los bytes
                    println("✅ Enviament completat.")
                } else {
                    println("⚠️ L'arxiu no existeix: ${file.absolutePath}")
                    output.writeBoolean(false)
                    output.flush()
                }
            }
        }

    } catch (e: Exception) {
        println("❌ Error processant dades: ${e.message}")
    } finally {
        try { socket.close() } catch (e: Exception) { }
        println("🔌 Connexió tancada.")
    }
}