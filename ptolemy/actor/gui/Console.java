/* A tableau for displaying Console messages.

 Copyright (c) 2013-2014 The Regents of the University of California.
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
 */
package ptolemy.actor.gui;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// Console

/**
 * A tableau that displays Console messages. The standard output is redirected
 * to here.
 *
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 */
public class Console extends Tableau {
    /** Create a new control panel displaying Console
     *  The tableau is itself an entity contained by the effigy
     *  and having the specified name.  The frame is not made visible
     *  automatically.  You must call show() to make it visible.
     *  @param container The containing effigy.
     *  @param name The name of this tableau within the specified effigy.
     *  @exception IllegalActionException If the tableau is not acceptable
     *   to the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public Console(PtolemyEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        ConsoleFrame frame = new ConsoleFrame(this);
        setFrame(frame);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The frame that is created by an instance of Console.
     */
    @SuppressWarnings("serial")
    public static class ConsoleFrame extends TextEditor {
        /** Construct a frame to display Console.
         *  After constructing this, it is necessary
         *  to call setVisible(true) to make the frame appear.
         *  This is typically accomplished by calling show() on
         *  enclosing tableau.Console
         *  @param tableau The tableau responsible for this frame.
         *  @exception IllegalActionException If the model rejects the
         *   configuration attribute.
         *  @exception NameDuplicationException If a name collision occurs.
         */
        public ConsoleFrame(Tableau tableau) throws IllegalActionException,
        NameDuplicationException {
            super(tableau.getTitle());

            JPanel component = new JPanel();
            component.setLayout(new BoxLayout(component, BoxLayout.Y_AXIS));

            // updateMemoryStatistics(memoryArea);

            // Display total memory and free memory.
            final JTextArea textArea = new JTextArea("", 20, 80);

            ByteArrayOutputStream baos = new MyByteArrayOutputStream(textArea,
                    tableau);
            System.setOut(new PrintStream(baos));

            textArea.setText("Standard output is redirected to here.");
            textArea.setEditable(false);

            ((DefaultCaret) textArea.getCaret())
            .setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
            JScrollPane scrollPane = new JScrollPane(textArea);

            add(scrollPane);
        }

        /**
         * Redirect the standard output back when closing the frame.
         */
        @Override
        protected boolean _close() {
            System.setOut(new PrintStream(new FileOutputStream(
                    FileDescriptor.out)));
            System.out.println("Standard output is back.");
            return super._close();
        }

        ///////////////////////////////////////////////////////////////////
        ////                         inner classes                     ////

        /**
         * Customized ByteArrayOutputStream. Refresh the frame whenever new
         * print-outs come in.
         */
        public static class MyByteArrayOutputStream extends
        ByteArrayOutputStream {
            /** Create an output stream.
             *  @param textArea The textarea that gets updated.
             *  @param tableau The tableau containing the text area.
             */
            public MyByteArrayOutputStream(JTextArea textArea, Tableau tableau) {
                super();
                _textArea = textArea;
                _tableau = tableau;
            }

            @Override
            public void write(int b) {
                super.write(b);
                _textArea.setText(this.toString());
                _tableau.show();
            }

            @Override
            public void write(byte[] b, int off, int len) {
                super.write(b, off, len);
                _textArea.setText(this.toString());
                _tableau.show();
            }

            private JTextArea _textArea;
            private Tableau _tableau;

        }
    }

    /** A factory that creates a control panel to display Console.
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

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Create a new instance of Console in the specified
         *  effigy. If the specified effigy is not an
         *  instance of PtolemyEffigy, then do not create a tableau
         *  and return null. It is the responsibility of callers of
         *  this method to check the return value and call show().
         *
         *  @param effigy The model effigy.
         *  @return A new control panel tableau if the effigy is
         *    a PtolemyEffigy, or null otherwise.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
         */
        @Override
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (effigy instanceof PtolemyEffigy) {
                // First see whether the effigy already contains a tableau
                Console tableau = (Console) effigy.getEntity("ConsoleTableau");

                if (tableau == null) {
                    tableau = new Console((PtolemyEffigy) effigy,
                            "ConsoleTableau");
                }

                return tableau;
            } else {
                return null;
            }
        }
    }
}
