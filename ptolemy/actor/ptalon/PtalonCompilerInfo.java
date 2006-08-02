package ptolemy.actor.ptalon;

import java.io.File;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
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
public class PtalonCompilerInfo {

    public PtalonCompilerInfo() {
        _counter = 0;
        _root = new IfTree(getNextIfSymbol(), null);
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
            _currentTree.addParameter(name, parameter);
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
            _currentTree.addPort(name, port);
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
            _currentTree.addParameter(name, parameter);
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
            _currentTree.addPort(name, port);
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
            _currentTree.addParameter(name, parameter);
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
            _currentTree.addPort(name, port);
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
            _currentTree.addRelation(name, relation);
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
     * Return true if the current if-block scope is ready to be
     * entered.  It is ready when all ports, parameters, and relations
     * in the containing scope have been created, and when all parameters
     * in the containing scope have been assigned values.  
     * @return true if the current if-block scope is ready to be entered.
     */
    public boolean isReady() {
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
        return true;
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
     * Set the PtalonActor in which this PtalonCompilerInfo is used.
     * @param actor The PtalonActor.
     * @param name The desired name for the actor.  In case of a name
     * conflict, the actual name will have the desired name as a prefix.
     * @throws PtalonRuntimeException If an exception is thrown trying to
     * change the name.
     */
    public void setActor(PtalonActor actor, String name) throws PtalonRuntimeException {
        _actor = actor;
        try {
            String uniqueName = actor.getContainer().uniqueName(name);
            actor.setName(uniqueName);
        } catch (Exception e) {
            throw new PtalonRuntimeException(
                    "Exception thrown in trying to change the actor name", e);
        }
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
        String output = "---------Compiler info---------\n";
        output += _root.toString();
        return output;
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
            _parameters = new Hashtable<String, Parameter>();
            _parent = parent;
            _ports = new Hashtable<String, TypedIOPort>();
            _relations = new Hashtable<String, TypedIORelation>();
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
         * Associate a parameter with a symbol.
         * @param symbol The symbol name.
         * @param parameter The associated parameter.
         * @throws PtalonRuntimeException If the symbol does not exist, or if
         * the symbol already has a parameter associated with it.
         * 
         */
        public void addParameter(String symbol, Parameter parameter) throws PtalonRuntimeException {
            if (!getSymbols().contains(symbol)) {
                throw new PtalonRuntimeException("Symbol " + symbol + " does not exist.");
            }
            if (_parameters.keySet().contains(symbol)) {
                throw new PtalonRuntimeException("Symbol " + symbol + " already has a parameter associated with it.");
            }
            _parameters.put(symbol, parameter);
            _setStatus.put(symbol, true);
        }
        
        /**
         * Associate a port with a symbol.
         * @param symbol The symbol name.
         * @param port The associated port.
         * @throws PtalonRuntimeException If the symbol does not exist, or if
         * the symbol already has a port associated with it.
         * 
         */
        public void addPort(String symbol, TypedIOPort port) throws PtalonRuntimeException {
            if (!getSymbols().contains(symbol)) {
                throw new PtalonRuntimeException("Symbol " + symbol + " does not exist.");
            }
            if (_ports.keySet().contains(symbol)) {
                throw new PtalonRuntimeException("Symbol " + symbol + " already has a port associated with it.");
            }
            _ports.put(symbol, port);
            _setStatus.put(symbol, true);
        }
        
        /**
         * Associate a relation with a symbol.
         * @param symbol The symbol name.
         * @param relation The associated relation.
         * @throws PtalonRuntimeException If the symbol does not exist, or if
         * the symbol already has a relation associated with it.
         * 
         */
        public void addRelation(String symbol, TypedIORelation relation) throws PtalonRuntimeException {
            if (!getSymbols().contains(symbol)) {
                throw new PtalonRuntimeException("Symbol " + symbol + " does not exist.");
            }
            if (_relations.keySet().contains(symbol)) {
                throw new PtalonRuntimeException("Symbol " + symbol + " already has a relation associated with it.");
            }
            _relations.put(symbol, relation);
            _setStatus.put(symbol, true);
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
         */
        public boolean isFullyAssigned() {
            for (String symbol : _setStatus.keySet()) {
                if (!_setStatus.get(symbol)) {
                    return false;
                }
                LinkedList<String> paramTypes = new LinkedList<String>();
                paramTypes.add("parameter");
                paramTypes.add("intparameter");
                paramTypes.add("boolparameter");
                if (paramTypes.contains(_symbols.get(symbol))) {
                    if (!_parameters.keySet().contains(symbol)) {
                        return false;
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
         * The children, which correspond to sub if-blocks
         * of this if-block.
         */
        private LinkedList<IfTree> _children;
        
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
         * The parameters for this level of the hierarchy.
         */
        private Hashtable<String, Parameter> _parameters;
        
        /**
         * The parent of this if block.
         */
        private IfTree _parent;
        
        /**
         * The ports for this level of the hierarchy.
         */
        private Hashtable<String, TypedIOPort> _ports;
        
        /**
         * The relations for this level of the hierarchy.
         */
        private Hashtable<String, TypedIORelation> _relations;

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
