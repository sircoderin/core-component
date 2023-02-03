package dot.cpp.core.services;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;

@Singleton
public class EmailService {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final MailerClient mailerClient;

  @Inject
  public EmailService(MailerClient mailerClient) {
    this.mailerClient = mailerClient;
  }

  public void sendResetPasswordEmail(String emailAddress, String uuid) {
    Email email =
        new Email()
            .setSubject("dot.cpp Reset Password")
            .setFrom("dot.cpp <alshopcontact@gmail.com>")
            .addTo(emailAddress)
            .setBodyText(
                "Reset your dot.cpp password by going to: http://localhost:9000/reset-password/"
                    + uuid);

    mailerClient.send(email);
  }

  public void sendInviteEmail(String emailAddress, String uuid) {
    Email email =
        new Email()
            .setSubject("dot.cpp Invite")
            .setFrom("dot.cpp <alshopcontact@gmail.com>")
            .addTo(emailAddress)
            .setBodyText(
                "Create your dot.cpp account by going to: http://localhost:9000/accept-invite/"
                    + uuid);

    mailerClient.send(email);
  }
}
