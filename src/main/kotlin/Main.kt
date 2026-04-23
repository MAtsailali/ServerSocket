import java.io.*
import java.net.ServerSocket
import java.net.Socket

fun main() {
    val port = 1234
    val server = ServerSocket(port)
    println("🚀 Servidor de fitxers actiu al port $port...")

    while (true) {
        try {
            // El servidor es queda esperant fins que arriba un client
            val clientSocket = server.accept()
            println("📱 Client connectat des de: ${clientSocket.inetAddress.hostAddress}")
            
            // Gestionem la petició del client
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

        // 1. Llegim la "negociació" inicial
        val accio = input.readUTF()     // "PUJAR" o "BAIXAR"
        val userId = input.readUTF()    // El telèfon del client
        val fileName = input.readUTF()  // El nom de l'arxiu (ex: dni.jpg)

        // 2. Preparem la ruta del fitxer (/app/uploads/telèfon/dni.jpg)
        val directory = File("uploads/$userId")
        if (!directory.exists()) directory.mkdirs()
        
        val file = File(directory, fileName)

        when (accio) {
            "PUJAR" -> {
                println("📥 Rebent fitxer xifrat de l'usuari $userId...")
                
                FileOutputStream(file).use { fos ->
                    var byteLlegit: Int
                    // LLEGIM BYTE A BYTE (Segons requeriment de la pràctica)
                    // Nota: read() retorna un int de 0 a 255, o -1 si s'acaba el stream
                    while (input.read().also { byteLlegit = it } != -1) {
                        fos.write(byteLlegit)
                    }
                }
                println("✅ Fitxer guardat correctament a: ${file.absolutePath}")
            }

            "BAIXAR" -> {
                if (file.exists()) {
                    println("📤 Enviant fitxer xifrat a l'usuari $userId...")
                    output.writeBoolean(true) // Confirmem que el fitxer existeix

                    FileInputStream(file).use { fis ->
                        var byteAEnviar: Int
                        // ENVIEM BYTE A BYTE
                        while (fis.read().also { byteAEnviar = it } != -1) {
                            output.write(byteAEnviar)
                        }
                    }
                    output.flush()
                    println("✅ Enviament completat.")
                } else {
                    println("⚠️ L'arxiu sol·licitat no existeix: ${file.name}")
                    output.writeBoolean(false) // Informem que no existeix
                }
            }
        }

    } catch (e: Exception) {
        println("❌ Error processant dades: ${e.message}")
    } finally {
        socket.close()
        println("🔌 Connexió tancada.")
    }
}