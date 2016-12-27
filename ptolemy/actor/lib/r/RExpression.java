/*
 * Copyright (c) 2003-2010 The Regents of the University of California.
 * All rights reserved.
 *
 * '$Author$'
 * '$Date$' 
 * '$Revision$'
 * 
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies
 * of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 * CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 * ENHANCEMENTS, OR MODIFICATIONS.
 *
 */

package ptolemy.actor.lib.r;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.kepler.util.DotKeplerManager;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.BrowserLauncher;
import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanMatrixToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.FloatToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.toolbox.TextEditorTableauFactory;
//import util.WorkflowExecutionListener;

////RExpression
/**
 * The RExpression actor is an actor designed to run an R script or function
 * with inputs and outputs determined by the ports created by the user. Port
 * names will correspond to R object names. The RExpression actor is modeled
 * after the Ptolemy expression actor, except that that instead of using a
 * single mathematical expression in Ptolemy's expression language, it uses a
 * set of the more powerful, higher order expressions available under R. Both
 * input and output port will usually be added to the actor; The names of these
 * ports correspond to R variables used in the R script.
 * 
 * @author Dan Higgins, NCEAS, UC Santa Barbara
 * @UserLevelDocumentation This actor let the user insert R scripts in a Kepler
 *                         workflow. It requires the R system to be installed on
 *                         the computer executing the workflow
 * @version $Id$
 * @since Ptolemy II 11.0
 */

public class RExpression extends TypedAtomicActor {

    /**
     * Construct an actor with the given container and name.
     * 
     * @param container
     *            The container.
     * @param name
     *            The name of this actor.
     * @exception IllegalActionException
     *                If the actor cannot be contained by the proposed
     *                container.
     * @exception NameDuplicationException
     *                If the container already has an actor with this name.
     */
    public RExpression(CompositeEntity container, String name)
	throws NameDuplicationException, IllegalActionException {
	super(container, name);

	expression = new StringAttribute(this, "expression");
	expression.setDisplayName("R function or script");
	new TextStyle(expression, "R Expression"); //looks odd, but gives us the larger text area
	expression.setExpression("a <- c(1,2,3,5)\nplot(a)");

	// use the text editor when we "open" the actor
	TextEditorTableauFactory _editorFactory = new TextEditorTableauFactory(
									       this, "_editorFactory");
	_editorFactory.attributeName.setExpression("expression");

	Rcwd = new StringParameter(this, "Rcwd");
	Rcwd.setDisplayName("R working directory");
	//Rcwd.setExpression( DotKeplerManager.getInstance()
	//		.getTransientModuleDirectory("r").toString() );

	save_nosave = new StringParameter(this, "save_nosave");
	save_nosave.setDisplayName("Save or not");
	save_nosave.setExpression(_NO_SAVE);
	save_nosave.addChoice(_NO_SAVE);
	save_nosave.addChoice(_SAVE);

	graphicsFormat = new StringParameter(this, "graphicsFormat");
	graphicsFormat.setDisplayName("Graphics Format");
	graphicsFormat.setExpression("png");
	graphicsFormat.addChoice("pdf");
	graphicsFormat.addChoice("png");
	graphicsFormat.addChoice("jpg");
	graphicsFormat.addChoice("bmp");
	graphicsFormat.addChoice("tiff");
	graphicsFormat.addChoice("eps");
	graphicsFormat.addChoice("ps");
	//graphicsFormat.addChoice("wmf");
	graphicsFormat.addChoice("svg");
	//graphicsFormat.addChoice("fig");
	graphicsFormat.addChoice("ghostscript bitmap type pngalpha");
	graphicsFormat.addChoice("ghostscript bitmap type png16m");
	graphicsFormat.addChoice("ghostscript bitmap type png256");

	// restore parameter is removed for now because it doesn't work
	// .RData is saved in the working directory by 'save' but R doesn't look
	// there
	// To restore a saved workspace, add the command 'load(".RData') to the
	// script
	// restore_norestore = new StringParameter(this, "restore or not");
	// restore_norestore.setExpression(_NO_RESTORE);
	// restore_norestore.addChoice(_NO_RESTORE);
	// restore_norestore.addChoice(RESTORE);

	graphicsOutput = new Parameter(this, "graphicsOutput");
	graphicsOutput.setDisplayName("Graphics Output");
	graphicsOutput.setTypeEquals(BaseType.BOOLEAN);
	graphicsOutput.setToken(BooleanToken.TRUE);

	displayGraphicsOutput = new Parameter(this, "displayGraphicsOutput");
	displayGraphicsOutput.setDisplayName("Automatically display graphics");
	displayGraphicsOutput.setTypeEquals(BaseType.BOOLEAN);
	displayGraphicsOutput.setToken(BooleanToken.FALSE);

	numXPixels = new StringParameter(this, "numXPixels");
	numXPixels.setDisplayName("Number of X pixels in image");
	numXPixels.setExpression("480");
	numYPixels = new StringParameter(this, "numYPixels");
	numYPixels.setDisplayName("Number of Y pixels in image");
	numYPixels.setExpression("480");

	graphicsFileName = new TypedIOPort(this, "graphicsFileName", false,
					   true);
	graphicsFileName.setTypeEquals(BaseType.STRING);

	output = new TypedIOPort(this, "output", false, true);
	output.setTypeEquals(BaseType.STRING);

    }

    /** The log. */
    public static Log log = LogFactory.getLog(RExpression.class);

    ///////////////////////////////////////////////////////////////////
    ////            ports and parameters                           ////

    /**
     * The output port.
     */
    public TypedIOPort output;

    /**
     * The expression that is evaluated to produce the output.
     */
    public StringAttribute expression;

    /**
     * This setting determines whether or not to save the R workspace when R is
     * closed; set to '--save' if you need to retreive the workspace later in a
     * workflow in another RExpression actor.
     */
    public StringParameter save_nosave;

    /**
     * The 'R' working directory (home dir by default).
     */
    public StringParameter Rcwd;

    /**
     * If <i>true</i>, then display plot. If <i>false</i>, then don't. (the
     * default).
     */
    public Parameter displayGraphicsOutput;

    /**
     * The graphics output format. Currently the format is either a *.pdf or a
     * *.png
     */
    public StringParameter graphicsFormat;

    /**
     * If <i>true</i>, then create a graphics output port. (the default); If
     * <i>false</i>, then don't.
     */
    public Parameter graphicsOutput;

    /**
     *The width of the output graphics bitmap in pixels.
     */
    public StringParameter numXPixels;

    /**
     * The height of the output graphics bitmap in pixels.
     */
    public StringParameter numYPixels;

    /**
     * The name of the default graphics output file created by the actor.
     */
    public TypedIOPort graphicsFileName;

