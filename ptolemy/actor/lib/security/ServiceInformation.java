/* Determine the cryptographic services available on the system.

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


import java.security.Provider;
import java.security.Security;
import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.lib.Source;
import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

//////////////////////////////////////////////////////////////////////////
//// ServiceInformation
/**
Determine the cryptographic services available on the system.
This actor lists the following services:

<ul>
<li>Cipher
<li>KeyGenerator
<li>KeyPairGenerator
<li>MessageDigest
<li>Providers
<li>Signature
</ul>

Services are those algorithms have been implemented by providers and are
installed on the local system.  To add providers please refer to the
Java Cryptography Architecture (JCA) and Java Cryptography Extension (JCE).

<br>Information about JCA can be found at
<a href="http://java.sun.com/products/jca/" target="_top">http://java.sun.com/products/jca/">.
Information about JCE can be found at
<a href="http://java.sun.com/products/jce/" target="_top">http://java.sun.com/products/jce/">.

@author Rakesh Reddy, Christopher Hylands Brooks
@version $Id$
@since Ptolemy II 3.1
*/
public class ServiceInformation extends Source {
    // FIXME: Isn't this a source?

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ServiceInformation(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        output.setTypeEquals(new ArrayType(BaseType.STRING));

        service = new StringParameter(this, "service");
        service.setExpression("Providers");
        service.addChoice("Cipher");
        service.addChoice("KeyGenerator");
        service.addChoice("KeyPairGenerator");
        service.addChoice("MessageDigest");
        service.addChoice("Signature");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The service that the system is queried for.  Must be one
     *  of the following strings: 
     *  <ul>
     *  <li>Cipher
     *  <li>KeyGenerator
     *  <li>KeyPairGenerator
     *  <li>MessageDigest
     *  <li>Providers
     *  <li>Signature
     *  </ul>
     *  The default value is Providers.
     */
    public StringParameter service;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send the token in the <i>value</i> parameter to the output.
     *  @exception IllegalActionException If it is thrown by the
     *   send() method sending out the token.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        String request = service.getExpression();

        if (request.equals("Cipher") || request.equals("Signature")
                || request.equals("MessageDigest")
                || request.equals("KeyGenerator")
                || request.equals("KeyPairGenerator")) {

            // FIXME: this just always sends the same data?
            Set algorithms = Security.getAlgorithms(request);
            Iterator algorithmsIterator = algorithms.iterator();
            Token [] outputArray = new StringToken[algorithms.size()];
            for(int i = 0; algorithmsIterator.hasNext(); i++) {
                outputArray[i] =
                    new StringToken((String)algorithmsIterator.next());
            }
            output.send(0, new ArrayToken(outputArray));
        } else if (request.equals("Providers")) {
            Provider [] providers = Security.getProviders();
            Token [] outputArray = new StringToken[providers.length];
            for (int i = 0; i < providers.length; i++) {
                outputArray[i] =
                    new StringToken((String)providers[i].toString());
            }
            output.send(0, new ArrayToken(outputArray));
        } else {
            throw new IllegalActionException(this,
                    "Service request '" + request + "' is not valid.");
        }
    }
}
