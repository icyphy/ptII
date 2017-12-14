/* Encrypt an unsigned byte array using a symmetric algorithm.

 Copyright (c) 2003-2016 The Regents of the University of California.
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
package ptolemy.actor.lib.security;

import java.io.ByteArrayOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// SymmetricCrypto

/**
 Encrypt or decrypt an unsigned byte array using a symmetric cryptography.

 @author Hokeun Kim
 @version $Id$
 @since Ptolemy II 11.0
 @Pt.ProposedRating Green (hokeunkim)
 @Pt.AcceptedRating Yellow (hokeunkim)
 */
public class SymmetricCrypto extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SymmetricCrypto(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        error = new TypedIOPort(this, "error", false, true);
        error.setTypeEquals(BaseType.STRING);

        operationMode = new StringParameter(this, "operationMode");
        operationMode.addChoice("encrypt");
        operationMode.addChoice("decrypt");
        operationMode.setExpression("encrypt");

        algorithm = new StringParameter(this, "algorithm");
        algorithm.addChoice("AES");
        algorithm.addChoice("DES");
        algorithm.setExpression("AES");

        cipherMode = new StringParameter(this, "cipherMode");
        cipherMode.addChoice("CBC");
        cipherMode.addChoice("CFB");
        cipherMode.setExpression("CBC");

        padding = new StringParameter(this, "padding");
        padding.addChoice("NoPadding");
        padding.addChoice("PKCS5Padding");
        padding.setExpression("PKCS5Padding");

        macAlgorithm = new StringParameter(this, "macAlgorithm");
        macAlgorithm.addChoice("None");
        macAlgorithm.addChoice("MD5");
        macAlgorithm.addChoice("SHA-1");
        macAlgorithm.addChoice("SHA-256");
        macAlgorithm.setExpression("SHA-256");

        key = new PortParameter(this, "key");
        key.setExpression(
                "{0ub,0ub,0ub,0ub,0ub,0ub,0ub,0ub,0ub,0ub,0ub,0ub,0ub,0ub,0ub,0ub}");
        key.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The key to be used by this actor to encrypt/decrypt the data.
     */
    public PortParameter key;

    /** The operation mode of the cipher, either encrypt or decrypt.
     */
    public StringParameter operationMode;

    /** The cryptography algorithm to be used for this actor.
     */
    public StringParameter algorithm;

    /** The cipher mode to be used for the cipher.
     */
    public StringParameter cipherMode;

    /** The padding to be used for the cipher.
     */
    public StringParameter padding;

    /** The secure hash algorithm for message authentication code (MAC)
     *  to be used for this actor.
     */
    public StringParameter macAlgorithm;

    /** The input data (for encrypt mode) or encrypted data (for decrypt mode).
     */
    public TypedIOPort input;

    /** The encrypted data (for encrypt mode) or decrypted data (for decrypt mode).
     */
    public TypedIOPort output;

    /** The error (e.g. integrity check error for MAC)
     */
    public TypedIOPort error;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to reinitialize the state if
     *  the <i>operationMode</i>, <i>algorithm</i>, <i>cipherMode</i>, or <i>padding</i>
     *  parameter is changed.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == operationMode) {
            String modeString = ((StringToken) operationMode.getToken())
                    .stringValue();
            if (modeString.equals("encrypt")) {
                _isEncryption = true;
            } else {
                _isEncryption = false;
            }
        } else if (attribute == algorithm) {
            _algorithm = ((StringToken) algorithm.getToken()).stringValue();
        } else if (attribute == cipherMode) {
            _cipherMode = ((StringToken) cipherMode.getToken()).stringValue();
        } else if (attribute == padding) {
            _padding = ((StringToken) padding.getToken()).stringValue();
        } else if (attribute == macAlgorithm) {
            // initialize() is synchronized, so accessing _macAlgorithm better be synchronized.
            synchronized (this) {
                _macAlgorithm = ((StringToken) macAlgorithm.getToken())
                        .stringValue();
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** If there is a token on the <i>input</i> port, this method takes the
     *  data from the <i>input</i> and encrypts the data based on the
     *  <i>algorithm</i>, <i>provider</i>, <i>mode</i> and <i>padding</i>
     *  using the key read in from the <i>key</i> port.
     *  This processed data is then sent on the <i>output</i> port.
     *  All parameters should be the same as the corresponding
     *  decryption actor.  This method calls javax.crypto.Cipher.init()
     *  with the value of the <i>key</i>.
     *
     *  @exception IllegalActionException If thrown by base class.
     */
    @Override
    public void fire() throws IllegalActionException {
        // AES block size - 128bit = 16byte
        // DES block size - 64bit - 8byte

        int blockSize = 0;

        if (_algorithm.equals("AES")) {
            blockSize = 16;
        } else if (_algorithm.equals("DES")) {
            blockSize = 8;
        }
        byte[] encryptedBytes;
        byte[] initVector;
        if (_isEncryption) {
            int opmode = Cipher.ENCRYPT_MODE;

            // generate initialization vector
            SecureRandom random = new SecureRandom();
            byte seed[] = random.generateSeed(blockSize);
            random.setSeed(seed);
            initVector = new byte[blockSize];
            random.nextBytes(initVector);

            IvParameterSpec ivspec = new IvParameterSpec(initVector);
            byte[] keyBytes = ArrayToken
                    .arrayTokenToUnsignedByteArray((ArrayToken) key.getToken());
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes,
                    _algorithm);

            _initCipher(opmode, ivspec, secretKeySpec);
            super.fire(); // super.fire() will print out debugging messages.

            try {
                if (input.hasToken(0)) {
                    byte[] dataBytes = ArrayToken.arrayTokenToUnsignedByteArray(
                            (ArrayToken) input.get(0));

                    if (_messageDigest != null) {
                        byte[] digestedBytes = _messageDigest.digest(dataBytes);

                        dataBytes = _concatByteArrays(dataBytes, digestedBytes);
                    }

                    dataBytes = _process(dataBytes);
                    byte[] outputBytes = _concatByteArrays(initVector,
                            dataBytes);
                    output.send(0, ArrayToken
                            .unsignedByteArrayToArrayToken(outputBytes));
                }
            } catch (Throwable throwable) {
                throw new IllegalActionException(this, throwable,
                        "Problem sending data");
            }
        } else {
            int opmode = Cipher.DECRYPT_MODE;

            if (input.hasToken(0)) {
                byte[] inputBytes = ArrayToken.arrayTokenToUnsignedByteArray(
                        (ArrayToken) input.get(0));

                initVector = Arrays.copyOfRange(inputBytes, 0, blockSize);
                encryptedBytes = Arrays.copyOfRange(inputBytes, blockSize,
                        inputBytes.length);

                IvParameterSpec ivspec = new IvParameterSpec(initVector);
                byte[] keyBytes = ArrayToken.arrayTokenToUnsignedByteArray(
                        (ArrayToken) key.getToken());
                SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes,
                        _algorithm);

                _initCipher(opmode, ivspec, secretKeySpec);
                super.fire(); // super.fire() will print out debugging messages.

                byte[] decryptedBytes = _process(encryptedBytes);

                if (_messageDigest == null) {
                    output.send(0, ArrayToken
                            .unsignedByteArrayToArrayToken(decryptedBytes));
                } else {
                    if (decryptedBytes.length < _messageDigest
                            .getDigestLength()) {
                        error.send(0, new StringToken(
                                "Decrypted message is shorter than MAC"));
                    } else {
                        byte[] dataBytes = Arrays.copyOfRange(decryptedBytes, 0,
                                decryptedBytes.length
                                        - _messageDigest.getDigestLength());
                        byte[] receivedDigestBytes = Arrays.copyOfRange(
                                decryptedBytes,
                                decryptedBytes.length
                                        - _messageDigest.getDigestLength(),
                                decryptedBytes.length);
                        byte[] digestedBytes = _messageDigest.digest(dataBytes);
                        if (Arrays.equals(receivedDigestBytes, digestedBytes)) {
                            output.send(0, ArrayToken
                                    .unsignedByteArrayToArrayToken(dataBytes));
                        } else {
                            error.send(0, new StringToken(
                                    "Data integrity error, MAC doesn't match"));
                        }
                    }
                }

            }
        }
    }

    /** Override the base class to initialize the index.
     *  @exception IllegalActionException If the parent class throws it,
     *   or if the <i>values</i> parameter is not a row vector, or if the
     *   fireAt() method of the director throws it, or if the director does not
     *   agree to fire the actor at the specified time.
     */
    @Override
    public synchronized void initialize() throws IllegalActionException {
        // FIXME: Why is initialize() synchronized here?
        super.initialize();
        if (_macAlgorithm.equals("None")) {
            _messageDigest = null;
        } else {
            try {
                _messageDigest = MessageDigest.getInstance(_macAlgorithm);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalActionException(this, e,
                        "Failed to initialize messageDigest");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                    Private Variables                    ////

    /** The cipher that will be used to process the data.
     */
    private Cipher _cipher;
    /** The name of the algorithm to be used. */
    private String _algorithm;
    /** The name of the cipher mode to be used. */
    private String _cipherMode;
    /** The name of the padding to be used. */
    private String _padding;
    /** The name of the MAC algorithm to be used. */
    private String _macAlgorithm;
    /** The message authentication code (MAC) object that will be used to process the data.
     */
    private MessageDigest _messageDigest;
    /** Whether encrypt or decrypt the input data. */
    private boolean _isEncryption;

    ///////////////////////////////////////////////////////////////////
    ////                    Private  Methods                      ////

    /** Encrypt the data using the javax.crypto.Cipher.
     *
     * @param dataBytes the data to be encrypted.
     * @return byte[] the encrypted data.
     * @exception IllegalActionException If error occurs in
     * ByteArrayOutputStream, if the key is invalid, if the padding is bad
     * or if the block size is illegal.
     */
    private byte[] _process(byte[] dataBytes) throws IllegalActionException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            byteArrayOutputStream.write(_cipher.doFinal(dataBytes));
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Problem processing " + dataBytes.length + " bytes.");
        }

        return byteArrayOutputStream.toByteArray();
    }

    /** Initialize the javax.crypto.Cipher.
    */
    private void _initCipher(int opmode, IvParameterSpec ivspec,
            SecretKeySpec secretKeySpec) throws IllegalActionException {
        try {
            _cipher = Cipher.getInstance(
                    _algorithm + "/" + _cipherMode + "/" + _padding);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e1) {
            throw new IllegalActionException(this, e1,
                    "Failed to get instance of cipher");
        }

        try {
            _cipher.init(opmode, secretKeySpec, ivspec);
        } catch (InvalidKeyException e) {
            throw new IllegalActionException(this, e,
                    "Failed to initialize crypto");
        } catch (InvalidAlgorithmParameterException e) {

            throw new IllegalActionException(this, e,
                    "Failed to initialize crypto");
        }
    }

    private byte[] _concatByteArrays(byte[] firstArray, byte[] secondArray) {
        byte[] resultArray = Arrays.copyOf(firstArray,
                firstArray.length + secondArray.length);
        System.arraycopy(secondArray, 0, resultArray, firstArray.length,
                secondArray.length);
        return resultArray;
    }
}
