/* Creates a signature for the given piece of data and creates the key to be sent to the signature verifier.

 Copyright (c) 1998-2003 The Regents of the University of California.
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
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.SignatureException;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.kernel.SDFIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// SignatureSigner
/**
This actor takes an unsigned byte array at the input creates a signature for it.
The resulting output is an unsigned byte array.  The Signature class in Java is based on calculating the message digest of some data and encrypting it.  Various signature algorithms that are
implemented by "providers" and installed maybe used by specifying the algorithm
in the <i>algorithm</i> parameter.  In case a provider specific instance of an algorithm is needed, the provider may
also be specified in the <i>provider</i> parameter.  This actor creates a private key
to sign data and a public key which is sent on the <i>keyOut</i> port to
verify the signature.

The following actor relies on the Java Cryptography Architecture (JCA) and Java
Cryptography Extension (JCE).


TODO: include sources of information on JCE cipher and algorithms

TODO: Use cipher streaming to allow for easier file input reading.
@author Rakesh Reddy
@version $Id$
*/

public class SignatureSigner extends SignatureActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SignatureSigner(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        keyAlgorithm = new Parameter(this, "keyAlgorithm");
        keyAlgorithm.setTypeEquals(BaseType.STRING);
        keyAlgorithm.setExpression("");


        keyOut = new SDFIOPort(this, "keyOut", false, true);
        keyOut.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        data = new SDFIOPort(this, "data", false, true);
        data.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////


    /** This port outputs the key to be used by the AsymmetricEncryption actor
     *  as an unsigned byte array.
     */
    public SDFIOPort keyOut;

    /** This port sends out the original data to be verified with the encypted
     *  digest
     */
    public SDFIOPort data;

    /** The algrotihm to be used to generate the key pair.  For example, using
     *  RSAwithMD5 as the signature algorithm, RSA would be used for the
     *  <i>keyAlgrotrithm</i> parameter.
     */
    public Parameter keyAlgorithm;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there is a token on the <i>input</i> port, this method takes the
     *  data from the <i>input</i> and decrypts the data based on the
     *  <i>algorithm</i>, <i>provider</i>, <i>mode</i> and  <i>padding</i>
     *  using the created private key.  This is then sent on the
     *  <i>output</i>.  The public key is also sent out on the <i>keyOut</i>
     *  port.  All parameters should be the same as the corresponding
     *  encryption actor.
     *
     *  @throws IllegalActionException if thrown by base class.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        keyOut.send(0, _unsignedByteArrayToArrayToken(_keyToBytes(_publicKey)));

    }

    /** Get an instance of the cipher and outputs the key required for
     *  decryption.
     *  @exception IllegalActionException if thrown by base class.
     *  @exception NoSuchAlgorihmException when the algorithm is not found.
     *  @exception NoSuchPaddingException when the padding scheme is illegal
     *      for the given algorithm.
     *  @exception NoSuchProviderException if the specified provider does not
     *      exist.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _keyAlgorithm = ((StringToken)keyAlgorithm.getToken()).stringValue();
        KeyPair pair = _createAsymmetricKeys();
        _publicKey = pair.getPublic();
        _privateKey = pair.getPrivate();
        keyOut.send(0, _unsignedByteArrayToArrayToken(_keyToBytes(_publicKey)));
    }

    /** Takes the data and calculates a message digest for it.
     *
     * @param initialData the data to be decrypted.
     * @return byte[] the decrypted data.
     * @throws IllegalActionException if exception is thrown.
     * @exception IOException when error occurs in ByteArrayOutputStream.
     * @exception InvalideKeyException when key is invalid.
     * @exception BadPaddingException when padding is bad.
     * @exception IllegalBockSizeException for illegal block sizes.
     */
    protected byte[] _process(byte[] dataBytes)throws IllegalActionException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try{
            data.send(0, _unsignedByteArrayToArrayToken(dataBytes));
            _signature.initSign(_privateKey);
            _signature.update(dataBytes);
            return _signature.sign();
        } catch (SignatureException e){
            e.printStackTrace();
            throw new IllegalActionException(this.getName()+e.getMessage());
        } catch (InvalidKeyException e){
            e.printStackTrace();
            throw new IllegalActionException(this.getName()+e.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /* The private key to be used for asymmetric decryption.  This key is null
     *  for symmetric decryption.
     */
    private PrivateKey _privateKey = null;
}
