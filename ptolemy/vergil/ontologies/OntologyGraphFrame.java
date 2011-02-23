/*
 * Below is the copyright agreement for the Ptolemy II system.
 * 
 * Copyright (c) 2009-2010 The Regents of the University of California. All rights
 * reserved.
 * 
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
package ptolemy.vergil.ontologies;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;

import ptolemy.actor.gui.DebugListenerTableau;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.data.ontologies.Ontology;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.BasicGraphPane;
import ptolemy.vergil.modal.FSMGraphFrame;
import diva.graph.GraphPane;

/**
 * This is a graph editor frame for lattice graphs. Given a composite entity and
 * a tableau, it creates an editor and populates the menus and toolbar. This
 * overrides the base class to associate with the editor an instance of
 * LatticeGraphFrameController.
 * 
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class OntologyGraphFrame extends FSMGraphFrame implements ActionListener {

    private static final String CHECK_LATTICE = "Check Lattice Graph";

    /**
     * Construct a frame associated with the specified lattice graph. After
     * constructing this, it is necessary to call setVisible(true) to make the
     * frame appear. This is typically done by calling show() on the controlling
     * tableau. This constructor results in a graph frame that obtains its
     * library either from the model (if it has one) or the default library
     * defined in the configuration.
     * @see Tableau#show()
     * @param entity The model to put in this frame.
     * @param tableau The tableau responsible for this frame.
     */
    public OntologyGraphFrame(CompositeEntity entity, Tableau tableau) {
        this(entity, tableau, null);
    }

    /**
     * Construct a frame associated with the specified lattice model. After
     * constructing this, it is necessary to call setVisible(true) to make the
     * frame appear. This is typically done by calling show() on the controlling
     * tableau. This constructor results in a graph frame that obtains its
     * library either from the model (if it has one), or the <i>defaultLibrary</i>
     * argument (if it is non-null), or the default library defined in the
     * configuration.
     * @see Tableau#show()
     * @param entity The model to put in this frame.
     * @param tableau The tableau responsible for this frame.
     * @param defaultLibrary An attribute specifying the default library to use
     * if the model does not have a library.
     */
    public OntologyGraphFrame(CompositeEntity entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);
    }

    protected GraphPane _createGraphPane(NamedObj entity) {
        _controller = new OntologyGraphController();
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);

        // NOTE: The cast is safe because the constructor accepts
        // only CompositeEntity.
        final OntologyGraphModel graphModel = new OntologyGraphModel(
                (CompositeEntity) entity);
        return new BasicGraphPane(_controller, graphModel, entity);
    }

    protected JMenuItem[] _debugMenuItems() {
        // Add debug menu.
        JMenuItem[] debugMenuItems = { new JMenuItem(CHECK_LATTICE,
                KeyEvent.VK_D) };
        return debugMenuItems;
    }

    protected ActionListener _getDebugMenuListener() {
        DebugMenuListener debugMenuListener = new DebugMenuListener();
        return debugMenuListener;
    }

    /** Listener for debug menu commands. */
    public class DebugMenuListener implements ActionListener {
        /** React to a menu command. */
        public void actionPerformed(ActionEvent e) {
            JMenuItem target = (JMenuItem) e.getSource();
            String actionCommand = target.getActionCommand();

            // there is only one command, which is to check to see if the
            // ontology is a lattice.
            if (actionCommand.equals(CHECK_LATTICE)) {
                Ontology ontologyModel = (Ontology) getModel();                    
                ReportOntologyLatticeStatus.showStatusAndHighlightCounterExample(
                        ontologyModel, (OntologyGraphController) _controller);
            }
        }
    }
}
