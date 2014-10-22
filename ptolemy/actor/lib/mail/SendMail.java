/** Send email.

 @Copyright (c) 2011-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.actor.lib.mail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.JFrame;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.style.TextStyle;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.util.MessageHandler;

/** Upon firing, send email to the specified recipient.
 *  This actor uses the SMTP protocol and will prompt for a password
 *  upon being first invoked. The password is sent encrypted.
 *  <p>
 *  By default, this actor will not actually send email, but
 *  will rather produce the formatted email on its output port.
 *  The reason for this is that sending email should be done
 *  with some hesitation, since it is easy with such an actor
 *  to create a flood of email that will have undesirable effects.
 *  To get the actor to actually send email, change the value
 *  of the <i>reallySendMail</i> parameter to true. Upon completion
 *  of an execution, <i>reallySendMail</i> will be set back to false
 *  (in the wrapup() method) to help prevent accidental duplicate
 *  mailings.</p>
 *  <p>
 *  This actor requires the JavaMail 1.5 javax.mail.jar file be in the
 *  classpath.</p>
 *  <p>To use this actor, download <code>javax.mail.jar</code>
 *  <a href="https://java.net/projects/javamail/pages/Home"><code>https://java.net/projects/javamail/pages/Home</code></a> and place it in
 *  <code>$PTII/vendors/misc/javamail</code>.  Below are the steps:</p>
 *  <pre>
 *    cd $PTII/vendors/misc/javamail
 *     wget --no-check-certificate http://java.net/projects/javamail/downloads/download/javax.mail.jar
 *     cd $PTII
 *     ./configure
 *  </pre>
 *  <p>In Eclipse, you will then need to refresh the project.</p> *
 *
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Red (cxh)
 */
