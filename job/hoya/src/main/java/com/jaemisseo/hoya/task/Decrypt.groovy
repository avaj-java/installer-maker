package com.jaemisseo.hoya.task


import com.jaemisseo.hoya.util.encryptor.SEED256Util
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.TerminalValueProtocol
import com.jaemisseo.hoya.util.encryptor.AES256FromCryptoJS
import com.jaemisseo.hoya.util.encryptor.AESUtil
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
class Decrypt extends TaskHelper{

    @Value(name='method', caseIgnoreValidList=['aes','aes256','des128','seed128','seed256'])
    String method

    @Value('value')
    String value

    @Value('key')
    String key

//    @Value('salt')
//    String salt

//    @Value('charset')
//    String charset

//    @Value('iterations')
//    Long iterations

    static final String error1 = "It can not decrypt."


    @Override
    Integer run(){
        String decryptedText

        logger.debug "METHOD : ${method}"
        logger.debug "VALUE  : ${value}"
        logger.debug ""

        //Decrypt
        switch (method?.toUpperCase()){
            case Encrypt.AES:
                decryptedText = new AESUtil(key).decrypt(value)
                break
            case Encrypt.AES256:
                decryptedText = new AES256FromCryptoJS(key).decrypt(value)
                break
            case Encrypt.DES128:
                decryptedText = new DES128Util(key).decrypt(value)
                break
            case Encrypt.SEED128:
                decryptedText = new SEED128Util(key).decrypt(value)
                break
            case Encrypt.SEED256:
                decryptedText = new SEED256Util(key).decrypt(value)
                break
            case Encrypt.BASE64:
                decryptedText = new Base64Util().decrypt(value)
                break
            case Encrypt.MD5:
                throw new Exception(error1)
                decryptedText = new MD5Util().decrypt(value)
                break
            case Encrypt.SHA1:
                throw new Exception(error1)
                decryptedText = new SHA1Util().decrypt(value)
                break
            case Encrypt.SHA256:
                throw new Exception(error1)
                decryptedText = new SHA256Util().decrypt(value)
                break
            default:
                break
        }

        //LOG
        logger.debug "<RESULT>"
        logger.debug decryptedText

        //Set 'answer' and 'value' Property
        set('value', decryptedText)
        setPropValue()

        return STATUS_TASK_DONE
    }

}

