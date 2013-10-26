/* A GUI that builds the Java and Actor documentation.

 Copyright (c) 2006-2013 The Regents of the University of California.
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
package ptolemy.vergil.actor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Configurer;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.Tableau;
import ptolemy.gui.JTextAreaExec;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// DocBuilderGUI

/**
 A PtolemyFrame that builds the Java and Actor documentation.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Red (eal)
 */
public class DocBuilderGUI extends PtolemyFrame {

    /** Construct a frame to build the Java and Actor documentation.
     *  After constructing this, it is necessary to
     *  call setVisible(true) to make the frame appear.  This is
     *  typically accomplished by calling show() on enclosing tableau.
     *
     *  @param docBuilder The doc builder object associated with this builder.
     *  @param tableau The tableau responsible for this frame.
     *  @exception IllegalActionException If the model rejects the
     *   configuration attribute.
     *  @exception NameDuplicationException If a name collision occurs.
     */
    public DocBuilderGUI(final DocBuilder docBuilder, Tableau tableau)
            throws IllegalActionException, NameDuplicationException {
        super(docBuilder, tableau);

        setTitle("Ptolemy II Java and Actor Documentation Builder");

        if (getEffigy() == null) {
            throw new InternalErrorException("Cannot get an effigy!");
        }

        // Caveats panel.
        JPanel caveatsPanel = new JPanel();
        caveatsPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        caveatsPanel.setLayout(new BoxLayout(caveatsPanel, BoxLayout.X_AXIS));

        // We handle the applicationName specially so that we create
        // only the docs for the app we are running.
        Configuration configuration = getConfiguration();

        try {
            StringAttribute applicationNameAttribute = (StringAttribute) configuration
                    .getAttribute("_applicationName", StringAttribute.class);

            if (applicationNameAttribute != null) {
                _applicationName = applicationNameAttribute.getExpression();
            }
        } catch (Throwable throwable) {
            // Ignore and use the default applicationName
        }

        JTextArea messageArea = new JTextArea(
                "NOTE: Use this tool to build the Java"
                        + " and Actor Documentation"
                        + (_applicationName != null ? "for " + _applicationName
                                : "") + ".");
        messageArea.setEditable(false);
        messageArea.setBorder(BorderFactory.createEtchedBorder());
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        caveatsPanel.add(messageArea);

        JButton moreInfoButton = new JButton("More Info");
        moreInfoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                String infoFile = "ptolemy/vergil/actor/docViewerHelp.htm";
                try {
                    Configuration configuration = getConfiguration();

                    // FIXME: Help should bring this up as well.
                    URL infoURL = Thread.currentThread()
                            .getContextClassLoader().getResource(infoFile);
                    configuration.openModel(null, infoURL,
                            infoURL.toExternalForm());
                } catch (Exception ex) {
                    throw new InternalErrorException(docBuilder, ex,
                            "Failed to open " + infoFile);
                }
            }
        });
        caveatsPanel.add(moreInfoButton);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        caveatsPanel.setMaximumSize(new Dimension(500, 100));
        left.add(caveatsPanel);

        // Panel for push buttons.
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4));

        JButton goButton = new JButton("Generate");
        goButton.setToolTipText("Generate documentation");
        buttonPanel.add(goButton);

        JButton stopButton = new JButton("Cancel");
        stopButton.setToolTipText("Terminate executing processes");
        buttonPanel.add(stopButton);

        JButton clearButton = new JButton("Clear");
        clearButton.setToolTipText("Clear Log");
        buttonPanel.add(clearButton);

        buttonPanel.setMaximumSize(new Dimension(500, 50));
        left.add(buttonPanel);

        Configurer configurer = new Configurer(docBuilder);
        JPanel controlPanel = new JPanel();
        controlPanel.add(configurer);

        JScrollPane scrollPane = new JScrollPane(controlPanel);

        left.add(scrollPane, BorderLayout.CENTER);

        // Create a JTextAreaExec without Start and Cancel buttons.
        final JTextAreaExec exec = new JTextAreaExec("Documentation Builder"
                + " Commands", false);

        exec.setPreferredSize(new Dimension(500, 300));

        docBuilder.setConfiguration(configuration);

        // If we execute any commands, print the output in the text area.
        docBuilder.setExecuteCommands(exec);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                left, exec);
        splitPane.setOneTouchExpandable(true);

        // Adjust the divider so that the control panel does not
        // have a horizontal scrollbar.
        Dimension preferred = left.getPreferredSize();
        splitPane.setDividerLocation(preferred.width + 20);

        getContentPane().add(splitPane, BorderLayout.CENTER);

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                exec.cancel();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                exec.clear();
            }
        });

        goButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    exec.updateStatusBar("// Starting Doc Building"
                            + (_applicationName != null ? " for "
                                    + _applicationName : ""));

                    docBuilder.buildDocs();

                    exec.updateStatusBar(" ");
                } catch (Exception ex) {
                    MessageHandler.error("Doc Building failed.", ex);
                }
            }
        });
    }

    /** The name of the application, usually from the _applicationName
     *  StringAttribute in configuration.xml.
     *  If null, then use the default documentation in doc/codeDoc.
     */
    private String _applicationName;
}
