/* Read in a Keystore from a FileParameter, look up
a Certificate and output a public or private key.

@Copyright (c) 2003 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.security;

import ptolemy.actor.lib.Source;
import ptolemy.data.BooleanToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// KeyReader
/**  Read in a Keystore from a FileParameter, look up
a Certificate and output a public or private key.

<p>Keystores are ways to manage keys and certificates.  A keystore file can
be created by using the keytool binary that comes with Java.
To create a simple keystore, run
<pre>
cd $PTII
make ptKeystore
</pre>
which will create a keystore store password and key password is
<pre>this.is.not.secure,it.is.for.testing.only</code>
<br>The alias of the certificate will be <code>ptClaudius</code>

<p>For more information, see
<a href="http://java.sun.com/docs/books/tutorial/security1.2/summary/tools.html">Security Tools Summary</a>
<br><a href="http://java.sun.com/j2se/1.4.2/docs/tooldocs/windows/keyotol.html">Keytool</a.
@see PrivateKeyReader
@see PublicKeyReader
@author  Christopher Hylands Brooks
@version $Id$
@since Ptolemy II 3.1
*/
public class KeyReader extends Source {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public KeyReader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        output.setTypeEquals(BaseType.OBJECT);

        alias = new StringParameter(this, "alias");
        alias.setExpression("claudius");
        // We could try to set the alias choices here, but instead
        // we wait until initialize() so that we are sure the
        // KeyStore file is present.

        fileOrURL = new FileParameter(this, "fileOrURL");
        // To create the initial default KeyStore, do
        // cd $PTII; make ptKeystore
        fileOrURL.setExpression("$PTII/ptKeystore");

        getPublicKey = new Parameter(this, "getPublicKey",
                new BooleanToken(true));
        getPublicKey.setTypeEquals(BaseType.BOOLEAN);                

        keyPassword = new StringParameter(this, "keyPassword");
        keyPassword.setExpression(
                "this.is.not.secure,it.is.for.testing.only");

        storePassword = new StringParameter(this, "storePassword");
        storePassword.setExpression(
                "this.is.not.secure,it.is.for.testing.only");
        try {
            // We could have parameters that allow the user to set
            // the KeyStore type and provider, but since most users
            // will just use the default, let's wait.
            // BTW - to see the KeyStore type, run keytool -list
            _keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to get instance of key store");
        }

        signatureAlgorithm = new StringParameter(this, "signatureAlgorithm");
        signatureAlgorithm.setExpression(
                "Unknown, will be set after first run");
        signatureAlgorithm.setVisibility(Settable.NOT_EDITABLE);
        signatureAlgorithm.setPersistent(false);