    /**
     * Override the base class to set type constraints.
     * 
     * @param workspace
     *            The workspace for the new object.
     * @return A new instance of RExpression.
     * @exception CloneNotSupportedException
     *                If a derived class contains an attribute that cannot be
     *                cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
	RExpression newObject = (RExpression) super.clone(workspace);
	newObject.output.setTypeEquals(BaseType.STRING);
	newObject.graphicsFileName.setTypeEquals(BaseType.STRING);
	return newObject;
    }

    /*
     * The fire method should first call the superclass. Then all the input
     * ports should be scanned to see which ones have tokens. The names of those
     * ports should be used to create a named R object (array?). R script for
     * creating objects corresponding to these ports should be inserted before
     * the script in the expressions parameter. Then the R engine should be
     * started and run, with the output sent to the output port.
     */
    public synchronized void fire() throws IllegalActionException {
	String newline = System.getProperty("line.separator");

	super.fire();

	boolean graphicsOutputValue = ((BooleanToken) graphicsOutput.getToken())
	    .booleanValue();

	boolean displayGraphicsOutputValue = ((BooleanToken) displayGraphicsOutput
					      .getToken()).booleanValue();

	_saveString = save_nosave.stringValue();
	// _restoreString = restore_norestore.stringValue();

	String graphicsFormatString = graphicsFormat.stringValue();

	// following line insures that graphics is pdf if automatically
	// displayed
	// NOT going to automatically do this anymore. Do what the user asks.
	//if (displayGraphicsOutputValue)
	//	graphicsFormatString = "pdf";

	// force file format to 'pdf' is this is a Mac
	// NOT going to force PDF for Mac - not sure why this was in place (legacy?)
	//		String lcOSName = System.getProperty("os.name").toLowerCase();
	//		boolean MAC_OS_X = lcOSName.startsWith("mac os x");
	//		if (MAC_OS_X) {
	//			graphicsFormatString = "pdf";
	//		}

	String nxs = numXPixels.stringValue();
	try {
	    (new Integer(nxs)).intValue();
	} catch (Exception w) {
	    nxs = "480";
	}

	String nys = numYPixels.stringValue();
	try {
	    (new Integer(nys)).intValue();
	} catch (Exception w1) {
	    nys = "480";
	}

	String setCWD = "setwd('" + _home + "')\n";
	_graphicsOutputFile = _getUniqueFileName(graphicsFormatString);
	String graphicsDevice = "";

	if (graphicsOutputValue) {
	    // Why not move this stuff up to the try statements for nxs and nys?
	    // It looks like we're doing this twice. --Oliver
	    int nxp = (new Integer(nxs)).intValue();
	    double nxd = nxp / 72.0;
	    int nyp = (new Integer(nys)).intValue();
	    double nyd = nyp / 72.0;

	    if (graphicsFormatString.equals("pdf")) {
		graphicsDevice = "pdf(file = '" + _graphicsOutputFile + "'"
		    + ", width = " + nxd + ", height = " + nyd + ")";
	    } else if (graphicsFormatString.equals("jpeg") || graphicsFormatString.equals("jpg")) {
		graphicsDevice = "jpeg(filename = '" + _graphicsOutputFile + "'"
		    + ", width = " + nxs + ", height = " + nys + ")";
	    } else if (graphicsFormatString.equals("png")) {
		graphicsDevice = "png(file = '" + _graphicsOutputFile + "'"
		    + ", width = " + nxs + ", height = " + nys + ")";
	    } else if (graphicsFormatString.equals("bmp")) {
		graphicsDevice = "bmp(filename = '" + _graphicsOutputFile + "'"
		    + ", width = " + nxs + ", height = " + nys + ")";
	    } else if (graphicsFormatString.equals("tiff") || graphicsFormatString.equals("tif")) {
		graphicsDevice = "tiff(filename = '" + _graphicsOutputFile + "'"
		    + ", width = " + nxs + ", height = " + nys + ")";
	    } else if (graphicsFormatString.equals("postscript") || graphicsFormatString.equals("ps")) {
		_graphicsOutputFile = _getUniqueFileName("ps");
		graphicsDevice = "postscript(file = '" + _graphicsOutputFile + "'"
		    + ", width = " + nxd + ", height = " + nyd + ")";
	    } else if (graphicsFormatString.equals("eps")) {
		graphicsDevice = "setEPS()\n";
		graphicsDevice += "postscript(file = '" + _graphicsOutputFile + "'"
		    + ", width = " + nxd + ", height = " + nyd + ")";
	    } else if (graphicsFormatString.equals("win.metafile") || graphicsFormatString.equals("wmf")) {
		_graphicsOutputFile = _getUniqueFileName("wmf");
		graphicsDevice = "win.metafile(filename = '" + _graphicsOutputFile + "'"
		    + ", width = " + nxd + ", height = " + nyd + ")";
	    } else if (graphicsFormatString.equals("svg")) {
		graphicsDevice = "svg(filename = '" + _graphicsOutputFile + "'"
		    + ", width = " + nxd + ", height = " + nyd + ")";
	    } else if (graphicsFormatString.equals("xfig") || graphicsFormatString.equals("fig")) {
		_graphicsOutputFile = _getUniqueFileName("fig");
		graphicsDevice = "xfig(file = '" + _graphicsOutputFile + "'"
		    + ", width = " + nxd + ", height = " + nyd + ")";
	    } else if (graphicsFormatString.equals("ghostscript bitmap type pngalpha")) {
		graphicsDevice = "bitmap(file = '" + _graphicsOutputFile
		    + "', type = \"pngalpha\", width = " + nxd
		    + ", height = " + nyd + ")";
	    } else if (graphicsFormatString.equals("ghostscript bitmap type png16m")) {
		graphicsDevice = "bitmap(file = '" + _graphicsOutputFile
		    + "', type = \"png16m\", width = " + nxd
		    + ", height = " + nyd + ")";
	    } else if (graphicsFormatString.equals("ghostscript bitmap type png256")) {
		graphicsDevice = "bitmap(file = '" + _graphicsOutputFile
		    + "', type = \"png256\", width = " + nxd
		    + ", height = " + nyd + ")";
	    }
	}
	List ipList = inputPortList();
	Iterator iter_i = ipList.iterator();
	_opList = outputPortList();
	_iterO = _opList.iterator();
	String RPortInfo = "";
	RPortInfo = setCWD + graphicsDevice + "\n";
	Token at;
	String temp1;
	while (iter_i.hasNext()) {
	    TypedIOPort tiop = (TypedIOPort) iter_i.next();
	    int multiPortSize = tiop.numberOfSources();
	    List sourcePorts = tiop.sourcePortList();
	    for (int i = 0; i < multiPortSize; i++) {
		try {
		    if (tiop.hasToken(i)) {
			String finalPortName = tiop.getName();
			String sourcePortName = ((TypedIOPort) sourcePorts
						 .get(i)).getName();
			String tempPortName = tiop.getName();
			String temp = tiop.getName();
			Token token = tiop.get(i);
			String token_type_string = token.getType().toString();
			String token_class_name = token.getType()
			    .getTokenClass().getName();
			// if this is a multiport, use the upstream source for
			// the variable name
			if (tiop.isMultiport()) {
			    temp = temp + i;
			    tempPortName = temp;
			}
			// log.debug("token_type_string - " +
			// token_type_string);
			// log.debug("token_class_name - " +
			// token_class_name);
			// check token type and convert to R appropriately
			if (token_type_string.equals("string")) {
			    // check for special strings that indicate dataframe
			    // file reference
			    at = (Token) token;
			    temp1 = at.toString();
			    temp1 = temp1.substring(1, temp1.length() - 1); // remove
			    // quotes
			    if (temp1.startsWith("_dataframe_:")) {
				// assume that the string for a dataframe file
				// reference is of the form
				// '_dataframe_:"+<filename>
				temp1 = temp1.substring(12); // should be
				// filename
				// temp = "`" + temp + "` <- " +
				// "read.table(file='"+temp1+"')";
				// RPortInfo = RPortInfo + temp + "\n";
				// use binary version that was serialized
				RPortInfo = RPortInfo + "conn <- file('"
				    + temp1 + "', 'rb');\n`" + temp
				    + "` <- unserialize(conn);\n"
				    + "close(conn);\n";

				// remove the transfer file when we are done
				// consuming it
				// this is problematic when dataframes are
				// output to multiple sinks!
				// String removeCommand = "file.remove('" +
				// temp1 + "')";
				// RPortInfo = RPortInfo + removeCommand + "\n";
				continue; // stop for this token and go to the
				// next
			    } else if (temp1.startsWith("_object_:")) {
				// assume that the string for an object file
				// reference is of the form
				// '_object_:"+<filename>
				temp1 = temp1.substring(9); // should be
				// filename
				// use binary version that was serialized
				RPortInfo = RPortInfo + "conn <- file('"
				    + temp1 + "', 'rb');\n`" + temp
				    + "` <- unserialize(conn);\n"
				    + "close(conn);\n";
				// remove the transfer file when we are done
				// consuming it
				// this is problematic when objects are output
				// to multiple sinks!
				// String removeCommand = "file.remove('" +
				// temp1 + "')";
				// RPortInfo = RPortInfo + removeCommand + "\n";
				continue; // stop for this token and go to the
				// next
			    }
			}
			if (token instanceof RecordToken) {
			    String Rcommands = _recordToDataFrame(
								  (RecordToken) token, temp);
			    Rcommands = _breakIntoLines(Rcommands);
			    RPortInfo = RPortInfo + Rcommands + "\n";
			}

			// convert Kepler matrices to R matrices
			else if ((token_class_name.indexOf("IntMatrixToken") > -1)
				 || (token_class_name
				     .indexOf("DoubleMatrixToken") > -1)
				 || (token_class_name
				     .indexOf("BooleanMatrixToken") > -1)) {
			    int rows = ((MatrixToken) token).getRowCount();
			    int cols = ((MatrixToken) token).getColumnCount();
			    temp1 = token.toString();
			    temp1 = temp1.replace('\\', '/');
			    temp1 = temp1.replace('[', '(');
			    temp1 = temp1.replace(']', ')');
			    temp1 = temp1.replace(';', ',');
			    temp1 = temp1.replace('"', '\'');
			    // assume that the token's string value might be
			    // 'nil' for a missing value
			    temp1 = temp1.replaceAll("nil", "NA");
			    // TO DO:if string is long, should create a temp
			    // file for passing array data
			    temp = "`" + temp + "` <- matrix(c" + temp1 +
				", nrow=" + rows + ",ncol=" + cols + ")";
			    temp = _breakIntoLines(temp);
			    RPortInfo = RPortInfo + temp + "\n";
			} else if ((token_type_string.equals("double"))
				   || (token_type_string.equals("int"))
				   || (token_type_string.equals("string"))) {

			    at = (Token) token;
			    temp1 = at.toString();
			    // we need to check here if we are passing a string
			    // like '/t' (tab)
			    // Note that quotes are returned around string
			    // tokens
			    // The string "/t" is particularly meaningful when
			    // passed as a seperator
			    // for R expressions -- DFH April 19
			    // Note that previous versions of PTII returned
			    // slightly different format
			    // strings, so this was not necessary.
			    if (!temp1.equals("\"\\t\"")) {
				temp1 = temp1.replace('\\', '/');
				// assume that the token's string value might be
				// 'nil' for a missing value
				temp1 = temp1.replaceAll("nil", "NA");
			    }
			    temp = "`" + temp + "` <- " + temp1;
			    RPortInfo = RPortInfo + temp + "\n";
			} else if ((token_type_string.equals("boolean"))) {
			    at = (Token) token;
			    temp1 = at.toString();
			    // ensure uppercase for boolean
			    temp1 = temp1.toUpperCase();
			    temp = "`" + temp + "` <- " + temp1;
			    RPortInfo = RPortInfo + temp + "\n";
			} else if ((token_type_string.equals("float"))) {
			    FloatToken ft = (FloatToken) token;
			    DoubleToken dt = new DoubleToken(ft.doubleValue());
			    at = (Token) token;
			    temp1 = dt.toString();
			    // we need to check here if we are passing a string
			    // like '/t' (tab)
			    // Note that quotes are returned around string
			    // tokens
			    // The string "/t" is particularly meaningful when
			    // passed as a seperator
			    // for R expressions -- DFH April 19
			    // Note that previous versions of PTII returned
			    // slightly different format
			    // strings, so this was not necessary.
			    if (!temp1.equals("\"\\t\"")) {
				temp1 = temp1.replace('\\', '/');
				// assume that the token's string value might be
				// 'nil' for a missing value
				temp1 = temp1.replaceAll("nil", "NA");
			    }
			    temp = "`" + temp + "` <- " + temp1;
			    RPortInfo = RPortInfo + temp + "\n";
			} else if ((token_type_string.equals("{double}"))
				   || (token_type_string.equals("{int}"))
				   || (token_type_string.startsWith("arrayType(double"))
				   || (token_type_string.startsWith("arrayType(int"))
				   || (token_type_string.startsWith("arrayType(niltype"))
				   || (token_type_string.startsWith("arrayType(arrayType(double"))
				   || (token_type_string.startsWith("arrayType(arrayType(int"))) {
			    // token is an arrayToken !!!
			    at = (Token) token;
			    temp1 = at.toString();
			    temp1 = temp1.replace('\\', '/');
			    temp1 = temp1.replaceFirst("\\{", "(");
			    temp1 = temp1.replaceAll("\\{", "c(");
			    temp1 = temp1.replace('}', ')');
			    temp1 = temp1.replace('"', '\'');
			    // assume that the token's string value might be
			    // 'nil' for a missing value
			    temp1 = temp1.replaceAll("nil", "NA");
			    // if string is long, create a temp file for passing
			    // array data
			    if (temp1.length() > _maxCommandLineLength
				&& (!token_type_string.startsWith("arrayType(arrayType(double"))
				&& (!token_type_string.startsWith("arrayType(arrayType(int"))) {
				temp1 = temp1.replace('(', ' ');
				temp1 = temp1.replace(')', ' ');
				String filename = _writeDataFile(temp1);
				temp = "`" + temp + "` <- scan('" + filename
				    + "', sep=',')";
				temp = temp + "\n" + "file.remove('" + filename
				    + "')";
				RPortInfo = RPortInfo + temp + "\n";
			    } else { // otherwise use the modified string
				if (token_type_string.startsWith("arrayType(arrayType(int")
				    || token_type_string.startsWith("arrayType(arrayType(double")) {
				    temp = "`" + temp + "` <- list" + temp1;
				} else {
				    temp = "`" + temp + "` <- c" + temp1;
				}
				temp = _breakIntoLines(temp);
				RPortInfo = RPortInfo + temp + "\n";
			    }
			} else if ((token_type_string.equals("{float}"))
				   || (token_type_string
				       .startsWith("arrayType(float"))
				   || token_type_string.startsWith("arrayType(arrayType(float")) {
			    // token is an arrayToken !!!
			    ArrayToken arrtok = (ArrayToken) token;
			    StringBuffer buffer = new StringBuffer("{");
			    for (int j = 0; j < arrtok.length(); j++) {
				FloatToken ft = (FloatToken) arrtok
				    .getElement(j);
				buffer.append(new DoubleToken(ft.doubleValue())
					      .toString());
				if (j < (arrtok.length() - 1)) {
				    buffer.append(", ");
				}
			    }
			    buffer.append("}");

			    temp1 = buffer.toString();
			    temp1 = temp1.replace('\\', '/');
			    temp1 = temp1.replaceFirst("\\{", "(");
			    temp1 = temp1.replaceAll("\\{", "c(");
			    temp1 = temp1.replace('}', ')');
			    temp1 = temp1.replace('"', '\'');
			    // assume that the token's string value might be
			    // 'nil' for a missing value
			    temp1 = temp1.replaceAll("nil", "NA");
			    // if string is long, create a temp file for passing
			    // array data
			    if (temp1.length() > _maxCommandLineLength
				&& (!token_type_string.startsWith("arrayType(arrayType(float"))) {
				temp1 = temp1.replace('(', ' ');
				temp1 = temp1.replace(')', ' ');
				String filename = _writeDataFile(temp1);
				temp = "`" + temp + "` <- scan('" + filename
				    + "', sep=',')";
				temp = temp + "\n" + "file.remove('" + filename
				    + "')";
				RPortInfo = RPortInfo + temp + "\n";
			    } else { // otherwise use the modified string
				if (token_type_string.startsWith("arrayType(arrayType(float")) {
				    temp = "`" + temp + "` <- list" + temp1;
				} else {
				    temp = "`" + temp + "` <- c" + temp1;
				}
				temp = _breakIntoLines(temp);
				RPortInfo = RPortInfo + temp + "\n";
			    }
			} else if ((token_type_string.equals("{string}"))
				   || (token_type_string
				       .startsWith("arrayType(string")
				       || (token_type_string
					   .startsWith("arrayType(arrayType(string")))) {
			    // token is an arrayToken !!!
			    at = (Token) token;
			    temp1 = at.toString();
			    temp1 = temp1.replace('\\', '/');
			    temp1 = temp1.replaceFirst("\\{", "(");
			    temp1 = temp1.replaceAll("\\{", "c(");
			    temp1 = temp1.replace('}', ')');
			    temp1 = temp1.replace('"', '\'');
			    // assume that the token's string value might be
			    // 'nil' for a missing value
			    temp1 = temp1.replaceAll("nil", "NA");
			    // if string is long, create a temp file for passing
			    // array data ONLY
			    if ((temp1.length() > _maxCommandLineLength)
				&& (!token_type_string.startsWith("arrayType(arrayType(string"))) {
				temp1 = temp1.replace('(', ' ');
				temp1 = temp1.replace(')', ' ');
				String filename = _writeDataFile(temp1);
				temp = "`" + temp
				    + "` <- scan('"
				    + filename
				    + "', what='character', sep=',', strip.white=TRUE)";
				temp = temp + "\n" + "file.remove('" + filename
				    + "')";
				RPortInfo = RPortInfo + temp + "\n";
			    } else { // otherwise use the modified string
				//for arrays of arrays, use list()
				if (token_type_string.startsWith("arrayType(arrayType(string")) {
				    temp = "`" + temp + "` <- list" + temp1;
				}
				else {
				    temp = "`" + temp + "` <- c" + temp1;
				}
				temp = _breakIntoLines(temp);
				RPortInfo = RPortInfo + temp + "\n";
			    }
			}
			else if (token_type_string.equals("niltype")) {
			    at = token;
			    temp1 = at.toString();
			    temp1 = temp1.replaceAll("nil", "NA");
			    temp = "`" + temp + "` <- " + temp1;
			    RPortInfo = RPortInfo + temp + "\n";
			}
			// set metadata on the R objects
			// String metadataCommand = null;
			if (tiop.isMultiport()) {
			    // set the metadata on each list item
			    // ("tempPortName") before adding it to the list
			    /* metadataCommand = */_createMetadataCommand(
									  tempPortName, "name", sourcePortName);
			} else {
			    // just set the metadata attribute for the final
			    // variable name
			    /* metadataCommand = */_createMetadataCommand(
									  finalPortName, "name", sourcePortName);
			}
			// add the metadata attribute to the R object
			// leinfelder, 4/14/2008:
			// do not include the metadata as it introduces
			// incompatibility with
			// certain R methods that expect attributeless objects
			// (barplot(vector))
			// RPortInfo = RPortInfo + metadataCommand + "\n";

			// use lists for making multiport input available in R
			if (tiop.isMultiport()) {
			    String commandList = null;
			    if (i == 0) {
				// create list
				commandList = "`" + finalPortName + "` <- list("
				    + tempPortName + ")";
			    } else if (i > 0) {
				// append to list
				commandList = "`" + finalPortName + "` <- c("
				    + finalPortName + ", list("
				    + tempPortName + ") )";
			    }
			    RPortInfo = RPortInfo + commandList + "\n";
			}
		    }
		} catch (IllegalActionException iae) {
		    // just continue (port is probably not connected)
		}
	    }// for multiport
	}
	// log.debug("RPortInfo: "+RPortInfo);
	// The following command casues R to output a series of 4 dashes which
	// are used as a marker
	// Any R output after this marker is used to construct information for
	// the actor output
	// ports. This information is removed from the R output text displayed
	// to the user.
	StringBuffer r_out = new StringBuffer("cat('----\\n')\n");

	// Ensure that output is echoed from this point on
	// We don't need to echo before cat('----\\n') because the cat statement forces output.
	// This way, the options(echo = TRUE) isn't sent to the "output" port
	r_out.append("options(echo = TRUE)\n");
	// The following creates an R function called 'myput' to output port
	// info to output ports
	// r_out = r_out +
	// "if (class(x)=='data.frame') {write.table(x,file='"+df_fn+"');cat('_dataframe_:"+df_fn+"')}\n";
	r_out.append("myput <- function(x, filename) {\n"
	    // I'm wrapping the serialization into the doserialize function
	    // because it's gotten big.  Unique filename generation is 
	    // done here because this is where file creation is actually done.
	    // This code relies on the replaceAll code and the added - in the
	    // auto-generated .sav filename.  Remember that a \ in the regular 
	    // expression is quadrupled for passing through both Java and R.
	    + "  doserialize <- function(x, filename) {\n"
	    + "    if (file.exists(filename)) {"
	    + "      path <- dirname(filename); "
	    + "      filename <- basename(filename); "
	    + "      base <- sub('^(.*-)([0-9*])\\\\.(.*)$', '\\\\1', filename); "
	    + "      ext <- sub('^(.*-)([0-9*])\\\\.(.*)$', '\\\\3', filename); "
	    + "      dir_base_ext <- dir(pattern = paste(base, '[0-9]*\\\\.', ext, sep = '')); "
	    + "      cnt <- max(as.numeric(sub('^(.*-)([0-9*])\\\\.(.*)$', '\\\\2', dir_base_ext)), na.rm = TRUE) + 1; "
	    + "      filename <- file.path(path, paste(base, cnt, '.', ext, sep = ''))"
	    + "    }\n"
	    + "    conn <- file(filename, 'wb');"
	    + "    serialize(x, conn);"
	    + "    close(conn);"
	    + "    filename"
	    + "  }\n"
	    // use a binary serialization for data frames
	    + "  if (class(x)=='data.frame') {cat('_dataframe_:', doserialize(x, filename), '\\n', sep = '')}\n"
	    + "  else if (class(x)=='matrix') {cat('_matrix_:',deparse(x, control=c('keepNA', 'showAttributes')), '\\n', sep = '') }\n"
	    + "  else if (mode(x)=='numeric' && substr(deparse(x)[1], 1, 9) != \"structure\") {dput(as.double(x), control = NULL)}\n"
	    + "  else if (mode(x)=='character' && substr(deparse(x)[1], 1, 9) != \"structure\") {dput(x)}\n"
	    + "  else if (mode(x)=='logical' && substr(deparse(x)[1], 1, 9) != \"structure\") {dput(x)}\n"
	    // use R serialization for other unknown objects
	    + "  else {cat('_object_:', doserialize(x, filename), '\\n', sep = '')}"
            + "}\n");

	// Controlled newline test
	r_out.append("cat(\"before newline\\nafter newline\\n\")\n");

	while (_iterO.hasNext()) {
	    TypedIOPort tiop_o = (TypedIOPort) _iterO.next();
	    String temp_o = tiop_o.getName();
	    // now need to create an R script that returns info about an R
	    // object with the
	    // port name for use in creating Kepler output object
	    if ((!temp_o.equals("output"))
		&& (!temp_o.equals("graphicsFileName"))) {
		String df_fn = _getUniqueFileName(temp_o, "sav");
		String temp_o_escaped = temp_o;
		// Doing some basic escaping for the exists statement, 
		// although I'm not 100% sure all of these characters 
		// might occur. --Oliver
		temp_o_escaped = temp_o_escaped.replace("\\", "\\\\");
		temp_o_escaped = temp_o_escaped.replace("'", "\'");
		r_out.append("if(exists('" + temp_o_escaped + "', .GlobalEnv)) {"
		    + "cat(\"portName: " + temp_o + "\\nvectorVal: \"); "
		    + "myput(get(\"" + temp_o_escaped + "\", .GlobalEnv),'" + df_fn + "'); "
		    + "cat(\"endVectorVal\\n\")"
		    + "}\n");
	    }
	}

	String script = expression.getExpression();
	script = RPortInfo + script + "\n" + r_out + "\nquit()\n";
	try {
	    _exec();
	} catch (Exception w) {
	    log.error("Error in _exec()");
	}

	String outputString = "";
	String errorString = "";
	String noRErrorMessage = "There has been a problem running the R script!\n"
	    + "It may be due to an error in your script, it may be that R is not\n"
	    + "installed on your system, or it may not be on your path and cannot\n"
	    + "be located by Kepler.  Please make sure R is installed and the\n"
	    + "R command line executable is in the path."
	    + "For more information, see \n section 8.2.2 of the Kepler User Manual.";
	try {
	    _inputBufferedWriter.write(script);
	    _inputBufferedWriter.flush();
	    _inputBufferedWriter.close();
	} catch (IOException ex) {
	    log.error("IOException while executing R script.");
	    // Commenting out this loop--this can cause an infinite loop on XP,
	    // (when R is not on user's PATH), which keeps the noRErrorMessage 
	    // from ever showing. See bugs #4985 and #5025.
	    //while(outputString.equals("")) {
	    //	outputString = _outputGobbler.getAndReset();
	    //	errorString = _errorGobbler.getAndReset();
	    //	log.debug("R standard output: " + newline + outputString);
	    //	log.debug("R standard error: " + newline + errorString);
	    //}
	    throw new IllegalActionException(this, ex,
					     "Problem writing input. " + noRErrorMessage);
	} catch (NullPointerException npe) {
	    throw new IllegalActionException(this, npe, noRErrorMessage);
	}
	try {
	    int result = _process.waitFor();
	    log.debug("Process complete: " + result);
	    if(result != 0)
		throw new IllegalActionException(this, "R returned with value " + result + ", likely caused "
						 + newline + "by an error while executing the script.");
	} catch (IllegalActionException e) {
	    log.error(e.getMessage());
	    while(outputString.equals("")) {
		outputString = _outputGobbler.getAndReset();
		errorString = _errorGobbler.getAndReset();
		log.debug("R standard output: " + newline + outputString);
		log.error("R standard error: " + newline + errorString);
	    }
	    throw e;
	} catch (Exception www) {
	    log.error("Exception waiting for _process to end!");
	}

	while (outputString.equals("")) {
	    try {
		Thread.sleep(100);
	    } catch (Exception e) {
		log.error("Error in TestApp while sleeping!");
	    }
	    outputString = _outputGobbler.getAndReset();
	    errorString = _errorGobbler.getAndReset();
	    int loc = outputString.lastIndexOf("cat('----\\n')");
	    int loc1 = outputString.lastIndexOf("----");
	    String outputStringDisp = outputString;
	    if (loc1 > -1) {
		if(loc < 0) {
		    loc = loc1;
		}
		outputStringDisp = outputString.substring(0, loc);
		String rem = outputString.substring(loc1, outputString.length());
		_getOutput(rem);
	    }
	    output.send(0, new StringToken(outputStringDisp + "\n"
					   + errorString));
	    if (displayGraphicsOutputValue && (!graphicsDevice.equals(""))) {
		try {
		    File fout = new File(_home + _graphicsOutputFile);
		    URL furl = fout.toURL();
		    BrowserLauncher.openURL(furl.toString());
		} catch (Exception e) {
		    log.warn("problem launching browser:" + e);
		}
	    }
	    if (!graphicsDevice.equals(""))
		graphicsFileName.send(0, new StringToken(_home
							 + _graphicsOutputFile));

	}
    }

