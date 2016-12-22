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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.kepler.util.DotKeplerManager;
import org.rosuda.JRI.RBool;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RFactor;
import org.rosuda.JRI.RList;
import org.rosuda.JRI.RVector;
import org.rosuda.JRI.Rengine;

import ptolemy.actor.NoTokenException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.BrowserLauncher;
import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanMatrixToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.IntToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.OrderedRecordToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
//import util.WorkflowExecutionListener;

//////////////////////////////////////////////////////////////////////////
//// RExpression2
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
 * <p>Note that RExpression2 uses the Java/R interface from 
 * <a rhef="https://rforge.net/JRI/#in_browser">https://rforge.net/JRI/</a>
 * which requires that native libraries be installed in your java.library.path.
 * </p>
 *
 * @author Dan Higgins and Matt Jones, NCEAS, UC Santa Barbara
 * @version $Id$
 * @since Ptolemy II 11.0
 * @UserLevelDocumentation This actor let the user insert R scripts in a Kepler
 *                         workflow. It requires the R system to be installed on
 *                         the computer executing the workflow
 */

public class RExpression2 extends TypedAtomicActor {

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
    public RExpression2(CompositeEntity container, String name)
	throws NameDuplicationException, IllegalActionException {
	super(container, name);

	expression = new StringAttribute(this, "expression");
	expression.setDisplayName("R function or script");
	new TextStyle(expression, "R Expression"); //keep this for larger text area
	expression.setExpression("a <- c(1,2,3,5)\nplot(a)");

	Rcwd = new StringParameter(this, "Rcwd");
	Rcwd.setDisplayName("R working directory");
	//Rcwd.setExpression( DotKeplerManager.getInstance()
	//.getTransientModuleDirectory("r").toString() );

	graphicsFormat = new StringParameter(this, "graphicsFormat");
	graphicsFormat.setDisplayName("Graphics Format");
	graphicsFormat.setExpression("png");
	graphicsFormat.addChoice("pdf");
	graphicsFormat.addChoice("png");
		
	showDebug = new Parameter(this, "showDebug");
	showDebug.setDisplayName("Debug");
	showDebug.setTypeEquals(BaseType.BOOLEAN);
	showDebug.setToken(BooleanToken.TRUE);
		
	serializeData = new Parameter(this, "serializeData");
	serializeData.setDisplayName("Serialize Data Frame");
	serializeData.setTypeEquals(BaseType.BOOLEAN);
	serializeData.setToken(BooleanToken.TRUE);

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


    public static Log log = LogFactory.getLog(RExpression2.class);

    ///////////////////////////////////////////////////////////////////
    ////          ports and parameters                             ////

    /**
     * The output port.
     */
    public TypedIOPort output;

    /**
     * The expression that is evaluated to produce the output.
     */
    public StringAttribute expression;

    /**
     * The 'R' working directory (home dir by default).
     */
    public StringParameter Rcwd;

    /**
     * If <i>true</i>, then shoe debugging information about script. 
     * If <i>false</i>, then don't. (the default)
     */
    public Parameter showDebug;
	
    /**
     * If <i>true</i>, then daata frames (and other complexe data objects
     * will be transferred by serialization to disk.
     * If <i>false</i>, then they will be converted as losslessly as possible
     * to a Ptolemy data structure
     */
    public Parameter serializeData;
	
    /**
     * If <i>true</i>, then display plot. If <i>false</i>, then don't. (the
     * default)
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
     * The width of the output graphics bitmap in pixels.
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
	RExpression2 newObject = (RExpression2) super.clone(workspace);
	String lcOSName = System.getProperty("os.name").toLowerCase();
	boolean MAC_OS_X = lcOSName.startsWith("mac os x");
	if (MAC_OS_X) {
	    try {
		newObject.graphicsFormat.setExpression("pdf");
		newObject.displayGraphicsOutput.setToken(BooleanToken.TRUE);
	    } catch (Exception w) {
		System.out.println("Error in special Mac response in clone");
	    }
	}
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
	super.fire();

	// _fireUsingCommandLine();
	_fireUsingJRI();
    }

    public void initialize() throws IllegalActionException {
	super.initialize();

	// set the _home
	_home = Rcwd.stringValue();
	File homeFile = new File(_home);

	// if not a directory, use 'home'
	if (!homeFile.isDirectory()) {
	    throw new IllegalActionException(this, "Rcwd = \"" + _home + "\", which is not a directory?");
	    // home = DotKeplerManager.getInstance()
	    // .getTransientModuleDirectory("r").toString();
	}

	_home = _home.replace('\\', '/');
	if (!_home.endsWith("/"))
	    _home = _home + "/";

	// reset the name when workflow execution completes
	// this.getManager().addExecutionListener(
	//  WorkflowExecutionListener.getInstance());

	String workflowName = this.toplevel().getName();
	// workflowName = workflowName.replace(' ','_');
	// workflowName = workflowName.replace('-','_');
	String execDir = _home + workflowName + "_"
	    //+ WorkflowExecutionListener.getInstance().getId(toplevel())
	    + "/";

	File dir = new File(execDir);
	if (!dir.exists()) {
	    dir.mkdir();
	}
	_home = execDir;

    }

    // public boolean postfire() throws IllegalActionException {
    // 	// _errorGobbler.quit();
    // 	// _outputGobbler.quit();

    // 	return super.postfire();
    // }

    /** Iterate through the ports.
     *  @exception IllegalActionException If thrown by the
     *  base class or while accessing the ports.
     */
    public void preinitialize() throws IllegalActionException {
	// NOTE: there is a note about commenting out this method:
	// "remove for now since it causes problems with ENMs"
	// TODO: see if this causes problems (there are problems without it, too)

	super.preinitialize();
	// set all the ports to unknown for type resolution?
	_opList = outputPortList();
	_iterO = _opList.iterator();
	while (_iterO.hasNext()) {
	    TypedIOPort tiop = (TypedIOPort) _iterO.next();
	    if (tiop.getName().equals("output")
		|| tiop.getName().equals("graphicsFileName")) {
		continue;
	    }
	    tiop.setTypeEquals(BaseType.GENERAL);
	}
    }
	
    ///////////////////////////////////////////////////////////////////
    ////           private methods                                 ////

    //assign the array of arrays directly in RNI
    private void _convertArrayTokenToObject(ArrayToken token, String varName) {
	int arrayLength = -1;
	List columnRefs = new ArrayList();
	for (int i = 0; i < token.length(); i++) {
	    Token genericToken = token.getElement(i);
	    String token_type_string = genericToken.getType().toString();
			
	    ArrayToken arrayToken = null;
	    if (genericToken instanceof ArrayToken) {
		arrayToken = (ArrayToken) genericToken;
	    }
	    long columnRef = 0;
	    if ((token_type_string.equals("{double}"))
		|| (token_type_string.equals("{int}"))
		|| (token_type_string.equals("{string}"))
		|| (token_type_string.equals("{boolean}"))
		|| (token_type_string.startsWith("arrayType"))) {

		// make primative arrays for R
		if ((token_type_string.equals("{double}"))
		    || (token_type_string.startsWith("arrayType(double"))) {
		    double[] values = _convertArrayToken(arrayToken);
		    columnRef = _rEngine.rniPutDoubleArray(values);
		} else if ((token_type_string.equals("{int}"))
			   || (token_type_string.startsWith("arrayType(int"))) {
		    int[] values = _convertArrayTokenToInt(arrayToken);
		    columnRef = _rEngine.rniPutIntArray(values);
		} else if ((token_type_string.equals("{string}"))
			   || (token_type_string.startsWith("arrayType(string"))) {
		    String[] values = _convertArrayTokenToString(arrayToken);
		    columnRef = _rEngine.rniPutStringArray(values);
		} else if ((token_type_string.equals("{boolean}"))
			   || (token_type_string.startsWith("arrayType(boolean"))) {
		    boolean[] values = _convertArrayTokenToBoolean(arrayToken);
		    columnRef = _rEngine.rniPutBoolArray(values);
		}
	    } else if (token_type_string.equals("string")) {				
		columnRef = _rEngine.rniPutStringArray( new String[] { ((StringToken)genericToken).stringValue() });
	    }
	    else if (token_type_string.equals("double")) {
		columnRef = _rEngine.rniPutDoubleArray( new double[] { ((DoubleToken)genericToken).doubleValue() });
	    }
	    else if (token_type_string.equals("int")) {
		columnRef = _rEngine.rniPutIntArray( new int[] { ((IntToken)genericToken).intValue() });
	    }
	    else if (token_type_string.equals("boolean")) {
		columnRef = _rEngine.rniPutBoolArray( new boolean[] { ((BooleanToken)genericToken).booleanValue() });
	    }
	    columnRefs.add(columnRef);
	} // while
	// capture the column references in a "vector"
	long[] columns = new long[columnRefs.size()];
	for (int i = 0; i < columnRefs.size(); i++) {
	    columns[i] = (Long) columnRefs.get(i);
	}
	// assemble the dataframe reference
	long tableRef = _rEngine.rniPutVector(columns);

	// add the column names to the dataframe
	//		String[] columnNames = (String[]) labels.toArray(new String[0]);
	//		long columnNamesRef = _rEngine.rniPutStringArray(columnNames);
	//		_rEngine.rniSetAttr(tableRef, "names", columnNamesRef);

	// set the class as data.frame
	long classNameRef = _rEngine.rniPutString("data.frame");
	_rEngine.rniSetAttr(tableRef, "class", classNameRef);

	// set assign the data.frame to a variable
	_rEngine.rniAssign(varName, tableRef, 0);

    }

    /**
     * 
     * Given a recordToken and a portName, create the R script to make a
     * dataframe with the portName as its R name.
     * 
     * @param recordToken
     *            the record to convert to R dataframe
     * @param portName
     *            will become the object name used for the R dataframe
     */
    private void _recordToDataFrame(RecordToken recordToken, String portName) {
	int arrayLength = -1;
	Set labels = recordToken.labelSet();
	Iterator iter_l = labels.iterator();
	List columnRefs = new ArrayList();
	while (iter_l.hasNext()) {
	    String label = (String) iter_l.next();
	    // System.out.println("Label: "+label);
	    Token genericToken = recordToken.get(label);
	    String token_type_string = genericToken.getType().toString();
			
	    ArrayToken arrayToken = null;
	    if (genericToken instanceof ArrayToken) {
		arrayToken = (ArrayToken) genericToken;
	    }
	    long columnRef = 0;
	    if ((token_type_string.equals("{double}"))
		|| (token_type_string.equals("{int}"))
		|| (token_type_string.equals("{string}"))
		|| (token_type_string.equals("{boolean}"))
		|| (token_type_string.startsWith("arrayType"))) {
		// for now, assume that token is an arrayToken !!!
		// other token types are just ignored
		if (arrayLength == -1) {
		    arrayLength = arrayToken.length();
		} else {
		    int a_len = arrayToken.length();
		    if (a_len != arrayLength) {
			log.error("record elements are not all the same length!");
			return;
		    }
		}

		// make primative arrays for R
		if ((token_type_string.equals("{double}"))
		    || (token_type_string.startsWith("arrayType(double"))) {
		    double[] values = _convertArrayToken(arrayToken);
		    columnRef = _rEngine.rniPutDoubleArray(values);
		} else if ((token_type_string.equals("{int}"))
			   || (token_type_string.startsWith("arrayType(int"))) {
		    int[] values = _convertArrayTokenToInt(arrayToken);
		    columnRef = _rEngine.rniPutIntArray(values);
		} else if ((token_type_string.equals("{string}"))
			   || (token_type_string.startsWith("arrayType(string"))) {
		    String[] values = _convertArrayTokenToString(arrayToken);
		    columnRef = _rEngine.rniPutStringArray(values);
		} else if ((token_type_string.equals("{boolean}"))
			   || (token_type_string.startsWith("arrayType(boolean"))) {
		    boolean[] values = _convertArrayTokenToBoolean(arrayToken);
		    columnRef = _rEngine.rniPutBoolArray(values);
		}
	    } else if (token_type_string.equals("string")) {				
		columnRef = _rEngine.rniPutStringArray( new String[] { ((StringToken)genericToken).stringValue() });
		arrayLength = 1;
	    }
	    else if (token_type_string.equals("double")) {
		columnRef = _rEngine.rniPutDoubleArray( new double[] { ((DoubleToken)genericToken).doubleValue() });
		arrayLength = 1;
	    }
	    else if (token_type_string.equals("int")) {
		columnRef = _rEngine.rniPutIntArray( new int[] { ((IntToken)genericToken).intValue() });
		arrayLength = 1;
	    }
	    else if (token_type_string.equals("boolean")) {
		columnRef = _rEngine.rniPutBoolArray( new boolean[] { ((BooleanToken)genericToken).booleanValue() });
		arrayLength = 1;
	    }
	    columnRefs.add(columnRef);
	} // while
	// capture the column references in a "vector"
	long[] columns = new long[columnRefs.size()];
	for (int i = 0; i < columnRefs.size(); i++) {
	    columns[i] = (Long) columnRefs.get(i);
	}
	// assemble the dataframe reference
	long tableRef = _rEngine.rniPutVector(columns);

	// add the column names to the dataframe
	String[] columnNames = (String[]) labels.toArray(new String[0]);
	long columnNamesRef = _rEngine.rniPutStringArray(columnNames);
	_rEngine.rniSetAttr(tableRef, "names", columnNamesRef);

	// set the row names (just 1,2,3...n)
	// this works now - and is very important!
	String[] rowNames = new String[arrayLength];
	for (int i = 0; i < rowNames.length; i++) {
	    rowNames[i] = "" + (i+1);
	}
	long rowNamesRef = _rEngine.rniPutStringArray(rowNames);
	_rEngine.rniSetAttr(tableRef, "row.names", rowNamesRef);

	// set the class as data.frame
	long classNameRef = _rEngine.rniPutString("data.frame");
	_rEngine.rniSetAttr(tableRef, "class", classNameRef);

	// set assign the data.frame to a variable
	_rEngine.rniAssign(portName, tableRef, 0);
		
	//make the character columns into factors (default DF behavior)
	_rEngine.eval(portName + "[sapply(" + portName + ", is.character)] <- lapply(" + portName + "[sapply(" + portName + ", is.character)], as.factor)");

    }

    /**
     * The main execution of the actor as follows: Reads the input data, sets up
     * the graphic device (as needed), executes the script, sends output to
     * ports, optionally shows generated graphics, cleans up.
     * 
     * @throws IllegalActionException
     */
    private void _fireUsingJRI() throws IllegalActionException {

	_initializeRengine();
	if (_rEngine != null) {
	    _readInputData();
	    _setupJRIGraphicsDevice();
	    _executeRModel();
	    _writeOutputData();
	    _showGraphics();
	    _teardownJRI();
	} else {
	    throw new IllegalActionException(_noRErrorMessage);
	}
    }

    /**
     * Start up the R system by initalizing an instance of the JRI Rengine. This
     * REngine can be used to execute R scripts and to retrieve the results of
     * these execution events.
     * 
     * @return the REngine to be sued for executing R scripts
     * @throws IllegalActionException
     */
    private void _initializeRengine() throws IllegalActionException {
	log.warn("RNI version: " + Rengine.rniGetVersion());
	log.warn("API version: " + Rengine.getVersion());
	if (Rengine.getMainEngine() != null) {
	    _rEngine = Rengine.getMainEngine();
	    _console = new RConsole();
	    _rEngine.addMainLoopCallbacks(_console);
	    // clear all objects for safety's sake
	    _rEngine.eval("rm(list=ls())");
	    return;
	}
		
	if (!Rengine.versionCheck()) {
	    String msg = "** Version mismatch - Java files don't match R library version.";
	    log.error(msg);
	    throw new IllegalActionException(msg);
	}
	log.debug("Creating Rengine (with arguments)");
	// 1) we pass the arguments from the command line
	// 2) we won't use the main loop at first, we'll start it later
	// (that's the "false" as second argument)
	// 3) the callbacks are implemented by the Text_Console class above
	String args[] = new String[2];
	args[0] = _NO_SAVE;
	args[1] = _NO_RESTORE;

	_console = new RConsole();
	_rEngine = new Rengine(args, false, _console);
	log.debug("Rengine created, waiting for R");
	// the engine creates R in a new thread, so we should wait until it's
	// ready
	if (!_rEngine.waitForR()) {
	    String msg = "Cannot load R." + "\n " + _noRErrorMessage;
	    log.error(msg);
	    throw new IllegalActionException(msg);
	}
    }

    /**
     * Read the input data from the actors input ports, and for each port
     * convert the data from a Kepler Token into an appropriate R object that
     * can be loaded into the REngine before R scripts that depend on this input
     * data can be executed.
     * 
     * @param re
     *            the REngine used for loading input data
     */
    private void _readInputData() {
	log.debug("reading input form ports");
	List ipList = inputPortList();
	Iterator iter_i = ipList.iterator();
	String RPortInfo = "";
	Token at;
	String tokenValue;
	while (iter_i.hasNext()) {
	    TypedIOPort tiop = (TypedIOPort) iter_i.next();
	    int multiportSize = tiop.numberOfSources();

	    for (int i = 0; i < multiportSize; i++) {
		try {
		    if (tiop.hasToken(i)) {
			String portName = tiop.getName();
			String finalPortName = portName; // for use with
			// multiports
			if (tiop.isMultiport()) {
			    portName = portName + i; // temporary variable for
			    // list item
			}
			Token token = tiop.get(i);
			String token_type_string = token.getType().toString();
			String token_class_name = token.getType()
			    .getTokenClass().getName();
			log.debug("setting: " + portName + "=" + token);

			// check token type and convert to R appropriately
			// RecordTokens
			if (token instanceof RecordToken) {
			    // write it to the R environment
			    _recordToDataFrame((RecordToken) token, portName);
			}

			// STRINGS
			else if (token_type_string.equals("string")) {

			    // check for special strings that indicate dataframe
			    // file reference
			    at = (Token) token;
			    tokenValue = at.toString();
			    tokenValue = 
				tokenValue.substring(1, tokenValue.length() - 1); // remove quotes

			    // DATAFRAME (old)
			    if (tokenValue.startsWith("_dataframe_:")) {
				// assume that the string for a dataframe file
				// reference is of the form
				// '_dataframe_:"+<filename>
				tokenValue = tokenValue.substring(12); // should be filename
				REXP x = null;
				Token myToken = null;
								
				RPortInfo = 
				    "conn <- file('" + tokenValue + "', 'rb');";
				x = _rEngine.eval(RPortInfo, true);
				myToken = _convertToToken(x, null);
				log.debug("myToken=" + myToken);
								
				RPortInfo = 
				    portName + " <- unserialize(conn);";
				x = _rEngine.eval(RPortInfo, true);
				myToken = _convertToToken(x, null);
				log.debug("myToken=" + myToken);
								
				RPortInfo = "close(conn);";
								
				x = _rEngine.eval(RPortInfo, true);
				myToken = _convertToToken(x, null);
				log.debug("myToken=" + myToken);
								
				continue; // stop for this token and go to the
				// next
			    }
							
			    // assume that the token's string value might be
			    // 'nil' for a missing value
			    tokenValue = tokenValue.replaceAll("nil", "NA");
							
			    if (tokenValue.startsWith("\\")) {
				//use this evaluation as a workaround for getting the escaped chars (like tabs)
				_rEngine.eval(portName + " <- '" + tokenValue + "'");
			    } else {
				// set the string in the r engine directly
				_rEngine.assign(portName, tokenValue);
			    }
			}

			// BOOLEAN
			else if (token_type_string.equals("boolean")) {
			    at = (Token) token;
			    tokenValue = at.toString();
			    //use a boolean - JRI uses arrays...
			    boolean booleanValue = Boolean.parseBoolean(tokenValue);
			    _rEngine.assign(portName, new boolean[] {booleanValue});
			    //_rEngine.assign(portName, tokenValue);
			}

			// NUMBERS
			else if (token_type_string.equals("double")
				 || token_type_string.equals("float")
				 || token_type_string.equals("int")) {
			    at = (Token) token;
			    tokenValue = at.toString();
			    // TODO need support for assigning numeric scalars
			    _rEngine.eval(portName + " <- " + tokenValue);
			    // _rEngine.assign(portName, tokenValue);
			}

			// ARRAYS
			else if ((token_type_string.equals("{double}"))
				 || (token_type_string
				     .startsWith("arrayType(double"))) {
			    double[] values = _convertArrayToken((ArrayToken) token);
			    _rEngine.assign(portName, values);
			} else if ((token_type_string.equals("{int}"))
				   || (token_type_string
				       .startsWith("arrayType(int"))) {
			    int[] values = _convertArrayTokenToInt((ArrayToken) token);
			    _rEngine.assign(portName, values);
			} else if ((token_type_string.equals("{string}"))
				   || (token_type_string
				       .startsWith("arrayType(string"))) {
			    String[] values = _convertArrayTokenToString((ArrayToken) token);
			    _rEngine.assign(portName, values);
			} else if ((token_type_string.equals("{boolean}"))
				   || (token_type_string
				       .startsWith("arrayType(boolean"))) {
			    boolean[] values = _convertArrayTokenToBoolean((ArrayToken) token);
			    _rEngine.assign(portName, values);
			} else if ((token_type_string.equals("{scalar}"))
				   || (token_type_string
				       .startsWith("arrayType(scalar"))) {
			    //TODO: keep specific types for each item
			    Object[] values = _convertScalarArrayTokenToString((ArrayToken) token);
			    REXP objVal = new REXP(REXP.XT_ARRAY_STR, values);
			    _rEngine.assign(portName, objVal);
			} else if (token_type_string.startsWith("arrayType(arrayType")) {
			    //handle arrays of arrays
			    _convertArrayTokenToObject((ArrayToken) token, portName);
			}

			// MATRIX
			else if ((token_class_name.indexOf("IntMatrixToken") > -1)
				 || (token_class_name
				     .indexOf("DoubleMatrixToken") > -1)
				 || (token_class_name
				     .indexOf("BooleanMatrixToken") > -1)) {
			    int rows = ((MatrixToken) token).getRowCount();
			    int cols = ((MatrixToken) token).getColumnCount();
			    ArrayToken matrixAsArray = 
				((MatrixToken) token).toArrayColumnMajor();
			    String matrixType = matrixAsArray.getType()
				.toString();
			    if (matrixType.startsWith("arrayType(int")) {
				int[] values = _convertArrayTokenToInt((ArrayToken) matrixAsArray);
				_rEngine.assign(portName, values);
			    } else if (matrixType
				       .startsWith("arrayType(double")) {
				double[] values = _convertArrayToken((ArrayToken) matrixAsArray);
				_rEngine.assign(portName, values);
			    } else if (matrixType
				       .startsWith("arrayType(boolean")) {
				boolean[] values = _convertArrayTokenToBoolean((ArrayToken) matrixAsArray);
				_rEngine.assign(portName, values);
			    }

			    REXP x = _rEngine.eval(portName, true);
			    log.debug(portName + "=" + x);

			    // make a matrix from the array
			    String cmd = portName + " <- " + "matrix("
				+ portName + ", nrow=" + rows + ", ncol="
				+ cols + ")";
			    _rEngine.eval(cmd, false);
			    // REXP x = _rEngine.eval(cmd, true);
			    // _rEngine.assign(portName, x);
			}

			// CONSTRUCT LIST for objects on multiport
			if (tiop.isMultiport()) {
			    String commandList = null;
			    if (i == 0) {
				// create list
				commandList = finalPortName + " <- list("
				    + portName + ")";
			    } else if (i > 0) {
				// append to list
				commandList = finalPortName + " <- c("
				    + finalPortName + ", list(" + portName
				    + ") )";
			    }
			    // set in the R environment
			    _rEngine.eval(commandList);

			    // remove temporary objects that are now in the list
			    _rEngine.eval("rm(" + portName + ")");
			}

		    }
		} catch (NoTokenException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (IllegalActionException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (Exception e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }// multiport loop
	}
	log.debug("Done reading data from input ports");
    }

    private double[] _convertArrayToken(ArrayToken token) {
	double[] returnArray = new double[token.length()];
	Token[] tokens = token.arrayValue();
	for (int i = 0; i < tokens.length; i++) {
	    double value = ((DoubleToken) tokens[i]).doubleValue();
	    returnArray[i] = value;
	}
	return returnArray;
    }

    private int[] _convertArrayTokenToInt(ArrayToken token) {
	int[] returnArray = new int[token.length()];
	Token[] tokens = token.arrayValue();
	for (int i = 0; i < tokens.length; i++) {
	    int value = ((IntToken) tokens[i]).intValue();
	    returnArray[i] = value;
	}
	return returnArray;
    }

    private String[] _convertArrayTokenToString(ArrayToken token) {
	String[] returnArray = new String[token.length()];
	Token[] tokens = token.arrayValue();
	for (int i = 0; i < tokens.length; i++) {
	    String value = ((StringToken) tokens[i]).stringValue();
	    returnArray[i] = value;
	}
	return returnArray;
    }
	
    private Object[] _convertScalarArrayTokenToString(ArrayToken token) {
	Object[] returnArray = new String[token.length()];
	Token[] tokens = token.arrayValue();
	for (int i = 0; i < tokens.length; i++) {
	    Token t = tokens[i];
	    Object value = t.toString();
	    //TODO: REXP doesn't seem to support mixed arrays - needs to stay as String until fixed
	    if (t instanceof StringToken) {
		value = ((StringToken) t).stringValue();
	    } 
	    //			else if (t instanceof IntToken) {
	    //				value = ((IntToken) t).intValue();
	    //			} else if (t instanceof DoubleToken) {
	    //				value = ((DoubleToken) t).doubleValue();
	    //			} else if (t instanceof BooleanToken) {
	    //				value = ((BooleanToken) t).booleanValue();
	    //			}
			
	    returnArray[i] = value;
	}
	return returnArray;
    }

    private boolean[] _convertArrayTokenToBoolean(ArrayToken token) {
	boolean[] returnArray = new boolean[token.length()];
	Token[] tokens = token.arrayValue();
	for (int i = 0; i < tokens.length; i++) {
	    boolean value = ((BooleanToken) tokens[i]).booleanValue();
	    returnArray[i] = value;
	}
	return returnArray;
    }

    public static int[][] asIntMatrix(double[][] doubles) {
	int[][] returnArray = new int[doubles.length][doubles[0].length];
	for (int i = 0; i < doubles.length; i++) {
	    for (int j = 0; j < doubles[i].length; j++) {
		int value = Double.valueOf(doubles[i][j]).intValue();
		returnArray[i][j] = value;
	    }
	}
	return returnArray;
    }

    public static boolean[][] asBooleanMatrix(REXP x) {
	int[] ct = x.asIntArray();
	if (ct == null)
	    return null;
	REXP dim = x.getAttribute("dim");
	if (dim == null || dim.getType() != REXP.XT_ARRAY_INT)
	    return null; // we need dimension attr
	int[] ds = dim.asIntArray();
	if (ds == null || ds.length != 2)
	    return null; // matrix must be 2-dimensional
	int m = ds[0], n = ds[1];
	boolean[][] r = new boolean[m][n];
	if (ct == null)
	    return null;
	// R stores matrices as matrix(c(1,2,3,4),2,2) = col1:(1,2), col2:(3,4)
	// we need to copy everything, since we create 2d array from 1d array
	int i = 0, k = 0;
	while (i < n) {
	    int j = 0;
	    while (j < m) {
		boolean val = ct[k++] == 0 ? false : true;
		r[j++][i] = val;
	    }
	    i++;
	}
	return r;
    }

    /**
     * Creates a graphics device in the R environment where plots will be sent.
     * Actor will emit [pointer to] the graphics file that is generated. TODO:
     * emit actual file (object), not just a path (string)
     * 
     * @throws IllegalActionException
     */
    private void _setupJRIGraphicsDevice() throws IllegalActionException {
	log.debug("setting up graphics device: ");

	boolean graphicsOutputValue = ((BooleanToken) graphicsOutput.getToken())
	    .booleanValue();

	boolean displayGraphicsOutputValue = ((BooleanToken) displayGraphicsOutput
					      .getToken()).booleanValue();

	String graphicsFormatString = graphicsFormat.stringValue();

	// following line insures that graphics is pdf if automatically
	// displayed
	if (displayGraphicsOutputValue) {
	    graphicsFormatString = "pdf";
	}

	// force file format to 'pdf' is this is a Mac
	String lcOSName = System.getProperty("os.name").toLowerCase();
	boolean MAC_OS_X = lcOSName.startsWith("mac os x");
	if (MAC_OS_X) {
	    graphicsFormatString = "pdf";
	}

	String setCWD = "setwd('" + _home + "')\n";
	String graphicsDeviceCode = _generateGraphicsDeviceCode(
								graphicsFormatString, graphicsOutputValue);

	if (graphicsDeviceCode != null && graphicsDeviceCode.length() > 0) {
	    log.debug(setCWD + graphicsDeviceCode);

	    _rEngine.eval(setCWD);
	    _rEngine.eval(graphicsDeviceCode);
	}

	log.debug("done setting up graphics device");

    }

    /**
     * Turns off the graphics device that is created
     */
    private void _teardownJRI() {
	String cmd = "dev.off()";
	_rEngine.eval(cmd);
	// _rEngine.end();
	// re = null;
    }

    /**
     * Write tokens onto each output port of the actor if it is available from
     * the execution of the R Script. The type of each R Object is inspected to
     * determine which subclass of Token should be used for the conversion. Port
     * names are used to locate objects in the R environment with the same name.
     * 
     * @throws IllegalActionException
     */
    private void _writeOutputData() throws IllegalActionException {
	log.debug("Writing R output...");

	// Loop through all output ports, looking for R Objects of the same
	// name, and if they exist, convert the R object to a Kepler Token
	// and write it on the output port
	_opList = outputPortList();
	_iterO = _opList.iterator();
	while (_iterO.hasNext()) {
	    TypedIOPort tiop = (TypedIOPort) _iterO.next();
	    if (tiop.getName().equals("output")) {
		tiop.send(0, new StringToken(_console.getConsoleOutput()));
	    } else if (tiop.getName().equals("graphicsFileName")) {
		tiop.send(0, new StringToken(_graphicsOutputFile));
	    } else {
		// handle multiport
		int numSinks = tiop.numberOfSinks();
		int width = tiop.getWidth();

		REXP rDataObject = _rEngine.eval(tiop.getName());
		if (rDataObject != null) {
		    log.debug("Symbol found for port " + tiop.getName() + ": "
			      + rDataObject.toString());

		    // get the token from R
		    Token t = _convertToToken(rDataObject, tiop.getName());

		    // make sure that the sinks can handle the token
		    for (int channelIndex = 0; channelIndex < numSinks; channelIndex++) {
			Type sinkType = ((TypedIOPort) tiop.sinkPortList().get(
									       channelIndex)).getType();

			if (!sinkType.isCompatible(t.getType())) {
			    log.debug("[re]Setting sink type to: "
				      + t.getType().toString());
			    // set the Type for the sinks
			    // POSSIBLE BUG - not sure why the automatic type
			    // resolution was failing for downstream port
			    ((TypedIOPort) tiop.sinkPortList()
			     .get(channelIndex)).setTypeEquals(t
							       .getType());
			}
		    }

		    // send token to each channel
		    for (int channelIndex = 0; channelIndex < width; channelIndex++) {
			tiop.setTypeEquals(t.getType());
			tiop.send(channelIndex, t);
		    }

		} else {
		    log.debug("No symbol found for port: " + tiop.getName());
		}
	    }
	}
	log.debug("Done writing R output.");
    }

    private void _showGraphics() throws IllegalActionException {

	boolean displayGraphicsOutputValue = ((BooleanToken) displayGraphicsOutput
					      .getToken()).booleanValue();

	if (displayGraphicsOutputValue) {
	    try {
		File fout = new File(_graphicsOutputFile);
		URL furl = fout.toURL();
		BrowserLauncher.openURL(furl.toString());
	    } catch (Exception e) {
		log.error("Oops!:" + e);
	    }
	}
    }

    private Token _serializeRDataObject(REXP rDataObject, String name) {
	if (name != null) {
	    String fileName = _getUniqueFileName("sav");
	    _rEngine.eval("conn <- file('" + fileName + "', 'wb')");
	    _rEngine.eval("serialize(" + name + ", conn)");
	    _rEngine.eval("close(conn)");
	    return new StringToken("_dataframe_:" + fileName);
	}
	return null;
    }
	
    /**
     * Convert the data from the R expression type by determining the type of
     * the R object returned and then convert this to an appropriate Kepler
     * Token object that can be sent on the output port.
     * 
     * @param rDataObject
     *            the R Object that should be converted to a subclass of
     *            ptolemy.data.Token
     * @throws IllegalActionException
     */
    private Token _convertToToken(REXP rDataObject, String name)
	throws IllegalActionException {
	Token t = null;
	Token[] tokenArray = null;
	if (rDataObject != null) {
	    int xt = rDataObject.getType();
	    log.debug("Type found is: " + xt);
	    //Object rawContent = rDataObject.getContent();
	    //log.debug("Raw content is: " + rawContent);
			
	    switch (xt) {
	    case REXP.XT_BOOL:
		RBool b = rDataObject.asBool();
		if (b.isFALSE()) {
		    t = new BooleanToken(false);
		} else if (b.isTRUE()) {
		    t = new BooleanToken(true);
		} else { // b.isNA()
		    t = new BooleanToken("nil");
		}
		break;
	    case REXP.XT_DOUBLE:
		t = new DoubleToken(rDataObject.asDouble());
		break;
	    case REXP.XT_FACTOR:
		log.debug("R object is XT_FACTOR");
		RFactor factor = rDataObject.asFactor();
		List factorValues = new ArrayList();
		for (int i = 0; i < factor.size(); i++) {
		    StringToken stringToken = new StringToken(factor.at(i));
		    factorValues.add(stringToken);
		}
		t = new ArrayToken((Token[]) factorValues.toArray(new Token[0]));
		break;
	    case REXP.XT_INT:
		t = new IntToken(rDataObject.asInt());
		break;
	    case REXP.XT_LIST:
		log.debug("R object is XT_LIST");
		RList list = rDataObject.asList();
		String[] keys = list.keys();
		if (keys != null) {
		    List values = new ArrayList();
		    for (int i = 0; i < keys.length; i++) {
			REXP col = list.at(i);
			// recursion!
			Token token = _convertToToken(col, null);
			values.add(token);
		    }
		    // put it all together in a record
		    t = new OrderedRecordToken(
					       keys, 
					       (Token[]) values.toArray(new Token[0]));
		}
		break;
	    case REXP.XT_NULL:
		t = Token.NIL;
		break;
	    case REXP.XT_STR:
		t = new StringToken(rDataObject.asString());
		break;
	    case REXP.XT_SYM:
		log.debug("R object is XT_SYM");
		break;
	    case REXP.XT_UNKNOWN:
		log.debug("R object is XT_UNKNOWN");
		break;
	    case REXP.XT_VECTOR:
		log.debug("I am a XT_VECTOR!");
		RVector vector = rDataObject.asVector();
	
		// handle data.frame/Record structure
		List names = vector.getNames();
		if (names != null) {
		    // preserve _every_ aspect of the data object 
		    if (((BooleanToken) serializeData.getToken()).booleanValue()) {
			t = _serializeRDataObject(rDataObject, name);
		    }
		    else {
			List values = new ArrayList();
			for (int i = 0; i < names.size(); i++) {
			    String columnName = (String) names.get(i);
			    REXP col = vector.at(columnName);
			    // recursion!
			    Token token = _convertToToken(col, null);
			    values.add(token);
			}
			// put it all together in a record
			String[] namesArray = (String[]) names.toArray(new String[0]);
			Token[] valuesArray = (Token[]) values.toArray(new Token[0]);
			t = new OrderedRecordToken(namesArray, valuesArray);
		    }
		} else {
		    // handle a List
		    List values = new ArrayList();
		    for (int i = 0; i < vector.size(); i++) {
			REXP value = vector.at(i);
			// recursion!
			Token token = _convertToToken(value, null);
			values.add(token);
		    }
		    // put it all together in an array
		    if (values.isEmpty()) {
			t = new ArrayToken(Token.NIL.getType());
		    } else {
			t = new ArrayToken((Token[]) values.toArray(new Token[0]));
		    }
		}
		break;
	    case REXP.XT_ARRAY_BOOL:
		int[] xb = rDataObject.asIntArray();
		tokenArray = new Token[xb.length];
		for (int i = 0; i < xb.length; i++) {
		    String val = xb[i] == 0 ? "false" : (xb[i] == 1 ? "true"
							 : "nil");
		    BooleanToken bt = new BooleanToken(val);
		    tokenArray[i] = bt;
		}
		t = new ArrayToken(tokenArray);
		break;
	    case REXP.XT_ARRAY_BOOL_INT:
		// try matrix first
		boolean[][] bMatrix = asBooleanMatrix(rDataObject);
		if (bMatrix != null) {
		    t = new BooleanMatrixToken(bMatrix);
		    break;
		}
		int[] xbi = rDataObject.asIntArray();
		tokenArray = new Token[xbi.length];
		for (int i = 0; i < xbi.length; i++) {
		    String val = xbi[i] == 0 ? "false" : (xbi[i] == 1 ? "true"
							  : "nil");
		    BooleanToken bt = new BooleanToken(val);
		    tokenArray[i] = bt;
		}
		t = new ArrayToken(tokenArray);
		break;
	    case REXP.XT_ARRAY_DOUBLE:
		// try as matrix first
		double[][] matrix = rDataObject.asDoubleMatrix();
		if (matrix != null) {
		    t = new DoubleMatrixToken(matrix);
		    break;
		}
		// otherwise it is a simple list
		double[] xd = rDataObject.asDoubleArray();
		tokenArray = new Token[xd.length];
		for (int i = 0; i < xd.length; i++) {
		    DoubleToken dt = new DoubleToken(xd[i]);
		    tokenArray[i] = dt;
		}
		t = new ArrayToken(tokenArray);
		break;
	    case REXP.XT_ARRAY_INT:
		// try as matrix first
		double[][] matrixD = rDataObject.asDoubleMatrix();
		if (matrixD != null) {
		    t = new IntMatrixToken(asIntMatrix(matrixD));
		    break;
		}
		int[] xi = rDataObject.asIntArray();
		tokenArray = new Token[xi.length];
		for (int i = 0; i < xi.length; i++) {
		    IntToken dt = new IntToken(xi[i]);
		    tokenArray[i] = dt;
		}
		t = new ArrayToken(tokenArray);
		break;
	    case REXP.XT_ARRAY_STR:
		String[] xs = rDataObject.asStringArray();
		tokenArray = new Token[xs.length];
		for (int i = 0; i < xs.length; i++) {
		    StringToken st = new StringToken(xs[i]);
		    tokenArray[i] = st;
		}
		t = new ArrayToken(tokenArray);
		break;
	    }
	}
	//return a single token in cases when it's believed to be a list (JRI added this)
	//TODO: parameterize the switch
	boolean asArrayToken = true;
	if (!asArrayToken) {
	    if (t instanceof ArrayToken) {
		if ( ((ArrayToken)t).length() == 1) {
		    t = ((ArrayToken)t).getElement(0);
		}
	    }
	}
	return t;
    }

    private String _generateGraphicsDeviceCode(String graphicsFormatString,
					       boolean graphicsOutputValue) {
	String nxs = "";
	String nys = "";
	try {
	    nxs = numXPixels.stringValue();
	    try {
		int nxp = (new Integer(nxs)).intValue();
	    } catch (Exception w) {
		nxs = "480";
	    }
	} catch (IllegalActionException iae) {

	}

	try {
	    nys = numYPixels.stringValue();
	    try {
		int nyp = (new Integer(nys)).intValue();
	    } catch (Exception w1) {
		nys = "480";
	    }
	} catch (IllegalActionException iae) {

	}
	_graphicsOutputFile = _getUniqueFileName(graphicsFormatString);
	String graphicsDevice = "";
	if (graphicsOutputValue) {
	    int nxp = (new Integer(nxs)).intValue();
	    double nxd = nxp / 72.0;
	    int nyp = (new Integer(nys)).intValue();
	    double nyd = nyp / 72.0;
	    if (graphicsFormatString.equals("pdf")) {
		graphicsDevice = "pdf(file = '" + _graphicsOutputFile + "'"
		    + ",width = " + nxd + ", height = " + nyd + ")";
	    } else {
		graphicsDevice = "png(filename = '" + _graphicsOutputFile + "'"
		    + ",width = " + nxs + ", height = " + nys
		    + ", pointsize = 12, bg = 'white')";
	    }
	}
	return graphicsDevice;
    }

    private String _getUniqueFileName(String extender) {
	int cnt = 1;
	// String usr_name = System.getProperty("user.name");
	String actor_name = this.getName();
	actor_name = actor_name.replace(' ', '_');
	String fn = actor_name + cnt + "." + extender;
	String path = _home;
	while (new File(path, fn).exists()) {
	    cnt++;
	    fn = actor_name + cnt + "." + extender;
	}
	return new File(path, fn).getAbsolutePath();
    }

    ///////////////////////////////////////////////////////////////////
    ////           private variables                               ////

    /**
     * Execute the R system and run the model found in the expression provided
     * in the RExpression "expression" attribute. When complete, the REngine can
     * be queried to obtain an REXP object with the results of the execution.
     * @throws IllegalActionException 
     * 
     */
    private void _executeRModel() throws IllegalActionException {
	log.debug("Begin R script execution.");
		
	// get the lines of the script
	String script = expression.getExpression();
		
	// file for the source
	String tempFile = null;
	try {
	    tempFile = _getUniqueFileName("r");
	    // write to file
	    FileWriter fw = new FileWriter(tempFile);
	    fw.write(script);
	    fw.close();
	} catch (IOException e1) {
	    log.error("could not create temp R source file");
	    throw new IllegalActionException(e1.getMessage());
	}
		
	// simple!
	String line = "source('" + tempFile + "')";
			
	// write the expression to the logging _console
	_console.rWriteConsole(_rEngine, "> " + line + "\n", REXP.XT_STR);
	boolean showEval = ((BooleanToken) showDebug.getToken()).booleanValue();
			
	// evaluate the expression
	REXP x = _rEngine.eval(line, showEval);			
		
	// show result if configured to do so
	if (showEval) {
	    try {
		Token token = _convertToToken(x, null);
		_console.rWriteConsole(_rEngine, "Result: " + token + "\n", x.rtype);
	    }
	    catch (Exception e) {
		log.debug("error showing debug results from R engine: " + e.getMessage());
	    }
	}
	log.debug(x);
	
	log.debug("Finished R execution.");
    }

    private static String _noRErrorMessage = "There has been a problem launching R!\n"
	+ "It may be that R is not installed on your system, or it\n"
	+ "may not be on your path and cannot be located by Kepler.\n Please"
	+ "make sure R is installed and the R command line \n executable is in the path."
	+ "For more information, see \n section 8.2.2 of the Kepler User Manual.";

    private Rengine _rEngine = null;

    private RConsole _console = null;

    private static String _NO_SAVE = "--no-save";

    private static String _NO_RESTORE = "--no-restore";
	
    private String _graphicsOutputFile = "";

    private List _opList;

    private Iterator _iterO;

    private String _home;

}
