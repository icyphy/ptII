/* A base class for signature actors.

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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// SignatureActor
/**
A common base class for actors that use cryptographic signatures.

<p>In cryptography, digital signatures can be used to verify that the
data was not modified in transit.  However, the data itself is passed
in cleartext.

<p>The signature algorithms that maybe implemented are limited to the
signature algorithms that are implemented by providers following the
JCE specifications and installed on the machine being run. In case a
provider specific instance of an algorithm is needed, the provider may
also be specified in the <i>provider</i> parameter.

<p>The input and output are both arrays of unsigned bytes.

<p>In initialize(), this actor sets the value of the _signature member
to the results of calling java.security.Signature.getInstance() with
the values of the <i>signatureAlgorithm</i> and <i>provider</i>
parameters.  Derived classes should have a fire() method that uses the
_signature member to process data appropriately.

<p>This actor relies on the Java Cryptography Architecture (JCA) and Java
Cryptography Extension (JCE).  See the
{@link ptolemy.actor.lib.security.CryptographyActor} documentation for
resources about JCA and JCE.

@author Christopher Hylands Brooks, Contributor: Rakesh Reddy 
@version $Id$
@since Ptolemy II 3.1
*/
public class SignatureActor extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  The Java virtual machine is queried for algorithm and provider
     *  choices and these choices are added to the appropriate parameters.
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

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        provider = new StringParameter(this, "provider");
        provider.setExpression("SystemDefault");
        provider.addChoice("SystemDefault");
        Provider [] providers = Security.getProviders();
        for (int i = 0; i < providers.length; i++) {
            provider.addChoice(providers[i].getName());
        }

        signatureAlgorithm = new StringParameter(this, "signatureAlgorithm");
        Iterator signatureAlgorithms =
            Security.getAlgorithms("Signature").iterator();
        for(int i = 0; signatureAlgorithms.hasNext(); i++) {
            String algorithmName = (String)signatureAlgorithms.next();
            if (i == 0) {
                signatureAlgorithm.setExpression(algorithmName);
            }
            signatureAlgorithm.addChoice(algorithmName);
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** This port takes in an unsigned byte array and processes the data.
     */
    public TypedIOPort input;

    /** This port sends out the processed data received from <i>input</i> in
     *  the form of an unsigned byte array.
     */
    public TypedIOPort output;

    /** Specify a provider for the given algorithm.  Takes the algorithm name
     *  as a string. The default value is "SystemDefault" which allows the
     *  system chooses the provider based on the JCE architecture.
     */
    public StringParameter provider;

    /** Specify the algorithm to be used to sign data.  The algorithm is
     *  specified as a string. The algorithms are limited to those
     *  implemented by providers using the Java JCE which are found on the
     *  system.
     *  Depending on your JDK installation, possible values might
     *  be SHA1WITHDSA or MD5WITHRSA.
     *  The initial default is the first value returned by
     *  java.security.Security.getAlgorithms("Signature").
     */
    public StringParameter signatureAlgorithm;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to reinitialize the state if
     *  the the <i>signatureAlgorithm</i>, or <i>provider</i>
     *  parameter is changed.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == signatureAlgorithm) {
            _signatureAlgorithm =
                ((StringToken)signatureAlgorithm.getToken()).stringValue();
        } else if (attribute == provider) {
            _provider = ((StringToken)provider.getToken()).stringValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Use the values of the <i>signatureAlgorithm</i> and
     *  <i>provider</i> parameters to initialize the
     *  java.security.Signature object.
     *
     *  If provider is "SystemDefault" then the system chooses the
     *  provider based on the JCE.
     *
     * @exception IllegalActionException If the base class throws it,
     * if the algorithm is not found, or if the specified provider does
     *  not exist.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        try {
            if (_provider.equalsIgnoreCase("SystemDefault")) {
                _signature = Signature.getInstance(_signatureAlgorithm);
            } else {
                _signature = Signature.getInstance(_signatureAlgorithm,
                        _provider);
            }
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to initialize Signature with algorithm: '"
                    + _signatureAlgorithm + "', provider: '"
                    + _provider + "'");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         Protected Methods                 ////

    /** The name of the provider to be used for a provider specific
     *  implementation. */
    protected String _provider;

    /** The signature that will be used to process the data. */
    protected Signature _signature;

    /** The name of the signature algorithm to be used. */
    protected String _signatureAlgorithm;
}
