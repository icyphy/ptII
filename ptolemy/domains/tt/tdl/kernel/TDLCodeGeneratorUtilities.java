package ptolemy.domains.tt.tdl.kernel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.domains.fsm.modal.Refinement;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;

/**
 * Utilities for generating TDL code.
 * 
 * @author Patricia Derler
 */
public class TDLCodeGeneratorUtilities {
	/**
	 * Instances of this class cannot be created.
	 */
	private TDLCodeGeneratorUtilities() {
	}

	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	/**
	 * Throw an exception if the given string is a valid giotto reserved word,
	 * which prevents it from being used as an identifier.
	 * 
	 * @param string
	 *            A string to be used in Giotto program.
	 * @exception IllegalActionException
	 *                If the string can not be used.
	 */
	public static void checkTDLID(String string) throws IllegalActionException {
		if (string.equals("output")) {
			throw new IllegalActionException("The identifier " + string
					+ " cannot be used in a Giotto program.  "
					+ "Please change your model and attempt to "
					+ "generate code again.");
		}
	}

	/**
	 * Generate TDL code for the given TDL model.
	 * 
	 * @param model
	 *            The given TDL model.
	 * @return The TDL code.
	 * @exception IllegalActionException
	 *                If code can not be generated.
	 */
	public static String generateTDLCode(TypedCompositeActor model)
			throws IllegalActionException {
		StringBuffer sb = new StringBuffer();

		try {
			if (_initialize(model)) {
				String containerName = model.getName();

				_headerCode(sb, model);
				_getModules(model);
				Iterator it = _modules.iterator();
				while (it.hasNext()) {
					TDLModule module = (TDLModule) it.next();
					sb.append("module ");
					sb.append(module.getName());
					sb.append(" {\n\n");
					_sensorCode(sb, module);
					sb.append("\n");
					_actuatorCode(sb, module);
					_tasksCode(sb, module);
					Iterator modesIt = _getModes(module).iterator();
					while (modesIt.hasNext()) {
						_modeCode(sb, (State) modesIt.next(), module);
					}
					sb.append("}\n\n\n");
				}
			}

			model.wrapup();
		} catch (Throwable throwable) {
			System.out.println(throwable.getMessage());
			throw new IllegalActionException(model, throwable,
					"Failed to generate TDL code.");
		}

		return sb.toString();
	}

