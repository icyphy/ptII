/* A baseclass for actors that read or write keystores.

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

import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.lib.Sink;
import ptolemy.actor.lib.Source;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
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
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.util.Enumeration;
import java.util.Iterator;


//////////////////////////////////////////////////////////////////////////
//// KeyStore
/** A baseclass for actors that read or write keystores.

<p>Keystores are ways to manage keys and certificates.  A keystore file can
be created by using the <code>keytool</code> executable that comes with Java.
To create a simple keystore that contains a private key and
a public key signed with a self signed certificate, run:
<pre>
cd $PTII
make ptKeystore
</pre>
which will create a keystore store password and key password is
<code>this.is.not.secure,it.is.for.testing.only</code>
<br>The alias of the certificate will be <code>claudius</code>

<p>A keystore may have at most one type, which describes the format
of the keystore.  If a keyStore file exists, then the <i>keyStoreType</i>
parameter is set to the type of the preexisting keyStore.  Changing
the <i>keyStoreType</i> of a preexisting keystore to a different type
is likely to throw an exception when the keyStore is opened.
If a keyStore file does not exist, then when it is created it will
be created with the type from the <i>keyStoreType</i> parameter.
<p>The <code>keytool</code> creates keystores that have a type of
"JKS".  To view the keystore type, run
<code>keytool -keystore <i>keystoreFile</i>-list</code>.

<p>The {@link ptolemy.actor.lib.security.SecretKey} actor outputs a
key that must read in with a keystore type of "JCEKS", so if this
actor is being used with a SecretKey actor, then the type should be
set to "JCEKS".

<p>Derived classes should add input or output ports as necessary.
Derived classes should call _loadKeyStore() so that _keyStore is properly
initialized before accessing _keyStore themselves.

<h3>How to exchange data securely with a remote part</h3>
<a href="http://java.sun.com/docs/books/tutorial/security1.2/toolfilex/index.html" target="_top">http://java.sun.com/docs/books/tutorial/security1.2/toolfilex/index.html</a>
discusses how to exchange files using signatures, keytool
and jarsigner.  In Ptolemy II, we use actors derived from
the KeyStoreActor.

<h4>Steps for the Sender</h4>
<ol>
<li>Generate keys using keytool, which is included
in the JDK
<pre>
keytool -genkey -alias claudius -keystore $PTII/ptKeystore -keypass this.is.not.secure,it.is.for.testing.only -storepass this.is.not.secure,it.is.for.testing.only
</pre>
You will be prompted for information about yourself.
<li>Optional: Generate a Certificate Signing Request (CSR), send
it to your vendor and import the response.  Since we
are using a self signed certificate, this step is option.
<li> Export the certificate
<pre>
keytool -alias claudius -export -keystore $PTII/ptKeystore -keypass this.is.not.secure,it.is.for.testing.only -storepass this.is.not.secure,it.is.for.testing.only -file claudius.cer -rfc
</pre>
<li> Send the output file (claudius.cer) to the recipient
<li>Create a Ptolemy model that uses the 
{@link ptolemy.actor.lib.security.PrivateKeyReader} actor
to read $PTII/ptKeystore with the appropriate passwords
and sign your data.  
See the left side of $PTII/ptolemy/actor/lib/security/test/auto/Signature.xml
for an example model.

</ol>
<h4>Steps for the Receiver</h4>
<ol>
<li>Receive the public key from the sender and import it
into your keystore
<pre>
cxh@cooley 91% keytool -import -alias claudius -keystore $PTII/receivedKeystore -file claudius.cer
Enter keystore password:  foobar
Owner: CN=Claudius Ptolemaus, OU=Your Project, O=Your University, L=Your Town, ST=Your State, C=US
Issuer: CN=Claudius Ptolemaus, OU=Your Project, O=Your University, L=Your Town, ST=Your State, C=US
Serial number: 3fa9b2c5
Valid from: Wed Nov 05 18:32:37 PST 2003 until: Tue Feb 03 18:32:37 PST 2004
Certificate fingerprints:
	 MD5:  D7:43:A0:C0:39:49:A8:80:69:EA:11:91:17:CE:E5:E3
	 SHA1: C1:3B:9A:92:35:4F:7F:A5:23:AB:57:28:D6:67:ED:43:AB:EA:A9:2B
Trust this certificate? [no]:  yes
Certificate was added to keystore
cxh@cooley 92% 
</pre>

<li>Verify the signature by calling up the sender and comparing the
fingerprints on the phone.  The send can view the fingerprints with
<pre>
cxh@cooley 93% keytool -printcert -file claudius.cer
Owner: CN=Claudius Ptolemaus, OU=Your Project, O=Your University, L=Your Town, ST=Your State, C=US
Issuer: CN=Claudius Ptolemaus, OU=Your Project, O=Your University, L=Your Town, ST=Your State, C=US
Serial number: 3fa9b2c5
Valid from: Wed Nov 05 18:32:37 PST 2003 until: Tue Feb 03 18:32:37 PST 2004
Certificate fingerprints:
	 MD5:  D7:43:A0:C0:39:49:A8:80:69:EA:11:91:17:CE:E5:E3
	 SHA1: C1:3B:9A:92:35:4F:7F:A5:23:AB:57:28:D6:67:ED:43:AB:EA:A9:2B
cxh@cooley 94% 
</pre>
If the Certificate fingerprints match, then the file has not been
modified in transit.
<li> The receiver should then create a model that uses the
{@link ptolemy.actor.lib.security.PublicKeyReader} actor with
the appropriate passwords.
See the right side of $PTII/ptolemy/actor/lib/security/test/auto/Signature.xml
for an example model.

</ol>

<p>For more information about keystores, see
<a href="http://java.sun.com/docs/books/tutorial/security1.2/summary/tools.html" target="_top">Security Tools Summary</a>
and
<br><a href="http://java.sun.com/j2se/1.4.2/docs/tooldocs/windows/keytool.html" target="_top">Keytool</a.

@author  Christopher Hylands Brooks
@version $Id$
@since Ptolemy II 3.1
*/
public class KeyStoreActor extends TypedAtomicActor {

