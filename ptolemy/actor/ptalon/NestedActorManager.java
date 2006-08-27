package ptolemy.actor.ptalon;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.StringUtilities;

/**
 * This is just a code manager that manages the extra
 * complexity of dealing with nested actors.  It became
 * clear that several methods would need to be added to
 * CodeManager to make properly deal with nested actors,
 * so this class is seperated simply to make the code
 * a bit more digestable.
 * 
 * @author acataldo
 *
 */
public class NestedActorManager extends CodeManager {

    /**
     * Create a new NestedActorManager.
     * @param actor The ptalon actor for this manager.
     */
    public NestedActorManager(PtalonActor actor) {
        super(actor);
        _trees = new LinkedList<ActorTree>();
        _instanceNumbers = new Hashtable<String, Integer>();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /**
     * Add an actor to the PtalonActor.  In the case of an actor
     * specifed by an import statement, the actor will be a
     * PtalonActor.  In the case of an actor specified by a 
     * parameter, the actor will be arbitrary.
     * @param name The unique name of the actor declaration.
     * @throws PtalonRuntimeException If there is any trouble
     * loading the actor.
     */
    public void addActor(String name) throws PtalonRuntimeException {
        try {
            if (_currentTree == null) {
                throw new PtalonRuntimeException("Not in an actor declaration.");
            }
            String symbol = _currentTree.getSymbol();
            ActorTree parentTree = _currentTree.getParent();
            String uniqueName = _actor.uniqueName(symbol);
            if (getType(symbol).equals("import")) {
                PtalonActor actor = new PtalonActor(_actor, uniqueName);
                FileParameter location = actor.ptalonCodeLocation;
                File file = _imports.get(symbol);
                location.setToken(new StringToken(file.toString()));
                actor.setNestedDepth(_actor.getNestedDepth() + 1);
                _currentTree.assignParameters(actor);
            } else if (getType(symbol).equals("parameter")) {
                PtalonParameter parameter = (PtalonParameter) _actor
                        .getAttribute(getMappedName(symbol));
                if (!parameter.hasValue()) {
                    throw new PtalonRuntimeException("Parameter" + symbol + "has no value");
                }
                String expression = parameter.getExpression();
                if (expression.startsWith("ptalonActor:")) {
                    File file = new File(_parameterToImport(expression));
                    PtalonActor ptalonActor = new PtalonActor(_actor,
                            uniqueName);
                    ptalonActor.ptalonCodeLocation.setToken(new StringToken(
                            file.toString()));
                    ptalonActor.setNestedDepth(_actor.getNestedDepth() + 1);
                    _currentTree.assignParameters(ptalonActor);
                } else if (expression.startsWith("createWith:")) {
                    PtalonActor container = _actor;
                    int depth = _actor.getNestedDepth();
                    while (depth > 0) {
                        container = (PtalonActor) container.getContainer();
                        depth--;
                    }
                    ComponentEntity newInstance = container.createNestedActor(_actor,
                            expression.substring(11));
                    if (newInstance instanceof PtalonActor) {
                        _currentTree.assignParameters((PtalonActor) newInstance);
                    }
                } else {
                    Class<?> genericClass = Class.forName(expression);
                    Class<? extends ComponentEntity> entityClass = genericClass
                            .asSubclass(ComponentEntity.class);
                    Constructor<? extends ComponentEntity> entityConstructor = entityClass
                            .getConstructor(CompositeEntity.class, String.class);
                    ComponentEntity entity = entityConstructor.newInstance(
                            _actor, uniqueName);
                }
            } else { // type of name not "import" or "parameter".
                throw new PtalonRuntimeException("Invalid type for " + name);
            }
        } catch (Exception e) {
            throw new PtalonRuntimeException("Unable to add actor " + name, e);
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
     * @throws PtalonScopeException If there was any trouble locating
     * the file.
     */
    public String addImport(String name) throws PtalonScopeException {
        String symbol = super.addImport(name);
        _instanceNumbers.put(symbol, -1);
        return symbol;
    }
    
    /**
     * Add a symbol for an import statement, with the
     * specified filename.  The filename must begin
     * with $PTII.
     *
     * @param name The qualified identifier in the import statement.
     * @param filename The filename for the import.
     * @throws PtalonScopeException If there was any trouble locating
     * the file.
     */
    public void addImport(String name, String filename)
            throws PtalonScopeException {
        super.addImport(name, filename);
        _instanceNumbers.put(name, -1);
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
        super.addSymbol(name, type);
        if (type.equals("parameter")) {
            _instanceNumbers.put(name, -1);
        }
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
        super.addSymbol(symbol, type, status, uniqueName);
        if (type.equals("parameter")) {
            _instanceNumbers.put(symbol, -1);
        }
    }
    
    /**
     * Create a nested actor with respect to this code manager's
     * actor.
     * @param container The actor that will contain the created actor, which
     * should be a decendant of this code manager's actor.
     * @param uniqueName The unqique name for the nested actor declaration
     * this actor refers to.
     * @return The created actor.
     * @throws PtalonRuntimeException If there is any trouble creating this actor.
     */
    public ComponentEntity createNestedActor(PtalonActor container, String uniqueName) throws PtalonRuntimeException {
        ActorTree decendant = null;
        for (ActorTree tree : _trees) { 
            if (tree.getActorTree(uniqueName) != null) {
                decendant = tree.getActorTree(uniqueName);
                break;
            }
        }
        if (decendant == null) {
            throw new PtalonRuntimeException("No object with name " + uniqueName);
        }
        try {
            return decendant.createNestedActor(container);
        } catch (Exception e) {
            throw new PtalonRuntimeException("Unable to create " + uniqueName, e);
        }
    }

    /**
     * Enter the named actor declaration.
     * @param scope The name of the actor declaration.
     * @throws PtalonRuntimeException If such an actor declaration does not exist.
     */
    public void enterActorDeclaration(String name) throws PtalonRuntimeException {
        boolean exists = false;
        if (_currentTree == null) {
            for (ActorTree tree : _trees) {
                if (tree.getName().equals(name)) {
                    exists = true;
                    _currentTree = tree;
                    break;
                }                
            }
        } else {
            for (ActorTree tree : _currentTree.getChildren()) {
                if (tree.getName().equals(name)) {
                    exists = true;
                    _currentTree = tree;
                    break;
                }
            }
        }
        if (!exists) {
            throw new PtalonRuntimeException("Subscope " + name
                    + " does not exist");
        }
    }
    
    /**
     * Exit the current actor declaration.
     * @throws PtalonRuntimeException If already at the top-level if scope.
     */
    public void exitActorDeclaration() throws PtalonRuntimeException {
        if (_currentTree == null) {
            throw new PtalonRuntimeException("Already at top level");
        }
        _currentTree = _currentTree.getParent();
    }
    
    /**
     * @return true if the current actor declaration is ready to be created.
     * @throws PtalonRuntimeException If it is thrown trying to access a parameter,
     * or if there is no actor declaration to create..
     */
    public boolean isActorReady() throws PtalonRuntimeException {
        if (_currentTree == null) {
            throw new PtalonRuntimeException("No actor to create.");
        }
        if (_currentTree.created) {
            return false;
        }
        String symbol = _currentTree.getSymbol();
        try {
            if (getType(symbol).equals("import")) {
                return isReady();
            } else if (getType(symbol).equals("parameter")) {
                PtalonParameter param = _actor.getPtalonParameter(symbol);
                if (param.hasValue()) {
                    return isReady();
                } else {
                    return false;
                }
            } else {
                throw new PtalonRuntimeException("Symbol " + symbol + " does not have valid type.");
            }
        } catch (Exception e) {
            throw new PtalonRuntimeException("Problem with symbol " + symbol, e);
        }
        
    }
    
    /**
     * Pop into Push an actor name onto the current tree, or create a new
     * tree if entering a new nested actor declaration.
     * 
     * @return The unique name of the actor declaration being popped from.
     * @throws PtalonScopeException If not inside an actor declaration.
     */
    public String popActorDeclaration() throws PtalonScopeException {
        if (_currentTree == null) {
            throw new PtalonScopeException("Can't pop; not inside nested actor declaration.");
        }
        String output = _currentTree.getName();
        _currentTree = _currentTree.getParent();
        return output;
    }

    /**
     * Push an actor name onto the current tree, or create a new
     * tree if entering a new nested actor declaration.
     * 
     * @param actorName The name of the actor.
     * @throws PtalonScopeException If actorName is not a valid
     * parameter or import in the current scope.
     */
    public void pushActorDeclaration(String actorName) throws PtalonScopeException {
        String uniqueName = _uniqueSymbol(actorName);
        if (_currentTree == null) {
            _currentTree = new ActorTree(null, uniqueName);
            _trees.add(_currentTree);
        } else {
            _currentTree = _currentTree.addChild(uniqueName);
        }
        _currentTree.setSymbol(actorName);
    }

    /**
     * Push an actor name onto the current tree, or create a new
     * tree if entering a new nested actor declaration.  This is
     * called when loading an existsing Ptalon actor from
     * a PtalonML description.  After this is called 
     * setCurrentSymbol should also get called.
     * 
     * @param actorName The unique name of the actor.
     * @throws PtalonScopeException If actorName is not a valid
     * parameter or import in the current scope.
     */
    public void pushUniqueActorDeclaration(String actorName) throws PtalonScopeException {
        if (_currentTree == null) {
            _currentTree = new ActorTree(null, actorName);
            _trees.add(_currentTree);
        } else {
            _currentTree = _currentTree.addChild(actorName);
        }
    }
    
    /**
     * Sets the current actor's symbol, which should be a symbol
     * name in the Ptalon code for a parameter or import.
     * @param name The symbol.
     * @throws PtalonRuntimeException If not in the scope of an actor declaration.
     */
    public void setCurrentSymbol(String name) throws PtalonRuntimeException {
        if (_currentTree != null) {
            _currentTree.setSymbol(name);
        } else {
            throw new PtalonRuntimeException("Not in an actor declaration.");
        }
    }
    
    /**
     * Set the symbol in the PtalonCode which represents this
     * CodeManager's actor.
     * 
     * @param symbol The name of this actor in the Ptalon file.
     * @throws PtalonScopeException If the symbol has been added already,
     * or if there is some problem accessing its associated file.
     */
    public void setActorSymbol(String symbol) throws PtalonScopeException {
        super.setActorSymbol(symbol);
        _instanceNumbers.put(symbol, -1);
    }
    
    /**
     * Set the paramter name for the current actor declaration, if
     * any, to the given paramter name. 
     * @param paramName The name of the paramter.
     * @throws PtalonScopeException If not inside the scope of an
     * actor declaration.
     */
    public void setActorParameter(String paramName) throws PtalonScopeException {
        if (_currentTree == null) {
            throw new PtalonScopeException("Not inside the scope of an actor declaration.");
       }
       _currentTree.setActorParameter(paramName);
    }

    /**
     * Prepare the compiler to start at the outermost scope 
     * of the Ptalon program during run time.
     *
     */
    public void startAtTop() {
        super.startAtTop();
        _currentTree = null;
    }

    /**
     * Write an xml representation of this code manager
     * in PtalonML form.
     * 
     * @param output The writer to write to.
     * @param depth The indentation depth to start writing at.
     */
    public void xmlSerialize(Writer output, int depth) throws IOException {
        super.xmlSerialize(output, depth);
        for (ActorTree tree : _trees) {
            tree.xmlSerialize(output, depth);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////
    
    
    /**
     * This returns a string of form:
     * 
     * ptalonActor:foo.bar.baz
     * 
     * where $PTII/foo/bar/baz.ptln is a file refered to be
     * the import name.
     * 
     * @param importName The symbol name of the import in the
     * CodeManager.
     * @return The parameter string form of the import file.
     * @throws PtalonRuntimeException If this an invalid
     * import symbol is given.
     */
    private String _importToParameter(String importName) throws PtalonRuntimeException {
        File file = _imports.get(importName);
        if (file == null) {
            throw new PtalonRuntimeException("No such import symbol " + importName);
        }
        String filename = file.getAbsolutePath();
        String ptiiDir = StringUtilities.getProperty("ptolemy.ptII.dir");
        String ptiiFilename = StringUtilities.substituteFilePrefix(ptiiDir,
                filename.substring(1), "ptalonActor:");
        String unPtlnName = ptiiFilename.substring(0, ptiiFilename.lastIndexOf(".ptln") - 1);
        String output = unPtlnName.replace('/', '.');
        return output;
    }
    
    /**
     * The reverse of _importToParameter
     * @param expression The expression to convert.
     * @return The converted expression.
     */
    private String _parameterToImport(String expression) {
        String qualifiedName = expression.substring(12);
        String ptII = StringUtilities
                .getProperty("ptolemy.ptII.dir");
        String fileName = ptII.concat("/").concat(
                qualifiedName.replace('.', '/')).concat(".ptln");
        return fileName;
    }
    
    /**
     * Return a unique symbol for the given symbol.
     * The symbol will always end with a whole number.  For
     * instance _uniqueSymbol("Foo") may return "Foo0", "Foo1",
     * or "Foo2".  The input symbol is assumed to refer to a
     * previously declared parameter or import statement.
     * @symbol The symbol from which to derive the unique symbol.
     * @return A unique name.
     * @throws PtalonScopeException If the symbol does not refer
     * to a parameter or import valid in the current scope.
     */
    private String _uniqueSymbol(String symbol) throws PtalonScopeException {
        String type = getType(symbol);
        if (!(type.equals("import") || type.equals("parameter"))) {
            throw new PtalonScopeException("Symbol " + symbol + " not an import or paramter");
        }
        try {
            Integer number = _instanceNumbers.get(symbol) + 1;
            _instanceNumbers.put(symbol, number);
            String output = symbol + number;
            return output;
        } catch (Exception e) {
            throw new PtalonScopeException("Unable to get unique name for " + symbol, e);
        }
    }

    
    ///////////////////////////////////////////////////////////////////
    ////                        private members                    ////
    
    /**
     * This represents the current point in the scope
     * of a nested actor declaration.  It is null
     * when not inside an actor declaration.
     */
    private ActorTree _currentTree = null;    

    /**
     * This map gives the number for the next instance
     * of the specifed symbol.  As an example, if no
     * instance of the parameter Foo has been created,
     * and it has created, then _instanceNumbers.get("Foo") 
     * returns 0.  If it is created again, then 
     * _instanceNumbers.get("Foo") returns 1.
     */
    private Map<String, Integer> _instanceNumbers;
    
    /**
     * Each tree in this list represents a nested actor
     * declaration, like 
     *
     * Foo(a := Foo(a: = Bar(), b := Bar()), b := Bar())
     * 
     */
    private List<ActorTree> _trees;
    
    ///////////////////////////////////////////////////////////////////
    ////                        private classes                    ////
    
    
    /**
     * This class is a tree whose structure mimicks
     * that of a nested actor declaration.  For instance,
     * 
     * Foo(a := Foo(a: = Bar(), b := Bar()), b := Bar())
     * 
     * might have a tree that looks something like:
     *              Foo_0
     *             /     \
     *           Foo_1  Bar_0
     *           /   \
     *        Bar_1  Bar_2 
     */
    private class ActorTree extends NamedTree<ActorTree> {
        
        public ActorTree(ActorTree parent, String name) {
            super(parent, name);
            _symbol = name;
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /**
         * Create a new child tree to this tree with the specified
         * name and return it. 
         * @param name The name of the child.
         * @return The child ActorTree.
         */
        public ActorTree addChild(String name) {
            ActorTree tree = new ActorTree(this, name);
            _children.add(tree);
            return tree;
        }        

        /**
         * Assign all pararamters of the specified actor
         * their corresponding value, which should be something
         * like "createWith:Foo12", where "Foo12" is the 
         * unique name of the corresponding actor for this
         * parameter.
         * @param actor The actor that contains these parameters.
         * @throws PtalonRuntimeException If thrown trying to access the parameter,
         * or if unable to set the token for the corresponding paramter.
         */
        public void assignParameters(PtalonActor actor) throws PtalonRuntimeException {
            for (ActorTree child : _children) {
                String paramName = child.getActorParameter();
                PtalonParameter param = actor.getPtalonParameter(paramName);
                try {
                    String token = "createWith:" + child.getName();
                    param.setToken(new StringToken(token));
                } catch (IllegalActionException e) {
                    throw new PtalonRuntimeException("Unable to set token for name " + paramName, e);
                } 
            }
        }  
        
        public ComponentEntity createNestedActor(PtalonActor container)
                throws PtalonRuntimeException {
            try {
                String uniqueName = container.uniqueName(_symbol);
                if (getType(_symbol).equals("import")) {
                    PtalonActor actor = new PtalonActor(container, uniqueName);
                    FileParameter location = actor.ptalonCodeLocation;
                    File file = _imports.get(_symbol);
                    location.setToken(new StringToken(file.toString()));
                    actor.setNestedDepth(container.getNestedDepth() + 1);
                    assignParameters(actor);
                } else if (getType(_symbol).equals("parameter")) {
                    PtalonParameter parameter = (PtalonParameter) _actor
                            .getAttribute(getMappedName(_symbol));
                    if (!parameter.hasValue()) {
                        throw new PtalonRuntimeException(
                                "Parameter has no value");
                    }
                    String expression = parameter.getExpression();
                    if (expression.startsWith("ptalonActor:")) {
                        File file = new File(_parameterToImport(expression));
                        PtalonActor ptalonActor = new PtalonActor(container,
                                uniqueName);
                        ptalonActor.ptalonCodeLocation
                                .setToken(new StringToken(file.toString()));
                        ptalonActor.setNestedDepth(container.getNestedDepth() + 1);
                        assignParameters(ptalonActor);
                    } else if (expression.startsWith("createWith:")) {
                        PtalonActor newContainer = container;
                        int depth = _actor.getNestedDepth();
                        while (depth > 0) {
                            newContainer = (PtalonActor) newContainer.getContainer();
                            depth--;
                        }
                        ComponentEntity newInstance = newContainer
                                .createNestedActor(container, expression
                                        .substring(11));
                        if (newInstance instanceof PtalonActor) {
                            assignParameters((PtalonActor) newInstance);
                        }
                    } else {
                        Class<?> genericClass = Class.forName(expression);
                        Class<? extends ComponentEntity> entityClass = genericClass
                                .asSubclass(ComponentEntity.class);
                        Constructor<? extends ComponentEntity> entityConstructor = entityClass
                                .getConstructor(CompositeEntity.class,
                                        String.class);
                        ComponentEntity entity = entityConstructor.newInstance(
                                container, uniqueName);
                    }
                } else { // type of name not "import" or "parameter".
                    throw new PtalonRuntimeException("Invalid type for " + _symbol);
                }
            } catch (Exception e) {
                throw new PtalonRuntimeException(
                        "Unable to add nested actor to " + container, e);
            }
            return null;
        }
           
        /**
         * Get the name of the actor parameter, or throw an exception if
         * there is none.
         * @return The name of the actor paramter.
         * @throws PtalonRuntimeException If no parameter name has been
         * assigned to this actor.
         */
        public String getActorParameter() throws PtalonRuntimeException {
            if (_actorParameter == null) {
                throw new PtalonRuntimeException("Not assigned a paramter name");
            }
            return _actorParameter;
        }
        
        /**
         * Get the first actor tree decendant of this
         * actor tree with the specified name.  This
         * should be unique, as each subtree should have
         * a unqiue name.
         * @param uniqueName
         * @return The decendant, or null if there is none.
         * 
         */
        public ActorTree getActorTree(String uniqueName) {
            if (_name.equals(uniqueName)) {
                return this;
            }
            for (ActorTree child : _children) {
                if (child.getActorTree(uniqueName) != null) {
                    return child.getActorTree(uniqueName);
                }
            }
            return null;
        }
        
        /**
         * @return The CodeManager symbol
         * for this actor declaration.
         */
        public String getSymbol() {
            return _symbol;
        }
        
        /**
         * Set the paramter name for the current actor declaration, if
         * any, to the given paramter name. 
         * @param paramName The name of the paramter.
         */
        public void setActorParameter(String paramName) throws PtalonScopeException {
            _actorParameter = paramName;
        }


        /**
         * Set the symbol in the CodeManager
         * this actor declaration refers to.
         * It should have type "parameter" or
         * "import".
         * @param symbol The symbol to set.
         */
        public void setSymbol(String symbol) {
            _symbol = symbol;
        }
        
        /**
         * Write an xml version of this actor to the given output.
         * @param output The writer to send the output to.
         * @param depth The depth of indents to start with.
         * @throws IOException If there is a problem writing to the output.
         */
        public void xmlSerialize(Writer output, int depth) throws IOException {
            String text;
            if (_actorParameter == null) {
                text = _getIndentPrefix(depth) + "<actor_declaration name=\"" + _name
                + "\" symbol=\"" + _symbol + "\">\n";
            } else {
                text = _getIndentPrefix(depth) + "<actor_declaration name=\"" + _name
                + "\" actorParameter=\"" + _actorParameter  + "\" symbol=\"" + _symbol + "\">\n";
            }
            output.write(text);
            for (ActorTree child : _children) {
                child.xmlSerialize(output, depth + 1);
            }
            output.write(_getIndentPrefix(depth) + "</actor_declaration>\n");
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public members                    ////
        
        public boolean created = false;

        ///////////////////////////////////////////////////////////////////
        ////                        private members                    ////        
        
        /**
         * This is the symbol stored with the CodeManager
         * that this actor declaration refers to.  It's
         * either a "parameter" or "import" symbol.
         */
        private String _symbol;
        
        private String _actorParameter = null;
    }

}
