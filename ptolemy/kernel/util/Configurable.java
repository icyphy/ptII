/* Interface for objects that can be configured by reading a file.

Copyright (c) 1997-2003 The Regents of the University of California.
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
@AcceptedRating Green (bart@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// Configurable
/**
   Objects that can be configured by reading a file or configuration information
   given as text, typically in XML format, should implement this interface.
   This enables a user of a component to recognize that such file-based
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
   The <i>source</i> argument of the configure() method simply points
   to such a file.
   <p>
   This interface is designed to be reversible, so that an object can also
   provide enough information to reconstruct its current configuration.
   This mechanism is used when writing MoML from instantiated objects, although
   it could also be used to write a description of the object in other forms.
   In order for this to work properly calling the configure method on
   any object of the same type, given the data returned by the getSource and
   getText methods should result in an object that resemble the
   first as closely as possible.

   @author Edward A. Lee, Steve Neuendorffer
   @version $Id$
   @since Ptolemy II 1.0
   @see ptolemy.actor.CompositeActor
   @see ptolemy.actor.AtomicActor
*/
public interface Configurable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Configure the object with data from the specified input source
     *  (a URL) and/or textual data.  The object should interpret the
     *  source first, if it is specified, followed by the literal text,
     *  if that is specified.  The new configuration should usually
     *  override any old configuration wherever possible, in order to
     *  ensure that the current state can be successfully retrieved.
     *  <p>
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

    /** Return the input source that was specified the last time the configure
     *  method was called.
     *  @return The string representation of the input URL, or null if the
     *  no source has been used to configure this object, or null if no
     *  external source need be used to configure this object.
     */
    public String getConfigureSource();

    /** Return the text string that represents the current configuration of
     *  this object.  Note that any configuration that was previously
     *  specified using the source attribute need not be represented here
     *  as well.
     *  @return A configuration string, or null if no configuration
     *  has been used to configure this object, or null if no
     *  configuration string need be used to configure this object.
     */
    public String getConfigureText();
}
