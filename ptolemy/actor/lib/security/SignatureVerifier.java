/* Verify the signature of the input data.

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

import ptolemy.data.ObjectToken;
import ptolemy.data.ArrayToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.io.ByteArrayOutputStream;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.SignatureException;

//////////////////////////////////////////////////////////////////////////
//// AsymmetricEncryption
/**
Verify the signature of the input data.
<p>
<p>In cryptography, digital signatures can be used to verify that the
data was not modified in transit.  However, the data itself is passed
in cleartext.

<p>This actor reads an objectToken public key from the
<i>publicKey</i> port and then verifies the signature of
each unsigned byte array that appears on the <i>input</i> port.
If the signature is legitimate, then the unsigned byte array
data on the <i>data</i> port is passed to the <i>output</i> port.

<p>The algorithm and keySize parameters should be set to the same
value as the corresponding parameter in the SignatureSigner actor.
Two common values for the algorithm parameter are DSA and RSA.

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
public class SignatureVerifier extends SignatureActor {

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
    public SignatureVerifier(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        data = new TypedIOPort(this, "data", true, false);
        data.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        publicKey = new TypedIOPort(this, "publicKey", true, false);
        publicKey.setTypeEquals(BaseType.OBJECT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The original data in clear text that is signed.
     *  The type is unsigned byte array.   
     */
    public TypedIOPort data;

    /** This port receives the public key to be used from the
     *  The type is an ObjectToken containin a java.security.Key.
     */
    public TypedIOPort publicKey;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read in the publicKey, input and data tokens and verify
     *  the signature.  
     *
     *  @exception IllegalActionException If thrown by base class
     *  or if the signature does not properly verify.
     */
    public void fire() throws IllegalActionException {
        if (publicKey.hasToken(0)) {
            ObjectToken objectToken = (ObjectToken)publicKey.get(0);
            _publicKey = (PublicKey)objectToken.getValue(); 
        }
        if (input.hasToken(0) && data.hasToken(0) && _publicKey != null) {
            _data = _arrayTokenToUnsignedByteArray((ArrayToken)data.get(0));
            // Don't read input here, super.fire() will read it for us.
            super.fire();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         Protected Methods                 ////

    /** Verify the signature.
     * @exception IllegalActionException If there is a problem with
     * the signature or the key.
     */
    protected byte[] _process(byte[] signatureData)
            throws IllegalActionException {
        ByteArrayOutputStream byteArrayOutputStream =
            new ByteArrayOutputStream();
        try {
            _signature.initVerify(_publicKey);
            _signature.update(_data);
            boolean verify = _signature.verify(signatureData);
            if (verify) {
                return _data;
            } else {
                throw new IllegalActionException("Signature verification "
                        + "failed");
            }
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Problem processing " + signatureData.length
                    + " bytes of signature data");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The original data in cleartext that is being verified.
    private byte[] _data;
}
