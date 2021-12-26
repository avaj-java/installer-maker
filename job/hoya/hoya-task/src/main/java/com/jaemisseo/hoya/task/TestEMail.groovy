package com.jaemisseo.hoya.task


import jaemisseo.man.configuration.annotation.Value
import jaemisseo.man.configuration.annotation.type.Task
import jaemisseo.man.configuration.annotation.type.TerminalValueProtocol

import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * Created by sujkim on 2017-03-10.
 */
@Task
@TerminalValueProtocol(['mail.smtp.host', 'mail.smtp.port', 'mail.username', 'mail.password'])
class TestEMail extends TaskHelper{

    @Value('mail.smtp.host')
    String host

    @Value('mail.smtp.port')
    String port

    @Value('mail.username')
    String username

    @Value('mail.password')
    String password

    @Value('mail.from')
    String from

    @Value('mail.to')
    String to

    @Value('mail.subject')
    String subject

    @Value('mail.content')
    String content

    @Value('mail.smtp.auth')
    Boolean auth

    @Value('mail.smtp.starttls.enable')
    Boolean tls

    int printingMaximumLength = 20



    @Override
    Integer run(){
        try{
            //Log Parameter
            log()
            //Setup Properties
            Properties props = makeEmailProperties(host, port, auth, tls)
            //Send Email
            sendMail(props, username, password, from, to, subject, content)

        }catch(MessagingException e){
            logger.error "<ERROR>"
            throw new Exception(e)
        }
        return STATUS_TASK_DONE
    }



    private void log(){
        logger.debug "<REQUEST> - CHECK"
        logger.debug " - host        : ${host}"
        logger.debug " - port        : ${port}"
        logger.debug " - username    : ${username}"
        logger.debug " - password    : ${password}"
        if (auth)
            logger.debug " - auth        : ${auth}"
        if (tls)
            logger.debug " - tls         : ${tls}"
        logger.debug "-------------------------"
        logger.debug " - from    : ${from}"
        logger.debug " - to      : ${to}"
        logger.debug "-------------------------"
        logger.debug " - subject : ${omit(subject, printingMaximumLength)}"
        logger.debug "-------------------------"
        logger.debug " - content : ${omit(content, printingMaximumLength)}"
        logger.debug "-------------------------"
        logger.debug ""
    }

    private Properties makeEmailProperties(String host, String port, Boolean auth, Boolean tls){
        Properties props = new Properties()
        if (host)
            props.put("mail.smtp.host", host)
        if (port)
            props.put("mail.smtp.port", port)
        if (auth)
            props.put("mail.smtp.auth", auth)
        if (tls)
            props.put("mail.smtp.starttls.enable", tls)
        return props
    }

    private boolean sendMail(Properties properties, String username, String password, String from, String to, String subject, String content){
        //Make Session
        Session session = Session.getInstance(properties, new javax.mail.Authenticator(){
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(username, password);
            }
        });
        //Make Message
        Message message = new MimeMessage(session)
        message.setFrom(new InternetAddress(from))
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
        message.setSubject( subject ?: 'No Subject' )
        message.setText( content ?: 'No Content' )
        //Send Email
        logger.debug "(Email) Sending ..."
        Transport.send(message)
        logger.debug "Done"
        return true
    }



    private String omit(String str, int maximumLength){
        //Validate
        if (maximumLength <= 2)
            throw new Exception('MaximumLength must be more than 2.')
        else if (!str)
            throw new NullPointerException()

        //Omit
        int strLength = str.length()
        if (str && strLength > maximumLength)
            str = "${str.substring(0, maximumLength-3)}..."
        return str
    }

}