    // This actor could be called 'KeyStore', but that conflicts with
    // java.security.KeyStore, so we call it 'KeyStoreActor', which also
    // better matches the other actor base classes in this directory.

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public KeyStoreActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        alias = new StringParameter(this, "alias");
        alias.setExpression("claudius");

        fileOrURL = new FileParameter(this, "fileOrURL");
        // To create the initial default KeyStore, do
        // cd $PTII; make ptKeystore
        fileOrURL.setExpression("$PTII/ptKeystore");

        keyPassword = new PortParameter(this, "keyPassword");
        keyPassword.setTypeEquals(BaseType.STRING);
        keyPassword.setStringMode(true);
        keyPassword.setExpression(
                "this.is.not.secure,it.is.for.testing.only");

        // Add the possible keystore types.
        keyStoreType = new StringParameter(this, "keyStoreType");
        keyStoreType.setExpression(KeyStore.getDefaultType());
        Iterator keyStoreTypes = Security.getAlgorithms("KeyStore").iterator();
        while(keyStoreTypes.hasNext()) {
            String keyStoreName = (String)keyStoreTypes.next();
            keyStoreType.addChoice(keyStoreName);
        }

        // Add the possible provider choices.
        provider = new StringParameter(this, "provider");
        provider.setExpression("SystemDefault");
        provider.addChoice("SystemDefault");
        Provider [] providers = Security.getProviders();
        for (int i = 0; i < providers.length; i++) {
            provider.addChoice(providers[i].getName());
        }

        storePassword = new PortParameter(this, "storePassword");
        storePassword.setTypeEquals(BaseType.STRING);
        storePassword.setStringMode(true);
        storePassword.setExpression("this.is.not.secure,it.is.for.testing.only");
        _storePassword = storePassword.getExpression();
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

    /** The type of the keystore.  See
     *  <a href="http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html#AppA" target="_top">  Java Cryptography Architecture API Specification &amp; Reference</a>
     *  for information about keystore types.
     *  The initial value is the string returned by
     *  java.security.KeyStore.getDefaultType().
     *
     *  <p>Note that secret keys generated by the
     *  {@link SecretKey} actor should be saved in a keystore of type
     *  "JCEKS".
     */
    public StringParameter keyStoreType;

    /** The password to the Key.
     *  The default password is "this.is.not.secure,it.is.for.testing.only".
     *  If the port is left unconnected, then the parameter value will be used.
     */
    public PortParameter keyPassword;

    /** Specify a provider for the given algorithm.
     *  The default value is "SystemDefault" which allows the
     *  system to choose the provider based on the JCE architecture.
     */
    public StringParameter provider;