    public void initialize() throws IllegalActionException {
	super.initialize();
	// reset the tempfile counter
	_counter = 0;

	// set the home
	_home = Rcwd.stringValue();
	File homeFile = new File(_home);

	// if not a directory, use 'home'
	if (!homeFile.isDirectory()) {
	    throw new IllegalActionException(this, "Rcwd = \"" + _home + "\", which is not a directory?");
	    //home = DotKeplerManager.getInstance()
	    //.getTransientModuleDirectory("r").toString();
	}

	_home = _home.replace('\\', '/');
	if (!_home.endsWith("/"))
	    _home = _home + "/";

	// reset the name when workflow execution completes
	//this.getManager().addExecutionListener(
	//WorkflowExecutionListener.getInstance());

	String workflowName = this.toplevel().getName();
	// workflowName = workflowName.replace(' ','_');
	// workflowName = workflowName.replace('-','_');
	String execDir = _home + workflowName + "_";
	//+ WorkflowExecutionListener.getInstance().getId(toplevel());

	File dir = new File(execDir);
	if (!dir.exists()) {
	    if (!dir.mkdir()) {
		throw new IllegalActionException(null, this, "Failed to make directory " + dir);
	    };
	}
	_home = execDir + "/";

    }

    public boolean postfire() throws IllegalActionException {
	if (_errorGobbler != null) {
	    // If R was not in the path, then there is a chance that
	    // errorGobbler is null.
	    // http://bugzilla.ecoinformatics.org/show_bug.cgi?id=3735
	    _errorGobbler.quit();
	}
	if (_outputGobbler != null) {
	    _outputGobbler.quit();
	}
	return super.postfire();
    }

