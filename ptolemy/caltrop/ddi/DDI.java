/*
@Copyright (c) 2003 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.caltrop.ddi;

import ptolemy.actor.Executable;

//////////////////////////////////////////////////////////////////////////
//// DDI
/**
An interface for domain dependent interpretation. Each instance of
{@link ptolemy.caltrop.actors.CalInterpreter CalInterpreter} is
associated with its own <tt>DDI</tt>, which performs tasks needed to
interpret the actor in a specific domain.

 <p> <b>Note: this interface is likely to grow larger as more domains
are implemented.</b>

@author Christopher Chang <cbc@eecs.berkeley.edu>
@version $Id$
@since Ptolemy II 3.1
*/
public interface DDI extends Executable {

    /**
     * Perform static checking on the actor, ensuring its validity in
     * a given domain.
     * @return True, if the actor is legal.
     */
    boolean isLegalActor();

    /**
     * Perform any domain dependent setup. This can include hanging
     * various attributes off of the actor, for example, the rate of
     * the input and output ports.
     */
    void setupActor();

    /**
     * Get the name of the domain that this DDI implements.
     * @return The name of the domain that this DDI implements.
     */
    String getName();
}