        verifyCertificate = new Parameter(this, "verifyCertificate",
                new BooleanToken(true));
        verifyCertificate.setTypeEquals(BaseType.BOOLEAN);                
    }


    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The alias of the certificate that we are looking for.
     *  The default alias is the String "claudius"
     */
    public StringParameter alias;

    /** The file name or URL from which to read.  This is a string with
     *  any form accepted by FileParameter.
     *  The initial default is "$PTII/ptKeystore".  To create the
     *  initial default keystore, run "cd $PTII; make ptKeystore"
     *  @see FileParameter
     */
    public FileParameter fileOrURL;

    /** True if we should get the public key.  False if we should
     *  get the private key.  The default value is true.
     *  Getting the private key requires using the keyPassword.
     */
    public Parameter getPublicKey;

    /** The password to the Key itself.
     *  The default password is "this.is.not.secure,it.is.for.testing.only".
     */
    public StringParameter keyPassword;

    /** The name of the signature algorithm used to generate the key.
     *  This StringParameter is not settable by the user, it is set
     *  after initialize() is called and the certificate has been
     *  obtained from the KeyStore.
     */
    public StringParameter signatureAlgorithm;

    /** The password to the KeyStore.
     *  The default password is "this.is.not.secure,it.is.for.testing.only".
     */
    public StringParameter storePassword;

    /** True if the certificate associated with a key should be verified.
     *  False if the certificate (if any) need not be verified.
     *  <br>Public Keys must be associated with a certificate, so
     *  if <i>getPublicKey</i> is true, then this Parameter should
     *  be true as well.
     *  <br>Private keys are usually associated with a certificate, so
     *  verifying the certificate is a good idea.
     *  <br>Secret keys do not usually have a certficate, so if the
     *  key is a secret key, then usually <i>verifyCertificate</i>
     *  is set to false.
     */
    public Parameter verifyCertificate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to reinitialize the state if
     *  the <i>alias</i>, <i>fileOrURL</i>, or <i>getPublicKey</i>
     *  parameter is changed.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>URL</i> and the file cannot be opened.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == alias) {
            _updateKeyNeeded = true;
            _alias = alias.getExpression();
        } else if (attribute == fileOrURL) {
            _updateKeyNeeded = true;
            // Would it be worth checking to see if the URL exists and
            // is readable?
            _url = fileOrURL.asURL();
        } else if (attribute == getPublicKey) {
            _updateKeyNeeded = true;
            _getPublicKey =
                ((BooleanToken)getPublicKey.getToken()).booleanValue();
        } else if (attribute == verifyCertificate) {
            _updateKeyNeeded = true;
            _verifyCertificate = 
                ((BooleanToken)verifyCertificate.getToken()).booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Output the key that was created in initialize().
     *  @exception IllegalActionException If there's no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        _updateKey();
        output.broadcast(new ObjectToken(_key));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** If necessary, update _key by using the values of the 
     *  alias, fileOrURL and getPublicKey parameters. 
     *  @exception IllegalActionException If the parent class throws it
     *  or if there is a problem with the cryptographic configuration.
     */
    private void _updateKey() throws IllegalActionException {
        if (_updateKeyNeeded) {
            InputStream keyStoreStream;
            try {
                // FIXME: this will not work if the input is stdin.
                // FIXME: FileParameter needs to have a way of getting the
                // unbuffered stream.
                keyStoreStream = _url.openStream();
            } catch (IOException ex) {
                throw new IllegalActionException(this, ex,
                        "Failed to open " + _url);
            }

            try {
                _keyStore.load(keyStoreStream,
                        storePassword.getExpression().toCharArray());
                alias.removeAllChoices();
                // Add all the aliases as possible choices.
                for (Enumeration aliases = _keyStore.aliases();
                     aliases.hasMoreElements() ;) {
                    String aliasName = (String)aliases.nextElement();
                    alias.addChoice(aliasName);
                }

                if (!_verifyCertificate) {
                    if (_getPublicKey) {
                        throw new IllegalActionException(this,
                                "To get the public key, one must use "
                                + "certificates, so the verifyCertificate "
                                + "parameter must be set to true if the "
                                + "getPublicKey parameter is true.");
                    }
                } else {
                    Certificate certificate = _keyStore.getCertificate(_alias);
                    if (certificate == null) {
                        throw new KeyStoreException("Failed to get certificate "
                                + "for alias '"
                                + _alias + "' from  keystore '" + _url + "'");
                    }

                    PublicKey publicKey = certificate.getPublicKey();

                    // FIXME: The testsuite needs to test this with an
                    // invalid certificate.
                    certificate.verify(publicKey);

                    if (certificate instanceof X509Certificate) {
                        signatureAlgorithm.setExpression(
                                ((X509Certificate)certificate)
                                .getSigAlgName());
                    } else {
                        signatureAlgorithm.setExpression(
                                "Unknown, certificate was not a X509 cert.");
                    }
                    _key = publicKey;
                }                    
                if (!_getPublicKey) {
                    _key = _keyStore.getKey(_alias,
                            keyPassword.getExpression().toCharArray());
                }
                _updateKeyNeeded = false;
            } catch (Exception ex) {
                throw new IllegalActionException(this, ex,
                        "Failed to get key store aliases or certificate");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The alias of the Certificate that we are looking for.
    private String _alias;

    // True if we should get the public key, false if we should get
    // the private key or secret key.
    private boolean _getPublicKey;

    // The KeyStore itself.
    private KeyStore _keyStore;

    // The PublicKey located in the Certificate.
    private java.security.Key _key;

    // Set to true if one of the parameters changed and we need to
    // call _updateKey().
    private boolean _updateKeyNeeded = true;

    // The URL of the keystore file.
    private URL _url;

    // True if we should verify the certificate
    private boolean _verifyCertificate;

}
