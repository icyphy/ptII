/* An Interface Automaton graph view for Ptolemy models

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

@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.fsm.ia;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.domains.fsm.kernel.InterfaceAutomaton;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.vergil.fsm.FSMGraphTableau;

import java.awt.Color;

//////////////////////////////////////////////////////////////////////////
//// InterfaceAutomatonGraphTableau
/**

@author  Steve Neuendorffer, Yuhong Xiong
@version $Id$
*/
public class InterfaceAutomatonGraphTableau extends FSMGraphTableau {

    public InterfaceAutomatonGraphTableau(PtolemyEffigy container,
            String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                          public methods                   ////

    /** Override the super class to create an instance of
     *  InterfaceAutomatonGraphFrame.
     *  @param model The Ptolemy II model to display in the graph frame.
     */
    public void createGraphFrame(CompositeEntity model) {
	InterfaceAutomatonGraphFrame frame =
            new InterfaceAutomatonGraphFrame(model, this);
	setFrame(frame);
	frame.setBackground(BACKGROUND_COLOR);
	frame.pack();
	frame.centerOnScreen();
	frame.setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    /** A factory that creates graph editing tableaux for Ptolemy models.
     */
    public static class Factory extends TableauFactory {

	/** Create an factory with the given name and container.
	 *  @param container The container.
	 *  @param name The name of the entity.
	 *  @exception IllegalActionException If the container is incompatible
	 *   with this attribute.
	 *  @exception NameDuplicationException If the name coincides with
	 *   an attribute already in the container.
	 */
	public Factory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
	    super(container, name);
	}

	/** Create a tableau in the default workspace with no name for the
	 *  given Effigy.  The tableau will created with a new unique name
	 *  in the given model proxy.  If this factory cannot create a tableau
	 *  for the given proxy (perhaps because the proxy is not of the
	 *  appropriate subclass) then return null.
	 *  @param proxy The model proxy.
	 *  @return A new InterfaceAutomatonGraphTableau, if the proxy is a
	 *  PtolemyEffigy that references an InterfaceAutomaton or null
	 *  otherwise.
	 *  @exception Exception If an exception occurs when creating the
	 *  tableau.
	 */
	public Tableau createTableau(Effigy proxy) throws Exception {
	    if(!(proxy instanceof PtolemyEffigy))
		return null;
	    PtolemyEffigy effigy = (PtolemyEffigy)proxy;
	    if(effigy.getModel() instanceof InterfaceAutomaton) {
		InterfaceAutomatonGraphTableau tableau =
		    new InterfaceAutomatonGraphTableau((PtolemyEffigy)proxy,
                            proxy.uniqueName("tableau"));
		return tableau;
	    } else {
		return null;
	    }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static Color BACKGROUND_COLOR = new Color(0xe5e5e5);
}
