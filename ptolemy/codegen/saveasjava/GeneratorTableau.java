/* A tableau for controlling code generation.

 Copyright (c) 2000 The Regents of the University of California.
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

// FIXME: trim this.
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.domains.sdf.codegen.SDFCodeGenerator;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
            buttonPanel.setLayout(new BoxLayout(buttonPanel,
						BoxLayout.X_AXIS));
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

	    String[] generatorOptions = {"shallow", "deep"};
	    query.addRadioButtons("generator", "Generator",
				  generatorOptions, _generatorName);

            query.addLine("directory", "Destination directory",
			  _directoryName);

	    // The vergil start up script sets ptolemy.ptII.dir to $PTII
	    query.addLine("classpath", "Classpath", _classpathName);

            query.addLine("package", "Package name", _packageName);
            query.addCheckBox("show", "Show code", _show);
            query.addCheckBox("compile", "Compile code", _compile);
            query.addCheckBox("run", "Run code", _run);
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
			// Handle the directory entry.
                        _directoryName = query.stringValue("directory");

			// If the directory query is empty, then set
			// directory to the current working directory.
			File directory = _currentWorkingDirectory;
                        if (!_directoryName.trim().equals("")) {
                            directory = new File(_directoryName);
                            if(!directory.isDirectory()) {
                                throw new IllegalActionException(model,
                                "Not a directory: " + _directoryName);
                            }
                        }

			if (!directory.canWrite()) {
                                throw new IllegalActionException(model,
                                "Can't write: " + _directoryName);
			}

			// Handle the package entry.
			// This is out of order because we use the
			// packageName in the generator.
			_packageName = query.stringValue("package");
			if (_packageName.length() > 0
			    && ! _packageName.endsWith(".") ) {
			    _packageName = _packageName + '.';
			}

			// Handle the generator entry.
                        _generatorName = query.stringValue("generator");

			File destination = null; 
			if (_generatorName.equals("shallow")) {
			    // Write the generated code.
			    destination = new File(_directoryName,
						   model.getName()
						   + ".java");
			    
			    FileWriter outfile = new FileWriter(destination);
			    PrintWriter outprinter = new PrintWriter(outfile);
			    outprinter.print((new SaveAsJava()).generate(model));
			    outfile.close();
			} else if (_generatorName.equals("deep")) {
			    // FIXME: what if this is not an SDF Model? 
			    SDFCodeGenerator codeGenerator =
				new SDFCodeGenerator();
			    codeGenerator.
				setOutputDirectoryName(_directoryName);
			    codeGenerator.
				setOutputPackageName(_packageName);

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
			    // FIXME: this should be run in the backgroun
			    codeGenerator.generateCode();
			    destination = new File(codeGenerator.getPackageDirectoryName,
						   model.getName()
						   + ".java")
			} else {
			    throw new IllegalActionException(model,
			     "Unimplemented generator: " + _generatorName);
			}


			    report("Code generation complete.");


			// Handle the classpath entry.
			_classpathName = query.stringValue("classpath");
			if (_classpathName.length() > 0 
			    && !_classpathName.startsWith("-classpath")) {
			    _classpathName = "-classpath \""
				+ _classpathName + "\" ";
			}
			
			// Handle the show checkbox.
			_show = query.booleanValue("show");
                        if (_show) {
                            URL codeFile = destination.toURL();
                            Configuration config = (Configuration)toplevel();
                            // FIXME: If we previously had this file open,
                            // we need to refresh the tableau.
                            config.openModel(null, codeFile,
                                  codeFile.toExternalForm());
                        }

			// List of commands to be run
			LinkedList commands = new LinkedList();
			
			// Handle the compile checkbox.
			_compile=query.booleanValue("compile");
			String compileCommand = null;
                        if (_compile) {
			    commands.add(new String("javac "
						    + _classpathName
						    + " \""
						    + _directoryName
						    + File.separatorChar
						    + model.getName()
						    + ".java\""));
                        }
			
			// Handle the run checkbox.
			_run = query.booleanValue("run");
			String runCommand = null;
                        if (_run) {
				//FIXME: we should not need to set iterations.
			    commands.add(new String("java " 
						    + _classpathName
						    + "ptolemy.actor.gui.CompositeActorApplication "
						    + "-class " 
						    + _packageName 
						    + model.getName()
						    + " -iterations 5"));
                        }

			if (_compile || _run) {
			    // Do this in the Event thread before
			    // we start a new thread.
			    text.setText("");
			    _Exec exec = new _Exec(text, commands);
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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    // The current working directory.
    private static File _currentWorkingDirectory;

    // The name of the generator to use.
    private static String _generatorName;

    // The name of the directory to create the .java file in.
    private static String _directoryName;

    // The classpath to use when compiling and running.
    private static String _classpathName;

    // The .java file should be created in this package.
    private static String _packageName = new String("");;

    // If true, then show the .java file that is generated.
    private static boolean _show = true;

    // If true, then compile the .java file that is generated.
    private static boolean _compile = true;

    // If true, then run the .java file that is generated.
    private static boolean _run = true;

    // Initialize the static variables so that the user settings
    // are saved between invocations of the code generator.
    // Ideally, we would have a preferences manager for this.
    static {
	_generatorName = new String("shallow");

	// FIXME: getProperty() will probably fail in applets.
	_currentWorkingDirectory = new File(System.getProperty("user.dir"));
	_directoryName = _currentWorkingDirectory.toString();

	if (System.getProperty("ptolemy.ptII.dir").equals(null)) {
	    _classpathName = new String("-classpath . ");
	} else {
	    _classpathName = new String("-classpath \""
				    + System.getProperty("ptolemy.ptII.dir")
				    + File.pathSeparator
				    + ".\" ");
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
    //  LinkedList commands = new LinkedList();
    //  commands.add(new String("ls"));
    //  commands.add(new String("date"));
    //  _Exec exec = new _Exec(text, commands);
    //  new Thread(exec).start();
    // </pre>
    // FIXME: we need a way for the user to kill the thread.
    private class _Exec implements Runnable {
	
	// Construct an _Exec object to run a command.
	// @param text The JTextArea to update with the command and the
	// results
	// @param commands A List of Strings that contain commands to
	// be run sequentially.
	public _Exec(JTextArea text, List commands) {
	    _text = text;
	    _commands = commands;
	}

        // Execute the specified command and report errors to the
	// JTextArea.
        public void run() {
            Runtime runtime = Runtime.getRuntime();
	    Iterator commands = _commands.iterator();
	    while(commands.hasNext()) {
		_command = (String) commands.next();

		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			_text.append("Executing: " + _command + '\n' );
		    }
		});
		    
		try {
		    _process = runtime.exec(_command);
		} catch (final IOException io) {
		    SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    _text.append("IOException: " + io + '\n' );
			}
		    });
		}
		
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
		    _process.waitFor();
		    SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    if (_process.exitValue() == 0) {
				_text.append("\nDone.\n");
			    } else {
				_text.append("\nDone. Exit Value is: " 
					     + _process.exitValue()
					     + "\n");
			    }
			    
			}
		    });

		} catch (InterruptedException interrupted) {
		    // FIXME: this should probably be inside invokeLater.
		    // but it can't be because interrupted would
		    // need to be final
		    _text.append("InterruptedException: "
				 + interrupted + '\n' );
		}
	    }
        }

	// The command to be executed
	protected String _command;

	// JTextArea to write the command and the output of the command.
	// _text is protected so we can get at it from within the
	// Runnables
	protected JTextArea _text;

	// The List of Strings that contain commands to be executed.
	private List _commands;

	// java.lang.Process that controls the command being executed.
	private Process _process;
    }
}