	/**
	 * Create an instance of a model and generate TDL code for it The Giotto
	 * code is printed on standard out.
	 * 
	 * @param args
	 *            The command-line arguments naming the .xml or .moml file to
	 *            run
	 * @exception Throwable
	 *                If there is a problem reading the model or generating
	 *                code.
	 */
	public static void main(String[] args) throws Throwable {
		try {
			if (args.length != 1) {
				throw new IllegalArgumentException(
						"Usage: java -classpath $PTII "
								+ "ptolemy.domains.giotto.kernel"
								+ ".GiottoCodeGeneratorUtilities ptolemyModel.xml\n"
								+ "The model is read in and Giotto code is "
								+ "generated on stdout.");
			}

			MoMLParser parser = new MoMLParser();

			// We set the list of MoMLFilters to handle Backward Compatibility.
			MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());

			// Filter out any graphical classes.
			MoMLParser.addMoMLFilter(new RemoveGraphicalClasses());

			// If there is a MoML error, then throw the exception as opposed
			// to skipping the error. If we call StreamErrorHandler instead,
			// then the nightly build may fail to report MoML parse errors
			// as failed tests
			// parser.setErrorHandler(new StreamErrorHandler());
			// We use parse(URL, URL) here instead of parseFile(String)
			// because parseFile() works best on relative pathnames and
			// has problems finding resources like files specified in
			// parameters if the xml file was specified as an absolute path.
			TypedCompositeActor toplevel = (TypedCompositeActor) parser.parse(
					null, new File(args[0]).toURL());

			System.out.println(generateTDLCode(toplevel));
		} catch (Throwable ex) {
			System.err.println("Command failed: " + ex);
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * Return true if the given actor has at least one connected input port,
	 * which requires it to have an input driver.
	 * 
	 * @param actor
	 *            The actor to test.
	 * @return True if the given actor has at least on connected input port.
	 */
	public static boolean needsInputDriver(Actor actor) {
		boolean retVal = false;
		Iterator inPorts = actor.inputPortList().iterator();

		while (inPorts.hasNext() && !retVal) {
			TypedIOPort port = (TypedIOPort) inPorts.next();

			if (port.getWidth() > 0) {
				retVal = true;
			}
		}

		return retVal;
	}

	// /////////////////////////////////////////////////////////////////
	// // protected methods ////

	/**
	 * Generate code for the actuator. Usually, there is only one actuator.
	 * 
	 * @param sb
	 * @param model
	 *            The model.
	 * @return The actuator code.
	 * @exception IllegalActionException
	 *                If there is a problem accessing the ports.
	 */
	protected static void _actuatorCode(StringBuffer sb, TDLModule module)
			throws IllegalActionException {
		Iterator outPorts = module.outputPortList().iterator();

		while (outPorts.hasNext()) {
			TypedIOPort port = (TypedIOPort) outPorts.next();

			if (port.getWidthInside() > 0) {
				String portID = port.getName();
				String portTypeID = _getTypeString(port);
				String setterName = _getRefinementPortParameter(port, "setter");
				if (setterName == null) // is not an actuator but a module
										// output port
					return;
				checkTDLID(portID);
				sb.append("  actuator " + portTypeID + " " + portID + " uses "
						+ setterName + ";\n");
			}
		}

	}

	/**
	 * default type is double
	 * 
	 * @param port
	 *            An IO port.
	 * @return A string containing the type of the port.
	 * @throws IllegalActionException
	 */
	protected static String _getTypeString(IOPort port)
			throws IllegalActionException {
		if (port instanceof TypedIOPort)
			return ((TypedIOPort) port).getType().toString();
		String portType;
		Parameter parameter = (Parameter) port.getAttribute("type");
		if (parameter != null) {
			StringToken token = (StringToken) parameter.getToken();
			portType = token.stringValue();
		} else {
			portType = "double"; // default
		}
		return portType;
	}

	/**
	 * Generate header code for the file. Usually, there is only one header.
	 * 
	 * @param sb
	 * @param model
	 *            The model.
	 * @return The header code.
	 * @exception IllegalActionException
	 *                If there is a problem getting the model name.
	 */
	protected static void _headerCode(StringBuffer sb, TypedCompositeActor model)
			throws IllegalActionException {
	}

	/**
	 * Initialize the code generation process by checking whether the given
	 * model is a Giotto model. Return false if it is not.
	 * 
	 * @param model
	 *            A model to generate Giotto code from.
	 * @return True if in the given model is a tdl model.
	 */
	protected static boolean _initialize(TypedCompositeActor model) {
		Director director = model.getDirector();
		return (director instanceof DEDirector);
	}

	/**
	 * Generate code for the modes.
	 * 
	 * @param sb
	 * @param model
	 *            The model.
	 * @return The modes code.
	 * @exception IllegalActionException
	 *                If there is a problem getting the director or accessing
	 *                the ports.
	 */
	protected static void _modeCode(StringBuffer sb, State state,
			TDLModule module) throws IllegalActionException {
		int actorFreq = 0;

		String outputName;
		String actorName;
		String modeName;

		modeName = state.getName();

		double periodValue = ((TDLModuleDirector) module.getDirector())
				.getModePeriod(state);
		String start = "";
		if (((BooleanToken) state.isInitialState.getToken()).booleanValue())
			start = "start ";

		sb.append("  " + start + "mode " + modeName + " [period=" + periodValue
				+ " s] {\n");

		Refinement refinement = (Refinement) state.getRefinement()[0];

		Iterator taskIterator = refinement.entityList().iterator();
		if (taskIterator.hasNext())
			sb.append("    task\n");
		while (taskIterator.hasNext()) {
			Actor actor = (Actor) taskIterator.next();
			int frequency = TDLModeScheduler.getFrequency((NamedObj) actor);
			String taskoutputPorts = "";

			// get all input ports that are connected to another module's output
			// ports
			Iterator it = actor.inputPortList().iterator();
			while (it.hasNext()) {
				IOPort port = (IOPort) it.next();
				IOPort connectedToModuleOutputPort = (IOPort) port
						.deepConnectedInPortList().get(0);
				if (_portsConnectedToModuleOutputPorts
						.contains(connectedToModuleOutputPort)) {
					List ports = connectedToModuleOutputPort
							.deepConnectedOutPortList();
					IOPort moduleOutputPort = (IOPort) ports.get(0);
					ports = moduleOutputPort.deepInsidePortList();// deepConnectedInPortList();

					String fromPortName = ((Port) ports.get(1)).getFullName()
							.substring(1);
					fromPortName = fromPortName.substring(fromPortName
							.indexOf('.') + 1);
					fromPortName.replace(fromPortName.substring(fromPortName
							.indexOf('.'), fromPortName.indexOf('.',
							fromPortName.indexOf('.') + 1) + 1), "");
					taskoutputPorts += port.getName() + " := " + fromPortName
							+ "; ";
				}
			}
			if (taskoutputPorts.length() > 0)
				taskoutputPorts = taskoutputPorts.substring(0, taskoutputPorts
						.length() - 2);
			sb.append("      [freq=" + frequency + "] " + actor.getName() + "{"
					+ taskoutputPorts + "}\n"); // TODO: import
		}

		Iterator actorIterator = refinement.outputPortList().iterator();
		if (actorIterator.hasNext())
			sb.append("    actuator\n");
		while (actorIterator.hasNext()) {
			IOPort port = (IOPort) actorIterator.next();
			// TODO task output ports
			int frequency = TDLModeScheduler.getFrequency(port);
			// List ports = port.connectedPortList();
			// IOPort p = (IOPort) ports.get(2);
			String portName = port.getFullName().substring(1);
			portName = portName.substring(portName.indexOf('.') + 1);
			portName = portName.substring(portName.indexOf('.') + 1);
			sb.append("      [freq=" + frequency + "] " + port.getName()
					+ " := " + portName + ";\n");
		}

		Iterator transitionIterator = state.nonpreemptiveTransitionList()
				.iterator();
		if (transitionIterator.hasNext())
			sb.append("    mode\n");
		while (transitionIterator.hasNext()) {
			Transition transition = (Transition) transitionIterator.next();
			int frequency = TDLModeScheduler.getFrequency(transition);
			sb.append("      [freq=" + frequency + "] if "
					+ transition.getName() + " then "
					+ transition.destinationState().getName() + ";\n");
		}
		sb.append("  }\n");

	}

	/**
	 * Generate code for the sensors.
	 * 
	 * @param sb
	 * @param model
	 *            The model from which we generate code.
	 * @return The sensors code.
	 * @exception IllegalActionException
	 *                If there is a problem iterating over the actors.
	 */
	protected static void _sensorCode(StringBuffer sb, TDLModule module)
			throws IllegalActionException {
		Iterator inPorts = module.inputPortList().iterator();

		while (inPorts.hasNext()) {
			IOPort port = (IOPort) inPorts.next();

			// Ignore unconnected ports
			if (port.getWidthInside() > 0) {
				String portID = port.getName();
				String portTypeID = _getTypeString(port);
				checkTDLID(portID);
				String getterName = _getRefinementPortParameter(port, "getter");
				if (getterName == null) { // is not a sensor but an imported
											// module's output port
					_portsConnectedToModuleOutputPorts.add(port);
					List l = port.deepConnectedOutPortList();
					if (l.size() > 0) {
						Port actuator = (Port) l.get(0);
						String portName = actuator.getFullName().substring(1);
						String importModule = portName.substring(portName
								.indexOf('.') + 1, portName.lastIndexOf('.'));

						portName = port.getFullName().substring(1);
						String currentModule = portName.substring(portName
								.indexOf('.') + 1, portName.lastIndexOf('.'));
						if (!(sb.indexOf("import " + importModule) >= 0)) {
							String insertPosition = "module " + currentModule
									+ " {";
							sb.insert(sb.indexOf(insertPosition)
									+ insertPosition.length() + 2, "  import "
									+ importModule + ";\n");
						}
					}
				} else
					sb.append("  sensor " + portTypeID + " " + portID
							+ " uses " + getterName + ";\n");
			}
		}
	}

	/**
	 * Generate code for the tasks.
	 * 
	 * @param sb
	 * @param model
	 *            The model from which we generate code.
	 * @return The task code.
	 * @exception IllegalActionException
	 *                If there is a problem iterating over the actors.
	 */
	protected static void _tasksCode(StringBuffer sb, TDLModule module)
			throws IllegalActionException {
		sb.append("\n");
		Iterator it = module.entityList().iterator();
		while (it.hasNext()) {
			Object object = it.next();
			if (object instanceof Refinement) {
				Refinement refinement = (Refinement) object;
				Iterator taskIterator = refinement.entityList().iterator();
				while (taskIterator.hasNext()) {
					Actor actor = (Actor) taskIterator.next();
					if (!(sb.indexOf("task " + actor.getName() + " {") > 0)) {
						sb.append("  task ");
						sb.append(actor.getName());
						sb.append(" {\n");
						if (TDLModuleDirector.isFast((NamedObj) actor)) {
							// TODO fast task
						}
						// TODO [wcet= ...]
						StringBuffer taskList = _portList(sb, actor, "  input");
						_portList(sb, actor, "  output");
						sb.append("    uses " + actor.getName() + "Impl("
								+ taskList + ");\n");

						sb.append("  }\n\n");
					}
				}
			}
		}
	}

	/**
	 * for whatever reason, the parameter specified at the modalport is not
	 * stored there but at the refinement port one level inside
	 * modalport.insideReceivers[0][0].getParameter("getter") returns the
	 * correct value
	 * 
	 * @param port
	 * @return
	 * @throws KernelException
	 */
	private static String _getRefinementPortParameter(IOPort port,
			String paramName) {
		Port refinementPort = (Port) port.deepInsidePortList().get(0);
		Parameter getter = (Parameter) refinementPort.getAttribute(paramName);
		try {
			StringToken token = (StringToken) getter.getToken();
			return token.stringValue();
		} catch (Exception ex) {
			// nothing to do here
		}

		return null;
	}

	private static StringBuffer _portList(StringBuffer sb, Actor actor,
			String type) throws IllegalActionException {
		StringBuffer taskList = new StringBuffer();
		Iterator portsIt = null;
		if (type.equals("  input")) {
			List inputs = actor.inputPortList();
			List outputs = actor.outputPortList();
			List copiedinputs = new ArrayList(inputs);
			for (int i = 0; i < inputs.size(); i++) {
				Port inport = (Port) inputs.get(i);
				for (int j = 0; j < outputs.size(); j++) {
					Port outport = (Port) outputs.get(j);
					if (inport.connectedPortList().size() > 1
							&& inport.connectedPortList().get(1)
									.equals(outport)) {
						copiedinputs.remove(inport);
						continue;
					}
				}
			}
			portsIt = copiedinputs.iterator();
			// TODO find out which inports are outputs
		} else
			portsIt = actor.outputPortList().iterator();
		if (portsIt.hasNext())
			sb.append("  " + type + "\n");
		while (portsIt.hasNext()) {
			IOPort port = (IOPort) portsIt.next();
			String portName = port.getName();
			String portTypeID = _getTypeString(port);
			sb.append("      " + portTypeID + " " + portName);
			taskList.append(portName);
			if (portsIt.hasNext()) {
				taskList.append(", ");
			}
			sb.append(";\n");
		}
		return taskList;
	}

	private static void _getModules(TypedCompositeActor model) {
		Iterator it = model.entityList().iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof TDLModule)
				_modules.add(obj);
		}
	}

	private static List _getModes(TDLModule module)
			throws IllegalActionException {
		List modes = new ArrayList();
		Iterator it = ((TDLModuleDirector) module.getDirector())
				.getController().entityList().iterator();
		while (it.hasNext()) {
			State state = (State) it.next();
			modes.add(state);
		}
		return modes;
	}

	private static List _modules = new ArrayList();

	private static Set _portsConnectedToModuleOutputPorts = new HashSet();
}
