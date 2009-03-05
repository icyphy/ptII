package ptolemy.vergil.properties;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;

import ptolemy.actor.gui.DebugListenerTableau;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.domains.properties.PropertyLatticeComposite;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.fsm.FSMGraphFrame;
import ptolemy.vergil.fsm.FSMGraphModel;
import diva.graph.GraphPane;


public class LatticeGraphFrame extends FSMGraphFrame
        implements ActionListener {

    private static final String CHECK_LATTICE = "Check Lattice";

    /** Construct a frame associated with the specified FSM model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  This constructor results in a graph frame that obtains its library
     *  either from the model (if it has one) or the default library defined
     *  in the configuration.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     */
    public LatticeGraphFrame(CompositeEntity entity, Tableau tableau) {
        this(entity, tableau, null);
    }

    /** Construct a frame associated with the specified FSM model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  This constructor results in a graph frame that obtains its library
     *  either from the model (if it has one), or the <i>defaultLibrary</i>
     *  argument (if it is non-null), or the default library defined
     *  in the configuration.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     *  @param defaultLibrary An attribute specifying the default library
     *   to use if the model does not have a library.
     */
    public LatticeGraphFrame(CompositeEntity entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);
    }

    protected GraphPane _createGraphPane(NamedObj entity) {
        _controller = new LatticeGraphFrameController();
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);

        // NOTE: The cast is safe because the constructor accepts
        // only CompositeEntity.
        final FSMGraphModel graphModel = new FSMGraphModel(
                (CompositeEntity) entity);
        return new FSMGraphPane(_controller, graphModel, entity);
    }

    protected JMenuItem[] _debugMenuItems() {
        // Add debug menu.
        JMenuItem[] debugMenuItems = {
                new JMenuItem(CHECK_LATTICE, KeyEvent.VK_D)
        };
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

            // there is only one command, which is to check lattice
            try {
                if (actionCommand.equals(CHECK_LATTICE)) {
                    Effigy effigy = (Effigy) getTableau().getContainer();

                    // Create a new text effigy inside this one.
                    Effigy textEffigy = new TextEffigy(effigy, effigy
                            .uniqueName("debug listener"));
                    DebugListenerTableau tableau = new DebugListenerTableau(
                            textEffigy, textEffigy.uniqueName("debugListener"));

                    PropertyLatticeComposite lattice = 
                        (PropertyLatticeComposite) getModel();
                    
                    tableau.setDebuggable(lattice);
                    
                    lattice.isLattice();
                }
            } catch (KernelException ex) {
                try {
                    MessageHandler.warning("Failed to create debug listener: "
                            + ex);
                } catch (CancelException exception) {
                }
            }
        }

    }
}