    public void preinitialize() throws IllegalActionException {
	super.preinitialize();
		
	// Check for "unknown"-to-"unknown" port connections that are "unacceptable"
	// See: http://bugzilla.ecoinformatics.org/show_bug.cgi?id=3985
	List _opList = outputPortList();
	Iterator outputIter = _opList.iterator();
	while (outputIter.hasNext()) {
	    TypedIOPort outputPort = (TypedIOPort) outputIter.next();
	    if (outputPort.getType().equals(BaseType.UNKNOWN)) {
		List connectedPorts = outputPort.connectedPortList();
		Iterator connectedPortIter = connectedPorts.iterator();
		while (connectedPortIter.hasNext()) {
		    TypedIOPort connectedPort =  (TypedIOPort) connectedPortIter.next();
		    if (connectedPort.getType().equals(BaseType.UNKNOWN)) {
			outputPort.setTypeEquals(BaseType.GENERAL);
			break;
		    }
		}
	    }
	}
		
    }
	
    // remove for now since it causes problems with ENMs
    /*
     * public void preinitialize() throws IllegalActionException {
     * super.preinitialize(); _opList = outputPortList(); _iterO =
     * _opList.iterator(); while (_iterO.hasNext()) { TypedIOPort tiop_o =
     * (TypedIOPort)_iterO.next(); tiop_o.setTypeEquals(BaseType.GENERAL); } }
     */
    /** The pathname of the graphics output file. */
    protected String _graphicsOutputFile = "";

