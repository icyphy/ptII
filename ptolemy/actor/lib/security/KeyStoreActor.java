/* A baseclass for actors that read or write keystores.

 @Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.security;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.util.Enumeration;
import java.util.Iterator;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Constants;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// KeyStore

/** A baseclass for actors that read or write keystores.

 <p>Keystores are ways to manage keys and certificates.  A keystore file can
 be created by using the <code>keytool</code> executable that comes with Java,
 or, if the <i>createFileOrURLIfNecessary</i> parameter is true,
 then a keystore will be created for you.

 To create a simple keystore by hand that contains a private key and
 a public key signed with a self signed certificate, run:
 <pre>
 cd $PTII
 make ptKeystore
 </pre>
 which will create a keystore with a store password
 of <code>this.is.the.storePassword,change.it</code>
 and key password of
 of <code>this.is.the.keyPassword,change.it</code>.

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
 <a href="http://download.oracle.com/javase/tutorial/security/toolfilex/index.html" target="_top">http://download.oracle.com/javase/tutorial/security/toolfilex/index.html</a>
 discusses how to exchange files using signatures, keytool
 and jarsigner.  In Ptolemy II, we use actors derived from
 the KeyStoreActor.

 <h4>Steps for the Sender</h4>
 <ol>
 <li>Generate keys using keytool, which is included
 in the JDK
 <pre>
 keytool -genkey -alias claudius -keystore $PTII/ptKeystore -keypass myKeyPassword -storepass myStorePassword
 </pre>
 You will be prompted for information about yourself.
 <li>Optional: Generate a Certificate Signing Request (CSR), send
 it to your vendor and import the response.  Since we
 are using a self signed certificate, this step is option.
 <li> Export the certificate
 <pre>
 keytool -alias claudius -export -keystore $PTII/ptKeystore -keypass myKeyPassword -storepass myStorePassword -file claudius.cer -rfc
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
 <a href="http://download.oracle.com/javase/6/docs/technotes/guides/security/SecurityToolsSummary.html" target="_top">Security Tools Summary</a>.

 @author  Christopher Hylands Brooks
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (cxh)
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
        alias.setExpression("ptolemy");

        createFileOrURLIfNecessary = new Parameter(this,
                "createFileOrURLIfNecessary");
        createFileOrURLIfNecessary.setExpression("true");
        createFileOrURLIfNecessary.setTypeEquals(BaseType.BOOLEAN);

        fileOrURL = new FileParameter(this, "fileOrURL");

        // To create the initial default KeyStore, do
        // cd $PTII; make ptKeystore
        // or set createFileOrURLIfNecessary to true.
        fileOrURL.setExpression("$PTII/ptKeystore");

        keyPassword = new PortParameter(this, "keyPassword");
        keyPassword.setTypeEquals(BaseType.STRING);
        keyPassword.setStringMode(true);
        keyPassword.setExpression("this.is.the.keyPassword,change.it");

        // Add the possible keystore types.
        keyStoreType = new StringParameter(this, "keyStoreType");
        keyStoreType.setExpression(KeyStore.getDefaultType());

        Iterator keyStoreTypes = Security.getAlgorithms("KeyStore").iterator();

        while (keyStoreTypes.hasNext()) {
            String keyStoreName = (String) keyStoreTypes.next();
            keyStoreType.addChoice(keyStoreName);
        }

        // Add the possible provider choices.
        provider = new StringParameter(this, "provider");
        provider.setExpression("SystemDefault");
        provider.addChoice("SystemDefault");

        Provider[] providers = Security.getProviders();

        for (Provider provider2 : providers) {
            provider.addChoice(provider2.getName());
        }

        storePassword = new PortParameter(this, "storePassword");
        storePassword.setTypeEquals(BaseType.STRING);
        storePassword.setStringMode(true);
        storePassword.setExpression("this.is.the.storePassword,change.it");
        _storePassword = storePassword.getExpression();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The alias of the certificate that we are looking for.
     *  The default alias is the String "ptolemy"
     */
    public StringParameter alias;

    /** If true, then create the keystore named by <i>fileOrURL</i>
     *  if the <i>fileOrURL</i> does not exist.
     *  The default value is true.
     */
    public Parameter createFileOrURLIfNecessary;

    /** The file name or URL from which to read.  This is a string with
     *  any form accepted by FileParameter.
     *  The initial default is "$PTII/ptKeystore".  To create the
     *  initial default keystore, run "cd $PTII; make ptKeystore"
     *  or set the <i>createFileOrURLIfNecessary</i> to true.
     *  @see FileParameter
     */
    public FileParameter fileOrURL;

    /** The type of the keystore.  See
     *  <a href="http://download.oracle.com/javase/6/docs/technotes/guides/security/StandardNames.html#KeyStore" target="_top">  Java Cryptography Architecture API Specification &amp; Reference</a>
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
     *  The default password is "this.is.the.keyPassword,change.it".
     *  If the port is left unconnected, then the parameter value will be used.
     */
    public PortParameter keyPassword;

    /** Specify a provider for the given algorithm.
     *  The default value is "SystemDefault" which allows the
     *  system to choose the provider based on the JCE architecture.
     */
    public StringParameter provider;

    /** The password to the KeyStore.
     *  The default password is "this.is.the.storePassword,change.it".
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
    @Override
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

    /** Create the keystore file.
     *  @param keystoreFilename The name of the keystore file.
     *  @exception IllegalActionException If there is a problem creating
     *  the keystore.
     */
    public void createKeystore(String keystoreFilename)
            throws IllegalActionException {
        System.out.println("Creating keystore " + keystoreFilename);

        String javaHomeProperty = "ptolemy.ptII.java.home";
        String javaHome = null;

        try {
            javaHome = StringUtilities.getProperty(javaHomeProperty);
        } catch (SecurityException ex) {
            System.out.println("Warning: KeyStoreActor: Failed to get the "
                    + "java home directory "
                    + "(-sandbox always causes this): " + ex);
        }

        if (javaHome == null || javaHome.length() == 0) {
            // Use java.home property if the ptolemy.ptII.java.home property
            // can not be read.  For example, MoMLSimpleApplication does not
            // read $PTII/lib/ptII.properties.
            javaHome = StringUtilities.getProperty("java.home");

            if (javaHome != null && javaHome.length() > 0) {
                javaHome = javaHome.replace('\\', '/');
            } else {
                throw new InternalErrorException(this, null,
                        "Could not find the " + javaHomeProperty + " and the "
                                + "java.home property. Perhaps "
                                + "$PTII/lib/ptII.properties "
                                + "is not being read properly?");
            }
        }

        File javaHomeFile = new File(javaHome);

        if (!javaHomeFile.isDirectory()) {
            throw new InternalErrorException(
                    this,
                    null,
                    "Could not find the Java "
                            + "directory that contains bin/keytool.  "
                            + "Tried looking for the '"
                            + javaHome
                            + "' directory. "
                            + "Perhaps the "
                            + javaHomeProperty
                            + " or java.home property was not set "
                            + "properly because "
                            + "$PTII/lib/ptII.properties is not being read properly?");
        }

        String keytoolPath = javaHome + "/bin/keytool";

        String commonCommand = " -keystore " + keystoreFilename
                + " -storetype " + _keyStoreType + " -alias " + _alias
                + " -storepass \"" + _storePassword + "\"" + " -keypass \""
                + _keyPassword + "\"";

        String command1 = keytoolPath
                + " -genkey"
                + " -dname \"CN=Claudius Ptolemaus, OU=Your Project, O=Your University, L=Your Town, S=Your State, C=US\""
                + commonCommand;

        String command2 = keytoolPath + " -selfcert" + commonCommand;

        String command3 = keytoolPath + " -list" + " -keystore "
                + keystoreFilename + " -storepass \"" + _storePassword + "\"";

        _exec(command1);
        _exec(command2);
        _exec(command3);

        if (!new File(keystoreFilename).exists()) {
            throw new IllegalActionException(this, "Failed to create '"
                    + keystoreFilename + "', try running\n" + command1 + "\n"
                    + command2 + "\n" + command3);
        }
    }

    /** Load the keystore for use by derived classes.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire(); // Print debugging messages etc.
        keyPassword.update();
        _keyPassword = ((StringToken) keyPassword.getToken()).stringValue();

        if (!keyPassword.getExpression().equals(_keyPassword)) {
            // keyPassword changed, so reload the keyStore
            _loadKeyStoreNeeded = true;
        }

        // Set the persistent value to the current value.
        keyPassword.setExpression(_keyPassword);

        storePassword.update();
        _storePassword = ((StringToken) storePassword.getToken()).stringValue();

        if (!storePassword.getExpression().equals(_storePassword)) {
            _loadKeyStoreNeeded = true;
        }

        // Set the persistent value to the current value.
        storePassword.setExpression(_storePassword);

        // _loadKeystore() reads _keyPassword and _storePassword.
        _loadKeyStore();
    }

    /** Override the base class to stop waiting for input data.
     */
    @Override
    public synchronized void stopFire() {
        super.stopFire();
        _stopFireRequested = true;
        _terminateProcess();
    }

    /** Terminate the subprocess.
     *  This method is invoked exactly once per execution
     *  of an application.  None of the other action methods should be
     *  be invoked after it.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        _terminateProcess();
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
     *  KeyStore.getInstance().
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
                    _keyStore = KeyStore.getInstance(_keyStoreType, _provider);
                }

                _initializeKeyStoreNeeded = false;
                _loadKeyStoreNeeded = true;
            } catch (Throwable throwable) {
                throw new IllegalActionException(this, throwable,
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
                // The next line might throw a NullPointerException
                // if the fileOrURL does not exist.
                keyStoreInputStream = fileOrURL.asURL().openStream();
            } catch (IllegalActionException ex) {
                // asURL() throws IllegalActionException and sets
                // the cause to an IOException if the file cannot
                // be found.
                if (!(ex.getCause() instanceof IOException)) {
                    throw ex;
                }
            } catch (FileNotFoundException ex) {
                // Ignore, this means that the file does not exist,
                // so we are trying to create a new empty keyStore.
            } catch (IOException ex2) {
                // We once had a bug here where asURL() had a bug.
                throw new IllegalActionException(this, ex2, "Failed to open '"
                        + fileOrURL.asURL() + "' keyStore");
            }

            if (keyStoreInputStream == null) {
                if (((BooleanToken) createFileOrURLIfNecessary.getToken())
                        .booleanValue()) {
                    String keystoreFileName = fileOrURL.stringValue();

                    try {
                        String classpathProperty = ((StringToken) Constants
                                .get("CLASSPATH")).stringValue();

                        if (keystoreFileName.startsWith(classpathProperty)) {
                            keystoreFileName = ((StringToken) Constants
                                    .get("PTII")).stringValue()
                                    + "/"
                                    + keystoreFileName
                                    .substring(classpathProperty
                                            .length());
                        }

                        createKeystore(keystoreFileName);
                    } catch (IllegalActionException ex) {
                        throw new IllegalActionException(this, ex,
                                "Failed to create keystore '"
                                        + keystoreFileName + "'");
                    }

                    try {
                        // Try again
                        keyStoreInputStream = fileOrURL.asURL().openStream();
                    } catch (Exception ex) {
                        // Ignore, this means that the file does not exist,
                        // so we are trying to create a new empty keyStore.
                    }
                }
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
                    for (Enumeration aliases = _keyStore.aliases(); aliases
                            .hasMoreElements();) {
                        String aliasName = (String) aliases.nextElement();
                        alias.addChoice(aliasName);
                    }

                    keyStoreInputStream.close();
                } catch (java.io.EOFException ex) {
                    throw new IllegalActionException(this, ex,
                            "Problem loading " + fileOrURLDescription()
                            + ", perhaps the file is of length 0? "
                            + "To create a sample file, try "
                            + "cd $PTII; make ptKeystore");
                } catch (Exception ex) {
                    throw new IllegalActionException(this, ex,
                            "Problem loading " + fileOrURLDescription());
                }
            }

            _loadKeyStoreNeeded = false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return descriptive information about fileOrURL.
     *  @return The description.
     */
    protected String fileOrURLDescription() {
        if (fileOrURL == null) {
            return "Keystore URL is null";
        }

        StringBuffer results = new StringBuffer("Keystore");
        String name = null;

        try {
            name = ": '" + fileOrURL.stringValue() + "'";
        } catch (Throwable throwable) {
            name = ": " + fileOrURL.toString();
        }

        results.append(name);

        String exists = ", which does not exist";

        try {
            File fileHandle = fileOrURL.asFile();

            if (fileHandle.exists()) {
                if (fileHandle.canRead()) {
                    exists = ", which exists and is readable";
                } else {
                    exists = ", which exists and is not readable";
                }
            }
        } catch (Throwable throwable) {
            // Ignore
        }

        results.append(exists + ", ");

        String url = " and cannot be represented as a URL";

        try {
            url = " as a URL is: '" + fileOrURL.asURL().toString() + "'";
        } catch (Throwable throwable) {
            // Ignore
        }

        results.append(url);

        return results.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** Set to true if fileOrURL has changed and the keyStore needs to be
     * read in again and the aliases updated. */
    protected boolean _loadKeyStoreNeeded = true;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Execute a command.
    private void _exec(String command) throws IllegalActionException {
        // FIXME: Exec, KeyStoreActor, JTextAreaExec have duplicate code
        String outputString = "";
        String errorString = "";

        try {
            _stopFireRequested = false;

            System.out.println("Keystore Command: " + command);

            if (_process != null) {
                // Note that we assume that _process is null upon entry
                // to this method, but we check again here just to be sure.
                _terminateProcess();
            }

            Runtime runtime = Runtime.getRuntime();

            // Preprocess by removing lines that begin with '#'
            // and converting substrings that begin and end
            // with double quotes into one array element.
            final String[] commandTokens = StringUtilities
                    .tokenizeForExec(command);
            _process = runtime.exec(commandTokens);

            // Create two threads to read from the subprocess.
            _outputGobbler = new _StreamReaderThread(_process.getInputStream(),
                    "KeyStoreActor Stdout Gobbler-"
                            + _keystoreStreamReaderThreadCount++, this);
            _errorGobbler = new _StreamReaderThread(_process.getErrorStream(),
                    "KeyStoreActor Stderr Gobbler-"
                            + _keystoreStreamReaderThreadCount++, this);
            _errorGobbler.start();
            _outputGobbler.start();

            try {
                _process.waitFor();

                synchronized (this) {
                    _process = null;
                }
            } catch (InterruptedException interrupted) {
                // Ignored
            }

            outputString = _outputGobbler.getAndReset();
            errorString = _errorGobbler.getAndReset();

            if (_debugging) {
                _debug("Exec: Error: '" + errorString + "'");
                _debug("Exec: Output: '" + outputString + "'");
            }
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Problem setting up command '" + command + "'\n"
                            + outputString + "\n" + errorString);
        }

        System.out.print(outputString);
        System.err.print(errorString);
    }

    // Terminate the process and close any associated streams.
    private void _terminateProcess() {
        if (_process != null) {
            _process.destroy();
            _process = null;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    // Private class that reads a stream in a thread and updates the
    // stringBuffer.
    private class _StreamReaderThread extends Thread {
        /** Create a _StreamReaderThread.
         *  @param inputStream The stream to read from.
         *  @param name The name of this StreamReaderThread,
         *  which is useful for debugging.
         *  @param actor The parent actor of this thread, which
         *  is used in error messages.
         */
        _StreamReaderThread(InputStream inputStream, String name, Nameable actor) {
            super(name);
            _inputStream = inputStream;
            _inputStreamReader = new InputStreamReader(_inputStream, java.nio.charset.Charset.defaultCharset());
            _actor = actor;
            _stringBuffer = new StringBuffer();
        }

        /** Read any remaining data in the input stream and return the
         *  data read thus far.  Calling this method resets the
         *  cache of data read thus far.
         */
        public String getAndReset() {
            if (_debugging) {
                try {
                    _debug("getAndReset: Gobbler '" + getName() + "' Ready: "
                            + _inputStreamReader.ready() + " Available: "
                            + _inputStream.available());
                } catch (Exception ex) {
                    throw new InternalErrorException(ex);
                }
            }

            try {
                // Read any remaining data.
                _read();
            } catch (Throwable throwable) {
                if (_debugging) {
                    _debug("WARNING: getAndReset(): _read() threw an "
                            + "exception, which we are ignoring.\n"
                            + throwable.getMessage());
                }
            }

            String results = _stringBuffer.toString();
            _stringBuffer = new StringBuffer();

            try {
                _inputStreamReader.close();
            } catch (Exception ex) {
                throw new InternalErrorException(null, ex, getName()
                        + " failed to close.");
            }

            return results;
        }

        /** Read lines from the inputStream and append them to the
         *  stringBuffer.
         */
        @Override
        public void run() {
            _read();
        }

        // Read from the stream until we get to the end of the stream
        private void _read() {
            // We read the data as a char[] instead of using readline()
            // so that we can get strings that do not end in end of
            // line chars.
            char[] chars = new char[80];
            int length; // Number of characters read.

            try {
                // Oddly, InputStreamReader.read() will return -1
                // if there is no data present, but the string can still
                // read.
                while ((length = _inputStreamReader.read(chars, 0, 80)) != -1
                        && !_stopRequested && !_stopFireRequested) {
                    if (_debugging) {
                        // Note that ready might be false here since
                        // we already read the data.
                        _debug("_read(): Gobbler '" + getName() + "' Ready: "
                                + _inputStreamReader.ready() + " Value: '"
                                + String.valueOf(chars, 0, length) + "'");
                    }

                    _stringBuffer.append(chars, 0, length);
                }
            } catch (Throwable throwable) {
                throw new InternalErrorException(_actor, throwable, getName()
                        + ": Failed while reading from " + _inputStream);
            }
        }

        // The actor associated with this stream reader.
        private Nameable _actor;

        // StringBuffer to update.
        private StringBuffer _stringBuffer;

        // Stream from which to read.
        private InputStream _inputStream;

        // Stream from which to read.
        private InputStreamReader _inputStreamReader;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    // StreamReader with which we read stderr.
    private _StreamReaderThread _errorGobbler;

    // Instance count of output and error threads, used for debugging.
    // When the value is greater than 1000, we reset it to 0.
    private static int _keystoreStreamReaderThreadCount = 0;

    // Set to true if either the keyStoreType or provider attribute changed
    // and _keyStore needs to be updated.
    private boolean _initializeKeyStoreNeeded = true;

    // StreamReader with which we read stdout.
    private _StreamReaderThread _outputGobbler;

    // The Process that we are running.
    private Process _process;

    // Indicator that stopFire() has been called.
    private boolean _stopFireRequested = false;
}
