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
import ptolemy.domains.sdf.kernel.SDFIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

//////////////////////////////////////////////////////////////////////////
//// Decryption
/**
This actor takes an unsigned byte array at the input and decrypts the message.
The resulting ouput is an unsighned byte array. Various ciphers that are
implemented by "providers" and installed maybe used by specifying the algorithm
in the algorithm parameter.  The mode and padding can also be spcecified in the
mode and padding parameters.  In case a provider specific instance of an
algorithm is needed the provider may also be specified in the provider
parameter.  The mode parameter must also be set to asymmetric or symmetric
depending on the specified algorithm.  When in an asymmetric mode, this actor
creates a private key for decyption use and a public key which is sent on the
<i>keyOut</i> port to an encyption actor for encryption purposes.  In symmetric
mode, the share secret key is sent on the <i>keyOut</i> port to an encyption
actor for encryption purposes.  Key creation is done in preinitilization and is
put on the keyOut port during initilization so the encryption has a key to use
when its first fired.

The following actor relies on the Java Cryptography Architecture (JCA) and Java
Cryptography Extension (JCE).


TODO: include sources of information on JCE cipher and algorithms
TODO: split asymmetric and symmetric
TODO: make encryption create key for symmetric(main reason for above)
TODO: Send keys as ObjectOutputStreams
TODO: Use cipher streaming to allow for easier file input reading.
@author Rakesh Reddy
@version $Id$
@since Ptolemy II 3.1
*/