    /** The home */
    protected String _home;

    ///////////////////////////////////////////////////////////////////
    ////              private methods                              ////
    private void _getOutput(String str) throws IllegalActionException {
	// Newline behavior is inconsistent.  Using an inline test.
	//String newline = System.getProperty("line.separator");
	int nlp1 = -1;
	int nlp2 = -1;
	String beforeNL = "\nbefore newline";
	String afterNL = "\nafter newline";
	nlp1 = str.indexOf(beforeNL);
	nlp2 = str.indexOf(afterNL);
	String newline = str.substring(nlp1 + beforeNL.length(), nlp2 + 1); // nlp2 + 1 for the \n in afterNL

	// log.debug("output-"+str);
	// These are the strings we use to find the port and token information
	String findStr1 = newline + "portName: ";
	String findStr2 = newline + "vectorVal: ";
	String findStr3 = newline + "endVectorVal";

	int pos1 = -1;
	int pos1n = -1;
	int pos2e = -1;
	int pos3 = -1;
	pos1 = str.indexOf(findStr1);
	while (pos1 > -1) {
	    pos1 = pos1 + findStr1.length();
	    pos1n = str.indexOf(newline, pos1);
	    String portName = str.substring(pos1, pos1n);

	    pos2e = str.indexOf(findStr2, pos1) + findStr2.length();
	    pos3 = str.indexOf(findStr3, pos2e);
	    String vectorVal = str.substring(pos2e, pos3);
	    // log.debug("portName: "+portName+ " value: " +vectorVal);
	    _setOutputToken(portName, vectorVal);

	    pos1 = str.indexOf(findStr1, pos3);
	}
    }

