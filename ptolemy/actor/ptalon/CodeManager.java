package ptolemy.actor.ptalon;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import com.sun.corba.se.impl.logging.IORSystemException;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.util.StringUtilities;

/**
 * A helper class to store information, like variable
 * scope info, about the compiler.
 * @author acataldo
 *
 */
public class CodeManager {

    /**
     * Create a new CodeManager in the specified actor.
     * @param actor The actor to manage the code for.
     */
    public CodeManager(PtalonActor actor) {
        _actor = actor;
        _actorSet = false;
        _counter = 0;
        _root = new IfTree(getNextIfSymbol(), null);
        _root.setActiveBranch(true);
        _imports = new Hashtable<String, File>();
        _currentTree = _root;
    }
    
    /**
     * Add a PtalonBoolParameter to the PtalonActor
     * with the specified name.
     * @param name The name of the boolean parameter.
     * @throws PtalonRuntimeException If the symbol does not exist, or if
     * the symbol already has a parameter associated with it, or if an IllegalActionException is thrown
     * trying to create the parameter.
     */
    public void addBoolParameter(String name) throws PtalonRuntimeException {
        String uniqueName = _actor.uniqueName(name);
        try {
            PtalonBoolParameter parameter = new PtalonBoolParameter(_actor, uniqueName);
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
     * @throws PtalonRuntimeException If the symbol does not exist, or if
     * the symbol already has a port associated with it, or if an IllegalActionException is thrown
     * trying to create the port.
     */
    public void addInPort(String name) throws PtalonRuntimeException {
        String uniqueName = _actor.uniqueName(name);
        try {
            TypedIOPort port = new TypedIOPort(_actor, uniqueName);
            port.setInput(true);
            port.setOutput(false);
            _currentTree.setStatus(name, true);
            _currentTree.mapName(name, uniqueName);
        } catch (NameDuplicationException e) {
            throw new PtalonRuntimeException("NameDuplicationException", e);
        } catch (IllegalActionException e) {
            throw new PtalonRuntimeException("IllegalActionException", e);
        }
    }
    
    /**
     * Add a PtalonIntParameter to the PtalonActor
     * with the specified name.
     * @param name The name of the integer parameter.
     * @throws PtalonRuntimeException If the symbol does not exist, or if
     * the symbol already has a parameter associated with it, or if an IllegalActionException is thrown
     * trying to create the parameter.
     */
    public void addIntParameter(String name) throws PtalonRuntimeException {
        String uniqueName = _actor.uniqueName(name);
        try {
            PtalonIntParameter parameter = new PtalonIntParameter(_actor, uniqueName);
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
     * The corresponding symbol in the Ptalon code should be baz.
     * 
     * @param name The qualified identifier in the import statement.
     * @throws PtalonScopeException If there was any trouble locating
     * the file.
     */
    public void addImport(String name) throws PtalonScopeException {
        try {
            StringTokenizer tokens = new StringTokenizer(name, ".");
            File filename = new File(StringUtilities.getProperty("ptolemy.ptII.dir"));
            String symbol = "";
            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken();
                if (!tokens.hasMoreTokens()) {
                    symbol = new String (token);
                    token += ".ptln";
                }
                filename = new File(filename, token);
            }
            if (!filename.exists()) {
                throw new PtalonScopeException("File " + filename + " does not exist");
            }
            _imports.put(symbol, filename);
            addSymbol(symbol, "import");
            _currentTree.setStatus(symbol, true);
        } catch(Exception e) {
            throw new PtalonScopeException("Unable to import " + name, e);
        }
    }
    
    /**
     * Add a TypedIOPort to the PtalonActor
     * with the specified name, and output flow type
     * @param name The name of the port.
     * @throws PtalonRuntimeException If the symbol does not exist, or if
     * the symbol already has a port associated with it, or if an IllegalActionException is thrown
     * trying to create the port.
     */
    public void addOutPort(String name) throws PtalonRuntimeException {
        String uniqueName = _actor.uniqueName(name);
        try {
            TypedIOPort port = new TypedIOPort(_actor, uniqueName);
            port.setInput(false);
            port.setOutput(true);
            _currentTree.setStatus(name, true);
            _currentTree.mapName(name, uniqueName);
        } catch (NameDuplicationException e) {
            throw new PtalonRuntimeException("NameDuplicationException", e);
        } catch (IllegalActionException e) {
            throw new PtalonRuntimeException("IllegalActionException", e);
        }
    }
    
    /**
     * Add a PtalonParameter to the PtalonActor
     * with the specified name.
     * @param name The name of the parameter.
     * @throws PtalonRuntimeException If the symbol does not exist, or if
     * the symbol already has a parameter associated with it, or if an IllegalActionException is thrown
     * trying to create the parameter.
     */
    public void addParameter(String name) throws PtalonRuntimeException {
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
     * Add a TypedIOPort to the PtalonActor
     * with the specified name.
     * @param name The name of the port.
     * @throws PtalonRuntimeException If the symbol does not exist, or if
     * the symbol already has a port associated with it, or if an IllegalActionException is thrown
     * trying to create the port.
     */
    public void addPort(String name) throws PtalonRuntimeException {
        String uniqueName = _actor.uniqueName(name);
        try {
            TypedIOPort port = new TypedIOPort(_actor, uniqueName);
            port.setInput(true);
            port.setOutput(true);
            _currentTree.setStatus(name, true);
            _currentTree.mapName(name, uniqueName);
        } catch (NameDuplicationException e) {
            throw new PtalonRuntimeException("NameDuplicationException", e);
        } catch (IllegalActionException e) {
            throw new PtalonRuntimeException("IllegalActionException", e);
        }
    }
    
    /**
     * Add a TypedIORelation to the PtalonActor
     * with the specified name.
     * @param name The name of the relation.
     * @throws PtalonRuntimeException If the symbol does not exist, or if
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
     * @throws PtalonScopeException If a symbol with this name has already
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
    public void addSymbol(String symbol, String type, boolean status, String uniqueName) {
        _currentTree.addSymbol(symbol, type, status, uniqueName);
    }
    
    /**
     * Enter the named subscope.
     * @param scope The named subscope.
     * @throws PtalonRuntimeException If the subscope does not exist.
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
            throw new PtalonRuntimeException("Subscope " + scope + " does not exist");
        }
    }
    
    /**
     * Exit the current if scope.
     * @throws PtalonRuntimeException If already at the top-level if scope.
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
     * @throws PtalonRuntimeException If the paramter does not exist or have
     * a boolean value. 
     */
    public boolean getBooleanValueOf(String param) throws PtalonRuntimeException {
        try {
            String uniqueName = _currentTree.getMappedName(param);
            PtalonBoolParameter att = (PtalonBoolParameter) _actor.getAttribute(uniqueName);
            return ((BooleanToken) att.getToken()).booleanValue();
        } catch (Exception e) {
            throw new PtalonRuntimeException("Unable to access boolean value for " 
                    + param, e);
        }
    }

    /**
     * Get the int value associated with the specified parameter.
     * @param param The parameter's name in the Ptalon code.
     * @return It's unique value.
     * @throws PtalonRuntimeException If the paramter does not exist or have
     * a boolean value. 
     */
    public int getIntValueOf(String param) throws PtalonRuntimeException {
        try {
            String uniqueName = _currentTree.getMappedName(param);
            PtalonIntParameter att = (PtalonIntParameter) _actor.getAttribute(uniqueName);
            return ((IntToken) att.getToken()).intValue();
        } catch (Exception e) {
            throw new PtalonRuntimeException("Unable to access boolean value for " 
                    + param, e);
        }
    }

    
    /**
     * Get the unique name for the symbol in the PtalonActor. 
     * @param symbol The symbol to test.
     * @return The unique name.
     * @throws PtalonRuntimeException If no such symbol exists.
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
     * @throws PtalonScopeException If the symbol is not in the current scope.
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
    public boolean inScope(String symbol){
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
     * Return true if the current peice of code is ready to be
     * entered.  It is ready when all ports, parameters, and relations
     * in the containing scope have been created, when all parameters
     * in the containing scope have been assigned values, and when in
     * a branch of an if-block that is active.  
     * @return true if the current if-block scope is ready to be entered.
     * @throws PtalonRuntimeException If it is thrown trying to access a parameter.
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
        return (_currentTree.getActiveBranch() == _currentTree.getCurrentBranch());
    }
    
   /** 
    * Return true if an entity was created in PtalonActor for the given 
    * symbol.  This symbol is assumed to be in the current scope.
    * @param symbol The symbol to test.
    * @return true if an entity was created for this symbol.
    * @throws PtalonRuntimeException If the symbol is not in the current
    * scope.
    */
   public boolean isCreated(String symbol) throws PtalonRuntimeException {
       return _currentTree.isCreated(symbol);
   }
    
    /**
     * Map a name of a symbol from a Ptalon program to a name in the
     * PtalonActor which creates it.
     * @param symbol The name for the symbol in the Ptalon program.
     * @param uniqueName The unique name for the symbol in the PtalonActor.
     * @throws PtalonScopeException If the symbol does not exist.
     */
    public void mapName(String symbol, String uniqueName) throws PtalonRuntimeException {
        _currentTree.mapName(symbol, uniqueName);
    }
    
    /**
     * Pop out of the scope of the current if statement and into
     * its container block's scope.
     * @return The unique name of the if-statement block being exited.
     * @throws PtalonScopeException If the current scope is already
     * the outermost scope.
     */
    public String popIfStatement() throws PtalonScopeException {
        String name = _currentTree.getName();
        _currentTree = _currentTree.getParent();
        if (_currentTree == null) {
            throw new PtalonScopeException("Attempt to pop out of outermost scope");
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
     * a sublock of the current if statement.
     * @name The name of the if statement.
     */
    public void pushIfStatement(String name) {
        if (_firstPushWithString) {
            _root = new IfTree(name, null);
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
     * @throws PtalonRuntimeException If an exception is thrown trying to
     * change the name.
     *
     * @param name
     * @throws PtalonRuntimeException
     */
    public void setActor(String name) throws PtalonRuntimeException {
        try {
            String uniqueName = _actor.getContainer().uniqueName(name);
            _actor.setName(uniqueName);
            _root.mapName(name, uniqueName);
            _root.setStatus(name, true);
        } catch (Exception e) {
            throw new PtalonRuntimeException(
                    "Exception thrown in trying to change the actor name", e);
        }
        _actorSet = true;
    }
    
    /**
     * Set the current branch that's being walked.
     * @param branch True if the true branch is being walked.
     */
    public void setCurrentBranch(boolean branch) {
        _currentTree.setActiveBranch(branch);
    }
    
    /**
     * Prepare the compiler to start at the top of the Ptalon program
     * during run time.
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
     * Write an xml version of this actor to the given output.
     * @param output The writer to send the output to.
     * @param depth The depth of indents to start with.
     * @throws IOException If there is a problem writing to the output.
     */
    public void xmlSerialize(Writer output, int depth) throws IOException {
        output.write(_getIndentPrefix(depth) + "<codemanager>\n");
        _root.xmlSerialize(output, depth + 1);
        output.write(_getIndentPrefix(depth) + "</codemanager>\n");
    }

    /** Return a number of spaces that is proportional to the argument.
     *  If the argument is negative or zero, return an empty string.
     *  @param level The level of indenting represented by the spaces.
     *  @return A string with zero or more spaces.
     */
    protected static String _getIndentPrefix(int level) {
        return StringUtilities.getIndentPrefix(level);
    }
    
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
    
    /**
     * The actor in which this PtalonCompilerInfo is used.
     */
    private PtalonActor _actor;
    
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
     * A list of the import symbols and their corresponding
     * files.
     */
    private Hashtable<String, File> _imports;
    
    /**
     * The root of the tree containing the symbol tables for each level
     * of the if-statement hierarchy.
     */
    private IfTree _root;

    private class IfTree {
        
        /**
         * Create a new if tree.
         * @param name The name to give this if tree.
         * @parem parent The paretn to this tree, which may be null
         * if this is the root of a tree.
         */
        public IfTree(String name, IfTree parent) {
            _children = new LinkedList<IfTree>();
            _name = name;
            _nameMappings = new Hashtable<String, String>();
            _parent = parent;
            _setStatus = new Hashtable<String, Boolean>();
            _symbols = new Hashtable<String, String>();
        }
         
        /**
         * Create a new child tree to this tree with the specified
         * name and return it.
         * @param name The name of the child.
         * @return The child IfTree.
         */
        public IfTree addChild(String name) {
            IfTree child = new IfTree(name, this);
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
        public void addSymbol(String symbol, String type, boolean status, String uniqueName) {
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
            LinkedList<IfTree> list = new LinkedList<IfTree>();
            list.add(this);
            IfTree next = _parent;
            while (next != null) {
                list.addFirst(next);
                next = next.getParent();
            }
            return list;
        }
                
        /**
         * @return The children of this tree.
         */
        public List<IfTree> getChildren() {
            return _children;
        }
        
        public boolean getCurrentBranch() {
            return _currentBranch;
        }
        
        /**
         * Get the unique name for the symbol in the PtalonActor. 
         * @param symbol The symbol to test.
         * @return The unique name.
         * @throws PtalonRuntimeException If no such symbol exists.
         */
        public String getMappedName(String symbol) throws PtalonRuntimeException {
            String output = _nameMappings.get(symbol);
            if (output == null) {
                throw new PtalonRuntimeException("Symbol " + symbol + " not found.");
            }
            return output;
        }
        
        /**
         * @return The name associated with this tree.
         */
        public String getName() {
            return _name;
        }
        
        /**
         * @return The parent of this tree.
         */
        public IfTree getParent() {
            return _parent;
        }
        
        /**
         * Return true if an entity was created in PtalonActor for the given 
         * symbol.  This symbol is assumed to be in the current scope.
         * @param symbol The symbol to test.
         * @return true if an entity was created for this symbol.
         * @throws PtalonRuntimeException If the symbol is not in the current
         * scope.
         */
        public boolean isCreated(String symbol) throws PtalonRuntimeException {
            Boolean status = _setStatus.get(symbol);
            if (status == null) {
                throw new PtalonRuntimeException("Symbol " + symbol + " not found.");
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
         * @throws PtalonScopeException If the symbol is not in the scope
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
         * @throws PtalonRuntimeException If there is any problem accessing
         * a parameter.
         */
        public boolean isFullyAssigned() throws PtalonRuntimeException {
            for (String symbol : _setStatus.keySet()) {
                if (!_setStatus.get(symbol)) {
                    return false;
                }
                if (_symbols.get(symbol).endsWith("parameter")) {
                    try {
                        PtalonParameter param = (PtalonParameter) _actor.getAttribute(_nameMappings.get(symbol));
                        if (!param.hasValue()) {
                            return false;
                        }
                    } catch (Exception e) {
                        throw new PtalonRuntimeException("Could not access parameter " + symbol, e);
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
         * @throws PtalonRuntimeException If the symbol does not exist.
         */
        public void mapName(String symbol, String uniqueName) throws PtalonRuntimeException {
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
         * Write an xml version of this actor to the given output.
         * @param output The writer to send the output to.
         * @param depth The depth of indents to start with.
         * @throws IOException If there is a problem writing to the output.
         */
        public void xmlSerialize(Writer output, int depth) throws IOException {
            String activeBranch;
            if (_activeBranch == null) {
                activeBranch = "unknown";
            } else {
                activeBranch = _activeBranch.toString();
            }
            output.write(_getIndentPrefix(depth) + "<if name=\"" + _name +
                    "\" activeBranch=\"" + activeBranch + "\">\n");
            for (String symbol : _symbols.keySet()) {
                output.write(_getIndentPrefix(depth + 1) + "<symbol name =\""
                        + symbol + "\" type=\"" + _symbols.get(symbol) + 
                        "\" status=\"" + _setStatus.get(symbol) + 
                        "\" uniqueName=\"" + _nameMappings.get(symbol) + "\"/>\n");
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
         * The children, which correspond to sub if-blocks
         * of this if-block.
         */
        private LinkedList<IfTree> _children;
        
        /**
         * This is true if we are in the main scope
         * or the true part of a true branch.
         */
        private boolean _currentBranch = true;
        
        /**
         * The name of this if tree.
         */
        private String _name;
        
        /**
         * Each symbol gets mapped to its unique name in the
         * Ptalon Actor.
         */
        private Hashtable<String, String> _nameMappings;
        
        /**
         * The parent of this if block.
         */
        private IfTree _parent;

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
