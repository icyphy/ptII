/* An attribute that has a string value that names a class to be instantiated by vergil

 Copyright (c) 2000-2001 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.vergil.basic;

import ptolemy.kernel.util.ClassFactoryAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// VergilClassFactoryAttribute
/**
An attribute that has a string value that names a class to be
instantiated.

<p>We use this class to avoid requiring that all classes be available at
when a MoML file is read in.  In particular, some Ptolemy II models
may contain IconControllers or AnnotationEditors that are part of
vergil.  If the model is run in a non-graphical environment, and the
vergil classes are not available, then instantiation of these classes
is deferred.

<p>For example, NodeControllerFactory could look like:
<pre>
    public NodeControllerFactory extends VergilClassFactoryAttribute {

        public NamedObjController create(GraphController controller)
               throws IllegalActionException {
            Object[] args = new Object[1];
            args[0] = controller;
            return instantiate(args);
        }
    }
</pre>
And the moml to instantiate this would look like
<pre>
     <property name="_controllerClass"
         class="ptolemy.kernel.util.VergilClassFactoryAttribute">
         value="ptolemy.vergil.basic.NodeController">
     </property>
</pre>

<p>Use setExpression() to define the value, as in for example
<pre>
  classFactoryAttribute.setExpression("ptolemy.vergil.basic.NodeController");
</pre>
<p>The default value of the string contained by this attribute is the empty
string.

<p>By default, an instance of this class is not visible
a user interface.  The invisibility is indicated to the user
interface when the user interface calls the getVisibility() method
of this class and the value Settable.NONE is returned to the user
interface.
<p>
@see Settable#NONE
@author Christopher Hylands, Edward A. Lee, Steve Neuendorffer
@version $Id$
*/

public class VergilClassFactoryAttribute extends ClassFactoryAttribute
    implements Settable {

    /** Construct an attribute in the default workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public VergilClassFactoryAttribute() {
	super();
	System.out.println("VergilClassFactorAttribute()  after super();");
    }

    /** Construct an attribute with the given name contained by the specified
     *  container. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. The object is added to the directory of the workspace
     *  if the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public VergilClassFactoryAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
	System.out.println("VergilClassFactorAttribute(" + container + "," 
			   + name + ") after super();");
    }
}
