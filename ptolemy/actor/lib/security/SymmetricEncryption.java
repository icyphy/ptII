/* Creates and sends a key to a SymmetricDecryption and encrypts incoming data
   based on a given symmetric algorithm.

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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import ptolemy.actor.NoRoomException;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// SymmetricEncryption
/**

This actor takes an unsigned byte array at the input and encrypts the
message.  The resulting output is an unsigned byte array. Various
ciphers that are implemented by "providers" and installed maybe used
by specifying the algorithm in the <i>algorithm</i> parameter.  The
specified algorithm must be symmetric.  The mode and padding can also
be specified in the mode and padding parameters.  In case a provider
specific instance of an algorithm is needed the provider may also be
specified in the provider parameter. This actor sends its secret key
on the <i>keyOut</i> port to a decryption actor as an unsigned byte
array.  This key should be protected in some manner as the security of
the encrypted message relies on the secrecy of this key. Key creation
is done in pre-initialization and is put on the <i>keyOut</i> port
during initialization so the decryption actor has a key to use when
its first fired.

<p>This actor relies on the Java Cryptography Architecture (JCA) and Java
Cryptography Extension (JCE).

<br>Information about JCA can be found at
<a href="http://java.sun.com/products/jca/" target="_top">http://java.sun.com/products/jca/">.
Information about JCE can be found at
<a href="http://java.sun.com/products/jce/" target="_top">http://java.sun.com/products/jce/">.

@author Rakesh Reddy, Christopher Hylands Brooks
@version $Id$
@since Ptolemy II 3.1
*/
public class SymmetricEncryption extends CipherActor {

    // TODO: include sources of information on JCE cipher and algorithms
    // TODO: Use cipher streaming to allow for easier file input reading.

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SymmetricEncryption(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        keyOut = new TypedIOPort(this, "keyOut", false, true);
        keyOut.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        parameters = new TypedIOPort(this, "parameters", false, true);
        parameters.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////


    /** This port outputs the key to be used by the SymmetricDecryption actor
     *  as an unsigned byte array.
     */
    public TypedIOPort keyOut;

    // FIXME: what does this parameter do?
    public TypedIOPort parameters;



    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there is a token on the <i>input</i> port, this method takes the
     *  data from the <i>input</i> and encrypts the data based on the
     *  <i>algorithm</i>, <i>provider</i>, <i>mode</i> and <i>padding</i>
     *  using the created secret key.  This is then sent on the
     *  <i>output</i>.  The public key is also sent out on the <i>keyOut</i>
     *  port.  All parameters should be the same as the corresponding
     *  decryption actor.  The call for encryption is done in the base class.
     *
     *  @exception IllegalActionException If thrown by base class.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        keyOut.send(0,
                _unsignedByteArrayToArrayToken(_keyToBytes(_secretKey)));
        if (_algorithmParameters != null) {
            try {

                parameters.send(0, _unsignedByteArrayToArrayToken(
                        _algorithmParameters.getEncoded()));

            } catch (Exception ex) {
                throw new IllegalActionException(this, ex, "send failed");
            }
        }

        //   if (FIRST_RUN == true) {
        //       _byteArrayOutputStream = new ByteArrayOutputStream();
        //       try {
        //  _cipher.init(Cipher.ENCRYPT_MODE, _secretKey, _algorithmParameters);
        //
        //       } catch (InvalidKeyException e) {
        //  // TODO Auto-generated catch block
        //       } catch (InvalidAlgorithmParameterException e) {
        //  // TODO Auto-generated catch block
        //       }
        //       _cos = new CipherOutputStream(_byteArrayOutputStream,
        //           _cipher);
        //       FIRST_RUN = false;
        //   }
        //        //} catch (NoRoomException e) {
        //  } catch (IllegalActionException e) {
        //        } catch (IOException e) {
        //        } catch (InvalidKeyException e) {
        //        } catch (InvalidAlgorithmParameterException e) {
        //        }
        //        super.fire();
    }

    /** Get an instance of the cipher and outputs the key required for
     *  decryption.
     *  @exception IllegalActionException If thrown by base class or
     *  if the algorithm is not found, or if the padding scheme is illegal,
     *  or if the specified provider does not exist.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        try {
            super.initialize();
            _secretKey = (SecretKey)_createSymmetricKey();
            //keyOut.send(0, _unsignedByteArrayToArrayToken(
            //    _keyToBytes(_secretKey)));

            _cipher.init(Cipher.ENCRYPT_MODE, _secretKey);

            _algorithmParameters = _cipher.getParameters();
            //if (_algorithmParameters != null) {
            //    parameters.send(0,
            //       _unsignedByteArrayToArrayToken(_algorithmParameters
            //       .getEncoded()));
            //}

            FIRST_RUN = true;
        } catch (Exception ex) {
            throw new IllegalActionException (this, ex,
                    "Failed to initialize");
        }
    }

    /** Sets token production for initialize to one and resolves scheduling.
     *
     * @exception IllegalActionException If thrown by base class.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        getDirector().invalidateResolvedTypes();
    }

    /** Encrypt the data with the specified key.  Receives the data to be
     *  encrypted as a byte array and returns a byte array.  Also creates
     *  and sends an initialization vector if necessary.
     *
     * @param dataBytes the data to be encrypted.
     * @return byte[] the encrypted data.
     * @exception IllegalActionException If error occurs in
     * ByteArrayOutputStream, if the key is invalid, if the padding is bad
     * or if the block size is illegal.
     */
    protected byte[] _process(byte[] dataBytes)
            throws IllegalActionException{
        // ByteArrayOutputStream byteArrayOutputStream =
        //      new ByteArrayOutputStream();
        _byteArrayOutputStream = new ByteArrayOutputStream();
        _byteArrayOutputStream.reset();
        // byteArrayInputStream = new ByteArrayInputStream(initialData);
        // int length = 0;
        // byte [] buffer = new byte [BUFFER_SIZE];
        // try {
        //     while ((length = byteArrayInputStream.read(buffer)) != -1) {
        //         _cos.write(buffer, 0, length);
        //     }
        //     _cos.flush();
        // } catch (IOException e) {
        //      throw new IllegalActionException(this, ex);
        // }

        try {
            _byteArrayOutputStream.write(_cipher.doFinal(dataBytes));
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Problem processing " + dataBytes.length + " bytes.");
        }
        return _byteArrayOutputStream.toByteArray();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /* The public key to be used for asymmetric encryption. This key is null
     * for symmetric decryption.
     */
    private SecretKey _secretKey = null;

    private AlgorithmParameters _algorithmParameters;

    private static int BUFFER_SIZE = 8192;

    private boolean FIRST_RUN;

    //    private CipherOutputStream _cos;

    private ByteArrayOutputStream _byteArrayOutputStream;

    private ByteArrayInputStream byteArrayInputStream;
}
