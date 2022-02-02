package ru.radmir.synClientTest.synClientTest.encryption

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.radmir.synClientTest.synClientTest.database.swaydb.Swaydb
import ru.radmir.synClientTest.synClientTest.init.Vars
import java.security.spec.KeySpec
import java.util.*
import javax.crypto.*
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

@Component
class Cryptographer() {
    @Autowired
    private lateinit var storage: Swaydb

    fun encryptString(input: String, who: String, nameClient: String = Vars.otherEmpty): String {
        val cipher: Cipher = Cipher.getInstance(Vars.cryptoMethodCipher)
        cipher.init(Cipher.ENCRYPT_MODE, getKeyFromPassword(who, nameClient))
        val cipherText: ByteArray = cipher.doFinal(input.toByteArray(Charsets.UTF_8)) // было без аргкмента
        return Base64.getEncoder()
            .encodeToString(cipherText)
    }
    fun decryptString(cipherText: String /*maybe base64*/, who: String, nameClient: String = Vars.otherEmpty): String {
        val cipher = Cipher.getInstance(Vars.cryptoMethodCipher)
        cipher.init(Cipher.DECRYPT_MODE, getKeyFromPassword(who, nameClient))
        val plainText = cipher.doFinal(
            Base64.getDecoder()
                .decode(cipherText)
        )
        return String(plainText)
    }

    fun encryptFile(inputFile: ByteArray, who: String, nameClient: String = Vars.otherEmpty): String {
        val cipher: Cipher = Cipher.getInstance(Vars.cryptoMethodCipher)
        cipher.init(Cipher.ENCRYPT_MODE, getKeyFromPassword(who, nameClient))
        val cipherText: ByteArray = cipher.doFinal(inputFile)
        return Base64.getEncoder()
            .encodeToString(cipherText)
    }
    fun decryptFile(cipherFile: String /*maybe base64*/, who: String, nameClient: String = Vars.otherEmpty): String  /*after convert this base64 to byteArray*/ {
        val cipher = Cipher.getInstance(Vars.cryptoMethodCipher)
        cipher.init(Cipher.DECRYPT_MODE, getKeyFromPassword(who, nameClient))
        return Base64.getEncoder().encodeToString(
            cipher.doFinal(Base64.getDecoder().decode(cipherFile))
        )
    }

    private fun getKeyFromPassword(who: String, nameClient: String = Vars.otherEmpty): SecretKey {
        lateinit var password: String
        if (who == Vars.cryptoWhoServer) {
            password = storage.get(Vars.configPasswordServer)!!
        } else {
            val friends = storage.get(Vars.configFriends)!!.split(Vars.otherDelimiterBetweenNodes)
            for (f in friends) {
                if (nameClient in f.split(Vars.otherDelimiterBetweenComponentsOfNode)[0]) {
                    password = f.split(Vars.otherDelimiterBetweenComponentsOfNode)[1]
                    break
                }
            }
        }
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), "salt".toByteArray(), 128, 128)
        return SecretKeySpec(factory.generateSecret(spec).encoded, Vars.cryptoMethodCipher)
    }
}