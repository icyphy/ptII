/* A tableau for controlling code generation.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.codegen.saveasjava;

// FIXME: trim this.
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.gui.CancelException;
import ptolemy.gui.MessageHandler;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Debuggable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

//////////////////////////////////////////////////////////////////////////
//// GeneratorTableau
/**
A tableau that creates a new control panel for code generation.

@author Shuvra Bhattacharyya and Edward A. Lee
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

    // FIXME: should be somewhere else?
    // Default background color is a light grey.
    private static Color BACKGROUND_COLOR = new Color(0xe5e5e5);

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
	 */
	public GeneratorFrame(final CompositeEntity model, Tableau tableau) {
	    super(model, tableau);
            JPanel component = new JPanel();

            // Panel for push buttons.
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
            buttonPanel.setBorder(
                    BorderFactory.createEmptyBorder(10, 0, 10, 0));
            buttonPanel.setAlignmentX(LEFT_ALIGNMENT);

            // Generate button.
            JButton goButton = new JButton("Generate");
            goButton.setToolTipText("Generate code");
            goButton.setAlignmentX(LEFT_ALIGNMENT);
            buttonPanel.add(goButton);

            // Next, put in a Query to set parameters.
            final Query query = new Query();
            // FIXME: getProperty() will probably fail in applets.
            final File cwd = new File(System.getProperty("user.dir"));

            query.addLine("directory", "Destination directory",
			  cwd.toString());
	    // The vergil start up script sets ptolemy.ptII.dir to $PTII
	    query.addLine("classpath",
			  "Usually, location of Ptolemy II home directory",
			  System.getProperty("ptolemy.ptII.dir")
			  + File.pathSeparator
			  + ".");
            query.addLine("package", "Package name", "");
            query.addCheckBox("show", "Show code", true);
            query.addCheckBox("compile", "Compile code", true);
            query.addCheckBox("run", "Run code", true);
	    // FIXME: we need entries for javac and java

            component.setLayout(new BoxLayout(component, BoxLayout.Y_AXIS));
            component.add(buttonPanel);
            component.add(query);
	    
	    // JTextArea for compiler and run output.
	    final JTextArea text = new JTextArea("", 20, 40);
	    text.setEditable(false);
	    JScrollPane scrollPane = new JScrollPane(text);
	    component.add(scrollPane);

            getContentPane().add(component, BorderLayout.CENTER);

            goButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    try {
                        String directoryName = query.stringValue("directory");
                        File directory = cwd;
                        if (!directoryName.trim().equals("")) {
                            directory = new File(directoryName);
                            if(!directory.isDirectory()) {
                                throw new IllegalActionException(model,
                                "Not a directory: " + directoryName);
                            }
                        }
                        // FIXME: Check that directory is writable.

                        // Write the generated code.
                        File destination = new File(directory,
                                model.getName() + ".java");

                        FileWriter outfile = new FileWriter(destination);
                        PrintWriter outprinter = new PrintWriter(outfile);
                        outprinter.print((new SaveAsJava()).generate(model));
                        outfile.close();
                        report("Code generation complete.");

                        if (query.booleanValue("show")) {
                            URL codeFile = destination.toURL();
                            Configuration config = (Configuration)toplevel();
                            // FIXME: If we previously had this file open,
                            // we need to refresh the tableau.
                            config.openModel(null, codeFile,
                                  codeFile.toExternalForm());
                        }

			String classpath = query.stringValue("classpath");
			if (classpath.length() > 0 
			    && !classpath.startsWith("-classpath")) {
			    classpath = "-classpath \"" + classpath + "\" ";
			}

                        if (query.booleanValue("compile")) {
			    text.setText("");
			    _Exec exec = new _Exec(text,
						   "javac "
						   + classpath
						   + directoryName
						   + File.separatorChar
						   + model.getName()
						   + ".java");
			    new Thread(exec).start();
                            report("Compilation complete.");
                        }
                        if (query.booleanValue("run")) {
			    String packageName =
				query.stringValue("package");
			    if (packageName.length() > 0
				&& ! packageName.endsWith(".") ) {
				packageName = packageName + '.';
			    }
			    _Exec exec =
				new _Exec(text,
					  "java " 
					  + classpath
					  + "ptolemy.actor.gui.CompositeActorApplication "
					  + "-class " 
					  + packageName 
					  + model.getName()
					  + " -iterations 5");
			    new Thread(exec).start();
                            report("Execution complete.");
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
    private class _Exec implements Runnable {
	
	// Construct an _Exec object to run a command.
	public _Exec(JTextArea text, String command) {
	    _text = text;
	    _command = command;
	}

        // Execute the specified command and report errors to the
	// JTextArea.
        public void run() {
            Runtime runtime = Runtime.getRuntime();
	    try {
		_text.append("Executing: " + _command + '\n' );

		Process process = runtime.exec(_command);

		InputStream errorStream = process.getErrorStream();
		BufferedReader reader =
		    new BufferedReader(new InputStreamReader(errorStream));
		String line;
		while((line = reader.readLine()) != null) {
		    _text.append(line + '\n');
		}
		reader.close();

		errorStream = process.getInputStream();
		reader =
		    new BufferedReader(new InputStreamReader(errorStream));
		while((line = reader.readLine()) != null) {
		    _text.append(line);
		}
		reader.close();

		try {
		    process.waitFor();
		} catch (InterruptedException interrupted) {
		    _text.append("InterruptedException: "
				+ interrupted + '\n' );
		}

	    } catch (IOException io) {
		_text.append("IOException: " + io + '\n' );
	    }
	    _text.append("Done.\n");

        }

	// The command to be executed
	private String _command;

	// JTextArea to write the command and the output of the command.
	private JTextArea _text;
    }
}

