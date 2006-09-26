/*
@Copyright (c) 1998-2006 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.actor.ptalon;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.Type;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.util.StringUtilities;

/**
 A helper class to store information, like variable
 scope info, about the compiler.

 @author Adam Cataldo
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class CodeManager {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /**
     * Create a new CodeManager in the specified actor.
     * @param actor The actor to manage the code for.
     */
    public CodeManager(PtalonActor actor) {
        _actor = actor;
        _actorSet = false;
        _counter = 0;
        _root = new IfTree(null, getNextIfSymbol());
        _root.setActiveBranch(true);
        _imports = new Hashtable<String, File>();
        _currentTree = _root;
    }

    /**
     * Notify this manager that the actor name
     * has been set.
     * @param set
     */
    public void actorNameSet(boolean set) {
        _actorSet = set;
    }
    /**
     * Add a PtalonParameter to the PtalonActor
     * with the specified name.
     * @param name The name of the parameter.
     * @exception PtalonRuntimeException If the symbol does not exist, or if
     * the symbol already has a parameter associated with it, or if an IllegalActionException is thrown
     * trying to create the parameter.
     */
    public void addActorParameter(String name) throws PtalonRuntimeException {
        String uniqueName = _actor.uniqueName(name);
        try {
            PtalonParameter parameter = new PtalonParameter(_actor, uniqueName);
            _currentTree.setStatus(name, true);
            _currentTree.mapName(name, uniqueName);
        } catch (NameDuplicationException e) {
            throw new PtalonRuntimeException("NameDuplicationException", e);
        } catch (IllegalActionException e) {
            throw new PtalonRuntimeException("IllegalActionException", e);
        }
    }
    
    /**
     * Add an invisible PtalonParameter to the PtalonActor
     * with the specified name.
     * @param name The name of the parameter.
     * @param expression The expression representing the parameter.
     * @exception PtalonRuntimeException If the symbol does not exist, or if
     * the symbol already has a parameter associated with it, or if an IllegalActionException is thrown
     * trying to create the parameter.
     */
    public void addActorParameter(String name, String expression) throws PtalonRuntimeException {
        String uniqueName = _actor.uniqueName(name);
        try {
            PtalonParameter parameter = new PtalonParameter(_actor, uniqueName);
            parameter.setVisibility(Settable.NONE);
            _currentTree.setStatus(name, true);
            _currentTree.mapName(name, uniqueName);
            _unassignedParameters.add(parameter);
            _unassignedParameterValues.add(expression);
        } catch (NameDuplicationException e) {
            throw new PtalonRuntimeException("NameDuplicationException", e);
        } catch (IllegalActionException e) {
            throw new PtalonRuntimeException("IllegalActionException", e);
        }
    }

    /**
     * Add a PtalonBoolParameter to the PtalonActor
     * with the specified name.
     * @param name The name of the boolean parameter.
     * @exception PtalonRuntimeException If the symbol does not exist, or if
     * the symbol already has a parameter associated with it, or if an IllegalActionException is thrown
     * trying to create the parameter.
     */
    public void addBoolParameter(String name) throws PtalonRuntimeException {
        String uniqueName = _actor.uniqueName(name);
        try {
            PtalonBoolParameter parameter = new PtalonBoolParameter(_actor,
                    uniqueName);
            _currentTree.setStatus(name, true);
            _currentTree.mapName(name, uniqueName);
        } catch (NameDuplicationException e) {
            throw new PtalonRuntimeException("NameDuplicationException", e);
        } catch (IllegalActionException e) {
            throw new PtalonRuntimeException("IllegalActionException", e);
        }
    }

    /**
     * Add a TypedIOPort to the PtalonActor
     * with the specified name, and input flow type
     * @param name The name of the port.
     * @exception PtalonRuntimeException If the symbol does not exist, or if
     * the symbol already has a port associated with it, or if an IllegalActionException is thrown
     * trying to create the port.
     */
    public void addInPort(String name) throws PtalonRuntimeException {
        String uniqueName = _actor.uniqueName(name);
        try {
            TypedIOPort port = new TypedIOPort(_actor, uniqueName);
            port.setInput(true);
            port.setOutput(false);
            if (_currentTree.getType(name).equals("multiinport")) {
                port.setMultiport(true);
            }
            _currentTree.setStatus(name, true);
            _currentTree.mapName(name, uniqueName);
        } catch (NameDuplicationException e) {
            throw new PtalonRuntimeException("NameDuplicationException", e);
        } catch (IllegalActionException e) {
            throw new PtalonRuntimeException("IllegalActionException", e);
        } catch (PtalonScopeException e) {
            throw new PtalonRuntimeException("Couldn't find symbol " + name, e);
        }
    }

    /**
     * Add a PtalonIntParameter to the PtalonActor
     * with the specified name.
     * @param name The name of the integer parameter.
     * @exception PtalonRuntimeException If the symbol does not exist, or if
     * the symbol already has a parameter associated with it, or if an IllegalActionException is thrown
     * trying to create the parameter.
     */
    public void addIntParameter(String name) throws PtalonRuntimeException {
        String uniqueName = _actor.uniqueName(name);
        try {
            PtalonIntParameter parameter = new PtalonIntParameter(_actor,
                    uniqueName);
            _currentTree.setStatus(name, true);
            _currentTree.mapName(name, uniqueName);
        } catch (NameDuplicationException e) {
            throw new PtalonRuntimeException("NameDuplicationException", e);
        } catch (IllegalActionException e) {
            throw new PtalonRuntimeException("IllegalActionException", e);
        }
    }

    /**
     * Add a symbol for an import statement.  The statement has form
     * > import foo.bar.baz
     * where $PTII\foo\bar\baz.ptln is a valid Ptalon file.
     * The corresponding symbol in the Ptalon code should be baz. This 
     * returns the name of the symbol in the Ptalon code that refers to
     * this import.  In this case, it returns "baz". 
     * 
     * @param name The qualified identifier in the import statement.
     * @return The name of the symbol in the rest of the code refering to this
     * import. 
     * @exception PtalonScopeException If there was any trouble locating
     * the file.
     */
    public String addImport(String name) throws PtalonScopeException {
        try {
            StringTokenizer tokens = new StringTokenizer(name, ".");
            File filename = new File(StringUtilities
                    .getProperty("ptolemy.ptII.dir"));
            String symbol = "";
            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken();
                if (!tokens.hasMoreTokens()) {
                    symbol = new String(token);
                    token += ".ptln";
                }
                filename = new File(filename, token);
            }
            if (!filename.exists()) {
                throw new PtalonScopeException("File " + filename
                        + " does not exist");
            }
            _root.addSymbol(symbol, "import");
            _root.setStatus(symbol, true);
            _imports.put(symbol, filename);
            return symbol;
        } catch (Exception e) {
            throw new PtalonScopeException("Unable to import " + name, e);
        }
    }

    /**
     * Add a symbol for an import statement, with the
     * specified filename.  The filename must begin
     * with $PTII.
     *
     * @param name The qualified identifier in the import statement.
     * @param filename The filename for the import.
     * @exception PtalonScopeException If there was any trouble locating
     * the file.
     */
    public void addImport(String name, String filename)
            throws PtalonScopeException {
        try {
            String ptiiDir = StringUtilities.getProperty("ptolemy.ptII.dir");
            String absolutePathName = StringUtilities.substituteFilePrefix(
                    "$PTII", filename, ptiiDir);
            File file = new File(absolutePathName);
            if (!file.exists()) {
                throw new PtalonScopeException("Unable to import " + name);
            }
            _currentTree.addSymbol(name, "import");
            _currentTree.setStatus(name, true);
            _imports.put(name, file);
        } catch (Exception e) {
            throw new PtalonScopeException("Unable to import " + name, e);
        }
    }

    /**
     * Add a TypedIOPort to the PtalonActor
     * with the specified name, and output flow type
     * @param name The name of the port.
     * @exception PtalonRuntimeException If the symbol does not exist, or if
     * the symbol already has a port associated with it, or if an IllegalActionException is thrown
     * trying to create the port.
     */
    public void addOutPort(String name) throws PtalonRuntimeException {
        String uniqueName = _actor.uniqueName(name);
        try {
            TypedIOPort port = new TypedIOPort(_actor, uniqueName);
            port.setInput(false);
            port.setOutput(true);
            if (_currentTree.getType(name).equals("multioutport")) {
                port.setMultiport(true);
            }
            _currentTree.setStatus(name, true);
            _currentTree.mapName(name, uniqueName);
        } catch (NameDuplicationException e) {
            throw new PtalonRuntimeException("NameDuplicationException", e);
        } catch (IllegalActionException e) {
            throw new PtalonRuntimeException("IllegalActionException", e);
        } catch (PtalonScopeException e) {
            throw new PtalonRuntimeException("Couldn't find symbol " + name, e);
        }
    }

    /**
     * Add a Parameter to the PtalonActor
     * with the specified name.
     * @param name The name of the parameter.
     * @exception PtalonRuntimeException If the symbol does not exist, or if
     * the symbol already has a parameter associated with it, or if an IllegalActionException is thrown
     * trying to create the parameter.
     */
    public void addParameter(String name) throws PtalonRuntimeException {
        String uniqueName = _actor.uniqueName(name);
        try {
            PtalonExpressionParameter parameter = new PtalonExpressionParameter(_actor, uniqueName);
            _currentTree.setStatus(name, true);
            _currentTree.mapName(name, uniqueName);
        } catch (NameDuplicationException e) {
            throw new PtalonRuntimeException("NameDuplicationException", e);
        } catch (IllegalActionException e) {
            throw new PtalonRuntimeException("IllegalActionException", e);
        }
    }
    
    /**
     * Add an invisible Parameter to the PtalonActor
     * with the specified name and the given expression as its value.
     * @param name The name of the parameter.
     * @exception PtalonRuntimeException If the symbol does not exist, or if
     * the symbol already has a parameter associated with it, or if an IllegalActionException is thrown
     * trying to create the parameter.
     */
    public void addParameter(String name, String expression) throws PtalonRuntimeException {
        String uniqueName = _actor.uniqueName(name);
        try {
            PtalonExpressionParameter parameter = new PtalonExpressionParameter(_actor, uniqueName);
            parameter.setVisibility(Settable.NONE);
            _currentTree.setStatus(name, true);
            _currentTree.mapName(name, uniqueName);
            _unassignedParameters.add(parameter);
            _unassignedParameterValues.add(expression);
        } catch (NameDuplicationException e) {
            throw new PtalonRuntimeException("NameDuplicationException", e);
        } catch (IllegalActionException e) {
            throw new PtalonRuntimeException("IllegalActionException", e);
        }
    }
    
    /**
     * Add a TypedIOPort to the PtalonActor
     * with the specified name.
     * @param name The name of the port.
     * @exception PtalonRuntimeException If the symbol does not exist, or if
     * the symbol already has a port associated with it, or if an IllegalActionException is thrown
     * trying to create the port.
     */
    public void addPort(String name) throws PtalonRuntimeException {
        String uniqueName = _actor.uniqueName(name);
        try {
            TypedIOPort port = new TypedIOPort(_actor, uniqueName);
            port.setInput(true);
            port.setOutput(true);
            if (_currentTree.getType(name).equals("multiport")) {
                port.setMultiport(true);
            }
            _currentTree.setStatus(name, true);
            _currentTree.mapName(name, uniqueName);
        } catch (NameDuplicationException e) {
            throw new PtalonRuntimeException("NameDuplicationException", e);
        } catch (IllegalActionException e) {
            throw new PtalonRuntimeException("IllegalActionException", e);
        } catch (PtalonScopeException e) {
            throw new PtalonRuntimeException("Couldn't find symbol " + name, e);
        }
    }

    /**
     * Add a TypedIORelation to the PtalonActor
     * with the specified name.
     * @param name The name of the relation.
     * @exception PtalonRuntimeException If the symbol does not exist, or if
     * the symbol already has a relation associated with it, or if an IllegalActionException is thrown
     * trying to create the relation.
     */
    public void addRelation(String name) throws PtalonRuntimeException {
        String uniqueName = _actor.uniqueName(name);
        try {
            TypedIORelation relation = new TypedIORelation(_actor, uniqueName);
            _currentTree.setStatus(name, true);
            _currentTree.mapName(name, uniqueName);
        } catch (NameDuplicationException e) {
            throw new PtalonRuntimeException("NameDuplicationException", e);
        } catch (IllegalActionException e) {
            throw new PtalonRuntimeException("IllegalActionException", e);
        }
    }

    /**
     * Add a symbol with the given name and type to the sybol table
     * at the current level of the if-tree hierachy.
     * @param name The symbol name.
     * @param type The symbol type.
     * @exception PtalonScopeException If a symbol with this name has already
     * been added somewhere in the current scope.
     */
    public void addSymbol(String name, String type) throws PtalonScopeException {
        List<IfTree> ancestors = _currentTree.getAncestors();
        for (IfTree tree : ancestors) {
            for (String symbol : tree.getSymbols()) {
                if (symbol.equals(name)) {
                    throw new PtalonScopeException("Cannot add " + type + " "
                            + name + " because symbol " + name
                            + " already exists in scope " + tree.getName());
                }
            }
        }
        _currentTree.addSymbol(name, type);
    }

    /**
     * Add a symbol to the scope of this if statement.
     * @param symbol The sybmol to add.
     * @param type Its corresponding type.
     * @param status It's statust, that is whether it has been loaded or not.
     * @param uniqueName The unique name of this 
     */
    public void addSymbol(String symbol, String type, boolean status,
            String uniqueName) {
        _currentTree.addSymbol(symbol, type, status, uniqueName);
    }    

    /**
     * Assign the lvalue in the specifed actor to the
     * rvalue in this CodeManager's actor.
     * @param actorName The name of the actor contained by this manager's actor,
     * or in the case where the name has for foo.bar.baz, it's the name
     * of the actor baz, contained by bar, contained by foo, contained
     * by this manager's actor.
     * @param lvalue The value in the contained actor.
     * @param rvalue The value in the containing actor.
     * @exception PtalonRuntimeException If there is a problem assigning
     * the values.
     */
    public void assign(String actorName, String lvalue, String rvalue)
            throws PtalonRuntimeException {
        try {
            StringTokenizer tokenizer = new StringTokenizer(actorName, ".");
            ComponentEntity contained = _actor.getEntity(tokenizer.nextToken());
            while (tokenizer.hasMoreElements()) {
                if (contained instanceof PtalonActor) {
                    PtalonActor composite = (PtalonActor) contained;
                    contained = composite.getEntity(tokenizer.nextToken());
                } else {
                    throw new PtalonRuntimeException("No such PtalonActor "
                            + actorName);
                }
            }
            if (_actor == null) {
                throw new PtalonRuntimeException("Actor " + actorName
                        + " does not exist");
            }
            if (getType(rvalue).endsWith("port")) {
                if (contained.getPort(lvalue) == null) {
                    throw new PtalonRuntimeException("Actor " + actorName
                            + " has no port " + lvalue);
                } else {
                    Port lport = contained.getPort(lvalue);
                    String rname = getMappedName(rvalue);
                    PtalonActor container = (PtalonActor) contained
                            .getContainer();
                    Port rport = container.getPort(rname);
                    String relName = container.uniqueName("relation");
                    TypedIORelation relation = new TypedIORelation(container,
                            relName);
                    lport.link(relation);
                    rport.link(relation);
                }
            } else if (getType(rvalue).equals("relation")) {
                if (contained.getPort(lvalue) == null) {
                    throw new PtalonRuntimeException("Actor " + actorName
                            + " has no port " + lvalue);
                } else {
                    Port lport = contained.getPort(lvalue);
                    String rname = getMappedName(rvalue);
                    TypedIORelation relation = (TypedIORelation) _actor
                            .getRelation(rname);
                    lport.link(relation);
                }
            }
        } catch (Exception e) {
            String message = "Unable to connect lvalue " + lvalue
                    + " to rvalue " + rvalue + " in contained actor "
                    + actorName;
            throw new PtalonRuntimeException(message, e);
        }
    }
    
    /**
     * Assign any internal parameters in the order they were set.
     * @throws PtalonRuntimeException If there is any trouble assigning
     * parameter values.
     */
    public void assignInternalParameters() throws PtalonRuntimeException {
        try {
            while (!_unassignedParameters.isEmpty()) {
        
                PtalonParameter parameter = _unassignedParameters.remove(0);
                String expression = _unassignedParameterValues.remove(0);
                parameter.setToken(expression);
            }
        } catch (Exception e) {
            throw new PtalonRuntimeException("Trouble assigning parameter", e);
        }
    }

    /**
     * Enter the named subscope.
     * @param scope The named subscope.
     * @exception PtalonRuntimeException If the subscope does not exist.
     */
    public void enterIfScope(String scope) throws PtalonRuntimeException {
        boolean exists = false;
        for (IfTree tree : _currentTree.getChildren()) {
            if (tree.getName().equals(scope)) {
                exists = true;
                _currentTree = tree;
                break;
            }
        }
        if (!exists) {
            throw new PtalonRuntimeException("Subscope " + scope
                    + " does not exist");
        }
    }

    /**
     * Exit the current if scope.
     * @exception PtalonRuntimeException If already at the top-level if scope.
     */
    public void exitIfScope() throws PtalonRuntimeException {
        if (_currentTree.getParent() == null) {
            throw new PtalonRuntimeException("Already at top level");
        }
        _currentTree = _currentTree.getParent();
    }

    /**
     * Get the boolean value associated with the specified parameter.
     * @param param The parameter's name in the Ptalon code.
     * @return It's unique value.
     * @exception PtalonRuntimeException If the paramter does not exist or have
     * a boolean value. 
     */
    public boolean getBooleanValueOf(String param)
            throws PtalonRuntimeException {
        try {
            String uniqueName = getMappedName(param);
            PtalonBoolParameter att = (PtalonBoolParameter) _actor
                    .getAttribute(uniqueName);
            return ((BooleanToken) att.getToken()).booleanValue();
        } catch (Exception e) {
            throw new PtalonRuntimeException(
                    "Unable to access boolean value for " + param, e);
        }
    }

    /**
     * Get the int value associated with the specified parameter.
     * @param param The parameter's name in the Ptalon code.
     * @return It's unique value.
     * @exception PtalonRuntimeException If the paramter does not exist or have
     * a boolean value. 
     */
    public int getIntValueOf(String param) throws PtalonRuntimeException {
        try {
            String uniqueName = getMappedName(param);
            PtalonIntParameter att = (PtalonIntParameter) _actor
                    .getAttribute(uniqueName);
            return ((IntToken) att.getToken()).intValue();
        } catch (Exception e) {
            throw new PtalonRuntimeException("Unable to access int value for "
                    + param, e);
        }
    }

    /**
     * Get the value associated with the specified parameter.
     * @param param The parameter's name in the Ptalon code.
     * @return It's unique value.
     * @exception PtalonRuntimeException If the paramter does not exist. 
     */
    public Token getValueOf(String param) throws PtalonRuntimeException {
        try {
            String uniqueName = getMappedName(param);
            PtalonParameter att = (PtalonParameter) _actor
                    .getAttribute(uniqueName);
            att.toString(); 
            /*This previous line seems to cause some evaluation that
             * is necessary for the next line to not throw an exception.  
             * I don't exactly know why, but things seemed to only work 
             * when I was in the debugger, and only when I viewed the "att"
             * value in the debugger.  Since I figured this would require 
             * toString() to be called, I tried adding this line, and the 
             * exception went away.
             */
            return att.getToken();
        } catch (Exception e) {
            throw new PtalonRuntimeException("Unable to access int value for "
                    + param, e);
        }
    }

    /**
     * Get the type associated with the specified parameter.
     * @param param The parameter's name in the Ptalon code.
     * @return It's type.
     * @exception PtalonRuntimeException If the paramter does not exist. 
     */
    public Type getTypeOf(String param) throws PtalonRuntimeException {
        try {
            String uniqueName = getMappedName(param);
            PtalonParameter att = (PtalonParameter) _actor
                    .getAttribute(uniqueName);
            return att.getType();
        } catch (Exception e) {
            throw new PtalonRuntimeException("Unable to access int value for "
                    + param, e);
        }
    }
    
    /**
     * Get the type term associated with the specified parameter.
     * @param param The parameter's name in the Ptalon code.
     * @return It's type.
     * @exception PtalonRuntimeException If the paramter does not exist. 
     */
    public InequalityTerm getTypeTermOf(String param) throws PtalonRuntimeException {
        try {
            String uniqueName = getMappedName(param);
            PtalonParameter att = (PtalonParameter) _actor
                    .getAttribute(uniqueName);
            return att.getTypeTerm();
        } catch (Exception e) {
            throw new PtalonRuntimeException("Unable to access int value for "
                    + param, e);
        }
    }
    
    /**
     * @return The parameters in the current scope.
     * @throws PtalonScopeException If there is any problem getting
     * the type of a symbol.
     */
    public Set<String> getParameters() throws PtalonScopeException {
        Set<String> output = new HashSet<String>();
        for (IfTree tree : _currentTree.getAncestors()) {
            for (String symbol : tree.getSymbols()) {
                if (tree.getType(symbol).equals("parameter")) {
                    output.add(symbol);
                }
            }
        }
        return output;
    }

    /**
     * Get the unique name for the symbol in the PtalonActor. 
     * @param symbol The symbol to test.
     * @return The unique name.
     * @exception PtalonRuntimeException If no such symbol exists.
     */
    public String getMappedName(String symbol) throws PtalonRuntimeException {
        for (IfTree tree : _currentTree.getAncestors()) {
            try {
                String output = tree.getMappedName(symbol);
                return output;
            } catch (PtalonRuntimeException e) {
            }
        }
        throw new PtalonRuntimeException("Symbol " + symbol + " not found");
    }

    /**
     * Return the type associated with the given symbol in the current scope.
     * @param symbol The symbol under test.
     * @return The type associated with the given symbol.
     * @exception PtalonScopeException If the symbol is not in the current scope.
     */
    public String getType(String symbol) throws PtalonScopeException {
        List<IfTree> ancestors = _currentTree.getAncestors();
        for (IfTree tree : ancestors) {
            try {
                String type = tree.getType(symbol);
                return type;
            } catch (PtalonScopeException e) {
                //Do nothing here, just go on to check the next if-block
                //sub-scope
            }
        }
        throw new PtalonScopeException("Symbol " + symbol + " not found.");
    }

    /**
     * Return true if the given symbol exists in the current scope.
     * @param symbol The symbol to test.
     * @return true if the given symbol exists in the current scope.
     */
    public boolean inScope(String symbol) {
        List<IfTree> ancestors = _currentTree.getAncestors();
        for (IfTree tree : ancestors) {
            if (tree.getSymbols().contains(symbol)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return True if the actor has been set for this instance.
     */
    public boolean isActorSet() {
        return _actorSet;
    }

    /**
     * Return true if the boolean for the current conditional is ready to be
     * entered.  It is ready when all ports, parameters, and relations
     * in the containing scope have been created, when all parameters
     * in the containing scope have been assigned values, and when in
     * a branch of an if-block that is active.  
     * @return true if the current if-block scope is ready to be entered.
     * @exception PtalonRuntimeException If it is thrown trying to access a parameter.
     */
    public boolean isIfReady() throws PtalonRuntimeException {
        IfTree parent = _currentTree.getParent();
        if (parent == null) {
            return false; //Should never make it here.
        } else if (parent.getActiveBranch() == null) {
            return false;
        } else if (parent.getActiveBranch() != parent.getCurrentBranch()) {
            return false;
        }
        List<IfTree> ancestors = parent.getAncestors();
        for (IfTree tree : ancestors) {
            if (!tree.isFullyAssigned()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return true if the current peice of code is ready to be
     * entered.  This is used by port, parameter, and relation
     * declarations only.  It is ready when all ports, parameters, and relations
     * in the containing scope have been created, when all parameters
     * in the containing scope have been assigned values, and when in
     * a branch of an if-block that is active.  
     * @return true if the current if-block scope is ready to be entered.
     * @exception PtalonRuntimeException If it is thrown trying to access a parameter.
     */
    public boolean isReady() throws PtalonRuntimeException {
        IfTree parent = _currentTree.getParent();
        if (parent == null) {
            return true;
        }
        List<IfTree> ancestors = parent.getAncestors();
        for (IfTree tree : ancestors) {
            if (!tree.isFullyAssigned()) {
                return false;
            }
        }
        if (_currentTree.getActiveBranch() == null) {
            return false;
        }
        return (_currentTree.getActiveBranch() == _currentTree
                .getCurrentBranch());
    }

    /** 
     * Return true if an entity was created in PtalonActor for the given 
     * symbol.  This symbol is assumed to be in the current scope.
     * @param symbol The symbol to test.
     * @return true if an entity was created for this symbol.
     * @exception PtalonRuntimeException If the symbol is not in the current
     * scope.
     */
    public boolean isCreated(String symbol) throws PtalonRuntimeException {
        List<IfTree> ancestors = _currentTree.getAncestors();
        for (IfTree parent : ancestors) {
            if (parent.isCreated(symbol)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Map a name of a symbol from a Ptalon program to a name in the
     * PtalonActor which creates it.
     * @param symbol The name for the symbol in the Ptalon program.
     * @param uniqueName The unique name for the symbol in the PtalonActor.
     * @exception PtalonScopeException If the symbol does not exist.
     */
    public void mapName(String symbol, String uniqueName)
            throws PtalonRuntimeException {
        _currentTree.mapName(symbol, uniqueName);
    }

    /**
     * Tests to see if the given parameter has it's value set yet.
     * @param name The name of the parameter.
     * @return true if the parameter has been given a value, false otherwise.
     * @exception PtalonRuntimeException If there is no parameter with the given
     * name in the given scope.
     */
    public boolean paramHasValue(String name) throws PtalonRuntimeException {
        try {
            if (getType(name).equals("actorparameter")) {
                if (isCreated(name)) {
                    String uniqueName = getMappedName(name);
                    PtalonParameter param = (PtalonParameter) _actor
                            .getAttribute(uniqueName);
                    if (param.hasValue()) {
                        return true;
                    }
                }
                return false;
            } else {
                throw new PtalonRuntimeException(name + " not a parameter");
            }
        } catch (Exception e) {
            throw new PtalonRuntimeException(name + " not a parameter", e);
        }
    }

    /**
     * Pop out of the scope of the current if statement and into
     * its container block's scope.
     * @return The unique name of the if-statement block being exited.
     * @exception PtalonScopeException If the current scope is already
     * the outermost scope.
     */
    public String popIfStatement() throws PtalonScopeException {
        String name = _currentTree.getName();
        _currentTree = _currentTree.getParent();
        if (_currentTree == null) {
            throw new PtalonScopeException(
                    "Attempt to pop out of outermost scope");
        }
        return name;
    }

    /**
     * Push into the scope of a new if statement contained as
     * a sublock of the current if statement.
     */
    public void pushIfStatement() {
        String name = getNextIfSymbol();
        _currentTree = _currentTree.addChild(name);
    }

    /**
     * Push into the scope of a new if statement contained as
     * a sub-block of the current if statement.  This is
     * called when loading an existsing Ptalon actor from
     * a PtalonML description.
     * @param name The name of the if statement.
     */
    public void pushIfStatement(String name) {
        if (_firstPushWithString) {
            _root = new IfTree(null, name);
            _currentTree = _root;
            _firstPushWithString = false;
        } else {
            _currentTree = _currentTree.addChild(name);
        }
    }

    /**
     * Set the active branch for the current if statement.
     * @param branch The branch to set.
     */
    public void setActiveBranch(boolean branch) {
        _currentTree.setActiveBranch(branch);
    }

    /**
     * Set the PtalonActor in which this PtalonCompilerInfo is used.
     * @param name The desired name for the actor.  In case of a name
     * conflict, the actual name will have the desired name as a prefix.
     * @exception PtalonRuntimeException If an exception is thrown trying to
     * change the name.
     *
     * @exception PtalonRuntimeException
     */
    public void setActor(String name) throws PtalonRuntimeException {
        try {
            if (!_actor.getName().startsWith(name)) {
                String uniqueName = _actor.getContainer().uniqueName(name);
                _actor.setName(uniqueName);
                _root.mapName(name, uniqueName);
            }
            _root.setStatus(name, true);
        } catch (Exception e) {
            throw new PtalonRuntimeException(
                    "Exception thrown in trying to change the actor name", e);
        }
        _actorSet = true;
    }
    
    /**
     * Set the symbol in the PtalonCode which represents this
     * CodeManager's actor.
     * 
     * @param symbol The name of this actor in the Ptalon file.
     * @exception PtalonScopeException If the symbol has been added already,
     * or if there is some problem accessing its associated file.
     */
    public void setActorSymbol(String symbol) throws PtalonScopeException {
        _root.addSymbol(symbol, "import");
        _root.setStatus(symbol, true);
        try {
            _imports.put(symbol, _actor.ptalonCodeLocation.asFile());
            if (!_actor.getName().startsWith(symbol)) {
                    String uniqueName = _actor.getContainer().uniqueName(symbol);
                    _actor.setName(uniqueName);
            }
        } catch (Exception e) {
            throw new PtalonScopeException("Unable to access file for " + symbol, e);
        }
    }

    /**
     * Set the current branch that's being walked.
     * @param branch True if the true branch is being walked.
     */
    public void setCurrentBranch(boolean branch) {
        _currentTree.setCurrentBranch(branch);
    }

    /**
     * Prepare the compiler to start at the outermost scope 
     * of the Ptalon program during run time.
     *
     */
    public void startAtTop() {
        _currentTree = _root;
    }

    /**
     * Print out the scope information in this compiler.
     */
    public String toString() {
        StringWriter writer = new StringWriter();
        try {
            xmlSerialize(writer, 0);
        } catch (IOException e) {
        }
        return writer.getBuffer().toString();
    }

    /**
     * Write an xml version of this code manager to the given output.
     * @param output The writer to send the output to.
     * @param depth The depth of indents to start with.
     * @exception IOException If there is a problem writing to the output.
     */
    public void xmlSerialize(Writer output, int depth) throws IOException {
        for (String imp : _imports.keySet()) {
            String rawFilename = _imports.get(imp).getAbsolutePath();
            String ptiiDir = StringUtilities.getProperty("ptolemy.ptII.dir");
            String ptiiFilename = StringUtilities.substituteFilePrefix(ptiiDir,
                    rawFilename, "$PTII");
            output.write(_getIndentPrefix(depth) + "<actornameset value=\""
                    + Boolean.toString(_actorSet) + "\"/>\n");
            output.write(_getIndentPrefix(depth) + "<import name=\"" + imp
                    + "\" file=\"" + ptiiFilename + "\"/>\n");
        }
        _root.xmlSerialize(output, depth);
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////
    
    /** Return a number of spaces that is proportional to the argument.
     *  If the argument is negative or zero, return an empty string.
     *  @param level The level of indenting represented by the spaces.
     *  @return A string with zero or more spaces.
     */
    protected static String _getIndentPrefix(int level) {
        return StringUtilities.getIndentPrefix(level);
    }
    
    /**
     * Return the type associated with the given symbol in the current scope.
     * This is the same as getType, but it is used to avoid a name conflict
     * in NestedActorManager.PtalonExpressionScope
     * @param symbol The symbol under test.
     * @return The type associated with the given symbol.
     * @exception PtalonScopeException If the symbol is not in the current scope.
     */
    protected String getTypeForScope(String symbol) throws PtalonScopeException {
        return getType(symbol);
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected members                    ////

    /**
     * The actor in which this PtalonCompilerInfo is used.
     */
    protected PtalonActor _actor;
    
    /**
     * A list of the import symbols and their corresponding
     * files.
     */
    protected Hashtable<String, File> _imports;
    
    ///////////////////////////////////////////////////////////////////
    ////                       private methods                     ////
    
    /**
     * @return The next symbol of form "_ifN" where
     * N is 0 if this funciton has not been called and
     * N is n if this is the nth call to this function.
     */
    private String getNextIfSymbol() {
        String symbol = "_if";
        symbol += (new Integer(_counter)).toString();
        _counter++;
        return symbol;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private members                     ////
    

    /**
     * True if the actor has been set.
     */
    private boolean _actorSet;

    /**
     * A counter used to associate a unqiue
     * number with each if-block.
     */
    private int _counter;

    /**
     * Some descendent of the root tree to which new input symbols
     * should be added.
     */
    private IfTree _currentTree;

    /**
     * This is true until #pushIfSymbol(String) is called for the first time.
     */
    private boolean _firstPushWithString = true;

    /**
     * The root of the tree containing the symbol tables for each level
     * of the if-statement hierarchy.
     */
    private IfTree _root;
    
    /**
     * These two lists are used to store parameters which need to be set
     * by Ptalon; i.e. constant parameters.  The first list are the parameters,
     * and the second list are the expressions to assign to the parameters.
     */
    private List<PtalonParameter> _unassignedParameters = new 
            LinkedList<PtalonParameter>();
    private List<String> _unassignedParameterValues = new LinkedList<String>();
    


    ///////////////////////////////////////////////////////////////////
    ////                       private classes                     ////
    
    private class IfTree extends NamedTree<IfTree> {

        /**
         * Create a new if tree.
         * @param name The name to give this if tree.
         * @parem parent The paretn to this tree, which may be null
         * if this is the root of a tree.
         */
        public IfTree(IfTree parent, String name) {
            super(parent, name);
            _nameMappings = new Hashtable<String, String>();
            _setStatus = new Hashtable<String, Boolean>();
            _symbols = new Hashtable<String, String>();
        }
        
        /**
         * Create a new child tree to this tree with the specified
         * name and return it.
         *  Subclasses should override this if it's not the case.
         *  
         * @param name The name of the child.
         * @return The child IfTree.
         */
        public IfTree addChild(String name) {
            IfTree child = new IfTree(this, name);
            _children.add(child);
            return child;
        }

        /**
         * Add a symbol to the scope of this if statement.
         * @param symbol The sybmol to add.
         * @param type Its corresponding type.
         */
        public void addSymbol(String symbol, String type) {
            _symbols.put(symbol, type);
            _nameMappings.put(symbol, symbol);
            _setStatus.put(symbol, false);
        }

        /**
         * Add a symbol to the scope of this if statement.
         * @param symbol The sybmol to add.
         * @param type Its corresponding type.
         * @param status It's statust, that is whether it has been loaded or not.
         * @param uniqueName The unique name of this 
         */
        public void addSymbol(String symbol, String type, boolean status,
                String uniqueName) {
            _symbols.put(symbol, type);
            _nameMappings.put(symbol, uniqueName);
            _setStatus.put(symbol, status);
        }

        /**
         * Return the active branch, which may be null if it has not
         * yet been set.
         * @return
         */
        public Boolean getActiveBranch() {
            return _activeBranch;
        }

        /**
         * @return The ancestors of this tree, including this tree.
         */
        public List<IfTree> getAncestors() {
            List<IfTree> list = getProperAncestors();
            list.add(this);
            return list;
        }
        
        public boolean getCurrentBranch() {
            return _currentBranch;
        }

        /**
         * Get the unique name for the symbol in the PtalonActor. 
         * @param symbol The symbol to test.
         * @return The unique name.
         * @exception PtalonRuntimeException If no such symbol exists.
         */
        public String getMappedName(String symbol)
                throws PtalonRuntimeException {
            String output = _nameMappings.get(symbol);
            if (output == null) {
                throw new PtalonRuntimeException("Symbol " + symbol
                        + " not found.");
            }
            return output;
        }

        /**
         * Return true if an entity was created in PtalonActor for the given 
         * symbol.  This symbol is assumed to be in the current scope.
         * @param symbol The symbol to test.
         * @return true if an entity was created for this symbol.
         * @exception PtalonRuntimeException If the symbol is not in the current
         * scope.
         */
        public boolean isCreated(String symbol) throws PtalonRuntimeException {
            Boolean status = _setStatus.get(symbol);
            if (status == null) {
                return false;
            }
            return status;
        }

        /**
         * @return All symbols in the scope of the if-block.
         */
        public Set<String> getSymbols() {
            return _symbols.keySet();
        }

        /**
         * Return the type associated with the given symbol.
         * @param symbol The symbol under test.
         * @return The type associated with the given symbol.
         * @exception PtalonScopeException If the symbol is not in the scope
         * of the if statement associated with this IfTree.
         */
        public String getType(String symbol) throws PtalonScopeException {
            String value = _symbols.get(symbol);
            if (value == null) {
                String message = symbol.concat(" not found.");
                throw new PtalonScopeException(message);
            }
            return value;
        }

        /**
         * Return true if all the symbols in this if block
         * have been assigned a value.  A symbol has been 
         * assigned a value if a corresponding entity for the
         * symbol has been created in the PtalonActor, and in the
         * case of parameters, that the user has provided a value
         * for the parameter.
         * 
         * @return True if all the symbols in this if block
         * have been assigned a value. 
         * @exception PtalonRuntimeException If there is any problem accessing
         * a parameter.
         */
        public boolean isFullyAssigned() throws PtalonRuntimeException {
            for (String symbol : _setStatus.keySet()) {
                if (!_setStatus.get(symbol)) {
                    return false;
                }
                if (_symbols.get(symbol).endsWith("parameter")) {
                    try {
                        PtalonParameter param = (PtalonParameter) _actor
                                .getAttribute(_nameMappings.get(symbol));
                        if (!param.hasValue()) {
                            return false;
                        }
                    } catch (Exception e) {
                        throw new PtalonRuntimeException(
                                "Could not access parameter " + symbol, e);
                    }
                }
            }
            return true;
        }

        /**
         * Map a name of a symbol from a Ptalon program to a name in the
         * PtalonActor which creates it.
         * @param symbol The name for the symbol in the Ptalon program.
         * @param uniqueName The unique name for the symbol in the PtalonActor.
         * @exception PtalonRuntimeException If the symbol does not exist.
         */
        public void mapName(String symbol, String uniqueName)
                throws PtalonRuntimeException {
            String value = _symbols.get(symbol);
            if (value == null) {
                String message = symbol.concat(" not found.");
                throw new PtalonRuntimeException(message);
            }
            _nameMappings.put(symbol, uniqueName);
        }

        /**
         * Set the active branch to true or false.
         * @param branch The branch to set it to.
         */
        public void setActiveBranch(boolean branch) {
            _activeBranch = new Boolean(branch);
        }

        /**
         * Set the current branch that's being walked.
         * @param branch True if the true branch is being walked.
         */
        public void setCurrentBranch(boolean branch) {
            _currentBranch = branch;
        }

        /**
         * Set the status of the symbol to true, if the symbol
         * is ready, and false otherwise.
         * @param symbol The symbol.
         * @param status The status.
         */
        public void setStatus(String symbol, boolean status) {
            _setStatus.put(symbol, status);
        }

        /**
         * Enumerate the info from this scope.
         */
        public String toString() {
            String output = "Scope: " + getName() + ":\n\n";
            for (String s : getSymbols()) {
                try {
                    output += s + "\t" + getType(s) + "\n";
                } catch (PtalonScopeException e) {

                }
            }
            output += "---------------\n";
            for (IfTree child : getChildren()) {
                output += child.toString();
            }
            return output;
        }

        /**
         * Write an xml version of this if tree to the given output.
         * @param output The writer to send the output to.
         * @param depth The depth of indents to start with.
         * @exception IOException If there is a problem writing to the output.
         */
        public void xmlSerialize(Writer output, int depth) throws IOException {
            String activeBranch;
            if (_activeBranch == null) {
                activeBranch = "unknown";
            } else {
                activeBranch = _activeBranch.toString();
            }
            output.write(_getIndentPrefix(depth) + "<if name=\"" + _name
                    + "\" activeBranch=\"" + activeBranch + "\">\n");
            for (String symbol : _symbols.keySet()) {
                output.write(_getIndentPrefix(depth + 1) + "<symbol name =\""
                        + symbol + "\" type=\"" + _symbols.get(symbol)
                        + "\" status=\"" + _setStatus.get(symbol)
                        + "\" uniqueName=\"" + _nameMappings.get(symbol)
                        + "\"/>\n");
            }
            for (IfTree child : _children) {
                child.xmlSerialize(output, depth + 1);
            }
            output.write(_getIndentPrefix(depth) + "</if>\n");
        }

        /**
         * This is true when the active branch for this if statement
         * is true, false when it is false, and null when it is unknown.
         */
        private Boolean _activeBranch = null;

        /**
         * This is true if we are in the main scope
         * or the true part of a true branch.
         */
        private boolean _currentBranch = true;

        /**
         * Each symbol gets mapped to its unique name in the
         * Ptalon Actor.
         */
        private Hashtable<String, String> _nameMappings;

        /**
         * A symbol maps to true if it has been set to some
         * value or false otherwise.
         */
        private Hashtable<String, Boolean> _setStatus;

        /**
         * The symbol table for this level of the if hierarchy.
         */
        private Hashtable<String, String> _symbols;

    }

}
