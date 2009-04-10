package ptolemy.data.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.kernel.util.Attribute;

public class SharedUtilities {

    /**
     * Construct a new SharedUtilities object.
     */
    public SharedUtilities() {
        // Since this is a shared (singleton) object per model,
        // it is important that all model-specific references
        // need to be reset when cloned. Otherwise, it will lead
        // to bugs that are hard to detect, and inconsistency
        // will occur.
        id = count++;
    }

    /**
     * Record the given error message.
     * @param error The error message to record.
     */
    public void addErrors(String error) {
        _errors.add(error);
    }

    /**
     * Mard the given property solver as already activated.
     * @param solver The given solver.
     */
    public void addRanSolvers(PropertySolver solver) {
        _ranSolvers.add(solver);
    }

    /**
     * Return the map that maps root ast node (keys) to the
     * corresponding attribute (values).
     * @return The mappings for root ast nodes to attributes.
     */
    public  Map<ASTPtRootNode, Attribute> getAttributes() {
        return _attributes;
    }

    /**
     * Return the list of error strings.
     * @return The list of error strings.
     */
    public List<String> getErrors() {
        Collections.sort(_errors);
        return _errors;
    }

    /**
     * Return the map that maps attributes (keys) to their root ast
     * nodes (values).
     * @return The mappings for attributes to their root ast nodes.
     */
    public Map<Attribute, ASTPtRootNode> getParseTrees() {
        return _parseTrees;
    }

    /**
     * Return the set of solvers that were marked activated.
     * @return The set of solvers that were activated previously.
     */
    public Set<PropertySolver> getRanSolvers() {
        return _ranSolvers;
    }

    /**
     * Record the mapping between the given attribute and the
     * given root ast node.
     * @param attribute The given attribute.
     * @param root The given root ast node.
     */
    public void putParseTrees(Attribute attribute, ASTPtRootNode root) {
        _parseTrees.put(attribute, root);
    }

    /**
     * Clear and return the previously recorded errors.
     * @return The previously recorded errors.
     */
    public List removeErrors() {
        List result = new ArrayList(_errors);
        _errors.clear();
        return result;
    }

    /**
     * Clear the states of this shared object. The states include
     * all previously recorded information.
     */
    public void resetAll() {
        _ranSolvers = new HashSet<PropertySolver>();
        _parseTrees = new HashMap<Attribute, ASTPtRootNode>();
        _attributes = new HashMap<ASTPtRootNode, Attribute>();
        _errors = new ArrayList<String>();

    }

    /**
     * Return the representation for the SharedUtilities object.
     */
    public String toString() {
        String result = "sharedUtilities#" + id;
        return result;
    }

    /**
     * Record the association between the given ast node and the
     * given attribute.
     * @param node The given ast node.
     * @param attribute The given attribute.
     */
    protected void putAttribute(ASTPtRootNode node, Attribute attribute) {
        _attributes.put(node, attribute);
    }

    /**
     * The set of solvers that have already been invoked.
     */
    private HashSet<PropertySolver> _ranSolvers = new HashSet<PropertySolver>();

    private Map<Attribute, ASTPtRootNode> _parseTrees =
        new HashMap<Attribute, ASTPtRootNode>();

    private Map<ASTPtRootNode, Attribute> _attributes =
        new HashMap<ASTPtRootNode, Attribute>();

    private ArrayList<String> _errors = new ArrayList<String>();

    protected PropertySolver _previousInvokedSolver = null;

    public int id;
    public static int count = 0;
}
