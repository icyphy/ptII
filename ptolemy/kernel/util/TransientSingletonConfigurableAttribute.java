/* A nonpersistent configurable singleton attribute.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (bilung@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

import java.io.Writer;
import java.io.IOException;

//////////////////////////////////////////////////////////////////////////
//// TransientSingletonConfigurableAttribute
/**
This class is a nonpersistent configurable singleton attribute.
By "nonpersistent" we mean that it does not export MoML.
A major application of this class is to define a default icon
description.  An icon description is XML code in the SVG
(scalable vector graphics) schema, set in a configure element
in MoML.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
*/

public class TransientSingletonConfigurableAttribute
    extends SingletonConfigurableAttribute {

    /** Construct a new attribute with no
     *  container and an empty string as its name. Add the attribute to the
     *  workspace directory.
     *  Increment the version number of the workspace.
     */
    public TransientSingletonConfigurableAttribute() {
        super();
    }

    /** Construct a new attribute with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.
     *  Add the attribute to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public TransientSingletonConfigurableAttribute(Workspace workspace) {
	super(workspace);
    }

    /** Construct an attribute with the given container and name.
     *  If an attribute already exists with the same name as the one
     *  specified here, and of class SingletonConfigurableAttribute, then that
     *  attribute is removed before this one is inserted in the container.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   attribute with this name, and the class of that container is not
     *   SingletonConfigurableAttribute.
     */
    public TransientSingletonConfigurableAttribute(
            NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException  {
	super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Write a MoML description of this object, which in this case is
     *  empty.  Nothing is written.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use instead of the current name.
     */
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
    }
}
