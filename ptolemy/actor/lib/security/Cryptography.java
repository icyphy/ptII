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


import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.UnsignedByteToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.kernel.SDFIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Cryptography
/**

This a base class that implements general and helper functions used by
cryptographic actors. Actors extending this class take in an unsigned
byte array at the <i>input</i>, perform the transformation specified
in the <i>algorithm</i> parameter and send a unsigned byte array on
the <i>output</i>.  The algorithms that maybe implemented are limited
to the ciphers that are implemented by "providers" following the JCE
specifications and installed in the machine being run. The mode and
padding can also be specified in the mode and padding parameters.  In
case a provider specific instance of an algorithm is needed, the
provider may also be specified in the <i>provider</i> parameter.  This
class takes care of basic initialization of the subclasses. The
<i>keySize</i> also allows implementations of algorithms with various
key sizes.

<p>This class and its subclasses rely on the Java Cryptography Extension (JCE)
and Java Cryptography Architecture(JCA).

@author Rakesh Reddy
@version $Id$
@since Ptolemy II 3.1
*/
public class Cryptography extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Cryptography(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        input = new SDFIOPort(this, "input", true, false);
        input.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        output = new SDFIOPort(this, "output", false, true);
        output.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        algorithm = new Parameter(this, "algorithm");
        algorithm.setTypeEquals(BaseType.STRING);
        algorithm.setExpression("");

        provider = new Parameter(this, "provider");
        provider.setTypeEquals(BaseType.STRING);
        provider.setExpression("");

        mode = new Parameter(this, "mode");
        mode.setTypeEquals(BaseType.STRING);
        mode.setExpression("");

        padding = new Parameter(this, "padding");
        padding.setTypeEquals(BaseType.STRING);
        padding.setExpression("");

        keySize = new Parameter(this, "keySize");
        keySize.setTypeEquals(BaseType.INT);
        keySize.setExpression("0");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Specifies the algorithm of the cipher to use for decrypting the data
     *  and making the key.  The algorithm is specified as a string input. The
     *  algorithms are limited to those implemented by providers using the Java
     *  JCE which are found on the system.
     */
    public Parameter algorithm;

    /** Used to specify a provider for the given algorithm.  Takes the
     *  algorithm name in a string format. If left blank the system chooses the
     *  provider based the JCE architecture.
     */
    public Parameter provider;

    /** The padding scheme used by the cipher during encryption.  The padding
     *  is specified as a string and includes the options of NOPADDING and
     *  PKCS#5.  This should be the same as the encryption actor.  If left
     *  blank the default setting for the algorithm is used.
     */
    public Parameter padding;

    /** The mode of the block cipher that was for used encryption.  The mode is
     *  specified as a string and includes ECB, CBC, CFB and OFB.  The modes
     *  are limited by their implementation in the provider library.  If left
     *  blank, the default setting for the algorithm used.  This should be the
     *  same as the mode used for in the corresponding encryption actor.
     */
    public Parameter mode;

    /** Specifies the size of the key to be created.  This is an int value
     *  representing the number of bits.  A default value exists for certain
     *  algorithms.  If a key is not specified and no default value is found
     *  then an error is produced.
     */
    public Parameter keySize;


    /** This port takes in as an UnsignedByteArray and performs a
     *  transformation on the data.
     */
    public SDFIOPort input;

    /** This port sends out the transformed data received from <i>input</i> in
     *  the form of an UnsignedByteArray.
     */
    public SDFIOPort output;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** This method takes the data from the <i>input</i> and transforms the
     *  data based on the <i>algorithm</i>, <i>provider</i>, <i>mode</i> and
     *  <i>padding</i>.  This is the sent on the <i>output</i>.  All parameters
     *  should be the same as the corresponding encryption or decryption
     *  actor.
     * @exception IllegalActionException If thrown by the base class.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        try{
            if (input.hasToken(0)) {
                byte[] dataBytes =
                    _arrayTokenToUnsignedByteArray((ArrayToken)input.get(0));
                dataBytes = _crypt(dataBytes);
                output.send(0, _unsignedByteArrayToArrayToken(dataBytes));
            }
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex, "Problem sending data");
        }

    }

    /** This method retrieves the <i>algorithm</i>, <i>provider</i>,
     *  <i>mode</i>, <i>keySize</i> and <i>padding</i>.  The cipher is also
     *  initialized.
     *
     * @exception IllegalActionException If the base class throws it, or
     * if the algorithm is not found, the padding scheme is illegal
     * for a given algorithm or if the specified provider does not exist.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _algorithm = ((StringToken)algorithm.getToken()).stringValue();
        _provider = ((StringToken)provider.getToken()).stringValue();
        _padding = ((StringToken)padding.getToken()).stringValue();
        _mode = ((StringToken)mode.getToken()).stringValue();
        _keySize = ((IntToken)keySize.getToken()).intValue();
        if (_keySize == 0) {
            _defaultKeySize();
        }

        try{
            _cipher =
                Cipher.getInstance(_algorithm+"/"+mode+"/"+padding, _provider);
        } catch (Exception ex)
            throw new IllegalActionException(ex, this,
                    "Failed to initialize Cipher with algorithm: '"
                    + _algorithm + "', padding: '"
                    + _padding + "', provider: '"
                    + _provider + "'");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         Protected Methods                 ////

    /** Take an ArrayToken and converts it to an array of unsigned bytes.
     *
     * @param dataArrayToken to be converted to a unsigned byte array.
     * @return dataBytes the resulting unsigned byte array.
     */
    protected byte[] _arrayTokenToUnsignedByteArray(
            ArrayToken dataArrayToken) {
        byte[] dataBytes = new byte[dataArrayToken.length()];
        for (int j = 0; j < dataArrayToken.length(); j++) {
            UnsignedByteToken dataToken =
                (UnsignedByteToken)dataArrayToken.getElement(j);
            dataBytes[j] = (byte)dataToken.byteValue();
        }
        return dataBytes;
    }

    /** Determine what key to use and transforms the data with the
     *  specified key.  Receives the data to be encrypted in as a byte
     *  array and returns a byte array.  The base class simply returns
     *  the data but all base classes should implement this method.
     *
     * @param initialData data to be transformed.
     * @return byte[] the transformed data.
     * @exception IllegalActionException If thrown in the base class
     */
    protected byte[] _crypt(byte[] initialData)throws IllegalActionException{
        return initialData;
    }

    /** If a key is not specified in the parameter, this function
     *  checks for a default key size. If the specified algorithm is
     *  found in the list below then the key size is set to this
     *  value.  If the algorithm is not found in this list then and
     *  exception is thrown.
     *
     * @exception IllegalActionException If a key size is not
     * specified and the does not exist in the list below.
     */
    protected void _defaultKeySize() throws IllegalActionException {
        if (_algorithm.equals("Blowfish")) {
            // valid values are: starting from 40bit up to
            // 448 in 8-bit increments
            _keySize = 448;
        } else if (_algorithm.equals("CAST5")) {
            //valid values are: starting from 40bit up to 128bit using
            // 8bit steps.
            _keySize = 128;
        } else if (_algorithm.equals("DES")) {
            _keySize = 56;
        } else if (_algorithm.equals("TripleDES")
                ||_algorithm.equals("DESede")) {
            _keySize = 3*56;
        } else if (_algorithm.equals("Rijndael")) {
            _keySize = 256; //valid values are: 128, 192, 256
        } else if (_algorithm.equals("SKIPJACK")) {
            // fixed size: 80 bits
            _keySize = 80;
        } else if (_algorithm.equals("Square")) {
            _keySize = 128;
        } else{
            throw new IllegalActionException(this, null
                    "No default key size found, "
                    + "the key size must be specified in parameters. "
                    + "The algorithm was '" + _algorithm + "'");
        }
    }

    /** Take an array of unsigned bytes and convert it to an ArrayToken.
     *
     * @param dataBytes data to be converted to an ArrayToken.
     * @return dataArrayToken the resulting ArrayToken.
     * @exception IllegalActionException If the ArrayToken can not be created.
     */
    protected ArrayToken _unsignedByteArrayToArrayToken(byte[] dataBytes)
            throws IllegalActionException {
        int bytesAvailable = dataBytes.length;
        Token[] dataArrayToken = new Token[bytesAvailable];
        for (int j = 0; j < bytesAvailable; j++) {
            dataArrayToken[j] = new UnsignedByteToken(dataBytes[j]);
        }
        return new ArrayToken(dataArrayToken);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // The name of the algorithm to be used.
    protected String _algorithm;

    // The key size to be used for the transformation.
    protected int _keySize;

    // The mode for transformation.
    protected String _mode;

    //The padding for transformation.
    protected String _padding;

    // The provider for transformation.
    protected String _provider;

    // The cipher to be used for transformation.
    protected Cipher _cipher;
}
