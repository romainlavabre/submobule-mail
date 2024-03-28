package org.romainlavabre.mail;

import org.romainlavabre.exception.HttpInternalServerErrorException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return send( from, to, subject, message, new ArrayList<>(), new HashMap<>() );
    }


    @Override
    public boolean send( String from, List< String > to, String subject, String message, List< File > files ) {
        return send( from, to, subject, message, files, new HashMap<>() );
    }


    @Override
    public boolean send( String from, List< String > to, String subject, String message, List< File > files, Map< String, String > headers ) {
        to = overwriteTo( to );
        return getInstance().send( from, to, subject, message, files, headers );
    }


    @Override
    public boolean send( String from, String to, String subject, String message ) {
        return send( from, to, subject, message, new HashMap<>() );
    }


    @Override
    public boolean send( String from, String to, String subject, String message, Map< String, String > headers ) {
        to = overwriteTo( to );
        return getInstance().send( from, to, subject, message, headers );
    }


    @Override
    public boolean send( String from, String to, String subject, String message, List< File > files ) {
        return send( from, to, subject, message, files, new HashMap<>() );
    }


    @Override
    public boolean send( String from, String to, String subject, String message, List< File > files, Map< String, String > headers ) {
        to = overwriteTo( to );
        return getInstance().send( from, to, subject, message, files, headers );
    }


    protected String overwriteTo( String to ) {
        if ( MailConfigurer.get().getRedirectTo() != null
                && !MailConfigurer.get().getRedirectTo().equals( "NONE" ) ) {
            return MailConfigurer.get().getRedirectTo();
        }

        return to;
    }


    protected List< String > overwriteTo( List< String > to ) {
        if ( MailConfigurer.get().getRedirectTo() != null
                && !MailConfigurer.get().getRedirectTo().equals( "NONE" ) ) {
            return List.of( MailConfigurer.get().getRedirectTo() );
        }

        return to;
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
