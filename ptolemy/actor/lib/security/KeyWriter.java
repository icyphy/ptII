/* Read in a Key from the input port and write it out to a KeyStore.

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

import ptolemy.actor.lib.Sink;
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// KeyWriter
/** Read in a Key from the input port and write it out to a KeyStore.

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

<p>The input is of type Object and is expected to contain object
of type java.security.Key.

<p>For more information, see
<a href="http://java.sun.com/docs/books/tutorial/security1.2/summary/tools.html">Security Tools Summary</a>

http://www.cs.ttu.edu/~cs5331/ns/modules/secretkeyCrypt/secretkeyCrypt.html
@author  Christopher Brooks
@version $Id$
@since Ptolemy II 3.1
*/
public class KeyWriter extends Sink {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public KeyWriter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input.setTypeEquals(BaseType.OBJECT);

        alias = new StringParameter(this, "alias");
        alias.setExpression("claudius");


        fileOrURL = new FileParameter(this, "fileOrURL");
        // To create the initial default KeyStore, do
        // cd $PTII; make ptKeystore
        fileOrURL.setExpression("$PTII/ptKeystore");

        keyPassword = new StringParameter(this, "keyPassword");
        keyPassword.setExpression("this.is.not.secure,it.is.for.testing.only");

        try {
            // FIXME: We could have parameters that allow the user to set
            // the KeyStore type and provider, but since most users
            // will just use the default, let's wait.
            // BTW - to see the KeyStore type, run keytool -list
            _keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to get instance of key store");
        }

        storePassword = new StringParameter(this, "storePassword");
        storePassword.setExpression("this.is.not.secure,it.is.for.testing.only");
        // Is calling _updateAliases too much to do in the constructor?
        _updateAliases();
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

    /** The password to the Key
     *  The default password is "this.is.not.secure,it.is.for.testing.only".
     */
    public StringParameter keyPassword;

    /** The password to the KeyStore.
     *  The default password is "this.is.not.secure,it.is.for.testing.only".
     */
    public StringParameter storePassword;

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
            _updateAliases();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Output the data read in the prefire.
     *  @exception IllegalActionException If there's no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            ObjectToken objectToken = (ObjectToken)input.get(0);
            java.security.Key key = (java.security.Key)objectToken.getValue(); 
            // Now we add the key to the keystore, protected
            // by the password.
            try {
                _keyStore.setKeyEntry(_alias, key, 
                        keyPassword.getExpression().toCharArray(),
                        null /* No certificate */);
            } catch (Exception ex) {
                throw new IllegalActionException(this, ex,
                    "Failed to set key '" + key + "' to alias '"
                    + alias + "'");
            }
        }
    }

    /** Write the key store out and close it.
     *  @exception IllegalActionException If an IO error occurs.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        try {
            _keyStoreOutputStream =
                new FileOutputStream(fileOrURL.asFile());
            _keyStore.store(_keyStoreOutputStream,
                    keyPassword.getExpression().toCharArray());
            _keyStoreOutputStream.close();
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to close '" + fileOrURL + "'");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Update the choice of aliases by reading in the keyStore file
    // if it exists
    private void _updateAliases() {

        try {
            // FIXME: this will not work if the input is stdin.
            // FileParameter needs to have a way of getting the unbuffered stream
            InputStream keyStoreInputStream;
            keyStoreInputStream = fileOrURL.asURL().openStream();
            try {
                _keyStore.load(keyStoreInputStream,
                        storePassword.getExpression().toCharArray());
                alias.removeAllChoices();
                // Add all the aliases as possible choices.
                for (Enumeration aliases = _keyStore.aliases();
                     aliases.hasMoreElements() ;) {
                    String aliasName = (String)aliases.nextElement();
                    //System.out.println("KeyWriter: " + aliasName 
                    //        + " isKeyEntry: " + _keyStore.isKeyEntry(aliasName)
                    //       + " isCertEntry: "
                    //        + _keyStore.isCertificateEntry(aliasName));
                    alias.addChoice(aliasName);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                // Ignore.  Perhaps the file does not exist?
            }
            keyStoreInputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            // Ignore.  Perhaps the file does not exist?
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The alias of the Certificate that we are looking for.
    private String _alias;

    // The Certificate that we look up in the KeyStore according to an alias
    private Certificate _certificate;

    // The stream that we output the keyStore to.
    private FileOutputStream _keyStoreOutputStream;

    // The KeyStore itself.
    private KeyStore _keyStore;

    // The PublicKey located in the Certificate
    private java.security.Key _key;

    // The URL of the file.
    private URL _url;
}
