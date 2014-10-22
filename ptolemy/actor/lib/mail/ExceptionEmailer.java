/* An attribute that sends an email upon occurrence of an exception.

 Copyright (c) 2014 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */

package ptolemy.actor.lib.mail;

import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import javax.mail.internet.MimeMessage;
import javax.swing.JFrame;

import ptolemy.actor.AbstractInitializableAttribute;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.lib.ExceptionManagerModel;
import ptolemy.actor.lib.ExceptionSubscriber;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// ExceptionEmailer

/**
 * An ExceptionEmailer sends an email to the specified party upon occurrence of 
 * an exception.  The functionality is a simplified version of {@link SendMail}
 * without attachments or an output.  The password is read from a file to avoid
 * storing it in the model.
 * 
 * @author Edward A. Lee, Elizabeth Latronico
 * @version $Id: ExceptionSubscriber.java 69467 2014-06-29 14:35:19Z beth@berkeley.edu$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (beth)
 * @Pt.AcceptedRating Red (beth)
 * @see SendMail
 */
public class ExceptionEmailer extends AbstractInitializableAttribute 
    implements ExceptionSubscriber {
    /** Invoked by an exception handler (e.g. CatchExceptionAttribute) when an
     *  exception occurs.  Some subscribers may need to set up access to 
     *  resources (such as opening a file) prior to being notified of an 
     *  exception. These could extend AbstractInitalizableAttribute to do so.
     * 
     *  <p>
     *  This attribute requires the JavaMail 1.5 javax.mail.jar file be in the
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
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public ExceptionEmailer(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
 
        to = new StringParameter(this,"to");
        to.setExpression("nobody1@nowhere.com, nobody2@nowhere.com");

        cc = new StringParameter(this, "cc");
        cc.setExpression("nobody1@nowhere.com, nobody2@nowhere.com");

        from = new StringParameter(this, "from");
        from.setExpression("noreply@noreply.com");

        replyTo = new StringParameter(this, "replyTo");
        replyTo.setExpression("");

        SMTPHostName = new StringParameter(this, "SMTPHostName");
        SMTPHostName.setExpression("smtp.myserver.com");
        
        SMTPPort = new StringParameter(this, "SMTPPort");
        SMTPPort.setExpression("");

        SMTPUserName = new StringParameter(this, "SMTPUserName");
        SMTPUserName.setExpression("myusername");

        passwordFile = new FileParameter(this, "passwordFile");
        passwordFile.setExpression("");

        enableSSL = new Parameter(this, "enableSSL");
        enableSSL.setTypeEquals(BaseType.BOOLEAN);
        enableSSL.setExpression("false");
        enableSSL.setPersistent(true);

        reallySendMail = new Parameter(this, "reallySendMail");
        reallySendMail.setTypeEquals(BaseType.BOOLEAN);
        reallySendMail.setExpression("false");
        reallySendMail.setPersistent(true);
        
        statusMessage = new StringParameter(this, "statusMessage");
        statusMessage.setExpression("No exception encountered");
        statusMessage.setVisibility(Settable.NOT_EDITABLE);
        
        _newline = new Parameter(this, "_newline");
        _newline.setExpression("property(\"line.separator\")");
        
        _props = new Properties();
        _props.put("mail.transport.protocol", "smtp");
        _props.put("mail.smtp.auth", "true");
        
        // Required by some mail servers 
        _props.put("mail.smtp.starttls.enable", "true");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////              public parameters                            ////

    /** Email address to copy on the message. */
    public StringParameter cc;
    
    /** Enable the Secure Sockets Layer (SSL) protocol.*/
    public Parameter enableSSL;

    /** Email address from which this is sent. */
    public StringParameter from;
    
    /** The file that the password is stored in. */
    public FileParameter passwordFile;

    /** If true, then actually send the email.
     *  This is a boolean that defaults to false,
     *  meaning that the message is only sent to the output port.
     *  This parameter will be set back to false in the
     *  wrapup() method, to help prevent duplicate mailings.
     */
    public Parameter reallySendMail;
    
    /** The address to which replies should be directed.
     *  This is a comma-separated list that defaults to
     *  an empty string, which indicates that the reply
     *  should go to the address specified by <i>from</i>.
     */
    public StringParameter replyTo;

    /** Host name for the send mail server. */
    public StringParameter SMTPHostName;

    /** Outgoing SMTP mail port. */
    public StringParameter SMTPPort;

    /** User name for the send mail server. */
    public StringParameter SMTPUserName;
    
    /** A status message reflecting the success or failure of actions taken
     * upon occurrence of an exception.  Implemented as a public parameter so
     * the message can be displayed in the icon.
     */
    public StringParameter statusMessage;  

    /** Email address(es) to which this is sent.
     *  This is a comma-separated list that defaults to
     *  "nobody1@nowhere.com, nobody2@nowhere.com".
     */
    public StringParameter to;
    
    ///////////////////////////////////////////////////////////////////
    ////              public methods                               ////
    
    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes.  In this class,
     *  if the SMTP host or user name is changed, this method forgets the password.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == SMTPHostName || attribute == SMTPUserName) {
            _password = null;
        } else {
            super.attributeChanged(attribute);
        }
    }
    
    /** Clone the object into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ExceptionEmailer newObject = (ExceptionEmailer) super.clone(workspace);

        newObject._newline = (Parameter)newObject.getAttribute("_newline");
        newObject._newline.setExpression("property(\"line.separator\")");
        
        newObject._password = null;
        newObject._props = null;
        
        return newObject;
    }
    
    // TODO:  Allow sending an email upon exception handled.  Could have one or
    // the other, or both
    @Override
    public boolean exceptionHandled(boolean succesful, String message) {
        return true;
        
    }

    /** Send a mail upon occurrence of an exception.
     * 
     * @param policy  The exception handling policy
     * @param exception The exception that occurred
     * @return True if email was sent successfully (or if notification message 
     *  was successfully generated if reallySendMail is false), false otherwise
     */
    @Override
    public boolean exceptionOccurred(String policy, Throwable exception) {

        //configure SMTP server
        _props.put("mail.smtp.host", SMTPHostName.getValueAsString());

        String SMTPPortValue = SMTPPort.getValueAsString(); 
        if (!SMTPPortValue.equals("")) {
            _props.put("mail.smtp.port", SMTPPortValue);
        } else {
            _props.remove("mail.smtp.port");
        }

        boolean enableSSLValue = false;
        try {
            enableSSLValue = 
                    ((BooleanToken) enableSSL.getToken()).booleanValue();
        } catch(IllegalActionException e){
            statusMessage.setExpression("Failed to enable SSL");
            try { 
                statusMessage.validate();
            } catch(IllegalActionException e2) {
                // Should not happen since expression is legal
            };
            return false;
        }
        
        if (enableSSLValue) {
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
        SMTPUserName.getValueAsString();
        
        // Open password file an retrieve password
        BufferedReader reader = null;

        StringBuffer lineBuffer = new StringBuffer();
        try {
            reader = passwordFile.openForReading();
            
            String newlineValue = ((StringToken) _newline.getToken())
                    .stringValue();
            while (true) {
                String line = reader.readLine();

                if (line == null) {
                    break;
                }

                lineBuffer = lineBuffer.append(line);
                lineBuffer = lineBuffer.append(newlineValue);
            }
        } catch (Throwable throwable) {
            statusMessage.setExpression("Failed to read password file.");
            try { 
                statusMessage.validate();
            } catch(IllegalActionException e) {
                // Should not happen since expression is legal
            };
            return false;
        } finally {
            if (passwordFile != null) {
                try {
                    passwordFile.close();
                } catch(IllegalActionException e){
                   statusMessage.setExpression("Failed to close password " 
                                   + "file.");
                   try { 
                       statusMessage.validate();
                   } catch(IllegalActionException e2) {
                       // Should not happen since expression is legal
                   };
                   return false;
                }
            }
        }
        
        String passwordValue = lineBuffer.toString();
        if (!passwordValue.equals("")) {
            _password = passwordValue.toCharArray();
        }

        // Construct the email
        StringBuffer result = new StringBuffer();
        
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        
        NamedObj toplevel;
        
        // If contained by an ExceptionManager, get toplevel of container
        if (getContainer() != null && 
                getContainer() instanceof ExceptionManagerModel) {
            toplevel = ((ExceptionManagerModel) getContainer())
                    .getModelContainer().toplevel();
        } else {
            toplevel = toplevel();
        }
        
        String modelName = toplevel.getName();
        String policyAction;
        
        // Policies restart, stop and throw
        if (policy.equals("restart")) {
            policyAction = "restarted";
        } else {
            policyAction = "stopped";
        }
        
        String subject = modelName + " " + policyAction + " at " 
                + dateFormat.format(date);
        String message = "An exception occurred in model " + modelName + " at "
                + dateFormat.format(date) + ".  The model was " 
                + policyAction + ".";

        StringTokenizer tokenizer = 
                new StringTokenizer(to.getValueAsString(), ",");
        while (tokenizer.hasMoreTokens()) {
            result.append("To: " + tokenizer.nextToken().trim() + "\n");
        }

        tokenizer = new StringTokenizer(cc.getValueAsString(), ",");
        while (tokenizer.hasMoreTokens()) {
            result.append("Cc: " + tokenizer.nextToken().trim() + "\n");
        }

        result.append("From: " + from.getValueAsString() + "\n");

        if (!replyTo.getValueAsString().equals("")) {
            result.append("Reply-To: " + replyTo.getValueAsString() + "\n");
        }

        result.append("Subject: " + subject + "\n");

        result.append("----\n");
        result.append(message);
        result.append("\n----\n");

        Boolean reallySendMailValue = false;
        try {
            reallySendMailValue = 
                    ((BooleanToken) reallySendMail.getToken()).booleanValue();
        } catch(IllegalActionException e){
            statusMessage.setExpression("Failed to read reallySendMail " 
                            + "parameter.");
            try { 
                statusMessage.validate();
            } catch(IllegalActionException e2) {
                // Should not happen since expression is legal
            };
            return false;
        }
        
        if (!reallySendMailValue) {
            // Don't want to actually send email, so we just return now.
            if (_debugging) {
                _debug("reallySendMail is false, so no mail is sent.");
            }
            statusMessage.setExpression(message);
            try { 
                statusMessage.validate();
            } catch(IllegalActionException e) {
                // Should not happen since expression is legal
            };
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
            mimeMessage.setContent(message, "text/plain");
            mimeMessage.setFrom(new InternetAddress(from.getValueAsString()));

            if (!replyTo.getValueAsString().equals("")) {
               ArrayList<Address> replyToAddresses = new ArrayList<Address>();
               tokenizer = new StringTokenizer(replyTo.getValueAsString(), ",");
               while (tokenizer.hasMoreTokens()) {
                    replyToAddresses.add(new InternetAddress(tokenizer
                            .nextToken().trim()));
               }
               mimeMessage.setReplyTo(replyToAddresses
                        .toArray(new Address[replyToAddresses.size()]));
            }

            mimeMessage.setSubject(subject);

            boolean atLeastOneToAddress = false;
            tokenizer = new StringTokenizer(to.getValueAsString(), ",");
            while (tokenizer.hasMoreTokens()) {
                mimeMessage.addRecipient(Message.RecipientType.TO,
                        new InternetAddress(tokenizer.nextToken().trim()));
                atLeastOneToAddress = true;
            }

            tokenizer = new StringTokenizer(cc.getValueAsString(), ",");
            while (tokenizer.hasMoreTokens()) {
                String nextToken = tokenizer.nextToken().trim();
                if (nextToken.equals("")) {
                    continue;
                }
                mimeMessage.addRecipient(Message.RecipientType.CC,
                        new InternetAddress(nextToken));
            }
            
            if (!atLeastOneToAddress) {
                statusMessage.setExpression("Error: No recipient specified.");
                try { 
                    statusMessage.validate();
                } catch(IllegalActionException e) {
                    // Should not happen since expression is legal
                };
                return false;
            }

            transport.connect();
            transport.sendMessage(mimeMessage,
                    mimeMessage.getRecipients(Message.RecipientType.TO));
            transport.close();
        } catch (MessagingException e) {
            statusMessage.setExpression("Message failed.");
            try { 
                statusMessage.validate();
            } catch(IllegalActionException e2) {
                // Should not happen since expression is legal
            };
            return false;
        }
        
        statusMessage.setExpression("Mail sent at " + dateFormat.format(date));
        return true;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////              private variables                            ////
    
    /** The end of line character. */
    private Parameter _newline;
    
    /** The password last entered. */
    private char[] _password;
    
    /** Reference to a persistent set of properties used to configure
     *  the SMTP parameters.
     */
    private Properties _props;
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    private class SMTPAuthenticator extends javax.mail.Authenticator {
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