    /** The password to the KeyStore.
     *  The default password is "this.is.not.secure,it.is.for.testing.only".
     *  If the port is left unconnected, then the parameter value will be used.
     */
    public PortParameter storePassword;

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
            _loadKeyStoreNeeded = true;
        } else if (attribute == keyPassword) {
            _keyPassword = keyPassword.getExpression();
        } else if (attribute == keyStoreType) {
            _initializeKeyStoreNeeded = true;
            _keyStoreType = keyStoreType.getExpression();
        } else if (attribute == provider) {
            _initializeKeyStoreNeeded = true;
            _provider = provider.getExpression();
        } else if (attribute == storePassword) {
            _loadKeyStoreNeeded = true;
            _storePassword = storePassword.getExpression();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /**
     */
    public void fire() throws IllegalActionException {
        super.fire(); // Print debugging messages etc.
        keyPassword.update();
        _keyPassword = ((StringToken)keyPassword.getToken()).stringValue();
        if (keyPassword.getExpression() != _keyPassword) {
            // keyPassword changed, so reload the keyStore
            _loadKeyStoreNeeded = true;
        }
        // Set the persistent value to the current value.
        keyPassword.setExpression(_keyPassword);

        storePassword.update();
        _storePassword = ((StringToken)storePassword.getToken()).stringValue();
        if (storePassword.getExpression() != _storePassword) {
            _loadKeyStoreNeeded = true;
        }
        // Set the persistent value to the current value.
        storePassword.setExpression(_storePassword);

        // _loadKeystore() reads _keyPassword and _storePassword.
        _loadKeyStore();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** The alias of the Certificate that we are looking for. */
    protected String _alias;

    /** The password for the key. */
    protected String _keyPassword;

    /** The KeyStore itself. */
    protected KeyStore _keyStore;

    /** The keyStore type. */
    protected String _keyStoreType;

    /** The provider to be used for a provider specific implementation. */
    protected String _provider;

    /** The password for the keyStore. */
    protected String _storePassword;


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** If necessary, initialize the _keyStore by calling
     *  KeyStore.getInstance()
     *  @exception IllegalActionException If KeyStore.getInstance()
     *  throws an exception.
     */
    protected void _initializeKeyStore() throws IllegalActionException {

        // One would think that we could call _initializeKeyStore() in
        // initialize(), but we need to be able to read the
        // PortParameters, so we call this method from fire() and
        // other places.

        if (_initializeKeyStoreNeeded) {
            try {
                // FIXME: do we try to write the old _keyStore?
                if (_provider.equalsIgnoreCase("SystemDefault")) {
                    _keyStore = KeyStore.getInstance(_keyStoreType);
                } else {
                    _keyStore = KeyStore.getInstance(_keyStoreType,
                            _provider);
                }
                _initializeKeyStoreNeeded = false;
                _loadKeyStoreNeeded = true;
            } catch (Exception ex) {
                throw new IllegalActionException(this, ex,
                        "Failed to get instance '" + keyStoreType
                        + "'of keyStore");
            }
        }
    }

    /** If necessary, load the _keyStore and update the choice of aliases.
     *  @exception IllegalActionException If there is a problem creating
     *  a new keystore or loading a preexisting keystore.
     */
    protected void _loadKeyStore() throws IllegalActionException {
        if (_loadKeyStoreNeeded) {
            // If need be, update the keyStore.
            _initializeKeyStore();

            // FIXME: this will not work if the input is stdin.
            // FileParameter needs to have a way of getting the
            // unbuffered stream.
            InputStream keyStoreInputStream = null;
            try {
                keyStoreInputStream = fileOrURL.asURL().openStream();
            } catch (IOException ex) {
                // Ignore, this means that the file does not exist,
                // so we are trying to create a new empty keyStore.
            }
            if (keyStoreInputStream == null) {
                // fileOrURL does not yet exist, so we are creating
                // a new empty keyStore.
                try {
                    _keyStore.load(null, null);
                    alias.removeAllChoices();
                } catch (Exception ex) {
                    throw new IllegalActionException(this, ex,
                            "Problem creating a new empty keyStore.");
                }
            } else {
                try {
                    _keyStore.load(keyStoreInputStream,
                            _storePassword.toCharArray());
                    alias.removeAllChoices();
                    // Add all the aliases as possible choices.
                    for (Enumeration aliases = _keyStore.aliases();
                         aliases.hasMoreElements() ;) {
                        String aliasName = (String)aliases.nextElement();
                        alias.addChoice(aliasName);
                    }
                    keyStoreInputStream.close();
                } catch (java.io.EOFException ex) {
                    throw new IllegalActionException(this, ex,
                            "Problem loading '" + fileOrURL.asURL()
                            + "', perhaps the file is of length 0? "
                            + "To create a sample file, try "
                            + "cd $PTII; make ptKeystore");

                } catch (Exception ex) {
                    throw new IllegalActionException(this, ex,
                            "Problem loading '" + fileOrURL.asURL()
                            + "'");
                }
            }
            _loadKeyStoreNeeded = false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** Set to true if fileOrURL has changed and the keyStore needs to be
     * read in again and the aliases updated. */
    protected boolean _loadKeyStoreNeeded = true;

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // Set to true if either the keyStoreType or provider attribute changed
    // and _keyStore needs to be updated.
    private boolean _initializeKeyStoreNeeded = true;

    // The URL of the file.
    private URL _url;
}
