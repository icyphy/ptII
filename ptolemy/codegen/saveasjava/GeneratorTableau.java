/* A tableau for controlling code generation.

 Copyright (c) 2000-2001 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.codegen.saveasjava;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Configurer;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.data.BooleanToken;
import ptolemy.domains.sdf.codegen.SDFCodeGenerator;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// GeneratorTableau
/**
A tableau that creates a new control panel for code generation.

@author Shuvra Bhattacharyya, Edward A. Lee, Christopher Hylands
@version $Id$
*/
public class GeneratorTableau extends Tableau {

    /** Create a new control panel for code generation.
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
    public GeneratorTableau(PtolemyEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
        NamedObj model = container.getModel();

        if (model instanceof CompositeEntity) {
            GeneratorFrame frame = new GeneratorFrame(
                    (CompositeEntity)model, this);
	    setFrame(frame);
            frame.setBackground(BACKGROUND_COLOR);
        } else {
            throw new IllegalActionException(model,
                    "Can only generate code for instances of CompositeEntity.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Default background color is a light grey.
    private static Color BACKGROUND_COLOR = new Color(0xe5e5e5);

    // List of execution wrappers that are currently active.
    private List _execs = new LinkedList();

    // The .java file should be created in this package.
    private static String _packageName = new String("");

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The frame that is created by an instance of GeneratorTableau.
     */
    public class GeneratorFrame extends PtolemyFrame {

