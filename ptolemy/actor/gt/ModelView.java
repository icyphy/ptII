/*  This actor opens a window to display the specified model and applies its inputs to the model.

 @Copyright (c) 1998-2007 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.actor.gt;

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URI;

import javax.swing.JFrame;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.lib.Sink;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ActorToken;
import ptolemy.data.StringToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// ModelView

/**
 This actor opens a window to display the specified model.
 If inputs are provided, they are expected to be MoML strings
 that are to be applied to the model. This can be used, for
 example, to create animations.

 @author  Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
*/
public class ModelView extends Sink implements WindowListener {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ModelView(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input.setTypeEquals(ActorToken.TYPE);

        title = new PortParameter(this, "title");
        title.setStringMode(true);
        title.setExpression("");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    public Object clone() throws CloneNotSupportedException {
        ModelView actor = (ModelView) super.clone();
        actor._parser = new MoMLParser();
        return actor;
    }

    /** Read the input, if there is any, and issue a change
     *  request to apply the MoML in the input to the displayed model.
     *  @exception IllegalActionException If there is an error reading
     *   the input.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        title.update();
        String titleValue = ((StringToken) title.getToken()).stringValue();
        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                synchronized (this) {
                    if (_tableaus[i] != null) {
                        _tableaus[i].close();
                        _tableaus[i] = null;
                    }

                    Entity model = ((ActorToken) input.get(0)).getEntity();
                    Configuration configuration = (Configuration) Configuration
                            .findEffigy(toplevel()).toplevel();
                    try {
                        _parser.reset();
                        // Export the model into moml string and then import it
                        // again. Needed b some models with unnoticeable state.
                        NamedObj newModel = _parser.parse(model.exportMoML());
                        Tableau tableau = configuration.openModel(newModel);
                        _tableaus[i] = tableau;
                        tableau.getFrame().addWindowListener(this);

                        String titleString = null;
                        if (titleValue.equals("")) {
                            URI uri = URIAttribute.getModelURI(newModel);
                            if (uri != null) {
                                URI modelURI = new URI(uri.getScheme(), uri
                                        .getUserInfo(), uri.getHost(), uri
                                        .getPort(), uri.getPath()
                                        + newModel.getName() + ".xml", null,
                                        null);
                                titleString = modelURI.toString() + " ("
                                        + getName() + ")";
                            }
                        } else {
                            titleString = titleValue;
                        }
                        tableau.setTitle(titleString);
                    } catch (NameDuplicationException e) {
                        throw new IllegalActionException(this, e,
                                "Cannot open model.");
                    } catch (Exception e) {
                        throw new IllegalActionException(this, e,
                                "Cannot parse model.");
                    }
                }
            }
        }
    }

    public void initialize() throws IllegalActionException {
        super.initialize();

        synchronized (this) {
            if (_tableaus != null) {
                for (Tableau tableau : _tableaus) {
                    if (tableau != null) {
                        tableau.close();
                    }
                }
            }

            _tableaus = new Tableau[input.getWidth()];
        }
    }

    public void windowActivated(WindowEvent e) {
    }

    public synchronized void windowClosed(WindowEvent e) {
        Window window = (Window) e.getSource();
        if (_tableaus != null) {
            for (int i = 0; i < _tableaus.length; i++) {
                if (_tableaus[i] != null) {
                    JFrame frame = _tableaus[i].getFrame();
                    if (frame == window) {
                        frame.removeWindowListener(this);
                        _tableaus[i] = null;
                    }
                }
            }
        }
    }

    public void windowClosing(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public PortParameter title;

    private MoMLParser _parser = new MoMLParser();

    private Tableau[] _tableaus;
}
