package org.romainlavabre.mail;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * @author Romain Lavabre <romainlavabre98@gmail.com>
 */
@Service( "mailSenderSMTP" )
public class SMTP implements MailSender {


    @Override
    public boolean send( String from, List< String > to, String subject, String message ) {
        boolean res = false;

        for ( String t : to ) {
            res = send( from, t, subject, message );
        }

        return res;
    }


    @Override
    public boolean send( String from, final List< String > to, final String subject, final String message, final List< File > files ) {
        return false;
    }


    @Override
    public boolean send( String from, final String to, final String subject, final String message ) {
        Properties prop = new Properties();
        prop.put( "mail.smtp.host", MailConfigurer.get().getSmtpHost() );
        prop.put( "mail.smtp.port", MailConfigurer.get().getSmtpPort() );
        prop.put( "mail.smtp.auth", "true" );
        prop.put( "mail.smtp.starttls.enable", "true" );

        Session session = Session.getInstance( prop,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication( MailConfigurer.get().getSmtpUsername(), MailConfigurer.get().getSmtpPassword() );
                    }
                } );

        try {

            Message mes = new MimeMessage( session );
            mes.setFrom( new InternetAddress( MailConfigurer.get().getSmtpUsername() ) );
            mes.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse( to )
            );
            mes.setSubject( subject );
            mes.setContent( message, "text/html" );

            Transport.send( mes );

            return true;
        } catch ( MessagingException e ) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public boolean send( String from, final String to, final String subject, final String message, final List< File > files ) {
        return false;
    }

}