public class Decryption extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Decryption(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        //Create ports and parameters and set defalut values and types.
        input = new SDFIOPort(this, "input", true, false);
        input.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        output = new SDFIOPort(this, "output", false, true);
        output.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        keyOut = new SDFIOPort(this, "keyOut", false, true);
        keyOut.setTypeEquals(BaseType.OBJECT);

        algorithm = new Parameter(this, "algorithm");
        algorithm.setTypeEquals(BaseType.STRING);
        algorithm.setExpression("RSA");

        provider = new Parameter(this, "provider");
        provider.setTypeEquals(BaseType.STRING);
        provider.setExpression("");

        cryptoMode = new StringAttribute(this, "mode");
        cryptoMode.setExpression("asymmetric");
        _keyMode=_ASYMMETRIC;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Specifies the algoithm of the cipher to use for decrypting the data and
     *  making the key.  The algorithm is specified as a string input. The
     *  algoithms are limited to those implemented by providers using the Java
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
     *  are limited by their implemetantion in the provider library.  If left
     *  blank the default seting for the algrithm used.  This should be the
     *  same as the mode used for in the coresponding encrytpion actor.
     */
    public Parameter mode;

    /** Specifies the size of the key to be created.  This is an int value
     *  representing the number of bits.  A default value exists for certain
     *  algorithms.  If a key is not specified and no default value is found
     *  then an error is produced.
     */
    public Parameter keySize;

    /** This StringAttribute determines whether decryption will be performed on
     *  a asymmetric or symmetric crytographic algorithm.  The to possible
     *  values are "symmetric" or "asymmetric".  THe default value is
     *  "asymmetric."
     */
    public StringAttribute cryptoMode;

    /** This port takes in as an UnsignedByteArray and decrypts the data based
     *  on the algoithm.
     */
    public SDFIOPort input;

    /** This port sends out the decrypted data received from <i>input</i> in
     *  the form of a UnsignedByteArray.
     */
    public SDFIOPort output;

    /** This port outputs the key to be used by the encryption actor.  If set
     *  to asymmetric encryption the generated public key is sent out.
     *  Otherwise the generated secret key is sent out.
     */
    public SDFIOPort keyOut;



    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Determine whether to use aysmmetric or symmetric decryption.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException if namming conflict is encountered.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == cryptoMode) {
            String function = cryptoMode.getExpression();

            if (function.equals("asymmetric")) {
                _keyMode = _ASYMMETRIC;
            }else if (function.equals("symmetric")) {
                _keyMode = _SYMMETRIC;
            }
        } else super.attributeChanged(attribute);
    }

    /** This method takes the data from the <i>input</i> and decrypts the data
     *  based on the <i>algorithm</i>, <i>provider</i>, <i>mode</i> and
     *  <i>padding</i>.  This is the sent on the <i>output</i>.  All paramters
     *  should be the same as the corresponding encryptoion actor.
     *  @exception IllegalActionException
     */
    public void fire() throws IllegalActionException {
        super.fire();
        try{
            if (input.hasToken(0)) {
                byte[] dataBytes = _ArrayTokenToUnsignedByteArray((ArrayToken)input.get(0));
                dataBytes=_crypt(dataBytes);
                output.send(0, _UnsignedByteArrayToArrayToken(dataBytes));
            }

            if (_keyMode == _ASYMMETRIC) {
                keyOut.send(0, new ObjectToken(_publicKey));
            } else {
                keyOut.send(0, new ObjectToken(_secretKey));
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalActionException(this.getName() + e.getMessage());
        }

    }

    /** Get an instance of the cipher and outputs the key required for
     *  decryption.
     *  @exception IllegalActionException
     *  @exception NoSuchAlgorihmException when the algorithm is not found.
     *  @exception NoSuchPaddingException when the padding scheme is illegal
     *      for the given algorithm.
     *  @exception NoSuchProviderException if the specified proviedr does not
     *      exist.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        try{
            _cipher = Cipher.getInstance(_algo, _provider);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }

        if (_keyMode == _ASYMMETRIC) {
            keyOut.send(0, new ObjectToken(_publicKey));
        } else {
            keyOut.send(0, new ObjectToken(_secretKey));
        }
    }

    /** Get parameter information and sets token poduction for iniitialize to
     *  one.
     *
     * @exception IllegalActionException
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        _algo = ((StringToken)algorithm.getToken()).stringValue();
        byte[] keyBytes = _createKeys();
        //_keyOutput = _UnsignedByteArrayToArrayToken(keyBytes);
        keyOut.setTokenInitProduction(1);
        getDirector().invalidateResolvedTypes();
    }



    ///////////////////////////////////////////////////////////////////
    ////                         Protected Methods                 ////

    /** Take an ArrayTooken and converts it to an array of unsigned bytes.
     *
     * @param dataArrayToken
     * @return dataBytes
     */
    protected byte[] _ArrayTokenToUnsignedByteArray( ArrayToken dataArrayToken) {
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
     * @exception IllegalActionException
     * @exception NoSuchAlgorithmException when algorithm is not found.
     * @exception NoSuchProviderException when provider is not found.
     */
    protected void _createAsymmetricKeys()throws IllegalActionException{
        try{
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(_algo, _provider);
            keyPairGen.initialize(1024, new SecureRandom());
            KeyPair pair = keyPairGen.generateKeyPair();
            _publicKey = pair.getPublic();
            _privateKey = pair.getPrivate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalActionException(this.getName() + e.getMessage());
        }
    }

    /** Determine what kind of keys to create and calls the respective
     *  function.
     *
     * @exception ThrowsIllegalActionException
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
     * @exception IllegalActionException
     */
    protected void _createSymmetricKey() throws IllegalActionException{
        try{
            KeyGenerator keyGen = KeyGenerator.getInstance(_algo, _provider);
            keyGen.init(56, new SecureRandom());
            _secretKey = keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new IllegalActionException(this.getName() + e.getMessage());
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            throw new IllegalActionException(this.getName() + e.getMessage());
        }

    }

    /** Determine what key to use and decrypt the data with the specified
     *  key.  Receives the data to be encrypted in as a byte array and returns
     *  a byte array.
     *
     * @param initialData
     * @return byte[]
     * @exception IllegalActionException
     * @exception IOException
     * @exception InvalideKeyException when key is invalid.
     * @exception BadPaddingException when padding is bad.
     * @exception IllegalBockSizeException for illegal blcok sizes.
     */
    protected byte[] _crypt(byte[] initialData)throws IllegalActionException{
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Key key;
        try{
            if (_keyMode == _SYMMETRIC) {
                key = _secretKey;
                _cipher.init(Cipher.DECRYPT_MODE, key);
                byteArrayOutputStream.write(_cipher.doFinal(initialData));
            } else if (_keyMode == _ASYMMETRIC) {
                key = _privateKey;
                _cipher.init(Cipher.DECRYPT_MODE, key);
                int blockSize = _cipher.getBlockSize();
                int length = 0;
                for (int i = 0; i<initialData.length; i+=blockSize) {
                    if (initialData.length-i <= blockSize) {
                        length = initialData.length-i;
                    } else{
                        length = blockSize;
                    }
                    byteArrayOutputStream.write(_cipher.doFinal(initialData, i, length));
                }
                byteArrayOutputStream.flush();
                byteArrayOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalActionException(this.getName()+e.getMessage());
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            throw new IllegalActionException(this.getName()+e.getMessage());
        } catch (BadPaddingException e) {
            e.printStackTrace();
            throw new IllegalActionException(this.getName()+e.getMessage());
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
            throw new IllegalActionException(this.getName()+e.getMessage());
        }
        return byteArrayOutputStream.toByteArray();
    }

    /** Take an array of unsigned bytes and convert it to an ArrayToken.
     *
     * @param dataBytes
     * @return dataArrayToken
     * @exception IllegalActionException
     */
    protected ArrayToken _UnsignedByteArrayToArrayToken( byte[] dataBytes)throws IllegalActionException{
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

    /** Set to _ASYMMETRIC or _SYMMETRIC depending on <i>crypoMode</i>
     *  paramter.
     */
    private int _keyMode;

    /** The algorithm to be used for decryption specified by the
     * <i>algorithm</i> parameter.
     */
    private String _algo;

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

    /** The name of the provider to be used in determing which instance of an
     *  algorithm to provide.
     */
    private String _provider = "BC";
}