    private void _setOutputToken(String portName, String tokenValue) throws IllegalActionException {
	_opList = outputPortList();
	_iterO = _opList.iterator();
	while (_iterO.hasNext()) {
	    TypedIOPort tiop_o = (TypedIOPort) _iterO.next();
	    String temp_o = tiop_o.getName();
	    Token token = null;
	    if ((!temp_o.equals("output"))
		&& (!temp_o.equals("graphicsFileName"))) {
		if (temp_o.equals(portName)) {
		    try {
			if (tokenValue.equals("TRUE")) {
			    BooleanToken btt = BooleanToken.getInstance(true);
			    tiop_o.setTypeEquals(BaseType.BOOLEAN);
			    token = btt;
			} else if (tokenValue.equals("FALSE")) {
			    BooleanToken btf = BooleanToken.getInstance(false);
			    tiop_o.setTypeEquals(BaseType.BOOLEAN);
			    token = btf;
			} else if (tokenValue.equals("NA")) {
			    tiop_o.setTypeEquals(BaseType.STRING);
			    token = StringToken.NIL;
			    // this solution just sends a string token with
			    // value 'nil'
			    // in R, 'NA' is considered a boolean state
			    // i.e. 3 state logic, so this isn't really correct
			    // but nil support for Ptolemy BooleanTokens not yet
			    // available

			} else if (tokenValue.startsWith("_dataframe_:")) {
			    StringToken st = new StringToken(tokenValue);
			    tiop_o.setTypeEquals(BaseType.STRING);
			    token = st;
			} else if (tokenValue.startsWith("_object_:")) {
			    StringToken st = new StringToken(tokenValue);
			    tiop_o.setTypeEquals(BaseType.STRING);
			    token = st;
			} else if (tokenValue.startsWith("_matrix_:")) {
			    int pos1, pos2;
			    pos1 = tokenValue.indexOf(".Dim");
			    pos1 = tokenValue.indexOf("c(", pos1);
			    pos2 = tokenValue.indexOf(",", pos1);
			    String nrowS = tokenValue.substring(pos1 + 2, pos2);
			    String ncolS = tokenValue.substring(pos2 + 1,
								tokenValue.indexOf(")", pos2 + 1));
			    int nrows = Integer.parseInt(nrowS.trim());
			    int ncols = Integer.parseInt(ncolS.trim());
			    pos1 = "_matrix_:".length();
			    pos1 = tokenValue.indexOf("c(", pos1);
			    pos2 = tokenValue.indexOf(")", pos1);
			    String valS = tokenValue.substring(pos1 + 2, pos2);
			    pos1 = 0;
			    for (int j = 0; j < nrows - 1; j++) {
				for (int i = 0; i < ncols; i++) {
				    pos2 = valS.indexOf(",", pos1);
				    pos1 = pos2 + 1;
				}
				valS = valS.substring(0, pos1 - 1) + ";"
				    + valS.substring(pos1, valS.length());
			    }
			    valS = "[" + valS + "]";
			    MatrixToken mt = null;
			    try {
				mt = new IntMatrixToken(valS);
			    } catch (Exception ee) {
				try {
				    mt = new DoubleMatrixToken(valS);
				} catch (Exception eee) {
				    mt = new BooleanMatrixToken();
				}
			    }
			    token = mt;
			    tiop_o.setTypeEquals(mt.getType());
			} else if (tokenValue.startsWith("\"")) { // these are strings
			    // now remove the start and end quotes
			    tokenValue = tokenValue.substring(1, tokenValue
							      .length() - 1);
			    //remove the escapes that dput() added
			    tokenValue = StringEscapeUtils.unescapeJava(tokenValue);
			    StringToken st = new StringToken(tokenValue);
			    tiop_o.setTypeEquals(BaseType.STRING);
			    token = st;
			}
			NumberFormat nf = NumberFormat.getInstance();
			try {
			    nf.parse(tokenValue);
			    DoubleToken dt = new DoubleToken(tokenValue);
			    tiop_o.setTypeEquals(BaseType.DOUBLE);
			    token = dt;
			} catch (Exception eee) {
			    // just continue if not a number
			}

			if (tokenValue.startsWith("c(")) { // handles R vectors
			    // hack alert! this does not support R's c(1:10)
			    // syntax for arrays
			    String temp = "{"
				+ tokenValue.substring(2, tokenValue
						       .length());
			    temp = temp.replace(')', '}');
			    // convert NA values to 'nil'
			    temp = temp.replaceAll("NA", "nil");
			    ArrayToken at = new ArrayToken(temp);
			    tiop_o.setTypeEquals(new ArrayType(at
							       .getElementType()));//, at.length()));
			    token = at;
			}
						
			// check for empty arrays
			if (tokenValue.equals("character(0)")) {
			    token = new ArrayToken(BaseType.STRING);
			} else if (tokenValue.equals("numeric(0)")) {
			    token = new ArrayToken(BaseType.DOUBLE);
			} else if (tokenValue.equals("logical(0)")) {
			    token = new ArrayToken(BaseType.BOOLEAN);
			}
						
			// verify that we have a token
			if (token == null) {
			    log.warn("No token could be created on portName: " 
				     + portName 
				     + ", for tokenValue: " + tokenValue);
			    return;
			}
			// send whatever token we happened to generate - all
			// channels
			// (note: sinkPortList size does not necessarily == port
			// width)
			int numSinkPorts = tiop_o.sinkPortList().size();
			int portWidth = tiop_o.getWidth();

			// check the types of the sink ports for compatibility
			for (int channelIndex = 0; channelIndex < numSinkPorts; channelIndex++) {
			    Type sinkType = ((TypedIOPort) tiop_o
					     .sinkPortList().get(channelIndex))
				.getType();
			    // if (!sinkType.isCompatible(token.getType())) {
			    // change to equals for bug #3451:
			    if (!sinkType.equals(token.getType())) {
				log.debug("[re]Setting sink type to: "
					  + token.getType().toString());
				// set the Type for the sinks
				// POSSIBLE BUG - not sure why the automatic
				// type resolution was failing for downstream
				// port
								
				// NOTE: if the token is an array, set the type to be
				// an unbounded array type, since the length may change
				// in the next execution. 
				Type tokenType = token.getType();
				if(tokenType instanceof ArrayType) {
				    tokenType = new ArrayType(((ArrayType) tokenType).getElementType());
				}
				((TypedIOPort) tiop_o.sinkPortList().get(
									 channelIndex)).setTypeEquals(tokenType);
			    }
			}

			// send the token to the channel[s] of the port
			for (int channelIndex = 0; channelIndex < portWidth; channelIndex++) {
			    tiop_o.send(channelIndex, token);
			}

		    } catch (Exception w) {
			log.error("Problem sending to output port! "
				  + w);
			w.printStackTrace();
			throw new IllegalActionException(this, w, "Problem sending to output port.");
		    }

		    return;
		}
	    }
	}
    }

