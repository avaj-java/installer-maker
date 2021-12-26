package com.jaemisseo.hoya.util.encryptor

interface EncryptionUtil {

    String encrypt(String content) throws Exception

    String decrypt(String encryptedContent) throws Exception

}