public class SendMail extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SendMail(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        to = new PortParameter(this, "to");
        to.setStringMode(true);
        to.setExpression("nobody1@nowhere.com, nobody2@nowhere.com");
        new SingletonParameter(to.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);

        cc = new PortParameter(this, "cc");
        cc.setStringMode(true);
        cc.setExpression("nobody1@nowhere.com, nobody2@nowhere.com");
        new SingletonParameter(cc.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);

        from = new PortParameter(this, "from");
        from.setStringMode(true);
        from.setExpression("noreply@noreply.com");
        new SingletonParameter(from.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);

        replyTo = new PortParameter(this, "replyTo");
        replyTo.setStringMode(true);
        replyTo.setExpression("");
        new SingletonParameter(replyTo.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);

        subject = new PortParameter(this, "subject");
        subject.setStringMode(true);
        new SingletonParameter(subject.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);

        message = new PortParameter(this, "message");
        message.setStringMode(true);
        new SingletonParameter(message.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);
        TextStyle style = new TextStyle(message, "style");
        style.height.setExpression("30");

        attach = new FileParameter(this, "attach");

        SMTPHostName = new PortParameter(this, "SMTPHostName");
        SMTPHostName.setStringMode(true);
        SMTPHostName.setExpression("smtp.myserver.com");
        new SingletonParameter(SMTPHostName.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);

        SMTPUserName = new PortParameter(this, "SMTPUserName");
        SMTPUserName.setStringMode(true);
        SMTPUserName.setExpression("myusername");
        new SingletonParameter(SMTPUserName.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);

        password = new PortParameter(this, "password");
        password.setStringMode(true);
        password.setExpression("");
        new SingletonParameter(password.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);

        SMTPPort = new PortParameter(this, "SMTPPort");
        SMTPPort.setStringMode(true);
        SMTPPort.setExpression("");
        new SingletonParameter(SMTPPort.getPort(), "_showName")
        .setToken(BooleanToken.TRUE);

        enableSSL = new Parameter(this, "enableSSL");
        enableSSL.setTypeEquals(BaseType.BOOLEAN);
        enableSSL.setExpression("false");
        enableSSL.setPersistent(true);

        reallySendMail = new Parameter(this, "reallySendMail");
        reallySendMail.setTypeEquals(BaseType.BOOLEAN);
        reallySendMail.setExpression("false");
        reallySendMail.setPersistent(true);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** File to attach, if any. By default, this is empty,
     *  which means to not attach any file.
     */
    public FileParameter attach;

    /** Email address to copy on the message. */
    public PortParameter cc;

    /** Email address from which this is sent. */
    public PortParameter from;

    /** The message to send. This defaults to an empty string. */
    public PortParameter message;

    /** Output to which the formatted message is sent.
     *  The type of this output is string.
     */
    public TypedIOPort output;

    /** Password port of the account. */
    public PortParameter password;

    /** The address to which replies should be directed.
     *  This is a comma-separated list that defaults to
     *  an empty string, which indicates that the reply
     *  should go to the address specified by <i>from</i>.
     */
    public PortParameter replyTo;

    /** If true, then actually send the email.
     *  This is a boolean that defaults to false,
     *  meaning that the message is only sent to the output port.
     *  This parameter will be set back to false in the
     *  wrapup() method, to help prevent duplicate mailings.
     */
    public Parameter reallySendMail;

    /** Host name for the send mail server. */
    public PortParameter SMTPHostName;

    /** Outgoing SMTP mail port. */
    public PortParameter SMTPPort;

    /** User name for the send mail server. */
    public PortParameter SMTPUserName;

    /** Enable the Secure Sockets Layer (SSL) protocol.*/
    public Parameter enableSSL;

    /** The subject line. This defaults to an empty string. */
    public PortParameter subject;

    /** Email address(es) to which this is sent.
     *  This is a comma-separated list that defaults to
     *  "nobody1@nowhere.com, nobody2@nowhere.com".
     */
    public PortParameter to;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes.  In this class,
     *  if the SMTP host or user name is changed, this method forgets the password.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == SMTPHostName || attribute == SMTPUserName) {
            _password = null;
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Set up the properties for the SMTP protocol.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _props = new Properties();
        _props.put("mail.transport.protocol", "smtp");
        _props.put("mail.smtp.auth", "true");

        // Required by some mail servers
        _props.put("mail.smtp.starttls.enable", "true");
    }

    /** Update the parameters based on any available inputs
     *  and then send one email message.
     *  @exception IllegalActionException If any of several errors
     *   occur while attempting to send the message.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        from.update();
        to.update();
        message.update();
        subject.update();

        //configure SMTP server

        SMTPHostName.update();
        _props.put("mail.smtp.host",
                ((StringToken) SMTPHostName.getToken()).stringValue());

        SMTPPort.update();
        String SMTPPortValue = ((StringToken) SMTPPort.getToken())
                .stringValue();
        if (!SMTPPortValue.equals("")) {
            _props.put("mail.smtp.port", SMTPPortValue);
        } else {
            _props.remove("mail.smtp.port");
        }

        if (((BooleanToken) enableSSL.getToken()).booleanValue()) {
            if (!SMTPPortValue.equals("")) {
                _props.put("mail.smtp.socketFactory.port", SMTPPortValue);
            } else {
                _props.remove("mail.smtp.socketFactory.port");
            }

            _props.remove("mail.smtp.starttls.enable");

            _props.put("mail.smtp.socketFactory.class",
                    "javax.net.ssl.SSLSocketFactory");
        } else {
            _props.remove("mail.smtp.socketFactory.class");

            // The following is needed to prevent the message
            // Relaying denied. Proper authentication required.
            _props.put("mail.smtp.starttls.enable", "true");
        }

        // Make sure the user name is valid, since parse errors will
        // be ignored in the authenticator below.
        //SMTPUserName.stringValue();
        SMTPUserName.update();
        ((StringToken) SMTPUserName.getToken()).stringValue();

        password.update();

        String passwordValue = ((StringToken) password.getToken())
                .stringValue();
        if (!passwordValue.equals("")) {
            _password = passwordValue.toCharArray();
        }

        // First construct the string to send to the output.
        StringBuffer result = new StringBuffer();

        String toValue = ((StringToken) to.getToken()).stringValue();
        String fromValue = ((StringToken) from.getToken()).stringValue();
        String replyToValue = ((StringToken) replyTo.getToken()).stringValue()
                .trim();
        String ccValue = ((StringToken) cc.getToken()).stringValue();
        String subjectValue = ((StringToken) subject.getToken()).stringValue();
        String messageValue = ((StringToken) message.getToken()).stringValue();

        StringTokenizer tokenizer = new StringTokenizer(toValue, ",");
        while (tokenizer.hasMoreTokens()) {
            result.append("To: " + tokenizer.nextToken().trim() + "\n");
        }

        tokenizer = new StringTokenizer(ccValue, ",");
        while (tokenizer.hasMoreTokens()) {
            result.append("Cc: " + tokenizer.nextToken().trim() + "\n");
        }

        result.append("From: " + fromValue + "\n");

        if (!replyToValue.equals("")) {
            result.append("Reply-To: " + replyToValue + "\n");
        }

        result.append("Subject: " + subjectValue + "\n");
        if (attach.asFile() != null) {
            result.append("Attachment: " + attach.asFile().getAbsolutePath());
        }
        result.append("----\n");
        result.append(messageValue);
        result.append("\n----\n");

        output.send(0, new StringToken(result.toString()));

        if (!((BooleanToken) reallySendMail.getToken()).booleanValue()) {
            // Don't want to actually send email, so we just return now.
            if (_debugging) {
                _debug("reallySendMail is false, so no mail is sent.");
            }
            return true;
        }

        if (_debugging) {
            _debug("Sending mail with properties: " + _props.toString());
        }

        Authenticator auth = new SMTPAuthenticator();
        Session mailSession = Session.getDefaultInstance(_props, auth);
        // Uncomment for debugging info to stdout.
        if (_debugging) {
            _debug("Debug info from mail session going to standard out.");
            mailSession.setDebug(true);
        }

        try {
            Transport transport = mailSession.getTransport();

            MimeMessage mimeMessage = new MimeMessage(mailSession);

            // If there is an attachment, then we need
            // a multipart message to hold the attachment.
            if (attach.asFile() != null) {
                MimeBodyPart messagePart = new MimeBodyPart();
                messagePart.setText(messageValue);
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(attach.asFile());
                MimeMultipart multipart = new MimeMultipart();
                multipart.addBodyPart(messagePart);
                multipart.addBodyPart(attachmentPart);
                mimeMessage.setContent(multipart);
            } else {
                mimeMessage.setContent(messageValue, "text/plain");
            }
            mimeMessage.setFrom(new InternetAddress(fromValue));

            if (!replyToValue.equals("")) {
                ArrayList<Address> replyToAddresses = new ArrayList<Address>();
                tokenizer = new StringTokenizer(replyToValue, ",");
                while (tokenizer.hasMoreTokens()) {
                    replyToAddresses.add(new InternetAddress(tokenizer
                            .nextToken().trim()));
                }
                mimeMessage.setReplyTo(replyToAddresses
                        .toArray(new Address[replyToAddresses.size()]));
            }

            mimeMessage.setSubject(subjectValue);

            boolean atLeastOneToAddress = false;
            tokenizer = new StringTokenizer(toValue, ",");
            while (tokenizer.hasMoreTokens()) {
                mimeMessage.addRecipient(Message.RecipientType.TO,
                        new InternetAddress(tokenizer.nextToken().trim()));
                atLeastOneToAddress = true;
            }

            tokenizer = new StringTokenizer(ccValue, ",");
            while (tokenizer.hasMoreTokens()) {
                String nextToken = tokenizer.nextToken().trim();
                if (nextToken.equals("")) {
                    continue;
                }
                mimeMessage.addRecipient(Message.RecipientType.CC,
                        new InternetAddress(nextToken));
            }
            if (!atLeastOneToAddress) {
                if (MessageHandler
                        .yesNoQuestion("Message has no destination address. "
                                + " Skip this one?\n"
                                + "Clicking NO will abort execution. Message:\n"
                                + messageValue)) {
                    return true;
                }
                throw new IllegalActionException(this, "Aborted send.");
            }

            transport.connect();
            transport.sendMessage(mimeMessage,
                    mimeMessage.getRecipients(Message.RecipientType.TO));
            transport.close();
        } catch (MessagingException e) {
            if (MessageHandler.yesNoQuestion("Message to " + toValue
                    + " failed. Skip this one?\n"
                    + "Clicking NO will abort execution. Exception:\n" + e)) {
                return true;
            }
            throw new IllegalActionException(this, e, "Send mail failed.");
        } catch (IOException e) {
            throw new IllegalActionException(this, e,
                    "Failed to open attachment file.");
        }

        return super.postfire();
    }

    //    /** Override the base class to set <i>reallySendMail</i> back
    //     *  to false if it is true.
    //     */
    //    public void wrapup() throws IllegalActionException {
    //        super.wrapup();
    //        if (((BooleanToken) reallySendMail.getToken()).booleanValue()) {
    //           reallySendMail.setToken(BooleanToken.FALSE);
    //        }
    //    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The password last entered. */
    private char[] _password;

    /** Reference to a persistent set of properties used to configure
     *  the SMTP parameters.
     */
    private Properties _props;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    private class SMTPAuthenticator extends javax.mail.Authenticator {
        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            String username;
            try {
                username = ((StringToken) SMTPUserName.getToken())
                        .stringValue();
            } catch (IllegalActionException e) {
                // This should not occur, since bwfore constructing this
                // class, the user name was checked.
                throw new InternalErrorException(e);
            }
            if (_password == null) {
                // Open a dialog to get the password.
                // First find a frame to "own" the dialog.
                // Note that if we run in an applet, there may
                // not be an effigy.
                Effigy effigy = Configuration.findEffigy(toplevel());
                JFrame frame = null;
                if (effigy != null) {
                    Tableau tableau = effigy.showTableaux();
                    if (tableau != null) {
                        frame = tableau.getFrame();
                    }
                }

                // Next construct a query for user name and password.
                Query query = new Query();
                query.setTextWidth(60);
                query.addLine("SMTPuser", "SMTP user name", username);
                query.addPassword("password", "Password", "");
                ComponentDialog dialog = new ComponentDialog(frame,
                        "Send Mail Password", query);

                if (dialog.buttonPressed().equals("OK")) {
                    // Update the parameter values.
                    String newUserName = query.getStringValue("SMTPuser");
                    if (!username.equals(newUserName)) {
                        SMTPUserName.setExpression(newUserName);
                    }
                    // The password is not stored as a parameter.
                    _password = query.getCharArrayValue("password");
                } else {
                    return null;
                }
            }
            return new PasswordAuthentication(username, new String(_password));
        }
    }
}
