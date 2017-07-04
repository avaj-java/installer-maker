package install.task

import install.configuration.annotation.Value
import install.configuration.annotation.type.Task
import install.util.TaskUtil

import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * Created by sujkim on 2017-03-10.
 */
@Task
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

    @Value(property='mail.smtp.auth', method='getBoolean')
    Boolean auth

    @Value(property='mail.smtp.starttls.enable', method='getBoolean')
    Boolean tls

    int printingMaximumLength = 20



    @Override
    Integer run(){
        //START
        logMiddleTitle 'START TESTEMAIL'
        println "<REQUEST> - CHECK"
        println " - host        : ${host}"
        println " - port        : ${port}"
        println " - username    : ${username}"
        println " - password    : ${password}"
        if (auth)
            println " - auth        : ${auth}"
        if (tls)
            println " - tls         : ${tls}"
        println "-------------------------"
        println " - from    : ${from}"
        println " - to      : ${to}"
        println " - subject : ${omit(subject, printingMaximumLength)}"
        println " - content : ${omit(content, printingMaximumLength)}"
        println ""

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

            println "<EMail>"
            println "Sending ..."
            Transport.send(message)
            println "Done"

        } catch (MessagingException e) {
            throw new Exception(e)
        }

        //FINISH
        logMiddleTitle 'FINISHED TESTEMAIL'
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

