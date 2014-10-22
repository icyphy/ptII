/* A base class for cryptographic actors.

 Copyright (c) 2003-2014 The Regents of the University of California.
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

import java.security.Provider;
import java.security.Security;
import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// CryptographyActor

/**
 A base class for cryptographic actors.

 <p>Cryptographic actors are actors that perform encryption or decryption or
 generate signatures of data.

 <p>Actors extending this class take in an unsigned byte array at the
 <i>input</i>, process the data based on the <i>algorithm</i> parameter
 and send an unsigned byte array to the <i>output</i>.  The algorithms
 that maybe implemented are limited those that are implemented by
 "providers" following the Java Cryptography Extension (JCE)
 specifications and installed on the machine being run.
 If a provider specific instance of an algorithm is
 needed, the provider may be specified in the <i>provider</i>
 parameter. The <i>keySize</i> also allows implementations of
 algorithms using various key sizes.

 <p>Concrete actors derived from this base class must include a
 {@link #_process(byte[])} method that processes the data appropriately.
 The _process() method is called by CryptographyActor.fire().
 This class takes care of basic initialization of the <i>algorithm</i>
 and <i>provider</i> parameters for use by the subclasses.

 <p>This actor relies on the Java Cryptography Architecture (JCA) and Java
 Cryptography Extension (JCE).

 <br>Information about JCA can be found at
 <a href="http://download.oracle.com/javase/6/docs/technotes/guides/security/crypto/CryptoSpec.html#in_browser" target="_top">http://download.oracle.com/javase/6/docs/technotes/guides/security/crypto/CryptoSpec.html</a>.
 <br>Information about JCE can be found at
 <a href="http://www.oracle.com/technetwork/java/javase/tech/index-jsp-136007.html" target="_top">http://www.oracle.com/technetwork/java/javase/tech/index-jsp-136007.html</a>.

 <br>The Java security tutorial can be found at
 <a href="http://download.oracle.com/javase/tutorial/security/index.html" target="_top">http://download.oracle.com/javase/tutorial/security/index.html</a>

 @author Christopher Hylands Brooks, Contributor: Rakesh Reddy
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Yellow (cxh)
 */
abstract public class CryptographyActor extends TypedAtomicActor {
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
    public CryptographyActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        // Add the possible algorithm choices.
        algorithm = new StringParameter(this, "algorithm");

        Set algorithms = Security.getAlgorithms("Cipher");
        Iterator algorithmsIterator = algorithms.iterator();

        for (int i = 0; algorithmsIterator.hasNext(); i++) {
            String algorithmName = (String) algorithmsIterator.next();

            if (i == 0) {
                algorithm.setExpression(algorithmName);
            }

            algorithm.addChoice(algorithmName);
        }

        // Add the possible provider choices.
        provider = new StringParameter(this, "provider");
        provider.setExpression("SystemDefault");
        provider.addChoice("SystemDefault");

        Provider[] providers = Security.getProviders();

        for (Provider provider2 : providers) {
            provider.addChoice(provider2.getName());
        }

        keySize = new Parameter(this, "keySize", new IntToken(1024));
        keySize.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Specify the algorithm to be used to process data.
     *  The algorithm is specified as a string. The algorithms are
     *  limited to those implemented by providers using the Java JCE
     *  which are found on the Java virtual machine. The initial
     *  default is the first value returned by
     *  java.security.Security.getAlgorithms();
     */
    public StringParameter algorithm;

    /** The input port. The type of this port is unsigned byte array.
     *  Data is read in on this port, processed by the _process() method
     *  during fire() and passed to the <i>output</i> port.
     */
    public TypedIOPort input;

    /** Specify the size of the key to be created.
     *  The key size is an integer value representing the number of bits in
     *  the key.  The initial default depends on the algorithm that is
     *  selected, not all algorithms use <i>keySize</i>.
     *  In addition, only certain keySizes may work with certain
     *  algorithms, see the documentation for the algorithm you are using.
     *  <p>DSA is the most common algorithm that uses <i>keySize</i>, the Sun
     *  documentation says:
     *  "The length, in bits, of the modulus p. This must range from
     *  512 to 1024, and must be a multiple of 64. The default keysize
     *  is 1024."
     *  Refer to
     *  <a href="http://download.oracle.com/javase/6/docs/technotes/guides/security/StandardNames.html#spectemp" target="_top"><code>http://download.oracle.com/javase/6/docs/technotes/guides/security/StandardNames.html#spectemp</code></a>
     *  for a list of possible key sizes for certain algorithms.
     *  The initial default is 1024.
     */
    public Parameter keySize;

    /** The output port.  The type of this port is unsigned byte array.
     *  This port sends out the processed data received from the <i>input</i>
     *  port.
     */
    public TypedIOPort output;

    /** Specify a provider for the given algorithm.
     *  The default value is "SystemDefault" which allows the
     *  system to choose the provider based on the JCE architecture.
     */
    public StringParameter provider;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to reinitialize the state if
     *  the <i>algorithm</i>, <i>keySize</i>, or <i>provider</i>
     *  parameter is changed.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == algorithm) {
            _algorithm = ((StringToken) algorithm.getToken()).stringValue();
        } else if (attribute == keySize) {
            _keySize = ((IntToken) keySize.getToken()).intValue();
        } else if (attribute == provider) {
            _provider = ((StringToken) provider.getToken()).stringValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Read data from the from the <i>input</i> and process the
     *  data based on the <i>algorithm</i>, and <i>provider</i> by calling
     *  {@link #_process(byte [])}.  The transformed data is then sent to
     *  the <i>output</i> port.
     *
     *  @exception IllegalActionException If thrown by the base class or
     *  if there is a problem processing the data.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire(); // super.fire() will print out debugging messages.

        try {
            if (input.hasToken(0)) {
                byte[] dataBytes = ArrayToken
                        .arrayTokenToUnsignedByteArray((ArrayToken) input
                                .get(0));
                dataBytes = _process(dataBytes);
                output.send(0,
                        ArrayToken.unsignedByteArrayToArrayToken(dataBytes));
            }
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Problem sending data");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         Protected Methods                 ////

    /** Process the input data based on parameter specifications.
     *  Subclasses should process the data using one of the signature
     *  or cipher classes provided in the JCA or JCE.
     *
     * @param dataBytes The data to be processed.
     * @return The processed data.
     * @exception IllegalActionException Not thrown in this base class
     */
    abstract protected byte[] _process(byte[] dataBytes)
            throws IllegalActionException;

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The name of the algorithm to be used. */
    protected String _algorithm;

    /** The key size to be used when processing information. */
    protected int _keySize;

    /** The provider to be used for a provider specific implementation. */
    protected String _provider;
}
