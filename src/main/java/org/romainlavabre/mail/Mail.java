package org.romainlavabre.mail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One e-mail to one recipient, sent with {@link MailSender#send(Mail)}.
 * <pre>
 * Mail.builder()
 *         .from( "MySkillbox &lt;no-reply@myskillbox.fr&gt;" )
 *         .to( "client@example.com" )
 *         .replyTo( "artisan@example.com" )
 *         .subject( "Your report" )
 *         .html( html )
 *         .attach( "report.pdf", bytes, "application/pdf" )
 *         .build();
 * </pre>
 */
public final class Mail {

    private final String                from;
    private final String                to;
    private final String                replyTo;
    private final String                subject;
    private final String                html;
    private final Map< String, String > headers;
    private final List< Attachment >    attachments;
    private final List< Inline >        inlines;


    private Mail( Builder builder ) {
        this.inlines     = List.copyOf( builder.inlines );
        this.from        = builder.from;
        this.to          = builder.to;
        this.replyTo     = builder.replyTo;
        this.subject     = builder.subject;
        this.html        = builder.html;
        this.headers     = Collections.unmodifiableMap( new LinkedHashMap<>( builder.headers ) );
        this.attachments = List.copyOf( builder.attachments );
    }


    public static Builder builder() {
        return new Builder();
    }


    public String getFrom() {
        return from;
    }


    public String getTo() {
        return to;
    }


    /**
     * @return null when the replies go to the sender
     */
    public String getReplyTo() {
        return replyTo;
    }


    public String getSubject() {
        return subject;
    }


    public String getHtml() {
        return html;
    }


    public Map< String, String > getHeaders() {
        return headers;
    }


    public List< Attachment > getAttachments() {
        return attachments;
    }


    public List< Inline > getInlines() {
        return inlines;
    }


    /**
     * @return the same mail sent to another recipient
     */
    public Mail redirectTo( String recipient ) {
        Builder builder = new Builder();
        builder.from    = from;
        builder.to      = recipient;
        builder.replyTo = replyTo;
        builder.subject = subject;
        builder.html    = html;
        builder.headers.putAll( headers );
        builder.attachments.addAll( attachments );
        builder.inlines.addAll( inlines );

        return builder.build();
    }


    public record Attachment( String name, byte[] content, String contentType ) {
    }


    /**
     * An image shown in the HTML with <code>&lt;img src="cid:{contentId}"&gt;</code>: the mail clients that block
     * data URIs (Gmail) and remote images by default still show it.
     */
    public record Inline( String contentId, byte[] content, String contentType ) {
    }


    public static final class Builder {

        private final Map< String, String > headers     = new LinkedHashMap<>();
        private final List< Attachment >    attachments = new ArrayList<>();
        private final List< Inline >        inlines     = new ArrayList<>();
        private       String                from;
        private       String                to;
        private       String                replyTo;
        private       String                subject;
        private       String                html;


        private Builder() {
        }


        /**
         * @param from an address, or "Name &lt;address&gt;"
         */
        public Builder from( String from ) {
            this.from = from;

            return this;
        }


        public Builder to( String to ) {
            this.to = to;

            return this;
        }


        public Builder replyTo( String replyTo ) {
            this.replyTo = replyTo;

            return this;
        }


        public Builder subject( String subject ) {
            this.subject = subject;

            return this;
        }


        public Builder html( String html ) {
            this.html = html;

            return this;
        }


        public Builder header( String name, String value ) {
            headers.put( name, value );

            return this;
        }


        public Builder attach( String name, byte[] content, String contentType ) {
            attachments.add( new Attachment( name, content, contentType ) );

            return this;
        }


        /**
         * @param contentId referenced as <code>cid:{contentId}</code> in the HTML
         */
        public Builder inline( String contentId, byte[] content, String contentType ) {
            inlines.add( new Inline( contentId, content, contentType ) );

            return this;
        }


        /**
         * @throws IllegalArgumentException without from, to, subject or html
         */
        public Mail build() {
            require( from, "from" );
            require( to, "to" );
            require( subject, "subject" );
            require( html, "html" );

            return new Mail( this );
        }


        private static void require( String value, String name ) {
            if ( value == null || value.isBlank() ) {
                throw new IllegalArgumentException( "[mail] " + name + " is required" );
            }
        }
    }
}