    // given a recordToken and a portName, create the R script to make a
    // dataframe with the
    // portName as its R name. Should check that all the items in the record are
    // the same length
    private String _recordToDataFrame(RecordToken recordToken, String portName) {
	boolean isDataframe = true;
	String ret = "";
	String temp = "";
	String tempA = "";
	String labellist = "";
	int arrayLength = -1;
	Set labels = recordToken.labelSet();
	Iterator iter_l = labels.iterator();
	ret = "`" + portName + "` <- local({\n";
	while (iter_l.hasNext()) {
	    String label = (String) iter_l.next();
	    Token labelvaltoken = (recordToken).get(label);
	    String token_type_string = labelvaltoken.getType().toString();
	    if ((token_type_string.equals("{double}"))
		|| (token_type_string.equals("{int}"))
		|| (token_type_string.equals("{string}"))
		|| (token_type_string.startsWith("arrayType"))
		|| (token_type_string.equals("double"))
		|| (token_type_string.equals("int"))
		|| (token_type_string.equals("string"))) {
		labellist = labellist + "`" + label + "`,";
		if (token_type_string.equals("double")
		    || token_type_string.equals("int")
		    || token_type_string.equals("string")) {
		    if (arrayLength == -1) {
			arrayLength = 1;
		    } else {
			if (arrayLength != 1) {
			    log.warn("record elements are not all the same length!");
			    isDataframe = false;
			    //return "";
			}
		    }
		} else {
		    if (arrayLength == -1) {
			arrayLength = ((ArrayToken) labelvaltoken).length();
		    } else {
			int a_len = ((ArrayToken) labelvaltoken).length();
			if (a_len != arrayLength) {
			    log.warn("record elements are not all the same length!");
			    isDataframe = false;
			    //return "";
			}
		    }
		}
		temp = labelvaltoken.toString();
		if (token_type_string.equals("double")
		    || token_type_string.equals("int")
		    || token_type_string.equals("string")) {
		    temp = "(" + temp + ")";
		}
		temp = temp.replace('{', '(');
		temp = temp.replace('}', ')');
		// using double quotes for strings so that single quotes work
		// within them
		// temp = temp.replace('"', '\'');
		// assume that the token's string value might be 'nil' for a
		// missing value
		temp = temp.replaceAll("nil", "NA");
		// if string is long, create a temp file for passing array data
		String temp1 = temp;
		// need to estimate the total number of characters that this
		// record might have
		int estimatedTotalLength = temp1.length() * labels.size();
		log.debug("column length: " + temp1.length()
			  + " * number of columns: " + labels.size()
			  + " = estimated total record length: "
			  + estimatedTotalLength + ", _maxCommandLineLength: "
			  + _maxCommandLineLength);
		if (estimatedTotalLength > _maxCommandLineLength) {
		    temp1 = temp1.replace('(', ' ');
		    temp1 = temp1.replace(')', ' ');
		    String filename = _writeDataFile(temp1);
		    if (token_type_string.indexOf("string") > -1) {
			tempA = "`" + label
			    + "` <- scan('"
			    + filename
			    + "', sep=',', strip.white=TRUE, what='character' )";
		    } else {
			tempA = "`" + label + "` <- scan('" + filename + "', sep=',')";
		    }
		    tempA = tempA + "\n" + "file.remove('" + filename + "')";
		    ret = ret + tempA + "\n";
		} else { // otherwise use the modified string
		    tempA = "`" + label + "` <- c" + temp;
		    ret = ret + tempA + "\n";
		}

	    }
	}
	labellist = labellist.substring(0, labellist.length() - 1); // remove
	// last ','
	if (isDataframe) {
	    ret = ret + "data.frame(" + labellist + ", check.names = FALSE)\n";
	}
	else {
	    ret = ret + "list(" + labellist + ")\n";
	}
	ret += "})\n";
	// log.debug("ret: "+ret);
	return ret;
    }

    // there is ta problem when length of lines sent to R are too long
    // thus, break the line into pieces with newlines;
    // assume pieces of are approx. 512 chars but must be separate at ','
    // (R will accept multiple lines but the seperation cannot be arbitrart;
    // i.e. not in
    // middle of floating point number)

    private String _breakIntoLines(String temp) {
	int size = 512;
	int pieces = (int) temp.length() / size;
	int start = size;
	int indx = 0;
	for (int k = 0; k < pieces - 1; k++) {
	    indx = temp.indexOf(",", start);
	    temp = temp.substring(0, indx) + "\n"
		+ temp.substring(indx, temp.length());
	    start = start + size;
	}
	return temp;
    }

    // Execute a command, set _process to point to the subprocess
    // and set up _errorGobbler and _outputGobbler to read data.
    private void _exec() throws IllegalActionException {
	Runtime runtime = Runtime.getRuntime();
	String[] commandArray;

	String osName = System.getProperty("os.name");
	if (osName.equals("Windows NT") || osName.equals("Windows XP")
	    || osName.equals("Windows 2000")) {
	    // checkRLocation is commented out for now since it slows down the
	    // first execution of a
	    // workflow with an RExpression actor too much (>= 30 sec for a
	    // 'cold' machine)
	    _checkRLocation();
	    commandArray = new String[6];
	    commandArray[0] = "cmd.exe";
	    commandArray[1] = "/C";
	    commandArray[2] = _rDotExe;
	    commandArray[3] = "--silent";
	    commandArray[4] = _restoreString;
	    commandArray[5] = _saveString;
	} else if (osName.equals("Windows 95")) {
	    _checkRLocation();
	    commandArray = new String[6];
	    commandArray[0] = "command.com";
	    commandArray[1] = "/C";
	    commandArray[2] = _rDotExe;
	    commandArray[3] = "--silent";
	    commandArray[4] = _restoreString;
	    commandArray[5] = _saveString;
	} else {
	    commandArray = new String[4];
	    commandArray[0] = _rDotExe;
	    commandArray[1] = "--silent";
	    commandArray[2] = _restoreString;
	    commandArray[3] = _saveString;
	}

	// log.debug("commandArray :"+commandArray);
	try {
	    // log.debug("ready to create _process!");
	    _process = runtime.exec(commandArray);
	    log.debug("Process :" + _process);
	} catch (Exception e) {
	    log.error("Problem with creating process in RExpression!");
	}
	// log.debug("Ready to create threads");
	// Create two threads to read from the subprocess.
	try {
	    _outputGobbler = new _StreamReaderThread(_process.getInputStream(),
						 "Exec Stdout Gobbler-" + _streamReaderThreadCount++, this);
	    _errorGobbler = new _StreamReaderThread(_process.getErrorStream(),
						"Exec Stderr Gobbler-" + _streamReaderThreadCount++, this);
	} catch (UnsupportedEncodingException ex) {
	    throw new IllegalActionException(this, ex, "Failed to open exec process gobblers.");
	}
	_errorGobbler.start();
	_outputGobbler.start();

	if (_streamReaderThreadCount > 1000) {
	    // Avoid overflow in the thread count.
	    _streamReaderThreadCount = 0;
	}

	try {
	    OutputStreamWriter inputStreamWriter = new OutputStreamWriter(_process
								      .getOutputStream(), "UTF-8");
	    _inputBufferedWriter = new BufferedWriter(inputStreamWriter);
	} catch (UnsupportedEncodingException ex) {
	    throw new IllegalActionException(this, ex, "Failed to open the output stream: " + _process);
	}

    }

    private void _checkRLocation() {
	if (_rDotExe.equals("R")) {
	    List<File> l = new ArrayList<File>();
	    // check '$KEPLER/R'
	    String keplerDir = StringUtilities.getProperty("KEPLER");
	    _findFile(new File(keplerDir + "/R"), "R.exe", l);
	    if (!l.isEmpty()) {
		_rDotExe = l.get(0) + "";
		log.debug(_rDotExe);
	    }
	}
    }

