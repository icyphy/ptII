/* An actor that reads a Keystore from a FileParameter, looks up
a Certificate and outputs a PublicKey

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
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.KeyStore;
import java.security.PublicKey;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// KeyReader
/** Read in a keystore file, look up a certificate by alias name and
output the public key

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

@author  Christopher Brooks
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


        password = new StringParameter(this, "password");
        password.setExpression("this.is.not.secure,it.is.for.testing.only");

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
     *  Getting the private key requires using the password.
     */
    public Parameter getPublicKey;

    /** The password to the KeyStore.
     *  The default password is "this.is.not.secure,it.is.for.testing.only".
     */
    public StringParameter password;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>URL</i>, then close
     *  the current file (if there is one) and open the new one.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>URL</i> and the file cannot be opened.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == alias) {
            _alias = alias.getExpression();
        } else if (attribute == fileOrURL) {
            // Would it be worth checking to see if the URL exists and
            // is readable?
            _url = fileOrURL.asURL();
        } else if (attribute == getPublicKey) {
            _getPublicKey =
                ((BooleanToken)getPublicKey.getToken()).booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Output the data read in the prefire.
     *  @exception IllegalActionException If there's no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        System.out.println("KeyReader: " + _key);
        output.broadcast(new ObjectToken(_key));
    }

    /** Open the KeyStore, look up the Certificate and the PublicKey
     */
    public void initialize() throws IllegalActionException {
        attributeChanged(fileOrURL);

        InputStream keyStoreStream;
        try {
            // FIXME: this will not work if the input is stdin.
            // FileParameter needs to have a way of getting the unbuffered stream
            keyStoreStream = _url.openStream();
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to open " + _url);
        }

        try {
            char [] passwordArray = password.getExpression().toCharArray();
            _keyStore.load(keyStoreStream, passwordArray);
            // Add all the aliases as possible choices.
            for (Enumeration aliases = _keyStore.aliases();
                aliases.hasMoreElements() ;) {
                alias.addChoice((String)aliases.nextElement());
            }

            _certificate = _keyStore.getCertificate(_alias);

            // Get either the public key or the private key

            if (_getPublicKey) {
                _key = _certificate.getPublicKey();
            } else {
                _key = _keyStore.getKey(_alias, passwordArray);
            }
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to get key store aliases or certificate");
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The alias of the Certificate that we are looking for.
    private String _alias;

    // The Certificate that we look up in the KeyStore according to an alias
    private Certificate _certificate;

    // True if we should get the public key, false if we should get
    // the private key
    private boolean _getPublicKey;

    // The KeyStore itself.
    private KeyStore _keyStore;

    // The PublicKey located in the Certificate
    private java.security.Key _key;

    // The URL of the file.
    private URL _url;
}
