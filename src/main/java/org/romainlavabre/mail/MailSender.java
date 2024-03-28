package org.romainlavabre.mail;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * This class is PORT for send mail to client
 *
 * @author Romain Lavabre <romainlavabre98@gmail.com>
 */
public interface MailSender {

    /**
     * @param to      List of Recipients
     * @param subject Subject of mail
     * @param message Message on HTML or TEXT, you can use <code>com.replace.replace.api.mail.TemplateBuilder</code>
     * @return TRUE is mail sent
     */
    boolean send( String from, List< String > to, String subject, String message );


    /**
     * @param to      List of recipient
     * @param subject Subject of mail
     * @param message Message on HTML or TEXT, you can use <code>com.replace.replace.api.mail.TemplateBuilder</code>
     * @param files   List of attachment
     * @return TRUE is mail sent
     */
    boolean send( String from, List< String > to, String subject, String message, List< File > files );


    /**
     * @param to      List of recipient
     * @param subject Subject of mail
     * @param message Message on HTML or TEXT, you can use <code>com.replace.replace.api.mail.TemplateBuilder</code>
     * @param files   List of attachment
     * @return TRUE is mail sent
     */
    boolean send( String from, List< String > to, String subject, String message, List< File > files, Map< String, String > headers );


    /**
     * @param to      Recipient
     * @param subject Subject of mail
     * @param message Message on HTML or TEXT, you can use <code>com.replace.replace.api.mail.TemplateBuilder</code>
     * @return TRUE is mail sent
     */
    boolean send( String from, String to, String subject, String message );


    /**
     * @param to      Recipient
     * @param subject Subject of mail
     * @param message Message on HTML or TEXT, you can use <code>com.replace.replace.api.mail.TemplateBuilder</code>
     * @param headers Headers of mail
     * @return TRUE is mail sent
     */
    boolean send( String from, String to, String subject, String message, Map< String, String > headers );


    /**
     * @param to      Recipient
     * @param subject Subject of mail
     * @param message Message on HTML or TEXT, you can use <code>com.replace.replace.api.mail.TemplateBuilder</code>
     * @param files   List of attachments
     * @return TRUE is mail sent
     */
    boolean send( String from, String to, String subject, String message, List< File > files );


    /**
     * @param to      Recipient
     * @param subject Subject of mail
     * @param message Message on HTML or TEXT, you can use <code>com.replace.replace.api.mail.TemplateBuilder</code>
     * @param files   List of attachments
     * @param headers Headers of mail
     * @return TRUE is mail sent
     */
    boolean send( String from, String to, String subject, String message, List< File > files, Map< String, String > headers );
}
