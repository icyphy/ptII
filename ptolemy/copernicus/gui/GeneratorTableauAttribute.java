/* An attribute that stores the configuration of a generator tableau.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.copernicus.gui;

import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.actor.gui.style.ChoiceStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.*;
import ptolemy.moml.Documentation;

import java.io.File;
import java.lang.reflect.Field;

//////////////////////////////////////////////////////////////////////////
//// GeneratorTableauAttribute
/**
This is an attribute that stores the configuration of a generator tableau.
It contains a number of parameters that are presented to the user in
the generator tableau to configure the generator.

@author Edward A. Lee, Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/
public class GeneratorTableauAttribute extends SingletonAttribute {

    /** Construct an attribute with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public GeneratorTableauAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Create parameters, and populate them with style hints to
        // use a checkbox on screen.

        codeGenerator = new StringAttribute(this, "codeGenerator");
        Documentation doc = new Documentation(codeGenerator, "tooltip");
        doc.setValue("Type of code generator to run.");
	ChoiceStyle choiceStyle = new ChoiceStyle(codeGenerator, "style");

	// Deep first, then alphabetical
	sootDeep = new StringAttribute(choiceStyle, "sootDeep");
	sootDeep.setExpression("deep");

	// I'm not sure why this is necessary, but if we do not set
	// the expression of the codeGenerator attribute, then
	// when we start up the code generator, the combobox says
	// "deep", but codeGenerator.getExpression() returns "".
	codeGenerator.setExpression(sootDeep.getExpression());

	sootApplet = new StringAttribute(choiceStyle, "sootApplet");
	sootApplet.setExpression("applet");

	sootC = new StringAttribute(choiceStyle, "sootC");
	sootC.setExpression("c");

	sootJHDL = new StringAttribute(choiceStyle, "sootJHDL");
	sootJHDL.setExpression("jhdl");

	sootShallow = new StringAttribute(choiceStyle, "sootShallow");
	sootShallow.setExpression("shallow");

	// End of Code Generators.


        show = new Parameter(this, "show",
                new BooleanToken(true));
        new CheckBoxStyle(show, "style");
        doc = new Documentation(show, "tooltip");
        doc.setValue("Show generated code.");

        compile = new Parameter(this, "compile",
                new BooleanToken(true));
        new CheckBoxStyle(compile, "style");
        doc = new Documentation(compile, "tooltip");
        doc.setValue("Compile generated code.");

        run = new Parameter(this, "run",
                new BooleanToken(true));
        new CheckBoxStyle(run, "style");
        doc = new Documentation(run, "tooltip");
        doc.setValue("Execute generated code.");

        // Initialize the default directory.
        String defaultDirectory = "";
        String defaultClasspath = ".";
	String ptIIDirectory = null;
        try {
            // NOTE: getProperty() will probably fail in applets, which
            // is why this is in a try block.

            // Set the directory attribute.
            String cwd = System.getProperty("user.dir");
            if (cwd != null) {
                defaultDirectory = cwd;
            }

            // Identify a reasonable classpath.
            // NOTE: This property is set by the vergil startup script.
            ptIIDirectory = System.getProperty("ptolemy.ptII.dir");
            if (ptIIDirectory == null) {
                defaultClasspath = ".";
            } else {
                defaultClasspath = ptIIDirectory + File.pathSeparator + ".";
            }
        } catch (SecurityException ex) {
            // Ignore and use the default.
        }
	// FIXME: directory should be a file browser.
        directory = new StringAttribute(this, "directory");
        directory.setExpression(defaultDirectory);
        doc = new Documentation(directory, "tooltip");
        doc.setValue("Directory into which to put generated code.");

        compileOptions = new StringAttribute(this, "compileOptions");
        compileOptions.setExpression("-classpath \""
				     + defaultClasspath + "\"");
        doc = new Documentation(compileOptions, "tooltip");
        doc.setValue("Options to pass to the compiler.");

        runOptions = new StringAttribute(this, "runOptions");
        runOptions.setExpression("-classpath \"" + defaultClasspath + "\"");
        doc = new Documentation(runOptions, "tooltip");
        doc.setValue("Options to use when executing the code.");

        packageName = new StringAttribute(this, "packageName");
	// Attempt to figure out the package by comparing
	// current director and the ptIIDirectory and removing
	// the common prefix
	String packageNameString = "";
	try {
	    String canonicalPtIIDirectory =
		(new File(ptIIDirectory)).getCanonicalPath();
	    String canonicalDefaultDirectory =
		(new File(defaultDirectory)).getCanonicalPath();
	    if (canonicalDefaultDirectory.startsWith(canonicalPtIIDirectory)) {
		String packagePath =
		    canonicalDefaultDirectory
		    .substring(canonicalPtIIDirectory.length());
		packageNameString = (packagePath
				     .replace('/', '.')).replace('\\', '.');
		if (packageNameString.indexOf('.') == 0
		    && packageNameString.length() >= 2) {
		    // Strip off the leading . in the package.
		    packageNameString = packageNameString.substring(1);
		}
	    }
	} catch (Exception e) {
	    // Do nothing, stick with the empty default.
	}
        packageName.setExpression(packageNameString);
        doc = new Documentation(packageName, "tooltip");
        doc.setValue("Package name for the generated classes.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Code generator to run */
    public StringAttribute codeGenerator;

    /** If true, compile the generated code. This has type boolean, and
     *  defaults to true.
     */
    public Parameter compile;

    /** Options issued to the compile command.*/
    public StringAttribute compileOptions;

    /** The directory into which to put the generated code.*/
    public StringAttribute directory;

    /** Options issued to the java command to run the generated code.*/
    public StringAttribute packageName;

    /** If true, run the generated code. This has type boolean, and
     *  defaults to true.
     */
    public Parameter run;

    /** Options issued to the java command to run the generated code.*/
    public StringAttribute runOptions;

    /** If true, show the generated code. This has type boolean and
     *  defaults to true.
     */
    public Parameter show;

    /** The name of the applet code generator.  The default value is "applet".
     */
    public StringAttribute sootApplet;

    /** The name of the C code generator.  The default value is "c".
     */
    public StringAttribute sootC;

    /** The name of the deep code generator.  The default value is "deep".
     */
    public StringAttribute sootDeep;

    /** The name of the deep code generator.  The default value is "jhdl".
     */
    public StringAttribute sootJHDL;

    /** The name of the shallow code generator.
     *	The default value is "shallow".
     */
    public StringAttribute sootShallow;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    // FIXME: Check that directory is writable in attributeChanged.

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an attribute with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new GeneratorTableauAttribute.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        GeneratorTableauAttribute newObject = (GeneratorTableauAttribute)
            super.clone(workspace);

	/*
	// We use reflection here so that we don't have to edit
	// this method every time we add a field.
	Field fields [] = getClass().getFields();
	String fieldValue;
	for (int i = 0; i < fields.length; i++) {
	    try {
		if (fields[i].get(newObject) instanceof StringAttribute
		    || fields[i].get(newObject) instanceof Parameter) {
                    fields[i].set(newObject,
				  newObject.getAttribute(fields[i].getName()));
		}
	    } catch (IllegalAccessException e) {
                throw new CloneNotSupportedException(e.getMessage() +
                        ": " + fields[i].getName());
	    }
	}
	System.out.println("GenerateTableauAttribute.clone(): "
			   + newObject);

	return newObject;
	*/
	// Alphabetical
        newObject.codeGenerator = (StringAttribute)
            newObject.getAttribute("codeGenerator");
        newObject.compile = (Parameter)
            newObject.getAttribute("compile");
        newObject.compileOptions = (StringAttribute)
            newObject.getAttribute("compileOptions");
        newObject.directory = (StringAttribute)
            newObject.getAttribute("directory");
        newObject.packageName = (StringAttribute)
            newObject.getAttribute("packageName");
        newObject.run = (Parameter)
            newObject.getAttribute("run");
        newObject.runOptions = (StringAttribute)
            newObject.getAttribute("runOptions");
        newObject.show = (Parameter)
            newObject.getAttribute("show");

       	ChoiceStyle choiceStyle =
            (ChoiceStyle) newObject.codeGenerator.getAttribute("style");

        newObject.sootApplet = (StringAttribute)
            choiceStyle.getAttribute("sootApplet");
        newObject.sootC = (StringAttribute)
            choiceStyle.getAttribute("sootC");
        newObject.sootDeep = (StringAttribute)
            choiceStyle.getAttribute("sootDeep");
        newObject.sootJHDL = (StringAttribute)
            choiceStyle.getAttribute("sootJHDL");
        newObject.sootShallow = (StringAttribute)
            choiceStyle.getAttribute("sootShallow");

        return newObject;
    }

    /** Return a String representation of this object. */
    public String toString() {
	// We use reflection here so that we don't have to edit
	// this method every time we add a field.
	StringBuffer results = new StringBuffer();
	Field [] fields = getClass().getFields();
	String fieldValue;
	for (int i = 0; i < fields.length; i++) {
	    try {
		Object object = fields[i].get(this);
		if (object instanceof StringAttribute) {
		    fieldValue = "ptolemy.kernel.util.StringAttribute "
			+ ((StringAttribute)object).getExpression();
		} else {
                    if (object == null) {
                        fieldValue = null;
                    } else {
                        fieldValue = object.toString();
                    }
		}
	    } catch (IllegalAccessException e) {
		fieldValue = "IllegalAccessException?";
	    }
	    results.append(fields[i].getName() + " " + fieldValue
			   + "\n");
	}
	return results.toString();
    }
}
