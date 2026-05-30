package org.romainlavabre.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Romain Lavabre <romain.lavabre@proton.me>
 */
@Service( "mailSenderSes" )
public class Ses implements MailSender {
    private static final Logger LOGGER = LoggerFactory.getLogger( Ses.class );

    protected SesClient sesClient;


    @Override
    public boolean send( String from, List< String > to, String subject, String message ) {
        for ( String recipient : to ) {
            send( from, recipient, subject, message );
        }

        return true;
    }


    @Override
    public boolean send( String from, List< String > to, String subject, String message, List< File > files ) {
        for ( String recipient : to ) {
            send( from, recipient, subject, message, files );
        }

        return true;
    }


    @Override
    public boolean send( String from, List< String > to, String subject, String message, List< File > files, Map< String, String > headers ) {
        for ( String recipient : to ) {
            send( from, recipient, subject, message, files );
        }

        return true;
    }


    @Override
    public boolean send( String from, String to, String subject, String message ) {
        SendEmailRequest request = SendEmailRequest.builder()
                .destination( Destination.builder()
                        .toAddresses( to )
                        .build() )
                .message( Message.builder()
                        .subject( Content.builder()
                                .data( subject )
                                .charset( "UTF-8" )
                                .build() )
                        .body( Body.builder()
                                .html( Content.builder()
                                        .data( message )
                                        .charset( "UTF-8" )
                                        .build() )
                                .build() )
                        .build() )
                .source( MailConfigurer.get().getAwsFrom() )
                .build();

        SendEmailResponse sendEmailResponse = getSesClient().sendEmail( request );

        String messageId = sendEmailResponse.messageId();

        if ( messageId == null || messageId.isEmpty() ) {
            LOGGER.error( "Failed to send email to {}. No message ID returned.", to );
            return false;
        }

        LOGGER.info( "Email sent to {} with message ID: {}", to, sendEmailResponse.messageId() );

        return true;
    }


    @Override
    public boolean send( String from, String to, String subject, String message, Map< String, String > headers ) {
        return send( from, to, subject, message );
    }


    @Override
    public boolean send( String from, String to, String subject, String message, List< File > files ) {
        // Configuration de la session mail
        Session session = Session.getDefaultInstance( new Properties() );

        try {
            // Création du message
            MimeMessage mimeMessage = new MimeMessage( session );
            mimeMessage.setFrom( new InternetAddress( MailConfigurer.get().getAwsFrom() ) );
            mimeMessage.setRecipients( jakarta.mail.Message.RecipientType.TO, InternetAddress.parse( to ) );
            mimeMessage.setSubject( subject );

            // Partie texte
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent( message, "text/html; charset=UTF-8" );

            Multipart multipart = new MimeMultipart();
            for ( File file : files ) {
                // Partie pièce jointe
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile( file );
                multipart.addBodyPart( attachmentPart );
            }

            multipart.addBodyPart( htmlPart );

            mimeMessage.setContent( multipart );

            // Conversion en RawMessage
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            mimeMessage.writeTo( outputStream );

            ByteBuffer rawMessage = ByteBuffer.wrap( outputStream.toByteArray() );

            SendRawEmailRequest rawEmailRequest = SendRawEmailRequest.builder()
                    .rawMessage( r -> r.data( SdkBytes.fromByteBuffer( rawMessage ) ) )
                    .build();

            SendRawEmailResponse sendEmailResponse = getSesClient().sendRawEmail( rawEmailRequest );

            String messageId = sendEmailResponse.messageId();

            if ( messageId == null || messageId.isEmpty() ) {
                LOGGER.error( "Failed to send email to {}. No message ID returned.", to );
                return false;
            }

            LOGGER.info( "Email sent to {} with message ID: {}", to, sendEmailResponse.messageId() );
        } catch ( IOException | MessagingException e ) {
            e.printStackTrace();
            return false;
        }


        return true;
    }


    @Override
    public boolean send( String from, String to, String subject, String message, List< File > files, Map< String, String > headers ) {
        return send( from, to, subject, message, files );
    }


    protected SesClient getSesClient() {
        if ( sesClient != null ) {
            return sesClient;
        }

        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                MailConfigurer.get().getAwsAccessKeyId(),
                MailConfigurer.get().getAwsSecretAccessKey()
        );

        SesClient sesClient = SesClient.builder()
                .region( Region.EU_WEST_3 )
                .credentialsProvider( StaticCredentialsProvider.create( credentials ) )
                .build();

        this.sesClient = sesClient;

        return sesClient;
    }
}
