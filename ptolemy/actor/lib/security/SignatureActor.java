/* A base class for signature actors.

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

@ProposedRating Yellow (rnreddy@ptolemy.eecs.berkeley.edu)
@AcceptedRating Red (ptolemy@ptolemy.eecs.berkeley.edu)
*/

package ptolemy.actor.lib.security;


import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// SignatureActor
/**
This is a base class that implements general and helper functions used by
signature actors. Actors extending this class take in an unsigned byte
array at the <i>input</i>, create an encrypted hash of the message and send
it on the <i>output</i>.  The algorithms that maybe implemented are limited
to the signature algorithms that are implemented by "providers" following the
JCE specifications and installed on the machine being run. In case a provider
specific instance of an algorithm is needed, the provider may also be specified
in the <i>provider</i> parameter.  The <i>keySize</i> parameter also allows
implementations of algorithms using various key sizes.

This class and its subclasses rely on the Java Cryptography Extension (JCE)
and Java Cryptography Architecture(JCA).

@author Rakesh Reddy
@version $Id$
*/

public class SignatureActor extends CryptographyActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SignatureActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** This method initializes the Signature object.  If provider is left
     *  as "SystemDefault" the system chooses the provider based on the JCE.
     *
     * @throws IllegalActionException if exception below is thrown.
     * @exception NoSuchAlgorihmException when the algorithm is not found.
     * @exception NoSuchProviderException if the specified provider does not
     *  exist.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        try{
            if(_provider.equalsIgnoreCase("SystemDefault")){
                _signature = Signature.getInstance(_algorithm);
            } else{
                _signature = Signature.getInstance(_algorithm, _provider);
            }
        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }



    ///////////////////////////////////////////////////////////////////
    ////                         Protected Methods                 ////

    /** The signature that will be used to process the data.
     */
    protected Signature _signature;

    /** The public key to be used for the signature.
     */
    protected PublicKey _publicKey = null;
}
