/* Creates and sends a key to an encryptor and decrypts incoming data.

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

@ProposedRating Yellow (rnreddy@andrew.cmu.edu)
@AcceptedRating Red (ptolemy@ptolemy.eecs.berkeley.edu)
*/

package ptolemy.actor.lib.security;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.ArrayToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.UnsignedByteToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

//////////////////////////////////////////////////////////////////////////
//// Decryption
/**

This actor takes an unsigned byte array at the input and decrypts the
message.  The resulting output is an unsigned byte array. Various
ciphers that are implemented by "providers" and installed maybe used
by specifying the algorithm in the algorithm parameter.  The mode and
padding can also be specified in the mode and padding parameters.  In
case a provider specific instance of an algorithm is needed the
provider may also be specified in the provider parameter.  The mode
parameter must also be set to asymmetric or symmetric depending on the
specified algorithm.  When in an asymmetric mode, this actor creates a
private key for decryption use and a public key which is sent on the
<i>keyOut</i> port to an encryption actor for encryption purposes.  In
symmetric mode, the share secret key is sent on the <i>keyOut</i> port
to an encyption actor for encryption purposes.  Key creation is done
in preinitialization and is put on the keyOut port during initialization
so the encryption has a key to use when its first fired.

<p>This actor relies on the Java Cryptography Architecture (JCA) and Java
Cryptography Extension (JCE).

<br>Information about JCA can be found at
<a href="http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html" target="_top">http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html">.
<br>Information about JCE can be found at
<a href="http://java.sun.com/products/jce/" target="_top">http://java.sun.com/products/jce/">.

@author Rakesh Reddy, Christopher Hylands Brooks
@version $Id$
@since Ptolemy II 3.1
*/
public class Decryption extends TypedAtomicActor {

