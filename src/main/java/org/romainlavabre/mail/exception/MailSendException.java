package org.romainlavabre.mail.exception;

/**
 * The connection was open when the sending failed: the mail may have left, sending it again may duplicate it.
 */
public class MailSendException extends RuntimeException {

    public MailSendException( String message, Throwable cause ) {
        super( message, cause );
    }
}
