package com.jaemisseo.hoya.task


import com.jaemisseo.hoya.util.encryptor.SEED256Util
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.TerminalValueProtocol
import com.jaemisseo.hoya.util.encryptor.AESUtil
import com.jaemisseo.hoya.util.encryptor.AES256FromCryptoJS
import com.jaemisseo.hoya.util.encryptor.Base64Util
import com.jaemisseo.hoya.util.encryptor.DES128Util
import com.jaemisseo.hoya.util.encryptor.MD5Util
import com.jaemisseo.hoya.util.encryptor.SEED128Util
import com.jaemisseo.hoya.util.encryptor.SHA1Util
import com.jaemisseo.hoya.util.encryptor.SHA256Util

/**
 * Created by sujkim on 2017-03-10.
 */
@Task
@TerminalValueProtocol(['method', 'value'])
class Encrypt extends TaskHelper{

    @Value(name='method', caseIgnoreValidList=['aes','aes256','des128','seed128','seed256','md5','sha1','sha256'])
    String method

    @Value('value')
    String value

    @Value('key')
    String key

    @Value('salt')
    String salt

    @Value('charset')
    String charset

    @Value('iterations')
    Long iterations

    static final String AES = "AES"
    static final String AES256 = "AES256"
    static final String DES128 = "DES128"
    static final String SEED128 = "SEED128"
    static final String SEED256 = "SEED256"
    static final String BASE64 = "BASE64"

    static final String MD5 = "MD5"
    static final String SHA1 = "SHA1"
    static final String SHA256 = "SHA256"



    @Override
    Integer run(){
        String encryptedText

        logger.debug "METHOD : ${method}"
        logger.debug "VALUE  : ${value}"
        logger.debug ""

        //Encrypt
        switch (method?.toUpperCase()){
            case Encrypt.AES:
                encryptedText = new AESUtil(key).encrypt(value)
                break
            case Encrypt.AES256:
                encryptedText = new AES256FromCryptoJS(key).encrypt(value)
                break
            case Encrypt.DES128:
                encryptedText = new DES128Util(key).encrypt(value)
                break
            case Encrypt.SEED128:
                encryptedText = new SEED128Util(key).encrypt(value)
                break
            case Encrypt.SEED256:
                encryptedText = new SEED256Util(key).encrypt(value)
                break
            case Encrypt.BASE64:
                encryptedText = new Base64Util().encrypt(value)
                break

            case Encrypt.MD5:
                encryptedText = new MD5Util().encrypt(value)
                break
            case Encrypt.SHA1:
                encryptedText = new SHA1Util().encrypt(value)
                break
            case Encrypt.SHA256:
                encryptedText = new SHA256Util().encrypt(value)
                break
            default:
                break
        }

        //LOG
        logger.debug "<RESULT>"
        logger.debug encryptedText

        //Set 'answer' and 'value' Property
        set('value', encryptedText)
        setPropValue()

        return STATUS_TASK_DONE
    }

}

