# mail

Sending e-mails through SMTP, Mailgun or AWS SES.

## `send( Mail )` — SMTP (6.1.0)

One mail, one recipient, nothing swallowed:

```java
MailConfigurer.init()
        .setSmtpUri( "smtps://resend:re_xxx@smtp.resend.com:465" )
        .build();

String messageId = mailSender.send( Mail.builder()
        .from( "MySkillbox <no-reply@myskillbox.fr>" )
        .to( "client@example.com" )
        .replyTo( "artisan@example.com" )
        .subject( "Votre rapport" )
        .html( html )
        .attach( "rapport.pdf", bytes, "application/pdf" )
        .inline( "logo", logoBytes, "image/png" ) // <img src="cid:logo"> in the HTML
        .build() );
```

An inline image is shown by the clients that block data URIs (Gmail) and remote images by default.

| SMTP URI | Connection |
|---|---|
| `smtps://user:password@host:465` | implicit TLS, server identity checked |
| `smtp://user:password@host:587` | STARTTLS when the server offers it |
| `smtp://host:1025` | no login (a local mail catcher such as Mailpit) |

User and password are URL-decoded. Timeouts: `setSmtpConnectTimeoutMillis` (10 s),
`setSmtpTimeoutMillis` (30 s, read and write).

| Outcome | Meaning |
|---|---|
| returns the `Message-ID` | accepted by the SMTP server |
| returns `null` | `setRedirectTo( "BLOCK" )`: nothing sent |
| `MailConnectionException` | the server could not be reached or refused the login: **nothing was sent**, it can be sent again |
| `MailSendException` | the connection was open: the mail **may have left**, sending it again may duplicate it |
| `IllegalArgumentException` | a missing field or an invalid address |

`setRedirectTo( address )` sends every mail to that address instead.

`MailConfigurer.initOrReplace()` replaces a previous configuration instead of failing, for an application
context started several times in the same JVM (tests).

## Boolean methods

The `send( from, to, subject, message, … )` methods return `false` on failure and pick the provider from the
configuration: SES when `setAwsFrom` is set, Mailgun when `setMailgunDomain` is set, SMTP otherwise
(`setSmtpHost`, `setSmtpPort`, `setSmtpUsername`, `setSmtpPassword`, STARTTLS).
