package install.task

import install.configuration.annotation.Value
import install.configuration.annotation.type.Task
import install.configuration.annotation.type.TerminalValueProtocol
import install.util.TaskUtil

import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * Created by sujkim on 2017-03-10.
 */
@Task
@TerminalValueProtocol(['mail.smtp.host', 'mail.smtp.port', 'mail.username', 'mail.password'])
class TestEMail extends TaskUtil{

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
        //START
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
        logger.debug " - subject : ${omit(subject, printingMaximumLength)}"
        logger.debug " - content : ${omit(content, printingMaximumLength)}"
        logger.debug ""

        Properties props = new Properties()
        props.put("mail.smtp.host", host)
        props.put("mail.smtp.port", port)
        if (auth)
            props.put("mail.smtp.auth", auth)
        if (tls)
            props.put("mail.smtp.starttls.enable", tls)

        subject = subject ?: 'No Subject'
        content = content ?: 'No Content'

        //RUN
        Session session = Session.getInstance(props, new javax.mail.Authenticator(){
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(username, password);
            }
        });

        try{
            Message message = new MimeMessage(session)
            message.setFrom(new InternetAddress(from))
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            message.setSubject(subject)
            message.setText(content)

            logger.debug "<EMail>"
            logger.debug "Sending ..."
            Transport.send(message)
            logger.debug "Done"

        } catch (MessagingException e) {
            logger.error "<ERROR>"
            throw new Exception(e)
        }

        return STATUS_TASK_DONE
    }



    String omit(String str, int maximumLength){
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