	/** Construct a frame to control code generation for
         *  the specified Ptolemy II model.
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
	public GeneratorFrame(final CompositeEntity model, Tableau tableau)
                throws IllegalActionException, NameDuplicationException {
	    super(model, tableau);
            JPanel component = new JPanel();
            component.setLayout(new BoxLayout(component, BoxLayout.Y_AXIS));

            // Caveats panel.
            JPanel caveatsPanel = new JPanel();
            caveatsPanel.setBorder(
                    BorderFactory.createEmptyBorder(5, 0, 0, 0));
            JTextArea messageArea = new JTextArea(
                    "NOTE: This is a highly preliminary "
                    + "code generator facility, with many\n"
                    + "limitations.  It is best viewed as "
                    + "a concept demonstration.", 2, 80);
            messageArea.setEditable(false);
            messageArea.setBorder(BorderFactory.createEtchedBorder());
            caveatsPanel.add(messageArea);

            JButton moreInfoButton = new JButton("More Info");
            moreInfoButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    Configuration configuration = getConfiguration();
                    URL infoURL =
                        getClass().getResource("../../../doc/codegen.htm");
                    try {
                        configuration.openModel(
                                null, infoURL, infoURL.toExternalForm());
                    } catch (Exception ex) {
                        throw new InternalErrorException(
                                "Failed to open doc/codegen.htm: "
                                + ex);
                    }
                }
            });
            caveatsPanel.add(moreInfoButton);
            component.add(caveatsPanel);

            JPanel controlPanel = new JPanel();

            // Panel for push buttons.
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(3, 1));
            buttonPanel.setBorder(
                    BorderFactory.createEmptyBorder(10, 0, 10, 0));

            // Button panel first.
            JButton goButton = new JButton("Generate");
            goButton.setToolTipText("Generate code");
            buttonPanel.add(goButton);

            buttonPanel.add(Box.createVerticalStrut(10));

            JButton stopButton = new JButton("Cancel");
            stopButton.setToolTipText("Terminate executing processes");
            buttonPanel.add(stopButton);

            controlPanel.add(buttonPanel);

            // Add space right of the buttons
            controlPanel.add(Box.createHorizontalStrut(20));

            // Next, put in a panel to configure the code generator.
            // If the model contains an attribute with tableau
            // configuration information, use that.  Otherwise, make one.
            GeneratorTableauAttribute attribute =
                (GeneratorTableauAttribute)
                model.getAttribute("_generator",
                        GeneratorTableauAttribute.class);
            if(attribute == null) {
                attribute = new GeneratorTableauAttribute(
                        model, "_generator");
            }
            Configurer configurer = new Configurer(attribute);
            final GeneratorTableauAttribute options = attribute;
            controlPanel.add(configurer);

            component.add(controlPanel);

            // Add space under the control panel.
            component.add(Box.createVerticalStrut(10));

	    // JTextArea for compiler and run output.
	    final JTextArea text = new JTextArea("", 20, 100);
	    text.setEditable(false);
	    JScrollPane scrollPane = new JScrollPane(text);
	    component.add(scrollPane);

            getContentPane().add(component, BorderLayout.CENTER);

            stopButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    Iterator execs = _execs.iterator();
                    while(execs.hasNext()) {
                        _Exec exec = (_Exec)execs.next();
                        exec.destroy();
                        text.append("Cancelled.\n");
                    }
                }
            });

            goButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    try {
			// Handle the directory entry.
                        String directoryName = options.directory
                            .getExpression();

			// If the directory name is empty, then set
			// directory to the current working directory.
			File directory = new File(directoryName);
                        if(!directory.isDirectory()) {
                            throw new IllegalActionException(model,
                                    "Not a directory: " + directoryName);
                        }

			if (!directory.canWrite()) {
                            throw new IllegalActionException(model,
                                    "Can't write: " + directoryName);
			}
			// Handle the generator entry.
                        boolean deep = ((BooleanToken)options.deep.getToken())
                            .booleanValue();

			File destination = null;
			if (!deep) {
                            // Write the generated code.
                            destination = new File(directoryName,
                                    model.getName() + ".java");

                            FileWriter outfile = new FileWriter(destination);
                            PrintWriter outprinter = new PrintWriter(outfile);
                            outprinter.print(
                                    (new SaveAsJava()).generate(model));
                            outfile.close();
			} else {
			    // FIXME: what if this is not an SDF Model?

                            // Handle the package entry.
                            // This is out of order because we use the
                            // packageName in the generator.
                            String packageName = options.packageName
                                .getExpression();
                            if (packageName.length() > 0
                                    && ! packageName.endsWith(".") ) {
                                packageName = packageName + '.';
                            }

			    SDFCodeGenerator codeGenerator =
				new SDFCodeGenerator();
			    codeGenerator.
				setOutputDirectoryName(directoryName);
			    codeGenerator.
				setOutputPackageName(packageName);

			    // Create a manager.
			    Manager manager = ((CompositeActor)model)
				.getManager();
			    //			    if (manager == null) {

                            ((CompositeActor)model)
                                .setManager(new Manager(model.workspace(),
                                        "manager"));
                            manager = ((CompositeActor)model).getManager();
				//}

			    LinkedList models = new LinkedList();
			    models.add(model);
			    codeGenerator.setModels(models);

			    // FIXME: the output should go into the text widget
			    // FIXME: this should be run in the background
			    codeGenerator.generateCode();
			    destination = new File(
                                    codeGenerator.getPackageDirectoryName(),
                                    model.getName() + ".java");
			}
                        report("Code generation complete.");

			// Handle the show checkbox.
			boolean show = ((BooleanToken)options.show.getToken())
                            .booleanValue();
                        if (show) {
                            URL codeFile = destination.toURL();
                            Configuration config = (Configuration)toplevel();
                            // FIXME: If we previously had this file open,
                            // we need to refresh the tableau.
                            config.openModel(null, codeFile,
                                    codeFile.toExternalForm());
                        }

			// Handle the compile and run.
			boolean compile = ((BooleanToken)options.compile
                                .getToken()).booleanValue();
			boolean run = ((BooleanToken)options.run.getToken())
                            .booleanValue();

                        List execCommands = new LinkedList();

                        int index = 0;
                        if (compile) {
                            String compileOptions = options
                                .compileOptions.getExpression();
			    execCommands.add(
                                    "javac "
                                    + compileOptions
                                    + " "
                                    + directoryName
                                    + File.separatorChar
                                    + model.getName()
                                    + ".java");
                        }
                        if (run) {
                            String runOptions = options
                                .runOptions.getExpression();
                            String className = options
                                .packageName.getExpression();
                            if (className.length() > 0
                                    && ! className.endsWith(".") ) {
                                className = className + '.' + model.getName();
                            } else {
                                className = model.getName();
                            }
			    execCommands.add("java "
                                    + runOptions
                                    + " ptolemy.actor.gui"
                                    + ".CompositeActorApplication -class"
                                    + className);
                        }
                        if(execCommands.size() > 0) {
			    text.setText("");
			    _Exec exec = new _Exec(text, execCommands);
                            _execs.add(exec);

			    new Thread(exec).start();
                        }
                    } catch (Exception ex) {
                        MessageHandler.error("Code generation failed.", ex);
                    }
		}
            });
	}
    }

    /** A factory that creates a control panel for code generation.
     */
    public static class Factory extends TableauFactory {

