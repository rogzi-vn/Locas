package vn.vistark.locas.core.utils

import java.security.MessageDigest


class Crypto {
    companion object {
        fun md5(s: String): String {
            val MD5 = "MD5"
            try { // Create MD5 Hash
                val digest = MessageDigest
                    .getInstance(MD5)
                digest.update(s.toByteArray())
                val messageDigest = digest.digest()
                // Create Hex String
                val hexString = StringBuilder()
                for (aMessageDigest in messageDigest) {
                    var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
                    while (h.length < 2) h = "0$h"
                    hexString.append(h)
                }
                return hexString.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }
    }
}