    private String _getUniqueFileName(String extender) {
	int cnt = 1;
	// String usr_name = System.getProperty("user.name");
	String actor_name = this.getName();
	actor_name = actor_name.replaceAll("[^a-zA-Z0-9.]", "_");
	String fn = actor_name + "-" + cnt + "." + extender;
	String path = _home;
	while (new File(path, fn).exists()) {
	    cnt++;
	    fn = actor_name + "-" + cnt + "." + extender;
	}
	return fn;
    }

    //
    // overloaded version for use with new form of .sav files that have portname
    // prefix
    //
    private String _getUniqueFileName(String portname, String extender) {
	int cnt = 1;
	// String usr_name = System.getProperty("user.name");
	String actor_name = this.getName();
	// These replaceAll statements will make this operation OS-independent, 
	// but will increase the likelihood of a .sav file collision since this
	// is evaluated before any files are created.  In other words, 
	// "input 1" and "input_1" will collide with the same .sav file as both
	// will find input_1-RExpression-1.sav uncreated.  There is now R code to 
	// handle this.
	actor_name = actor_name.replaceAll("[^a-zA-Z0-9.]", "_");
	portname = portname.replaceAll("[^a-zA-Z0-9.]", "_");
	String fn = portname + "-" + actor_name + "-" + cnt + "." + extender;
	String path = _home;
	while (new File(path, fn).exists()) {
	    cnt++;
	    fn = portname + "-" + actor_name + "-" + cnt + "." + extender;
	}
	//make the filename play nice
	String retPath = new File(path, fn).getAbsolutePath();
	retPath = retPath.replace('\\', '/');
	return retPath;
    }

    private String _createMetadataCommand(String objectName,
					  String attributeName, String attributeValue) {
	String retVal = "attr(`" + objectName + "`, " + "\"" + attributeName
	    + "\"" + ") <- " + "\"" + attributeValue + "\"";
	return retVal;
    }

    private String _writeDataFile(String dat) {
	String fn = "";
	StringReader stringReader = null;
	PrintWriter printWriter = null;
	try {
	    String home = System.getProperty("user.home");
	    home = home.replace('\\', '/');
	    fn = home + "/" + _getUniqueFileName("dat") + _counter;
	    stringReader = new StringReader(dat);
	    File dataFile = new File(fn);
	    OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(dataFile), "UTF-8");
	    printWriter = new PrintWriter(w);
	    int c;
	    while ((c = stringReader.read()) != -1) {
		printWriter.write(c);
	    }
	} catch (Exception exc) {
	    log.error("error writing data file! - RExpression");
	} finally {
	    try {
		if (stringReader != null) {
		    stringReader.close();
		}
	    } finally {
		if (printWriter != null) {
		    printWriter.close();
		}
	    }
	}
	_counter++;
	return fn;
    }

    private void _findFile(File f, String name, List<File> r) {
	if (f.isDirectory()) {
	    File[] files = f.listFiles();
	    if (files == null)
		return;
	    for (int i = 0; i < files.length; i++) {
		// log.debug(files[i]+"");
		_findFile(files[i], name, r);
	    }
	} else {
	    String fn = f + "";
	    // log.debug("fn: "+fn);
	    if (fn.indexOf(name) > -1) {
		r.add(f);
	    }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////            inner classes                                  ////

    // Private class that reads a stream in a thread and updates the
    // stringBuffer.
    private class _StreamReaderThread extends Thread {

	/**
	 * Create a _StreamReaderThread.
	 * 
	 * @param inputStream
	 *            The stream to read from.
	 * @param name
	 *            The name of this StreamReaderThread, which is useful for
	 *            debugging.
	 * @param actor
	 *            The parent actor of this thread, which is used in error
	 *            messages.
	 */
	_StreamReaderThread(InputStream inputStream, String name, Nameable actor) throws UnsupportedEncodingException {
	    super(name);
	    _inputStream = inputStream;
	    _inputStreamReader = new InputStreamReader(_inputStream, "UTF-8");
	    _stringBuffer = new StringBuffer();
	    _keepRunning = true;
	    chars = new char[100001];
	}

	/**
	 * Read any remaining data in the input stream and return the data read
	 * thus far. Calling this method resets the cache of data read thus far.
	 */
	public String getAndReset() {
	    if (_debugging) {
		try {
		    _debug("getAndReset: Gobbler '" + getName());
		    // + "' Ready: " + _inputStreamReader.ready()
		    // + " Available: " + _inputStream.available());
		    // the previous lines (now commented out) cause a thread
		    // problem because (?)
		    // the inputStreamReader is used by the threads monitoring
		    // process io.

		} catch (Exception ex) {
		    throw new InternalErrorException(ex);
		}
	    }

	    // do a final _read before clearing buffer in case some characters
	    // are available; this was added to collect information that was
	    // sometimes missing on a newer, faster computer ! -- DFH 11/2005
	    _read(); // DFH - last chance to read

	    String results = _stringBuffer.toString();
	    _stringBuffer = new StringBuffer();

	    return results;
	}

	/**
	 * Read lines from the inputStream and append them to the stringBuffer.
	 */
	public void run() {
	    while (_keepRunning) {
		// log.debug("Starting read");
		_read();
		try {
		    Thread.sleep(100);
		} catch (Exception e) {
		    log.error("Error in StreamReaderThread while sleeping!");
		}

		// log.debug("Finishing read");
	    }
	}

	public void quit() {
	    _keepRunning = false;
	}

	// Read from the stream until we get to the end of the stream
	private void _read() {
	    // We read the data as a char[] instead of using readline()
	    // so that we can get strings that do not end in end of
	    // line chars.

	    // char [] chars = new char[20001];
	    int length; // Number of characters read.

	    try {
		// Oddly, InputStreamReader.read() will return -1
		// if there is no data present, but the string can still
		// read.
		length = _inputStreamReader.read(chars, 0, 20000);
		if (_debugging) {
		    // Note that ready might be false here since
		    // we already read the data.
		    _debug("_read(): Gobbler '" + getName()
			   + "' Ready: " + _inputStreamReader.ready()
			   + " Value: '" + String.valueOf(chars, 0, length)
			   + "'");
		}
		if (length > 0) {
		    String temp = String.valueOf(chars, 0, length);
		    // _stringBuffer.append(chars, 0, length);
		    _stringBuffer.append(temp);
		}
	    } catch (Throwable throwable) {
		log.warn("In catch block of _read: " + throwable.getMessage());
		_keepRunning = false;
	    }
	}

	// character array
	private char[] chars;

	// StringBuffer to update.
	private StringBuffer _stringBuffer;

	// Stream from which to read.
	private InputStream _inputStream;

	// Stream from which to read.
	private InputStreamReader _inputStreamReader;

	// this thread
	private boolean _keepRunning;
    }

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    // The subprocess gets its input from this BufferedWriter.
    private BufferedWriter _inputBufferedWriter;

    // StreamReader with which we read stderr.
    private _StreamReaderThread _errorGobbler;

    // StreamReader with which we read stdout.
    private _StreamReaderThread _outputGobbler;

    // The Process that we are running.
    private Process _process;

    // Instance count of output and error threads, used for debugging.
    // When the value is greater than 1000, we reset it to 0.
    private static int _streamReaderThreadCount = 0;

    private List _opList;
    private Iterator _iterO;
   
    private static String _NO_SAVE = "--no-save";
    private static String _SAVE = "--save";
    private static String _NO_RESTORE = "--no-restore";

    private static String _rDotExe = "R";

    // Temporary file counter.
    private int _counter = 0;

    private String _saveString;
    private String _restoreString = _NO_RESTORE;

    // if arrays sent to R are longer than this value, then use a file
    // rather than a string on the command line to pass the data
    // This is necessary because apparently R has a fixed buffer
    // for passing long commands
    private int _maxCommandLineLength = 30000;
}
