/* Receives a key from an AsymmetricDecryption actor and uses it to encrypt a
   data input based on a given asymmetric algorithm.

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
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.SignatureException;

import ptolemy.data.ArrayToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.kernel.SDFIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// AsymmetricEncryption
/**
This actor takes an unsigned byte array at the input and encrypts the message.
The resulting output is an unsigned byte array. Various ciphers that are
implemented by "providers" and installed on the system maybe used by specifying
the algorithm in the <i>algorithm</i> parameter. The algorithm specified must be
asymmetric. The mode and padding can also be specified in the mode and
padding parameters. In case a provider specific instance of an algorithm is
needed the provider may also be specified in the provider parameter.
This actor receives a public key from the AsymmetricDecyption actor and encrypts
the data input with the given key.

The following actor relies on the Java Cryptography Architecture (JCA) and Java
Cryptography Extension (JCE).


TODO: include sources of information on JCE cipher and algorithms

TODO: Use cipher streaming to allow for easier file input reading.
@author Rakesh Reddy
@version $Id$
@since Ptolemy II 3.1
*/

public class SignatureVerifier extends SignatureActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SignatureVerifier(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        keyIn = new SDFIOPort(this, "keyIn", true, false);
        keyIn.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        data = new SDFIOPort(this, "data", true, false);
        data.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////


    /** This port receives the public key to be used from the
     *  AsymmetricDecryption actor in the form of an unsigned byte array.
     *  This key is used to encrypt data from the <i>input</i> port.
     */
    public SDFIOPort keyIn;

    public SDFIOPort data;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there are tokens on the <i>input</i> and <i>keyIn</i> ports, they
     *  are consumed.  This method takes the data from the <i>input</i> and
     *  encrypts the data based on the <i>algorithm</i>, <i>provider</i>,
     *  <i>mode</i> and <i>padding</i> using the public key from the decryption
     *  actor.  This is then sent on the <i>output</i>.  All parameters should
     *  be the same as the corresponding decryption actor.
     *
     *  @exception IllegalActionException if base thrown by base class.
     */
    public void fire() throws IllegalActionException {
        if (keyIn.hasToken(0)) {
            _publicKey = (PublicKey)_bytesToKey(_arrayTokenToUnsignedByteArray((ArrayToken)keyIn.get(0)));
        }
        if (input.hasToken(0) && data.hasToken(0) && _publicKey!=null) {
            _data = _arrayTokenToUnsignedByteArray((ArrayToken)data.get(0));
            super.fire();
        }
    }

    /** Get an instance of the cipher.
     *
     *  @exception IllegalActionException if thrown by base class.
     *  @exception NoSuchAlgorihmException when the algorithm is not found.
     *  @exception NoSuchPaddingException when the padding scheme is illegal
     *      for the given algorithm.
     *  @exception NoSuchProviderException if the specified provider does not
     *      exist.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         Protected Methods                 ////
    protected byte[] _process(byte[] signatureData)throws IllegalActionException{
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try{
            _signature.initVerify(_publicKey);
            _signature.update(_data);
            boolean verify = _signature.verify(signatureData);
            if (verify) {
                return _data;
            } else {
                return new String("Signature verification failed").getBytes();
            }
        } catch (SignatureException e) {
            e.printStackTrace();
            throw new IllegalActionException(this.getName()+e.getMessage());
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            throw new IllegalActionException(this.getName()+e.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private byte[] _data;


}
