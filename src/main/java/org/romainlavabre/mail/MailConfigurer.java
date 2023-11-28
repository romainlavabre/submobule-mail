package org.romainlavabre.mail;

import org.romainlavabre.mail.exception.NotInitializedException;

public class MailConfigurer {
    private static MailConfigurer INSTANCE;
    private        String         mailgunDomain;
    private        String         mailgunPrivateKey;
    private        String         smtpHost;
    private        String         smtpPort;
    private        String         smtpUsername;
    private        String         smtpPassword;


    public MailConfigurer() {
        INSTANCE = this;
    }


    protected static MailConfigurer get() {
        if ( INSTANCE == null ) {
            throw new NotInitializedException();
        }

        return INSTANCE;
    }


    public static MailConfigurer init() {
        return new MailConfigurer();
    }


    protected String getMailgunDomain() {
        return mailgunDomain;
    }


    /**
     * @param mailgunDomain Provided by <a href="https://app.eu.mailgun.com/mg/sending/domains">Mailgun</a>
     */
    public MailConfigurer setMailgunDomain( String mailgunDomain ) {
        this.mailgunDomain = mailgunDomain;

        return this;
    }


    protected String getMailgunPrivateKey() {
        return mailgunPrivateKey;
    }


    /**
     * @param mailgunPrivateKey Provided by <a href="https://app.eu.mailgun.com/settings/api_security">Mailgun</a>
     */
    public MailConfigurer setMailgunPrivateKey( String mailgunPrivateKey ) {
        this.mailgunPrivateKey = mailgunPrivateKey;

        return this;
    }


    protected String getSmtpHost() {
        return smtpHost;
    }


    /**
     * @param smtpHost Provided by your mail provider
     */
    public MailConfigurer setSmtpHost( String smtpHost ) {
        this.smtpHost = smtpHost;

        return this;
    }


    protected String getSmtpPort() {
        return smtpPort;
    }


    /**
     * @param smtpPort Provided by your mail provider
     */
    public MailConfigurer setSmtpPort( String smtpPort ) {
        this.smtpPort = smtpPort;

        return this;
    }


    protected String getSmtpUsername() {
        return smtpUsername;
    }


    /**
     * @param smtpUsername Provided by your mail provider
     */
    public MailConfigurer setSmtpUsername( String smtpUsername ) {
        this.smtpUsername = smtpUsername;

        return this;
    }


    protected String getSmtpPassword() {
        return smtpPassword;
    }


    /**
     * @param smtpPassword Provided by your mail provider
     */
    public MailConfigurer setSmtpPassword( String smtpPassword ) {
        this.smtpPassword = smtpPassword;

        return this;
    }


    public void build() {
    }
}
