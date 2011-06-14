package ptolemy.actor.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.Writer;

import javax.swing.JFrame;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public abstract class AbstactAWTPlaceable {

    public void init(TypedAtomicActor actor) throws IllegalActionException,
            NameDuplicationException {
        _windowProperties = new WindowPropertiesAttribute(actor,
                "_windowProperties");
        // Note that we have to force this to be persistent because
        // there is no real mechanism for the value of the properties
        // to be updated when the window is moved or resized. By
        // making it persistent, when the model is saved, the
        // attribute will determine the current size and position
        // of the window and save it.
        _windowProperties.setPersistent(true);
        _paneSize = new SizeAttribute(actor, "_paneSize");
        _paneSize.setPersistent(true);
    }


    /** Specify the associated frame and set its properties (size, etc.)
     *  to match those stored in the _windowProperties attribute.
     *  @param frame The associated frame.
     */
    public void setFrame(JFrame frame) {

        if (_frame != null) {
            _frame.removeWindowListener(_windowClosingAdapter);
        }

        if (frame == null) {
            _frame = null;
            return;
        }

        _frame = frame;

        _windowClosingAdapter = new WindowClosingAdapter();
        frame.addWindowListener(_windowClosingAdapter);

        _windowProperties.setProperties(_frame);

        // Regrettably, since setSize() in swing doesn't actually
        // set the size of the frame, we have to also set the
        // size of the internal component.
        Component[] components = _frame.getContentPane().getComponents();

        if (components.length > 0) {
            _paneSize.setSize(components[0]);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Free up memory when closing. */
    protected void cleanUp() {
        setFrame(null);
    }

    /** Write a MoML description of the contents of this object. This
     *  overrides the base class to make sure that the current frame
     *  properties, if there is a frame, are recorded.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    protected void _exportMoMLContents(TypedAtomicActor actor, Writer output,
            int depth) throws IOException {
        // Make sure that the current position of the frame, if any,
        // is up to date.
        if (_frame != null) {
            _windowProperties.recordProperties(_frame);

            // Regrettably, have to also record the size of the contents
            // because in Swing, setSize() methods do not set the size.
            // Only the first component size is recorded.
            Component[] components = _frame.getContentPane().getComponents();

            if (components.length > 0) {
                _paneSize.recordSize(components[0]);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The associated frame. */
    protected JFrame _frame;

    /** A specification of the size of the pane if it's in its own window. */
    protected SizeAttribute _paneSize;

    /** A specification for the window properties of the frame. */
    protected WindowPropertiesAttribute _windowProperties;

    /** A reference to the listener for removal purposes. */
    protected WindowClosingAdapter _windowClosingAdapter;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Listener for windowClosing action. */
    class WindowClosingAdapter extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            cleanUp();
        }
    }

}
