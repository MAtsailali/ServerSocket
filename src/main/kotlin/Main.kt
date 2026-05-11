import java.io.*
import java.net.ServerSocket
import java.net.Socket
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.concurrent.thread 

private const val KEY_STRING = "1234567812345678"
private const val ALGORITHM = "AES/ECB/PKCS5Padding"

fun xifrar(dades: ByteArray): ByteArray {
    val key = SecretKeySpec(KEY_STRING.toByteArray(), "AES")
    val cipher = Cipher.getInstance(ALGORITHM)
    cipher.init(Cipher.ENCRYPT_MODE, key)
    return cipher.doFinal(dades)
}

fun desxifrar(dades: ByteArray): ByteArray {
    val key = SecretKeySpec(KEY_STRING.toByteArray(), "AES")
    val cipher = Cipher.getInstance(ALGORITHM)
    cipher.init(Cipher.DECRYPT_MODE, key)
    return cipher.doFinal(dades)
}

fun main() {
    val port = 1234
    val server = ServerSocket(port)
    println("🚀 Servidor MULTIHILO activo en el puerto $port...")

    while (true) {
        try {
            val clientSocket = server.accept()
            println("📱 Nuevo cliente conectado: ${clientSocket.inetAddress.hostAddress}")

            // Esto crea y arranca un hilo inmediatamente para cada cliente.
            thread {
                handleClient(clientSocket)
            }

        } catch (e: Exception) {
            println("❌ Error aceptando conexión: ${e.message}")
        }
    }
}

fun handleClient(socket: Socket) {
    // Se añade un identificador de hilo para debuggear mejor en consola
    val threadId = Thread.currentThread().id
    try {
        val input = DataInputStream(socket.getInputStream())
        val output = DataOutputStream(socket.getOutputStream())

        val accio = input.readUTF()
        val userId = input.readUTF()
        val fileName = input.readUTF()

        println("[Hilo $threadId] Acción: $accio | Usuario: $userId | Archivo: $fileName")

        val directory = File("uploads/$userId")
        if (!directory.exists()) directory.mkdirs()

        val file = File(directory, fileName)

        when (accio) {
            "PUJAR" -> {
                try {
                    val mida = input.readLong()
                    val bytesCifrats = ByteArray(mida.toInt())
                    var totalLlegit = 0
                    while (totalLlegit < mida) {
                        val llegitsNow = input.read(bytesCifrats, totalLlegit, (mida - totalLlegit).toInt())
                        if (llegitsNow == -1) break
                        totalLlegit += llegitsNow
                    }

                    val bytesClars = desxifrar(bytesCifrats)
                    FileOutputStream(file).use { fos ->
                        fos.write(bytesClars)
                    }
                    println("✅ [Hilo $threadId] Guardado: ${file.name}")
                } catch (e: Exception) {
                    println("❌ [Hilo $threadId] Error subiendo: ${e.message}")
                }
            }

            "BAIXAR" -> {
                if (file.exists()) {
                    val bytesClars = file.readBytes()
                    val bytesCifrats = xifrar(bytesClars)

                    output.writeBoolean(true)
                    output.writeLong(bytesCifrats.size.toLong())
                    output.flush()
                    output.write(bytesCifrats)
                    output.flush()
                    println("✅ [Hilo $threadId] Enviado: ${file.name}")
                } else {
                    output.writeBoolean(false)
                    output.flush()
                }
            }
        }

    } catch (e: Exception) {
        println("❌ [Hilo $threadId] Error de datos: ${e.message}")
    } finally {
        try { socket.close() } catch (e: Exception) { }
        println("🔌 [Hilo $threadId] Conexión cerrada.")
    }
}