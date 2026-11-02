package org.romainlavabre.mail;

import jakarta.activation.DataHandler;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;
import jakarta.mail.util.ByteArrayDataSource;
import org.romainlavabre.mail.exception.MailConnectionException;
import org.romainlavabre.mail.exception.MailSendException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public boolean send( String from, List< String > to, String subject, String message, List< File > files, Map< String, String > headers ) {
        return false;
    }


    @Override
    public boolean send( String from, final String to, final String subject, final String message ) {
        return send( from, to, subject, message, new HashMap<>() );
    }


    @Override
    public boolean send( String from, String to, String subject, String message, Map< String, String > headers ) {
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

            for ( Map.Entry< String, String > entry : headers.entrySet() ) {
                mes.addHeader( entry.getKey(), entry.getValue() );
            }

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


    @Override
    public String send( Mail mail ) {
        SmtpUri     uri     = SmtpUri.parse( MailConfigurer.get().getSmtpUri() );
        Session     session = Session.getInstance( properties( uri ) );
        MimeMessage message = message( session, mail );

        Transport transport;

        try {
            transport = session.getTransport( uri.protocol() );
            transport.connect( uri.host(), uri.port(), uri.username(), uri.password() );
        } catch ( MessagingException e ) {
            throw new MailConnectionException( "[mail] cannot connect to " + uri.host() + ":" + uri.port(), e );
        }

        try {
            transport.sendMessage( message, message.getAllRecipients() );

            return message.getMessageID();
        } catch ( MessagingException e ) {
            throw new MailSendException( "[mail] sending to " + uri.host() + ":" + uri.port() + " failed", e );
        } finally {
            close( transport );
        }
    }


    private static Properties properties( SmtpUri uri ) {
        String     prefix     = "mail." + uri.protocol() + ".";
        Properties properties = new Properties();
        properties.put( prefix + "host", uri.host() );
        properties.put( prefix + "port", String.valueOf( uri.port() ) );
        properties.put( prefix + "auth", String.valueOf( uri.username() != null ) );
        properties.put( prefix + "connectiontimeout", String.valueOf( MailConfigurer.get().getSmtpConnectTimeoutMillis() ) );
        properties.put( prefix + "timeout", String.valueOf( MailConfigurer.get().getSmtpTimeoutMillis() ) );
        properties.put( prefix + "writetimeout", String.valueOf( MailConfigurer.get().getSmtpTimeoutMillis() ) );

        if ( uri.implicitTls() ) {
            properties.put( prefix + "ssl.enable", "true" );
            properties.put( prefix + "ssl.checkserveridentity", "true" );
        } else {
            properties.put( prefix + "starttls.enable", "true" );
        }

        return properties;
    }


    private static MimeMessage message( Session session, Mail mail ) {
        try {
            MimeMessage message = new MimeMessage( session );
            message.setFrom( new InternetAddress( mail.getFrom(), true ) );
            message.setRecipients( Message.RecipientType.TO, InternetAddress.parse( mail.getTo(), true ) );

            if ( mail.getReplyTo() != null && !mail.getReplyTo().isBlank() ) {
                message.setReplyTo( InternetAddress.parse( mail.getReplyTo(), true ) );
            }

            message.setSubject( mail.getSubject(), StandardCharsets.UTF_8.name() );

            for ( Map.Entry< String, String > header : mail.getHeaders().entrySet() ) {
                message.addHeader( header.getKey(), header.getValue() );
            }

            MimeBodyPart html = new MimeBodyPart();
            html.setText( mail.getHtml(), StandardCharsets.UTF_8.name(), "html" );

            MimeBodyPart body = html;

            if ( !mail.getInlines().isEmpty() ) {
                MimeMultipart related = new MimeMultipart( "related" );
                related.addBodyPart( html );

                for ( Mail.Inline inline : mail.getInlines() ) {
                    MimeBodyPart part = new MimeBodyPart();
                    part.setDataHandler( new DataHandler( new ByteArrayDataSource( inline.content(), inline.contentType() ) ) );
                    part.setContentID( "<" + inline.contentId() + ">" );
                    part.setDisposition( Part.INLINE );
                    related.addBodyPart( part );
                }

                body = new MimeBodyPart();
                body.setContent( related );
            }

            if ( mail.getAttachments().isEmpty() ) {
                message.setContent( new MimeMultipart( body ) );
            } else {
                MimeMultipart mixed = new MimeMultipart( "mixed" );
                mixed.addBodyPart( body );

                for ( Mail.Attachment attachment : mail.getAttachments() ) {
                    MimeBodyPart part = new MimeBodyPart();
                    part.setDataHandler( new DataHandler( new ByteArrayDataSource( attachment.content(), attachment.contentType() ) ) );
                    part.setFileName( MimeUtility.encodeText( attachment.name(), StandardCharsets.UTF_8.name(), null ) );
                    part.setDisposition( Part.ATTACHMENT );
                    mixed.addBodyPart( part );
                }

                message.setContent( mixed );
            }

            message.saveChanges();

            return message;
        } catch ( MessagingException | UnsupportedEncodingException e ) {
            throw new IllegalArgumentException( "[mail] invalid mail: " + e.getMessage(), e );
        }
    }


    private static void close( Transport transport ) {
        try {
            transport.close();
        } catch ( MessagingException ignored ) {
            // The mail is already accepted, or already failed: the close changes nothing.
        }
    }


    record SmtpUri( String protocol, String host, int port, String username, String password ) {

        boolean implicitTls() {
            return protocol.equals( "smtps" );
        }


        static SmtpUri parse( String value ) {
            if ( value == null || value.isBlank() ) {
                throw new IllegalStateException( "[mail] no SMTP URI configured: MailConfigurer.setSmtpUri" );
            }

            URI uri = URI.create( value );

            if ( !"smtp".equals( uri.getScheme() ) && !"smtps".equals( uri.getScheme() ) ) {
                throw new IllegalStateException( "[mail] the SMTP URI must start with smtp:// or smtps://" );
            }

            int port = uri.getPort() > 0 ? uri.getPort() : ( uri.getScheme().equals( "smtps" ) ? 465 : 587 );

            if ( uri.getRawUserInfo() == null ) {
                return new SmtpUri( uri.getScheme(), uri.getHost(), port, null, null );
            }

            String[] userInfo = uri.getRawUserInfo().split( ":", 2 );

            return new SmtpUri(
                    uri.getScheme(),
                    uri.getHost(),
                    port,
                    URLDecoder.decode( userInfo[ 0 ], StandardCharsets.UTF_8 ),
                    userInfo.length > 1 ? URLDecoder.decode( userInfo[ 1 ], StandardCharsets.UTF_8 ) : null );
        }
    }


    @Override
    public boolean send( String from, String to, String subject, String message, List< File > files, Map< String, String > headers ) {
        return false;
    }
}
