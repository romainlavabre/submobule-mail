package org.romainlavabre.mail;

import org.romainlavabre.exception.HttpInternalServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service( "mailSender" )
public class MailSenderImpl implements MailSender {
    private static final Logger LOGGER = LoggerFactory.getLogger( "MailSender" );

    protected final MailSender mailSenderMailgun;
    protected final MailSender mailSenderSMTP;
    protected final MailSender mailSenderSes;


    public MailSenderImpl(
            MailSender mailSenderMailgun,
            MailSender mailSenderSMTP,
            MailSender mailSenderSes ) {
        this.mailSenderMailgun = mailSenderMailgun;
        this.mailSenderSMTP    = mailSenderSMTP;
        this.mailSenderSes     = mailSenderSes;
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

        if ( to.contains( "BLOCK" ) ) {
            LOGGER.warn( "Actually configured to block email" );
            return true;
        }

        return getInstance().send( from, to, subject, message, files, headers );
    }


    @Override
    public boolean send( String from, String to, String subject, String message ) {
        return send( from, to, subject, message, new HashMap<>() );
    }


    @Override
    public boolean send( String from, String to, String subject, String message, Map< String, String > headers ) {
        to = overwriteTo( to );

        if ( to.equalsIgnoreCase( "BLOCK" ) ) {
            LOGGER.warn( "Actually configured to block email" );
            return true;
        }

        return getInstance().send( from, to, subject, message, headers );
    }


    @Override
    public boolean send( String from, String to, String subject, String message, List< File > files ) {
        return send( from, to, subject, message, files, new HashMap<>() );
    }


    @Override
    public boolean send( String from, String to, String subject, String message, List< File > files, Map< String, String > headers ) {
        to = overwriteTo( to );

        if ( to.equalsIgnoreCase( "BLOCK" ) ) {
            LOGGER.warn( "Actually configured to block email" );
            return true;
        }

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
        if ( MailConfigurer.get().getAwsFrom() != null ) {
            return mailSenderSes;
        }

        if ( MailConfigurer.get().getMailgunDomain() != null
                && !MailConfigurer.get().getMailgunDomain().isBlank() ) {
            return mailSenderMailgun;
        }

        if ( MailConfigurer.get().getSmtpUsername() != null
                && !MailConfigurer.get().getSmtpUsername().isBlank() ) {
            return mailSenderSMTP;
        }

        throw new HttpInternalServerErrorException( "MAIL_CONFIGURATION_NOT_FOUND" );
    }
}
