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

import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.security.PublicKey;

//////////////////////////////////////////////////////////////////////////
//// SignatureVerifier
/**
Verify the signature of the input data.
<p>
<p>In cryptography, digital signatures can be used to verify that the
data was not modified in transit.  However, the data itself is passed
in cleartext.

<p>The <i>provider</i> and <i>signatureAlgorithm</i>
parameters should be set to the values used to generate the publicKey.
See {@link PublicKeyReader} and {@link SignatureActor}
for possible values.

<p>The <i>provider</i> and <i>signatureAlgorithm</i> parameters should
be set to the same value as the corresponding parameter in the
SignatureSigner actor.

<p>This actor reads an ObjectToken public key from the
<i>publicKey</i> port and then reads unsigned byte arrays from
the <i>signature</i> port and verifies the signature of
each unsigned byte array that appears on the <i>input</i> port.
If the signature is valid, then the unsigned byte array
data on the <i>input</i> port is passed to the <i>output</i> port.
If the signature is not valid, then an exception is thrown.

<p>This actor relies on the Java Cryptography Architecture (JCA) and Java
Cryptography Extension (JCE).

<br>Information about JCA can be found at
<a href="http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html" target="_top">http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html">.
<br>Information about JCE can be found at
<a href="http://java.sun.com/products/jce/" target="_top">http://java.sun.com/products/jce/">.

@see PublicKeyReader
@author Rakesh Reddy, Christopher Hylands Brooks
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

        signature = new TypedIOPort(this, "signature", true, false);
        signature.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        publicKey = new TypedIOPort(this, "publicKey", true, false);
        publicKey.setTypeEquals(BaseType.OBJECT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The signature of the data.  The type of this input port
     *  is unsigned byte array.
     */
    public TypedIOPort signature;

    /** This port receives the public key to be used from the
     *  The type of this input port is an ObjectToken containing
     *  a java.security.Key.
     */
    public TypedIOPort publicKey;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read in the publicKey, input and signature tokens, verify
     *  the signature and write the input data on the output port.
     *
     *  @exception IllegalActionException If thrown by base class
     *  or if the signature does not properly verify.
     */
    public void fire() throws IllegalActionException {
        if (publicKey.hasToken(0)) {
            ObjectToken objectToken = (ObjectToken)publicKey.get(0);
            _publicKey = (PublicKey)objectToken.getValue();
        }
        if (input.hasToken(0) && signature.hasToken(0) && _publicKey != null) {
            // Process the input data to generate a signature.

            byte [] signatureData =
                CryptographyActor.arrayTokenToUnsignedByteArray(
                        (ArrayToken)signature.get(0));
            ArrayToken inputToken = (ArrayToken)input.get(0);
            try {
                _signature.initVerify(_publicKey);
                _signature.update(CryptographyActor.
                        arrayTokenToUnsignedByteArray(inputToken));
                if (!_signature.verify(signatureData)) {
                    throw new IllegalActionException(
                            "Signature verification failed");
                }
            } catch (java.security.GeneralSecurityException ex) {
                throw new IllegalActionException("There was a problem with "
                        + "the key or signature");
            }

            // If we got to here, then the signature verified, so
            // output the data
            output.send(0, inputToken);

            super.fire();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The PublicKey that is read in from the publicKey port.
    private PublicKey _publicKey;
}