    // TODO: include sources of information on JCE cipher and algorithms
    // TODO: split asymmetric and symmetric
    // TODO: make encryption create key for symmetric(main reason for above)
    // TODO: Send keys as ObjectOutputStreams
    // TODO: Use cipher streaming to allow for easier file input reading.


    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Decryption(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        keyOut = new TypedIOPort(this, "keyOut", false, true);
        keyOut.setTypeEquals(BaseType.OBJECT);

        algorithm = new Parameter(this, "algorithm");
        algorithm.setTypeEquals(BaseType.STRING);
        algorithm.setExpression("\"RSA\"");

        provider = new Parameter(this, "provider");
        provider.setTypeEquals(BaseType.STRING);
        provider.setExpression("\"\"");

        cryptoMode = new StringAttribute(this, "cryptoMode");
        cryptoMode.setExpression("\"asymmetric\"");
        _keyMode = _ASYMMETRIC;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Specifies the algorithm of the cipher to use for decrypting
     *  the data and making the key.  The algorithm is specified as a
     *  string input. The algorithms are limited to those implemented
     *  by providers using the Java JCE which are found on the system.
     */
    public Parameter algorithm;

    /** Used to specify a provider for the given algorithm.  Takes the
     *  algorithm name in a string format. If left blank the system
     *  chooses the provider based the JCE architecture.
     */
    public Parameter provider;

    /** The padding scheme used by the cipher during encryption.  The
     *  padding is specified as a string and includes the options of
     *  NOPADDING and PKCS#5.  This should be the same as the
     *  encryption actor.  If left blank the default setting for the
     *  algorithm is used.
     */
    public Parameter padding;

    /** The mode of the block cipher that was for used encryption.
     *  The mode is specified as a string and includes ECB, CBC, CFB
     *  and OFB.  The modes are limited by their implementation in the
     *  provider library.  If left blank the default setting for the
     *  algorithm used.  This should be the same as the mode used for
     *  in the corresponding encryption actor.
     */
    public Parameter mode;

    /** Specifies the size of the key to be created.  This is an int
     *  value representing the number of bits.  A default value exists
     *  for certain algorithms.  If a key is not specified and no
     *  default value is found then an error is produced.
     */
    public Parameter keySize;

    /** This StringAttribute determines whether decryption will be
     *  performed on a asymmetric or symmetric crytographic algorithm.
     *  The to possible values are "symmetric" or "asymmetric".  THe
     *  default value is "asymmetric."
     */
    public StringAttribute cryptoMode;

    /** This port takes in as an UnsignedByteArray and decrypts the
     *  data based on the algorithm.
     */
    public TypedIOPort input;

    /** This port sends out the decrypted data received from
     *  <i>input</i> in the form of a UnsignedByteArray.
     */
    public TypedIOPort output;

    /** This port outputs the key to be used by the encryption actor.
     *  If set to asymmetric encryption the generated public key is
     *  sent out.  Otherwise the generated secret key is sent out.
     */
    public TypedIOPort keyOut;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Determine whether to use asymmetric or symmetric decryption.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If naming conflict is encountered.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == cryptoMode) {
            String function = cryptoMode.getExpression();

            if (function.equals("asymmetric")) {
                _keyMode = _ASYMMETRIC;
            } else if (function.equals("symmetric")) {
                _keyMode = _SYMMETRIC;
            }
        } else super.attributeChanged(attribute);
    }

    /** This method takes the data from the <i>input</i> and decrypts
     *  the data based on the <i>algorithm</i>, <i>provider</i>,
     *  <i>mode</i> and <i>padding</i>.  This is the sent on the
     *  <i>output</i>.  All parameters should be the same as the
     *  corresponding encryption actor.
     *
     *  @exception IllegalActionException If decryption fails or the
     *  base class throws it.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        try {
            if (input.hasToken(0)) {
                byte[] dataBytes =
                    _ArrayTokenToUnsignedByteArray((ArrayToken)input.get(0));
                dataBytes = _crypt(dataBytes);
                output.send(0, _unsignedByteArrayToArrayToken(dataBytes));
            }

            if (_keyMode == _ASYMMETRIC) {
                keyOut.send(0, new ObjectToken(_publicKey));
            } else {
                keyOut.send(0, new ObjectToken(_secretKey));
            }

        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Problem firing");
        }

    }

    /** Get an instance of the cipher and outputs the key required for
     *  decryption.
     *  @exception IllegalActionException If the algorithm is not found,
     *  the padding scheme is illegal or if the provider does not exist
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        try {
            _cipher = Cipher.getInstance(_algorithm, _provider);

        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to initialize Cipher with algorithm: '"
                    + _algorithm + "', provider: '"
                    + _provider + "'");
        }

        if (_keyMode == _ASYMMETRIC) {
            keyOut.send(0, new ObjectToken(_publicKey));
        } else {
            keyOut.send(0, new ObjectToken(_secretKey));
        }
    }

    /** Get parameter information and sets token production for
     *  initialize to one.
     *
     * @exception IllegalActionException If the base class throws it
     * or the keys cannot be created. 
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        _algorithm = ((StringToken)algorithm.getToken()).stringValue();
        byte[] keyBytes = _createKeys();
        //_keyOutput = _unsignedByteArrayToArrayToken(keyBytes);
        getDirector().invalidateResolvedTypes();
    }



    ///////////////////////////////////////////////////////////////////
    ////                         Protected Methods                 ////

    /** Take an ArrayToken and converts it to an array of unsigned bytes.
     *
     * @param dataArrayToken
     * @return dataBytes
     */
    protected byte[] _ArrayTokenToUnsignedByteArray(
            ArrayToken dataArrayToken) {
        byte[] dataBytes = new byte[dataArrayToken.length()];
        for (int j = 0; j < dataArrayToken.length(); j++) {
            UnsignedByteToken dataToken =
                (UnsignedByteToken)dataArrayToken.getElement(j);
            dataBytes[j] = (byte)dataToken.byteValue();
        }
        return dataBytes;
    }

    /** Create a pair of keys to be used for asymmetric encryption and
     *  decryption.
     *
     * @exception IllegalActionException If the algorithm or provider is
     * not found and the key cannot be created.
     */
    protected void _createAsymmetricKeys()throws IllegalActionException {
        try {
            KeyPairGenerator keyPairGen =
                KeyPairGenerator.getInstance(_algorithm, _provider);
            keyPairGen.initialize(1024, new SecureRandom());
            KeyPair pair = keyPairGen.generateKeyPair();
            _publicKey = pair.getPublic();
            _privateKey = pair.getPrivate();
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to create asymmetric keys");
        }
    }

    /** Determine what kind of keys to create and calls the respective
     *  function.
     *
     * @exception IllegalActionException If there are problems creating
     * the keys.
     */
    protected byte[] _createKeys() throws IllegalActionException {
        switch(_keyMode) {
        case _ASYMMETRIC:
            _createAsymmetricKeys();
            //_cipher.init(Cipher.WRAP_MODE, _privateKey);
            //return _cipher.wrap(_publicKey);
            return _publicKey.getEncoded();
        case _SYMMETRIC:
            _createSymmetricKey();
            return _secretKey.getEncoded();
            //_cipher.init(Cipher.WRAP_MODE, _secretKey);
            //return _cipher.wrap(_secretKey);
        }
        return null;
    }

    /** Create a symmetric secret key for encryption and decryption use.
     *
     * @exception IllegalActionException If the symmetric key could not
     * be created.
     */
    protected void _createSymmetricKey() throws IllegalActionException{
        try {
            KeyGenerator keyGen =
                KeyGenerator.getInstance(_algorithm, _provider);
            keyGen.init(56, new SecureRandom());
            _secretKey = keyGen.generateKey();
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to create symmetric keys");
        }
    }

    /** Determine what key to use and decrypt the data with the specified
     *  key.  Receives the data to be encrypted in as a byte array and returns
     *  a byte array.
     *
     * @param initialData
     * @return byte[]
     * @exception IllegalActionException If an error occurs in
     * ByteArrayOutputStream, a key is invalid, padding is bad, or if
     * the block size is illegal.
     */
    protected byte[] _crypt(byte[] initialData) throws IllegalActionException{
        ByteArrayOutputStream byteArrayOutputStream =
            new ByteArrayOutputStream();
        Key key;
        try {
            if (_keyMode == _SYMMETRIC) {
                key = _secretKey;
                _cipher.init(Cipher.DECRYPT_MODE, key);
                byteArrayOutputStream.write(_cipher.doFinal(initialData));
            } else if (_keyMode == _ASYMMETRIC) {
                key = _privateKey;
                _cipher.init(Cipher.DECRYPT_MODE, key);
                int blockSize = _cipher.getBlockSize();
                int length = 0;
                for (int i = 0; i < initialData.length; i += blockSize) {
                    if (initialData.length-i <= blockSize) {
                        length = initialData.length-i;
                    } else {
                        length = blockSize;
                    }
                    byteArrayOutputStream.write(_cipher.doFinal(initialData,
                            i, length));
                }
                byteArrayOutputStream.flush();
                byteArrayOutputStream.close();
            }
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Problem decrypting " + initialData.length
                    + "bytes data with the private key.");
        }
        return byteArrayOutputStream.toByteArray();
    }

    /** Take an array of unsigned bytes and convert it to an ArrayToken.
     *
     * @param dataBytes The array of unsigned bytes to convert
     * @return The dataBytes converted to and ArrayToken
     * @exception IllegalActionException If instantiating the ArrayToken
     * throws it.
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
    ////                         private variables                 ////


    /** Set for asymmetric encryption.
     */
    private static final int _ASYMMETRIC = 0;

    /** Set for asymmetric encryption.
     */
    private static final int _SYMMETRIC = 1;

    /** Set to _ASYMMETRIC or _SYMMETRIC depending on the <i>cryptoMode</i>
     *  parameter.
     */
    private int _keyMode;

    /** The algorithm to be used for decryption specified by the
     * <i>algorithm</i> parameter.
     */
    private String _algorithm;

    /** The public key to be used for asymmetric encryption. This key is null
     *  for symmetric decryption.
     */
    private PublicKey _publicKey = null;

    /** The private key to be used for asymmetric decryption.  This key is null
     *  for symmetric decryption.
     */
    private PrivateKey _privateKey = null;

    /** The secret key to be used for symmetric encryption and decryption.
     *  This key is null for asymmetric decryption.
     */
    private SecretKey _secretKey = null;

    /** Cipher that performs decryption on data
     */
    private Cipher _cipher;

    //private ArrayToken _keyOutput;

    /** The name of the provider to be used in determining which
     *  instance of an algorithm to provide.
     */
    private String _provider = "BC";
}
