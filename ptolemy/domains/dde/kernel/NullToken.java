/* A NullToken is a marker class used to break deadlock in certain
DDE topologies.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Red (davisj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.dde.kernel;

import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// NullToken
/**
A NullToken is a marker class used to break deadlock in certain
topologies of DDE models. A NullToken object does not represent
modeled computation but serves as an indicator that an actor in
a DDE model can safely advance time to be equal to the time stamp
associated with the NullToken.
<P>
NullTokens are not part of the Ptolemy II type lattice. As such,
a NullToken will violate type resolution if passed through a
typed IO port. For this reason, Null Tokens are placed directly
into receivers without using the send() method of TypedIOPort.
This tactic circumvents type resolution constraints and affirms
the notion that NullTokens do not represent computation and hence
should never be incorporated into actor code. Note further, that
the get() method of DDEReceiver does not return NullTokens but
instead consumes them and then proceeds until a "real" token
becomes available.

@author John S. Davis II
@version $Id$
@see ptolemy.data.Token
@see ptolemy.domains.dde.kernel.DDEReceiver
*/

public class NullToken extends Token {

}
