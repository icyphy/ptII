/* Interface for objects that can be configured by reading a file.

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

@ProposedRating Green (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)

7/22/00: Downgraded from yellow (neuendor) to red by EAL.
         Changed the signature of the configure() method.
*/

package ptolemy.actor;

import java.io.InputStream;
import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// Configurable
/**
Objects that can be configured by reading a file or configuration information
given as text, typically in XML format, should implement this interface.
This enables a user interface to recognize that such file-based
configuration is possible, and permits configuration to be specified
via the MoML configure element.
For example, a plotter actor implements this interface to
allow the visual appearance of the plot to be set using PlotML.
An icon for an actor implements this interface to allow
the actor-specific visual features of the icon to be
specified using graphics markup.
Additionally, a filter actor could implement this interface to allow
the filter specification to be given using markup.
<p>
This interface can also be used for actors which load configuration
information from non-XML formats, such as GIF images or binary lookup tables.

@author Edward A. Lee
@version $Id$
@see ptolemy.actor.CompositeActor
@see ptolemy.actor.AtomicActor
*/
public interface Configurable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Configure the object with data from the specified input source
     *  (a URL) and/or textual data.
     *  This method is defined to throw a very general exception to allow
     *  classes that implement the interface to use whatever exceptions
     *  are appropriate.
     *  @param base The base relative to which references within the input
     *   are found, or null if this is not known, or there is none.
     *  @param source The input source, which specifies a URL, or null
     *   if none.
     *  @param text Configuration information given as text, or null if
     *   none.
     *  @exception Exception If something goes wrong.
     */
    public void configure(URL base, String source, String text)
            throws Exception;
}