	/** Create an factory with the given name and container.
	 *  The container argument must not be null, or a
	 *  NullPointerException will be thrown.  This entity will use the
	 *  workspace of the container for synchronization and version counts.
	 *  If the name argument is null,
	 *  then the name is set to the empty string.
	 *  Increment the version of the workspace.
	 *  @param container The container entity.
	 *  @param name The name of the entity.
	 *  @exception IllegalActionException If the container is incompatible
	 *   with this entity.
	 *  @exception NameDuplicationException If the name coincides with
	 *   an entity already in the container.
	 */
	public Factory(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
	    super(container, name);
	}

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

	/** Create a new instance of GeneratorTableau in the specified
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
	    if(effigy instanceof PtolemyEffigy) {
                // First see whether the effigy already contains a tableau
                GeneratorTableau tableau =
                    (GeneratorTableau)effigy.getEntity("generatorTableau");
                if (tableau == null) {
                    tableau = new GeneratorTableau(
                            (PtolemyEffigy)effigy, "generatorTableau");
                }
		// Don't call show() here, it is called for us in
		// TableauFrame.ViewMenuListener.actionPerformed()
                return tableau;
	    } else {
		return null;
	    }
	}
    }

    // Wrapper for exec() that runs the process in a separate thread.
    // Sample Use:
    // <pre>
    //  String[] commands = new String[2]
    //  commands[0] = "ls";
    //  commands[1] = "date";
    //  _Exec exec = new _Exec(text, commands);
    //  new Thread(exec).start();
    // </pre>
    private class _Exec implements Runnable {

	// Construct an _Exec object to run a command.
	// @param text The JTextArea to update with the command and the
	// results
	// @param commands A List of Strings that contain commands to
	// be run sequentially.
	public _Exec(JTextArea text, String command) {
	    _text = text;
            _commands = new LinkedList();
	    _commands.add(command);
	}

	// Construct an _Exec object to run a sequence of commands.
	public _Exec(JTextArea text, List commands) {
	    _text = text;
	    _commands = commands;
	}

        // Destroy the currently executing process, if there is one.
        public synchronized void destroy() {
            if (_process != null) _process.destroy();
        }

        // Execute the specified commands and report errors to the
	// JTextArea.  This will terminate the process if it has previously
        // been started.
        public void run() {
            Runtime runtime = Runtime.getRuntime();
	    try {
                if (_process != null) _process.destroy();
                Iterator commands = _commands.iterator();
                while(commands.hasNext()) {
                    final String command = (String)commands.next();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            _text.append("Executing: " + command + "\n");
                        }
                    });

                    _process = runtime.exec(command);

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            try {
                                InputStream errorStream =
                                    _process.getErrorStream();
                                BufferedReader reader =
                                    new
                                    BufferedReader(new
                                            InputStreamReader(errorStream));
                                String line;
                                while((line = reader.readLine()) != null) {
                                    _text.append(line + '\n');
                                }
                                reader.close();

                                errorStream = _process.getInputStream();
                                reader =
                                    new
                                    BufferedReader(new
                                            InputStreamReader(errorStream));
			        while((line = reader.readLine()) != null) {
                                    _text.append(line);
                                }
                                reader.close();
                            } catch (IOException io) {
                                _text.append("IOException: " + io + '\n' );
                            }
                        }
                    });

                    try {
                        int processReturnCode = _process.waitFor();
                        synchronized(this) {
                            _process = null;
                            _execs.remove(this);
                        }
                        if (processReturnCode != 0) break;
                    } catch (InterruptedException interrupted) {
                        // FIXME: this should probably be inside invokeLater.
                        // but it can't be because interrupted would
                        // need to be final
                        _text.append("InterruptedException: "
                                + interrupted + '\n' );
                    }
                }
	    } catch (final IOException io) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        _text.append("IOException: " + io + '\n' );
                    }
                });
            }
        }

	// The command to be executed
	private List _commands;

        // The process that we are running.
        private Process _process;

	// JTextArea to write the command and the output of the command.
	// _text is protected so we can get at it from within the
	// Runnables
	protected JTextArea _text;
    }
}


