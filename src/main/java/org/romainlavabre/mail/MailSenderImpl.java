package org.romainlavabre.mail;

import org.romainlavabre.exception.HttpInternalServerErrorException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service( "mailSender" )
public class MailSenderImpl implements MailSender {
    protected final MailSender mailSenderMailgun;
    protected final MailSender mailSenderSMTP;


    public MailSenderImpl( MailSender mailSenderMailgun, MailSender mailSenderSMTP ) {
        this.mailSenderMailgun = mailSenderMailgun;
        this.mailSenderSMTP    = mailSenderSMTP;
    }


    @Override
    public boolean send( String from, List< String > to, String subject, String message ) {
        return getInstance().send( from, to, subject, message );
    }


    @Override
    public boolean send( String from, List< String > to, String subject, String message, List< File > files ) {
        return getInstance().send( from, to, subject, message, files );
    }


    @Override
    public boolean send( String from, String to, String subject, String message ) {
        return getInstance().send( from, to, subject, message );
    }


    @Override
    public boolean send( String from, String to, String subject, String message, List< File > files ) {
        return getInstance().send( from, to, subject, message, files );
    }


    protected MailSender getInstance() {
        if ( MailConfigurer.get().getSmtpUsername() != null
                && !MailConfigurer.get().getSmtpUsername().isBlank() ) {
            return mailSenderSMTP;
        }

        if ( MailConfigurer.get().getMailgunDomain() != null
                && !MailConfigurer.get().getMailgunDomain().isBlank() ) {
            return mailSenderMailgun;
        }

        throw new HttpInternalServerErrorException( "MAIL_CONFIGURATION_NOT_FOUND" );
    }
}
