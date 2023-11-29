package org.romainlavabre.mail.exception;

public class AlreadyInitializedException extends RuntimeException {
    public AlreadyInitializedException() {
        super( "Mail already initialized, call MailConfigurer.init() only once" );
    }
}
