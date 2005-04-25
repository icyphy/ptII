/* A tableau for displaying JVM information

Copyright (c) 2001-2005 The Regents of the University of California.
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

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


//////////////////////////////////////////////////////////////////////////
//// JVMTableau

/**
   A tableau that displays Java Virtual Machine information such
   as the version number and other properties

   @author Christopher Hylands
   @version $Id$
   @since Ptolemy II 2.0
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/
public class JVMTableau extends Tableau {
    /** Create a new control panel displaying the JVM properties
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
    public JVMTableau(PtolemyEffigy container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);

        NamedObj model = container.getModel();

        JVMFrame frame = new JVMFrame((CompositeEntity) model, this);
        setFrame(frame);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The frame that is created by an instance of JVMTableau.
     */
    public class JVMFrame extends PtolemyFrame {
        /** Construct a frame to display JVM properties.
         *  After constructing this, it is necessary
         *  to call setVisible(true) to make the frame appear.
         *  This is typically accomplished by calling show() on
         *  enclosing tableau.
         *  @param model The model to put in this frame, or null if none.
         *  @param tableau The tableau responsible for this frame.
         *  @exception IllegalActionException If the model rejects the
         *   configuration attribute.
         *  @exception NameDuplicationException If a name collision occurs.
         */
        public JVMFrame(final CompositeEntity model, Tableau tableau)
            throws IllegalActionException, NameDuplicationException {
            super(model, tableau);

            JPanel component = new JPanel();
            component.setLayout(new BoxLayout(component, BoxLayout.Y_AXIS));

            // Display total memory and free memory.
            final JTextArea memoryArea = new JTextArea("", 1, 100);
            updateMemoryStatistics(memoryArea);
            memoryArea.setEditable(false);
            component.add(memoryArea);

            // Button to request GC.
            JButton GCButton = new JButton("Request Garbage Collection");
            GCButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        // FindBugs warns about System.gc, but it is ok
                        System.gc();
                        updateMemoryStatistics(memoryArea);
                    }
                });
            component.add(GCButton);

            // Display system properties
            String lineSeparator = System.getProperty("line.separator");
            StringBuffer propertyBuffer = new StringBuffer();

            try {
                Properties properties = System.getProperties();
                Enumeration propertyNamesEnumeration = properties.propertyNames();

                // Sort by property name
                ArrayList propertyNamesList = Collections.list(propertyNamesEnumeration);
                Collections.sort(propertyNamesList);

                Iterator propertyNames = propertyNamesList.iterator();

                while (propertyNames.hasNext()) {
                    String propertyName = (String) propertyNames.next();
                    propertyBuffer.append(propertyName + " = "
                        + properties.getProperty(propertyName) + lineSeparator);
                }
            } catch (java.security.AccessControlException accessControl) {
                propertyBuffer.append("AccessControlException, probably from "
                    + "System.getProperties():\n" + accessControl);
            }

            final JTextArea messageArea = new JTextArea(propertyBuffer.toString(),
                    20, 100);

            messageArea.setEditable(false);

            JScrollPane scrollPane = new JScrollPane(messageArea);
            component.add(scrollPane);
            getContentPane().add(component, BorderLayout.CENTER);
        }
    }

    /** A factory that creates a control panel to display JVM Properties.
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

        /** Create a new instance of JVMTableau in the specified
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
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (effigy instanceof PtolemyEffigy) {
                // First see whether the effigy already contains a tableau
                JVMTableau tableau = (JVMTableau) effigy.getEntity("JVMTableau");

                if (tableau == null) {
                    tableau = new JVMTableau((PtolemyEffigy) effigy,
                            "JVMTableau");
                }

                return tableau;
            } else {
                return null;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Update the memory statistics in textArea.
    private void updateMemoryStatistics(JTextArea textArea) {
        // Report memory usage statistics.
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / 1024;
        long freeMemory = runtime.freeMemory() / 1024;
        long maxMemory = runtime.maxMemory() / 1024;

        textArea.setText("Memory: " + totalMemory + "K Free: " + freeMemory
            + "K ("
            + Math.round(
                (((double) freeMemory) / ((double) totalMemory)) * 100.0)
            + "%) Max: " + maxMemory + "K ("
            + Math.round(
                (((double) totalMemory) / ((double) maxMemory)) * 100.0) + "%)");
    }
}
