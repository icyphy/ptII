/* A debugging view for ptolemy models

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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.debugger;

import ptolemy.vergil.actor.ActorGraphTableau;
import ptolemy.vergil.actor.ActorGraphTableau;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.moml.MoMLParser;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;

import diva.canvas.CanvasUtilities;
import diva.canvas.Site;
import diva.canvas.Figure;
import diva.canvas.connector.FixedNormalSite;
import diva.canvas.connector.Terminal;
import diva.canvas.interactor.SelectionModel;

import diva.gui.toolbox.FocusMouseListener;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.awt.print.PrinterException;
import java.awt.print.PageFormat;

import java.io.IOException;
import java.io.StringWriter;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.KeyStroke;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

//////////////////////////////////////////////////////////////////////////
//// DebuggerTableau
/**
A debugging tableau for ptolemy models.

@author  Steve Neuendorffer
@version $Id$
*/
public class DebuggerTableau extends Tableau {

    public DebuggerTableau(PtolemyEffigy container,
            String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        NamedObj model = container.getModel();
        if (!(model instanceof CompositeActor)) {
            throw new IllegalActionException(this,
                    "Cannot debug a model that is not a CompositeActor.");
        }
	CompositeActor actor = (CompositeActor)model;

	DebuggerFrame frame = new DebuggerFrame(actor, this);
	setFrame(frame);
	frame.setBackground(BACKGROUND_COLOR);
	frame.pack();
	frame.centerOnScreen();
	frame.setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public parameters                 ////

    private static Color BACKGROUND_COLOR = new Color(0xe5e5e5);

    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    /** A factory that creates debugging tableaux for Ptolemy models.
     */
    public static class Factory extends TableauFactory {

	/** Create a factory with the given name and container.
	 *  @param container The container.
	 *  @param name The name.
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
	 *  @return A new ActorGraphTableau, if the proxy is a
	 *  PtolemyEffigy, or null otherwise.
	 *  @exception Exception If an exception occurs when creating the
	 *  tableau.
	 */
	public Tableau createTableau(Effigy proxy) throws Exception {
	    if(!(proxy instanceof PtolemyEffigy))
		return null;
	    DebuggerTableau tableau =
		new DebuggerTableau((PtolemyEffigy)proxy,
                        proxy.uniqueName("tableau"));
	    return tableau;
	}
    }
}
