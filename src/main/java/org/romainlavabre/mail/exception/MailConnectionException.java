package org.romainlavabre.mail.exception;

/**
 * The SMTP server could not be reached or refused the login: nothing was sent, the mail can be sent again.
 */
public class MailConnectionException extends RuntimeException {

    public MailConnectionException( String message, Throwable cause ) {
        super( message, cause );
    }
}
