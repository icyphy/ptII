/* A named object that represents a ptolemy model.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.gui.MessageHandler;
import ptolemy.gui.CancelException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import javax.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// Effigy
/**
An effigy represents model data, and is contained in the model directory.
An effigy contains all open instances of Tableau associated with the model.
It also contains a string attribute named "identifier" with a value that
uniquely identifies the model. A typical choice (which depend on
the configuration) is the canonical URL for a MoML file that
describes the model.
<p>
NOTE: It might seem more natural for the identifier
to be the name of the effigy rather than the value of a string attribute.
But the name cannot have periods in it, and a URL typically does
have periods in it, and periods are not allowed in the names of
Ptolemy II objects.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@see ModelDirectory
@see Tableau
*/
public class Effigy extends CompositeEntity {

    /** Create a new proxy in the specified workspace with an empty string
     *  for its name.
     *  @param workspace The workspace for this proxy.
     */
    public Effigy(Workspace workspace) {
	super(workspace);
        try {
            identifier = new StringAttribute(this, "identifier");
            identifier.setExpression("Unnamed");
        } catch (Exception ex) {
            throw new InternalErrorException("Can't create identifier!");
        }
    }

    /** Construct an effigy with the given name and container.
     *  @param container The container.
     *  @param name The name of the effigy.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public Effigy(ModelDirectory container, String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
        identifier = new StringAttribute(this, "identifier");
        identifier.setExpression("Unnamed");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public parameters                 ////

    /** The identifier for the effigy.  The default value is "Unnamed". */
    public StringAttribute identifier;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. This calls the
     *  base class and then sets the <code>identifier</code>
     *  public members to the parameters of the new object.
     *  @param ws The workspace for the new object.
     *  @return A new object.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Effigy newobj = (Effigy)super.clone(ws);
        newobj.identifier = (StringAttribute)newobj.getAttribute("identifier");
        return newobj;
    }

    /** Override the base class so that tableaux contained by this object
     *  are removed before this effigy is removed from the ModelDirectory.
     *  This causes the frames associated with those tableaux to be 
     *  closed.
     *  @param container The directory in which to list this effigy.
     *  @exception IllegalActionException If the proposed container is not
     *   an instance of ModelDirectory, or if the superclass throws it.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the specified name.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
	if (container == null) {
	    // Remove all tableaux.
	    Iterator tableaux = entityList(Tableau.class).iterator();
	    while (tableaux.hasNext()) {
		ComponentEntity tableau = (ComponentEntity)tableaux.next();
		tableau.setContainer(null);
	    }  
	    super.setContainer(container);
	} else if(container instanceof ModelDirectory) {
	    super.setContainer(container);
	} else {
	    throw new IllegalActionException(this, container, 
		"The container can only be set to an " + 
		"instance of ModelDirectory");
	}
    }

    /** Make all tableaux associated with this model visible by raising
     *  or deiconifying them.
     */
    public void showTableaux() {
        Iterator tableaux = entityList(Tableau.class).iterator();
        while(tableaux.hasNext()) {
            Tableau tableau = (Tableau)tableaux.next();
            tableau.show();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Remove the specified entity, and if there are no more tableaux
     *  contained, then remove this object from its container.
     *  @param entity The tableau to remove.
     */
    protected void _removeEntity(ComponentEntity entity) {
	super._removeEntity(entity);
       	if(entityList(Tableau.class).size() == 0) {
	    try {
		setContainer(null);
	    } catch (Exception ex) {
		throw new InternalErrorException("Cannot remove effigy!");
	    }
	}
    }
}

