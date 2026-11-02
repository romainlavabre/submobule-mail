package org.romainlavabre.mail;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.Multipart;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.romainlavabre.mail.exception.MailConnectionException;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SmtpMailTest {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension( new ServerSetup( 3925, "127.0.0.1", "smtp" ) );

    private final MailSender sender = new SMTP();


    @BeforeEach
    public void configure() {
        MailConfigurer.initOrReplace().setSmtpUri( "smtp://127.0.0.1:3925" ).build();
    }


    @Test
    public void a_mail_leaves_with_its_reply_to_its_headers_and_its_attachment() throws Exception {
        String messageId = sender.send( Mail.builder()
                .from( "MySkillbox <no-reply@myskillbox.fr>" )
                .to( "client@example.com" )
                .replyTo( "artisan@example.com" )
                .subject( "Votre rapport d'intervention" )
                .html( "<p>Bonjour é</p>" )
                .header( "X-Original-To", "real@example.com" )
                .attach( "rapport.pdf", "%PDF".getBytes( StandardCharsets.UTF_8 ), "application/pdf" )
                .build() );

        MimeMessage received = greenMail.getReceivedMessages()[ 0 ];

        assertThat( messageId ).isNotBlank().isEqualTo( received.getMessageID() );
        assertThat( ( ( InternetAddress ) received.getFrom()[ 0 ] ).getPersonal() ).isEqualTo( "MySkillbox" );
        assertThat( received.getReplyTo()[ 0 ].toString() ).isEqualTo( "artisan@example.com" );
        assertThat( received.getAllRecipients()[ 0 ].toString() ).isEqualTo( "client@example.com" );
        assertThat( received.getSubject() ).isEqualTo( "Votre rapport d'intervention" );
        assertThat( received.getHeader( "X-Original-To" ) ).containsExactly( "real@example.com" );
        assertThat( ( ( Multipart ) received.getContent() ).getCount() ).isEqualTo( 2 );
        assertThat( ( ( Multipart ) received.getContent() ).getBodyPart( 1 ).getFileName() ).isEqualTo( "rapport.pdf" );
        assertThat( GreenMailUtil.getBody( received ) ).contains( "rapport.pdf" );
    }


    @Test
    public void an_inline_image_is_related_to_the_html_by_its_content_id() throws Exception {
        sender.send( Mail.builder()
                .from( "no-reply@myskillbox.fr" )
                .to( "client@example.com" )
                .subject( "s" )
                .html( "<img src=\"cid:logo\">" )
                .inline( "logo", new byte[]{ 1, 2, 3 }, "image/png" )
                .build() );

        MimeMessage received = greenMail.getReceivedMessages()[ 0 ];
        Multipart   related  = ( Multipart ) ( ( Multipart ) received.getContent() ).getBodyPart( 0 ).getContent();

        assertThat( related.getContentType() ).startsWith( "multipart/related" );
        assertThat( related.getBodyPart( 1 ).getHeader( "Content-ID" ) ).containsExactly( "<logo>" );
    }


    @Test
    public void a_refused_connection_tells_that_nothing_left() {
        MailConfigurer.initOrReplace().setSmtpUri( "smtp://127.0.0.1:3926" ).setSmtpConnectTimeoutMillis( 1000 ).build();

        assertThatThrownBy( () -> sender.send( Mail.builder()
                .from( "no-reply@myskillbox.fr" )
                .to( "client@example.com" )
                .subject( "s" )
                .html( "h" )
                .build() ) )
                .isInstanceOf( MailConnectionException.class );
    }


    @Test
    public void the_uri_carries_the_tls_mode_and_the_decoded_credentials() {
        SMTP.SmtpUri smtps = SMTP.SmtpUri.parse( "smtps://resend:re_a%2Fb@smtp.resend.com:465" );
        SMTP.SmtpUri local = SMTP.SmtpUri.parse( "smtp://localhost:1025" );

        assertThat( smtps.implicitTls() ).isTrue();
        assertThat( smtps.username() ).isEqualTo( "resend" );
        assertThat( smtps.password() ).isEqualTo( "re_a/b" );
        assertThat( smtps.port() ).isEqualTo( 465 );
        assertThat( local.implicitTls() ).isFalse();
        assertThat( local.username() ).isNull();
        assertThat( local.port() ).isEqualTo( 1025 );
    }


    @Test
    public void a_mail_without_recipient_is_refused() {
        assertThatThrownBy( () -> Mail.builder().from( "a@b.c" ).subject( "s" ).html( "h" ).build() )
                .isInstanceOf( IllegalArgumentException.class )
                .hasMessage( "[mail] to is required" );
    }
}
