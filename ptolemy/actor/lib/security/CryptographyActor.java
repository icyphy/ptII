/* A base class for cryptographic actors.

 Copyright (c) 2003 The Regents of the University of California.
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

@ProposedRating Yellow (rnreddy@ptolemy.eecs.berkeley.edu)
@AcceptedRating Red (ptolemy@ptolemy.eecs.berkeley.edu)
*/

package ptolemy.actor.lib.security;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.UnsignedByteToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Iterator;
import java.util.Set;

import javax.crypto.KeyGenerator;


//////////////////////////////////////////////////////////////////////////
//// CryptographyActor
/**

This is a base class that implements general and helper functions used
by cryptographic actors. Actors extending this class take in an
unsigned byte array at the <i>input</i>, process the the data based on
the <i>algorithm</i> parameter and send an unsigned byte array on the
<i>output</i>.  These transformations include ciphers and signatures
implemented by JCE.  The algorithms that maybe implemented are limited
those that are implemented by "providers" following the JCE
specifications and installed on the machine being run.  In case a
provider specific instance of an algorithm is needed, the provider may
be specified in the <i>provider</i> parameter.  This class takes care
of basic initialization of the subclasses. The <i>keySize</i> also
allows implementations of algorithms using various key sizes.

<p>Actors derived from this baseclass usually have a 
process(byte[] dataBytes) method that processes the data appropriately.
This method is called by CryptographyActor.fire().

<p>This actor relies on the Java Cryptography Architecture (JCA) and Java
Cryptography Extension (JCE).

<br>Information about JCA can be found at
<a href="http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html" target="_top">http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html">.
<br>Information about JCE can be found at
<a href="http://java.sun.com/products/jce/" target="_top">http://java.sun.com/products/jce/">.

<br>The Java 1.2 security tutorial can be found at
<a href="http://java.sun.com/docs/books/tutorial/security1.2/index.html" target="_top">http://java.sun.com/docs/books/tutorial/security1.2/index.html</a>

<br>Another Java security tutorial can be found at
<a href="http://developer.java.sun.com/developer/onlineTraining/Security/Fundamentals/index.html" target="_top">http://developer.java.sun.com/developer/onlineTraining/Security/Fundamentals/index.html</a>

@author Rakesh Reddy, Christopher Hylands Brooks
@version $Id$
@since Ptolemy II 3.1
*/
public class CryptographyActor extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CryptographyActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        algorithm = new StringParameter(this, "algorithm");
        Set algorithms = Security.getAlgorithms("Cipher");
        Iterator algorithmsIterator = algorithms.iterator();
        for(int i = 0; algorithmsIterator.hasNext(); i++) {
            String algorithmName = (String)algorithmsIterator.next();
            if (i == 0) {
                algorithm.setExpression(algorithmName);
            }
            algorithm.addChoice(algorithmName);
        }

        provider = new StringParameter(this, "provider");
        provider.setExpression("SystemDefault");
        provider.addChoice("SystemDefault");
        Provider [] providers = Security.getProviders();
        for (int i = 0; i < providers.length; i++) {
            provider.addChoice(providers[i].getName());
        }

        keySize = new Parameter(this, "keySize", new IntToken(1024));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Specify the algorithm to be used to process data.  The algorithm is
     *  specified as a string. The algorithms are limited to those
     *  implemented by providers using the Java JCE which are found on the
     *  system.
     *  The initial default is the first value returned by
     *  Security.getAlgorithms();
     *  
     */
    public StringParameter algorithm;

    /** Specify a provider for the given algorithm.  Takes the algorithm name
     *  as a string. The default value is "SystemDefault" which allows the
     *  system chooses the provider based on the JCE architecture.
     */
    public StringParameter provider;

    /** Specify the size of the key to be created.  This is an integer value
     *  representing the number of bits in the key.  The initial default
     *  depends on the algorithm that is selected, not all algorithms use
     *  keySize.  
     *  <p>DSA is the most common algorithm that uses keySize, the Sun
     *  documentation says:
     *  "The length, in bits, of the modulus p. This must range from
     *  512 to 1024, and must be a multiple of 64. The default keysize
     *  is 1024."
     *  Refer to 
     *  <a href="http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html#AppB"><code>http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html#AppB</code></a>
     *  for a list of possible key sizes for certain algorithms.
     *  The initial default is 1024.
     */
    public Parameter keySize;

    /** This port takes in an UnsignedByteArray and processes the data.
     */
    public TypedIOPort input;

    /** This port sends out the processed data received from <i>input</i> in
     *  the form of an UnsignedByteArray.
     */
    public TypedIOPort output;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Convert an ArrayToken to an array of unsigned bytes.
     *
     * @param dataArrayToken to be converted to a unsigned byte array.
     * @return dataBytes the resulting unsigned byte array.
     */
    public static byte[] arrayTokenToUnsignedByteArray(
            ArrayToken dataArrayToken) {
        byte[] dataBytes = new byte[dataArrayToken.length()];
        for (int j = 0; j < dataArrayToken.length(); j++) {
            UnsignedByteToken dataToken =
                (UnsignedByteToken)dataArrayToken.getElement(j);
            dataBytes[j] = (byte)dataToken.byteValue();
        }
        return dataBytes;
    }


    /** Override the base class to reinitialize the state if
     *  the <i>algorithm</i>, <i>provider</i>, or <i>keysize</i>
     *  parameter is changed.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == algorithm) {
            _algorithm = ((StringToken)algorithm.getToken()).stringValue();
        } else if (attribute == provider) {
            _provider = ((StringToken)provider.getToken()).stringValue();
        } else if (attribute == keySize) {
            _keySize = ((IntToken)keySize.getToken()).intValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** This method takes the data from the <i>input</i> and processes the
     *  data based on the <i>algorithm</i>, and <i>provider</i>.  The
     *  transformed data is then sent on the <i>output</i>.
     *
     * @exception IllegalActionException If thrown by the base class.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        try {
            if (input.hasToken(0)) {
                byte[] dataBytes =
                    CryptographyActor.arrayTokenToUnsignedByteArray(
                            (ArrayToken)input.get(0));
                dataBytes = _process(dataBytes);
                output.send(0, 
                        CryptographyActor.unsignedByteArrayToArrayToken(
                                dataBytes));
            }
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Problem sending data");
        }

    }

    /** Convert a key Object to a byte array using an ObjectStream.
     *
     * @param key the object whose byte array value is determined.
     * @return the byte array of the key object.
     * @exception IllegalActionException If IOException occurs.
     */
    public static byte[] keyToBytes(Key key) throws IllegalActionException {
        ByteArrayOutputStream byteArrayOutputStream =
            new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream =
                new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(key);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalActionException(null, ex,
                    "Problem with writing key");
        }
    }

    /** This method retrieves the <i>algorithm</i>, <i>provider</i>,
     *  and, <i>keySize</i>.
     *
     * @exception IllegalActionException If the algorithm is not found,
     * the padding scheme is illegal for a given algorithm or the
     * specified provider does not exist.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _algorithm = ((StringToken)algorithm.getToken()).stringValue();
        _provider = ((StringToken)provider.getToken()).stringValue();
        _keySize = ((IntToken)keySize.getToken()).intValue();
    }

    /** Take an array of unsigned bytes and convert it to an ArrayToken.
     *
     * @param dataBytes data to be converted to an ArrayToken.
     * @return dataArrayToken the resulting ArrayToken.
     * @exception IllegalActionException If ArrayToken can not be created.
     */
    public static ArrayToken unsignedByteArrayToArrayToken( byte[] dataBytes)
            throws IllegalActionException{
        int bytesAvailable = dataBytes.length;
        Token[] dataArrayToken = new Token[bytesAvailable];
        for (int j = 0; j < bytesAvailable; j++) {
            dataArrayToken[j] = new UnsignedByteToken(dataBytes[j]);
        }
        return new ArrayToken(dataArrayToken);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         Protected Methods                 ////

    /** Processes the data based on parameter specifications.  This
     *  class returns the data in its original form.  Subclasses
     *  should process the data using one of the signature or cipher
     *  classes provided in the JCE or JCA.
     *
     * @param dataBytes the data to be processed.
     * @return dataBytes the data unchanged.
     * @exception IllegalActionException Not thrown in this base class
     */
    protected byte[] _process(byte [] dataBytes)
            throws IllegalActionException {
        return dataBytes;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The name of the algorithm to be used. */
    protected String _algorithm;

    /** The algorithm to be used for generating the key.  This is the
     * same as the _algorithm for Ciphers but needs to be specified
     * separately for Signatures
     */
    protected String _keyPairGenerator;

    /** The key size to be used when processing information. */
    protected int _keySize;

    /** The provider to be used for a provider specific implementation. */
    protected String _provider;
}
