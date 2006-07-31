package ptolemy.actor.ptalon;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
        _currentTree = _root;
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
     * @return The next symbol of form "_ifN" where
     * N is 0 if this funciton has not been called and
     * N is n if this is the nth call to this function.
     */
    private String getNextIfSymbol() {
        String symbol = "_if";
        symbol.concat((new Integer(_counter)).toString());
        _counter++;
        return symbol;
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
     * Print out the scope information in this compiler.
     */
    public String toString() {
        String output = "---------Compiler info---------\n";
        IfTree current = _root;
        output += current.toString();
        return output;
    }
    
    /**
     * A counter used to associate a unqiue
     * number with each if-block.
     */
    private int _counter;
    
    /**
     * The root of the tree containing the symbol tables for each level
     * of the if-statement hierarchy.
     */
    private IfTree _root;
    
    /**
     * Some descendent of the root tree to which new input symbols
     * should be added.
     */
    private IfTree _currentTree;
    
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
            _parent = parent;
            _symbols = new Hashtable<String, String>();
        }
         
        /**
         * Add a symbol to the scope of this if statement.
         * @param symbol The sybmol to add.
         * @param type Its corresponding type.
         */
        public void addSymbol(String symbol, String type) {
            _symbols.put(symbol, type);
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
         * @return The children of this tree.
         */
        public List<IfTree> getChildren() {
            return _children;
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
                String message = value.concat(" not found.");
                throw new PtalonScopeException(message);
            }
            return value;
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
         * The parent of this if block.
         */
        private IfTree _parent;
        
        /**
         * The symbol table for this level of the if hierarchy.
         */
        private Hashtable<String, String> _symbols;
    }
}
