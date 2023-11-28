package org.romainlavabre.mail.exception;

public class NotInitializedException extends RuntimeException {
    public NotInitializedException() {
        super( "Mail not initialized, use MailConfigurer for fix it" );
    }
}
