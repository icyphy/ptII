/* Layout Hint Attribute for Ptolemy relations to specify bendpoints for an explicit routing for links.

 @Copyright (c) 2011-2014 The Regents of the University of California.
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

package ptolemy.vergil.basic.layout.kieler;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.Vertex;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.actor.KielerLayoutConnector;
import diva.canvas.connector.ManhattanConnector;

///////////////////////////////////////////////////////////////////
////                      LayoutHint
/**
 * A LayoutHint is an Attribute for Ptolemy Relations that holds the
 * specification of bend points for links. Its value field contains a list of
 * {@link LayoutHintItem} objects because one Relation can correspond to
 * multiple links, which are not real objects in the Ptolemy abstract syntax and
 * therefore can not carry any attributes. Each item carries a list of
 * bendpoints for a specific link.
 * <p>
 * The LayoutHint uses a Ptolemy Expression as its value in which the
 * {@link LayoutHintItem} objects are encoded. Therefore the Expression is
 * expected to contain an {@link ArrayToken} of {@link LayoutHintItem} objects.</p>
 *
 * <p>
 * A complete LayoutHint with two {@link LayoutHintItem}s could look like this:</p>
 *
 * <pre>
 * {
 *   {
 *     head={id="Discard.input",x=60.0,y=115.0,index=2},
 *     tail={id="CompositeActor.port3",x=300.0,y=380.0,index=3},
 *     points={105.0,235.0,105.0,190.0,265.0,190.0,265.0,135.0}
 *   },
 *   {
 *     head={id="Ramp.output",x=320.0,y=225.0},
 *     tail={id="CompositeActor.port2",x=580.0,y=200.0,index=3},
 *     points={135.0,25.0,135.0,125.0}
 *   }
 * }
 * </pre>
 *
 * <p>This storage works like a {@link Map} with always two keys. One
 * {@link LayoutHintItem} is unambiguously identified by its head and tail,
 * which are Ptolemy objects like {@link Port}s or {@link Relation}s. The
 * methods to access this are {@link #getLayoutHintItem(Object, Object)},
 * {@link #setLayoutHintItem(NamedObj, NamedObj, double[])} and
 * {@link #removeLayoutHintItem(LayoutHintItem)}.</p>
 *
 * <p> The class extends {@link SingletonAttribute} because every
 * Relation is expected to have only one such Attribute, while one of
 * these Attributes can carry multiple {@link LayoutHintItem}s as
 * explained above. It is also {@link ptolemy.kernel.util.Settable} as
 * it can be set by loading a MOML file or by setting it manually
 * through the GUI. However, usually its visibility is set to EXPERT
 * mode only.</p>
 *
 * <p>
 * Some of the standard code for example for value listeners is copied from
 * {@link Location}.</p>
 *
 * @author Hauke Fuhrmann, (kieler@informatik.uni-kiel.de)
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (haf)
 * @Pt.AcceptedRating Red (haf)
 */
public class LayoutHint extends SingletonAttribute implements Settable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Construct an attribute with the given container and name.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   attribute with this name, and the class of that container is not
     *   SingletonAttribute.
     *
     * @see SingletonAttribute#SingletonAttribute(NamedObj, String)
     */
    public LayoutHint(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct a new attribute with
     *  no container and an empty string as a name.
     *  @param workspace The workspace that will list the attribute.
     *  @see SingletonAttribute#SingletonAttribute(Workspace)
     */
    public LayoutHint(Workspace workspace) {
        super(workspace);
    }

    /**
     * Add a listener to be notified when the value of this attribute changes.
     * If the listener is already on the list of listeners, then do nothing.
     *
     * @param listener The listener to add.
     * @see #removeValueListener(ValueListener)
     */
    @Override
    public void addValueListener(ValueListener listener) {
        if (_valueListeners == null) {
            _valueListeners = new LinkedList();
        }

        if (!_valueListeners.contains(listener)) {
            _valueListeners.add(listener);
        }
    }

    /**
     * Write a MoML description of this object. MoML is an XML modeling markup
     * language. In this class, the object is identified by the "property"
     * element, with "name", "class", and "value" (XML) attributes. The body of
     * the element, between the "&lt;property&gt;" and "&lt;/property&gt;", is
     * written using the _exportMoMLContents() protected method, so that derived
     * classes can override that method alone to alter only how the contents of
     * this object are described. The text that is written is indented according
     * to the specified depth, with each line (including the last one)
     * terminated with a newline. If this object is non-persistent, then nothing
     * is written.
     *
     * @see Location#exportMoML(Writer, int, String)
     * @param output The output writer to write to.
     * @param depth The depth in the hierarchy, to determine indenting.
     * @param name The name to use instead of the current name.
     * @exception IOException If an I/O error occurs.
     * @see #isPersistent()
     */
    @Override
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        // If the object is not persistent, and we are not at level 0, do nothing.
        if (_isMoMLSuppressed(depth)) {
            return;
        }

        String value = getExpression();
        String valueTerm = "";

        if (value != null && !value.equals("")) {
            valueTerm = " value=\"" + StringUtilities.escapeForXML(value)
                    + "\"";
        }

        // It might be better to use multiple writes here for performance.
        output.write(_getIndentPrefix(depth) + "<" + _elementName + " name=\""
                + name + "\" class=\"" + getClassName() + "\"" + valueTerm
                + ">\n");
        _exportMoMLContents(output, depth + 1);
        output.write(_getIndentPrefix(depth) + "</" + _elementName + ">\n");
    }

    /**
     * A LayoutHint has no default expression.
     * @return always null
     */
    @Override
    public String getDefaultExpression() {
        return null;
    }

    /**
     * Get the value that has been set by setExpression() or by
     * setLayoutHintItem(), whichever was most recently called, or return an
     * empty string if neither has been called.
     *
     * <p>
     * If setExpression(String value) was called, then the return value is
     * exactly what ever was passed in as the argument to setExpression. This
     * means that there is no guarantee that the return value of getExpression()
     * is a well formed Ptolemy array expression.</p>
     *
     * <p>
     * If setLayoutHintItem(NamedObj, NamedObj, double[]) was called, then the
     * return value is a well formed Ptolemy array expression that starts with
     * "{" and ends with "}", and contains the expressions of
     * {@link LayoutHintItem}s as array elements. Example:</p>
     *
     * <pre>
     * { item1, item2 }
     * </pre>
     *
     * @return The expression.
     * @see Location#getExpression()
     * @see #setExpression(String)
     */
    @Override
    public String getExpression() {
        if (_expressionSet) {
            // FIXME: If setExpression() was called with a string that does
            // not begin and end with curly brackets, then getExpression()
            // will not return something that is parseable by setExpression()
            return _expression;
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("{ ");
        int i = 0;
        for (LayoutHintItem item : _layoutHintItems) {
            if (i > 0) {
                buffer.append(",");
            }
            buffer.append(item.getExpression());
            i++;
        }
        buffer.append(" }");
        return buffer.toString();
    }

    /**
     * Get the {@link LayoutHintItem} stored in this LayoutHint that is
     * identified by the head and tail of the link for which it specifies bend
     * points. If no {@link LayoutHintItem} is stored for the given head and
     * tail, null is returned. It works like a map with two keys that have to
     * match. As links in Ptolemy are not directed, it does not matter if head
     * and tail get switched. However, for layout the direction does matter and
     * the bendpoint list is directed from head to tail. So if there is an item
     * available where head and tail are swapped, then this item will be
     * returned but the entries get swapped again to guarantee that head and
     * tail and the bendpoint order are correct.
     *
     * @param head The starting point of the link, e.g. a Ptolemy Port or
     *            Relation.
     * @param tail The ending point of the link, e.g. a Ptolemy Port or
     *            Relation.
     * @return the LayoutHintItem stored for this link or null
     * @see #setLayoutHintItem(NamedObj, NamedObj, double[])
     */
    public LayoutHintItem getLayoutHintItem(Object head, Object tail) {
        for (LayoutHintItem item : _layoutHintItems) {
            if (item.getHead() == head && item.getTail() == tail) {
                return item;
            }
            // also return this hint if head and tail are switched
            if (item.getHead() == tail && item.getTail() == head) {
                item._reverse();
                return item;
            }
        }
        return null;
    }

    /**
     * Get the value of the attribute, which is the evaluated expression.
     * @return The value.
     * @see Settable#getValueAsString()
     */
    @Override
    public String getValueAsString() {
        return getExpression();
    }

    /**
     * Get the visibility of this Settable, as set by setVisibility().
     * The returned value is one of the static
     * instances of the {@link ptolemy.kernel.util.Settable.Visibility} inner class.
     * @return The visibility of this Settable.
     * @see #setVisibility(ptolemy.kernel.util.Settable.Visibility)
     * @see ptolemy.kernel.util.Settable#getVisibility()
     */
    @Override
    public Visibility getVisibility() {
        return _visibility;
    }

    /**
     * Remove a {@link LayoutHintItem} from this storage. If that is the last
     * item contained in this layout hint, then the layout hint itself is
     * removed from its container.
     *
     * @param itemToRemove The layout hint item to remove
     */
    public void removeLayoutHintItem(final LayoutHintItem itemToRemove) {
        final NamedObj container = getContainer();
        if (container != null) {
            container.requestChange(new ChangeRequest(container,
                    "Remove Layout Hint") {
                @Override
                protected void _execute() throws Exception {
                    _layoutHintItems.remove(itemToRemove);
                    if (_layoutHintItems.isEmpty()) {
                        setContainer(null);
                    }
                }
            });
        }
    }

    /**
     * Remove a listener from the list of listeners that is notified when the
     * value of this variable changes. If no such listener exists, do nothing.
     *
     * @param listener The listener to remove.
     * @see Location#removeValueListener(ValueListener)
     * @see #addValueListener(ValueListener)
     */
    @Override
    public void removeValueListener(ValueListener listener) {
        if (_valueListeners != null) {
            _valueListeners.remove(listener);
        }
    }

    /**
     * Set the value of the attribute by giving some expression. This expression
     * is not parsed until validate() is called, and the container and value
     * listeners are not notified until validate() is called. See the class
     * comment for a description of the format.
     *
     * @param expression The value of the attribute.
     * @see #getExpression()
     */
    @Override
    public void setExpression(String expression) {
        _expression = expression;
        _expressionSet = true;
    }

    /**
     * Set a {@link LayoutHintItem} for a link which is specified by its head
     * and tail, i.e. Ptolemy {@link Port}s or {@link Relation}s. For this link
     * store the given list of bend points. Like in a {@link Map} with two keys,
     * a possibly existing item for the given head and tail will be reused and
     * updated with the bend points. If no such item yet exists, a new one is
     * added.
     *
     * @param head the head object of the corresponding link
     * @param tail the tail object of the corresponding link
     * @param bendPoints an array of double coordinates, where always two
     *            correspond to a bend point
     * @see #getLayoutHintItem(Object, Object)
     */
    public void setLayoutHintItem(NamedObj head, NamedObj tail,
            double[] bendPoints) {
        _expressionSet = false;
        LayoutHintItem item = getLayoutHintItem(head, tail);
        if (item == null) {
            item = new LayoutHintItem(head, tail);
            _layoutHintItems.add(item);
        }
        // make sure head and tail are in the right order
        if (head == item.getTail() && tail == item.getHead()) {
            // they are reversed, so reverse also bendpoints
            _reverseCoordinateArray(bendPoints);
        }

        item.setBendpoints(bendPoints);

        if (_valueListeners != null) {
            Iterator listeners = _valueListeners.iterator();

            while (listeners.hasNext()) {
                ValueListener listener = (ValueListener) listeners.next();
                listener.valueChanged(this);
            }
        }
    }

    /**
     * Set the visibility of this attribute. The argument should be one of the
     * public static instances in Settable.
     *
     * @param visibility The visibility of this attribute.
     * @see #getVisibility()
     */
    @Override
    public void setVisibility(Settable.Visibility visibility) {
        _visibility = visibility;
    }

    /**
     * Parse the layout hint specification given by setExpression(), if there
     * has been one, and otherwise do nothing, i.e. keep the list of layout
     * hints empty. Notify the container and any value listeners of the new
     * location, if it has changed. See the class comment for a description of
     * the format.
     *
     * @return Null, indicating that no other instances of Settable are
     *         validated.
     * @exception IllegalActionException If the expression is invalid.
     */
    @Override
    public Collection validate() throws IllegalActionException {
        _layoutHintItems = new ArrayList<LayoutHintItem>();
        if (_expression == null) {
            return null; // don't parse anything if there is no expression
        }
        // parse the expression
        Token result;
        try {
            PtParser parser = new PtParser();
            ASTPtRootNode parseTree = parser.generateParseTree(_expression);
            ParseTreeEvaluator parseTreeEvaluator = new ParseTreeEvaluator();
            result = parseTreeEvaluator.evaluateParseTree(parseTree, null);
        } catch (Throwable throwable) {
            // Chain exceptions to get the actor that threw the exception.
            // Note that if evaluateParseTree does a divide by zero, we
            // need to catch an ArithmeticException here.
            throw new IllegalActionException(this, throwable,
                    "Expression invalid.");
        }
        if (result == null) {
            throw new IllegalActionException(this,
                    "Expression yields a null result: " + _expression);
        }
        _addLayoutHintItems(result);
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Propagate the value of this object to the specified object. The specified
     * object is required to be an instance of the same class as this one, or a
     * ClassCastException will be thrown.
     *
     * @param destination Object to which to propagate the value.
     * @exception IllegalActionException If the value cannot be propagated.
     * @see Location#_propagateValue(NamedObj)
     */
    @Override
    protected void _propagateValue(NamedObj destination)
            throws IllegalActionException {
        ((LayoutHint) destination).setExpression(getExpression());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Create {@link LayoutHintItem}s from parsed Ptolemy Expression in form of
     * a {@link Token}. The token is expected to have exactly a specific format
     * as given in the class comment.
     *
     * @param hints a Token containing the required information about the
     *            LayoutHintItems
     * @exception IllegalActionException thrown when the Token does not conform to
     *             the expected format.
     */
    private void _addLayoutHintItems(Token hints) throws IllegalActionException {
        try {
            // The token is expected to be an array of LayoutHintItems.
            for (int i = 0; i < ((ArrayToken) hints).length(); i++) {
                // Each LayoutHintItem is expected to be a Record.
                RecordToken layoutItem = (RecordToken) ((ArrayToken) hints)
                        .getElement(i);
                // A LayoutHintItem has a head and tail entry, which are
                // records containing an identifying String, coordinates,
                // and optionally the width of a multiport.
                RecordToken headToken = (RecordToken) layoutItem.get("head");
                NamedObj head = _findNamedObj(this,
                        ((StringToken) headToken.get("id")).stringValue());
                Point2D.Double headLocation = new Point2D.Double();
                headLocation.x = ((DoubleToken) headToken.get("x"))
                        .doubleValue();
                headLocation.y = ((DoubleToken) headToken.get("y"))
                        .doubleValue();
                int headMultiportWidth = 1;
                if (headToken.get("index") != null) {
                    headMultiportWidth = ((IntToken) headToken.get("index"))
                            .intValue();
                }

                RecordToken tailToken = (RecordToken) layoutItem.get("tail");
                NamedObj tail = _findNamedObj(this,
                        ((StringToken) tailToken.get("id")).stringValue());
                Point2D.Double tailLocation = new Point2D.Double();
                tailLocation.x = ((DoubleToken) tailToken.get("x"))
                        .doubleValue();
                tailLocation.y = ((DoubleToken) tailToken.get("y"))
                        .doubleValue();
                int tailMultiportWidth = 1;
                if (tailToken.get("index") != null) {
                    tailMultiportWidth = ((IntToken) tailToken.get("index"))
                            .intValue();
                }

                // The LayoutHintItem record contains a points entry, containing
                // an array of bend points.
                ArrayToken bendPoints = (ArrayToken) layoutItem.get("points");
                // Only do if head and tail could be resolved.
                // This can fail if you insert a new relation vertex into an
                // existing link. Then first the attribute is copied and only
                // then the relation is inserted into the diagram. Therefore the
                // head and tail cannot be found. However, this LayoutHint will
                // be invalid in such case anyways.
                if (head != null && tail != null) {
                    // create new LayoutHintItem and add it to this LayoutHint
                    LayoutHintItem item = new LayoutHintItem(head, tail,
                            headLocation, tailLocation, headMultiportWidth,
                            tailMultiportWidth);
                    double[] primitiveBendPoints = new double[bendPoints
                                                              .length()];
                    for (int ii = 0; ii < bendPoints.length(); ii++) {
                        primitiveBendPoints[ii] = ((ScalarToken) bendPoints
                                .getElement(ii)).doubleValue();
                    }
                    item.setBendpoints(primitiveBendPoints);
                    _layoutHintItems.add(item);
                }
            }
        } catch (Exception e) {
            throw new IllegalActionException(
                    this,
                    e,
                    e.getMessage()
                    + "\nExpression is expected to be an Array of layout hint Records. "
                    + "The following expression is of wrong format: \n"
                    + _expression
                    + "\nAn example for a layoutHint expression is\n"
                    + EXAMPLE_EXPRESSION);
        }
    }

    /**
     * Find the first CompositeActor in the parent hierarchy of the given
     * NamedObj and find some other NamedObj with a given String name in there.
     *
     * @param start start object in the parent hierarchy
     * @param name name of the object to find in the first CompositeActor found
     * @return the object found or null if not found
     */
    private static NamedObj _findNamedObj(NamedObj start, String name) {
        NamedObj result = null;
        NamedObj container = start.getContainer();
        while (container != null && !(container instanceof CompositeActor)) {
            container = container.getContainer();
        }
        if (container != null) {
            result = ((CompositeActor) container).getPort(name);
            if (result == null) {
                result = ((CompositeActor) container).getEntity(name);
            }
            if (result == null) {
                result = ((CompositeActor) container).getAttribute(name);
            }
        }
        return result;
    }

    /**
     * Reverse the order of an array of coordinates, i.e. every two entries are
     * assumed to belong together and will be kept in right order.
     *
     * @param bendPoints the array to reverse, will be changed
     * @return the changed array
     */
    private static double[] _reverseCoordinateArray(double[] bendPoints) {
        int size = bendPoints.length - bendPoints.length % 2;
        // Make sure only to process even length.
        // Don't do anything if the array has only one location.
        if (size >= 4) {
            double tempx, tempy;
            int lastX, lastY;
            int iterations = size / 4;
            for (int i = 0; i < iterations; i += 1) {
                int index = i * 2;
                tempx = bendPoints[index];
                tempy = bendPoints[index + 1];
                lastY = size - 1 - index;
                lastX = lastY - 1;
                bendPoints[index] = bendPoints[lastX];
                bendPoints[index + 1] = bendPoints[lastY];
                bendPoints[lastX] = tempx;
                bendPoints[lastY] = tempy;
            }
        }
        return bendPoints;
    }

    /** A valid example expression to show in the GUI in case of errors. */
    private static final String EXAMPLE_EXPRESSION = "{  \n{head={id=\"a.out\",x=10,y=11},"
            + "tail={id=\"relation1\",x=20,y=21},points={1,2,3,4,5,6}} ,"
            + " \n{head={id=\"b.out1\",x=10,y=11},"
            + "tail={id=\"relation2\",x=20,y=21},points={1,2,3,4,5,6}} \n}";
    /** The expression given in setExpression(). */
    private String _expression;
    /** Indicator that the expression is the most recent spec for the location. */
    private boolean _expressionSet = false;
    /** List of layout hint items stored by this layout hint */
    private List<LayoutHintItem> _layoutHintItems = new ArrayList<LayoutHintItem>();
    /** Listeners for changes in value. */
    private List _valueListeners;
    /** The visibility of this attribute, which defaults to EXPERT. */
    private Settable.Visibility _visibility = Settable.EXPERT;

    /**
     * A LayoutHintItem is the specification of layout information for one Link.
     * As there are usually multiple links corresponding to one {@link Relation},
     * a {@link LayoutHint} is attached to a Relation and carries multiple of
     * these LayoutHintItems corresponding to the links. As links are no
     * persisted objects in Ptolemy, a link is identified by its head and tail
     * objects, which are {@link Port}s or {@link Relation}s.
     * <p>
     * The most important information such item carries is a list of bend points
     * that can be used to explicitly route a link along these bend points
     * instead of using a simple routing strategy like the
     * {@link ManhattanConnector}. A router that uses the bend point information
     * for example is the {@link KielerLayoutConnector}.
     * <p>
     * Such item can be serialized to the String representation of a Ptolemy
     * Expression by {@link #getExpression()}. This is used for persisting
     * LayoutHintItems. However, the bend point data are absolute coordinates
     * and therefore are only valid until the head and/or tail of the link are
     * moved. Hence, the LayoutHintItem also stores the coordinates and
     * optionally the multiport width of head and tail, which specify for which
     * layout of nodes the bend point information is only valid. The
     * {@link #revalidate()} method is used to check the validity of the
     * LayoutHintItem by comparing the stored positions with the actual
     * positions in the diagram, i.e. checking whether head and/or tail have
     * been moved or the width of a multiport has changed. If the LayoutHintItem
     * is not valid anymore, its bend points should not be used.
     * <p>
     * A special case is when head and tail moved relatively exactly the same,
     * which happens, if multiple elements are selected and moved together. In
     * such case the bend points are still valid relatively, but not absolutely.
     * Therefore the {@link #revalidate()} method also checks this case and
     * translates the bend point coordinates as well as the new head and tail
     * locations making the LayoutHintItem valid again. This avoids invalidating
     * bend points when whole model parts get moved.
     * <p>
     * An example for one LayoutHintItem's String representation is the
     * following
     *
     * <pre>
     * { head={"CompositeActor.port",20.0,200.0,2}, tail={"Discard.input",70.0,25.0}, points={135.0,25.0,135.0,125.0} }
     * </pre>
     *
     * The head contains the object's name, its coordinates in x and y and the
     * width, because the port is a multiport. The width defaults to 1 as can be
     * seen at the tail where it is omitted.
     */
    public static class LayoutHintItem {

        ///////////////////////////////////////////////////////////////////
        ////                     public methods                        ////

        /**
         * Simple constructor specifying only head and tail for this
         * LayoutHintItem. The current layout of head and tail that is required
         * for validity checking is obtained from these objects automatically.
         *
         * @param head the head object of the corresponding link
         * @param tail the tail object of the corresponding link
         */
        public LayoutHintItem(NamedObj head, NamedObj tail) {
            this._head = head;
            this._tail = tail;
            _updateHeadTailLocations();
        }

        /**
         * Constructor passing not only head and tail but also all required
         * layout information for the conditions under which this LayoutHintItem
         * is only valid.
         *
         * @param head the head object of the corresponding link
         * @param tail the tail object of the corresponding link
         * @param locationHead the location of the head as vector
         * @param locationTail the location of the tail as vector
         * @param multiportWidthHead the width of the head, which is relevant
         *            for multiports, 1, if no multiport
         * @param multiportWidthTail the width of the tail, which is relevant
         *            for multiports, 1, if no multiport
         */
        public LayoutHintItem(NamedObj head, NamedObj tail,
                Point2D.Double locationHead, Point2D.Double locationTail,
                int multiportWidthHead, int multiportWidthTail) {
            this(head, tail);
            this._headLocation = locationHead;
            this._tailLocation = locationTail;
            _headMultiportIndex[1] = multiportWidthHead;
            _tailMultiportIndex[1] = multiportWidthTail;
        }

        /**
         * Get the bend points stored in this hint as an array of doubles, where
         * each two entries correspond to x and y of one bend point.
         *
         * @return array containing bend point coordinates
         */
        public double[] getBendPoints() {
            return _bendPoints;
        }

        /**
         * Get a list of {@link Point2D} corresponding to the bend points stored
         * in this item. If by setting the bend points with
         * {@link #setBendpoints(double[])} the list of bend point coordinates
         * is odd, the last coordinate is discarded, and a list of points
         * without the dangling coordinate is returned.
         *
         * @return list of bend points
         */
        public List<Point2D> getBendPointList() {
            int size = _bendPoints.length / 2; // integer arithmetics will cut
            // off in odd cases
            ArrayList<Point2D> list = new ArrayList<Point2D>(size);
            for (int i = 0; i < size; i++) {
                Point2D point = new Point2D.Double(_bendPoints[2 * i],
                        _bendPoints[2 * i + 1]);
                list.add(point);
            }
            return list;
        }

        /**
         * Get the String representation of the Ptolemy Expression by which this
         * LayoutHint is persisted. See the class comment for the concrete
         * specification.
         *
         * @return String representation of this LayoutHint
         */
        public String getExpression() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("{ head={id=\"" + _getName(_head) + "\"" + ",x="
                    + _headLocation.x + ",y=" + _headLocation.y);
            if (_headMultiportIndex[1] != 1) {
                buffer.append(",index=" + _headMultiportIndex[1]);
            }
            buffer.append("}, " + "tail={id=\"" + _getName(_tail) + "\""
                    + ",x=" + _tailLocation.x + ",y=" + _tailLocation.y);
            if (_tailMultiportIndex[1] != 1) {
                buffer.append(",index=" + _tailMultiportIndex[1]);
            }
            buffer.append("}, " + "points={");
            for (int i = 0; i < _bendPoints.length - 1; i += 2) {
                if (i > 0) {
                    buffer.append(",");
                }
                buffer.append(_bendPoints[i] + "," + _bendPoints[i + 1]);
            }
            buffer.append("} }");
            return buffer.toString();
        }

        /**
         * Get the head of this LayoutHint which is used to identify this hint.
         *
         * @return head object of this LayoutHint
         */
        public NamedObj getHead() {
            return _head;
        }

        /**
         * Get the tail of this LayoutHint which is used to identify this hint.
         *
         * @return tail object of this LayoutHint
         */
        public NamedObj getTail() {
            return _tail;
        }

        /**
         * Check if the head and tail objects have been moved. If this is the
         * case but both have been moved while keeping the same relative
         * position to each other, the bend points can be translated
         * accordingly. In this case, update the bend points and the head and
         * tail location. If they have been moved and the relative positions are
         * different now, then return false. In that case the bend point list is
         * no longer feasible and should not be used anymore, however, no update
         * is done here.
         *
         * @return true if the relative positions of head and tail are the same
         *         as before, false otherwise
         */
        public boolean revalidate() {
            if (_bendPoints == null) {
                return false;
            }
            if (_head instanceof Port) {
                // check if the width of a multiport has changed
                int width = _getChannelWidth(_head);
                if (width != _headMultiportIndex[1]) {
                    return false;
                }
            }
            if (_tail instanceof Port) {
                int width = _getChannelWidth(_tail);
                if (width != _tailMultiportIndex[1]) {
                    return false;
                }
            }
            Point2D.Double newHeadLocation = _getEndpointLocation(_head);
            Point2D.Double newTailLocation = _getEndpointLocation(_tail);

            if (newHeadLocation.equals(_headLocation)
                    && newTailLocation.equals(_tailLocation)) {
                // nothing has changed, we don't need to update anything
                return true;
            }

            double oldDistanceX = _headLocation.x - _tailLocation.x;
            double oldDistanceY = _headLocation.y - _tailLocation.y;
            double newDistanceX = newHeadLocation.x - newTailLocation.x;
            double newDistanceY = newHeadLocation.y - newTailLocation.y;

            // do an integer compare. This is safe and enough.
            if ((int) oldDistanceX != (int) newDistanceX
                    || (int) oldDistanceY != (int) newDistanceY) {
                // in this case we cannot use the bend points anymore
                return false;
            }
            // now we know the head and tail have been moved but the relative
            // location is the same
            // we can safely translate everything
            _translate(newHeadLocation.x - _headLocation.x, newHeadLocation.y
                    - _headLocation.y);
            return true;
        }

        /**
         * Set a new list of bend points and update the current validation
         * information such as the current location of head and tail and their
         * port widths. Hence, this LayoutHint will be valid until the head and
         * tail are moved again.
         *
         * @param bendPoints new bend points
         */
        public void setBendpoints(double[] bendPoints) {
            _bendPoints = bendPoints;
            _updateHeadTailLocations();
        }

        /**
         * Get a String representation of a LayoutHint which will be the same as
         * {@link #getExpression()}.
         *
         * @see #getExpression()
         * @return String representation of this LayoutHint
         */
        @Override
        public String toString() {
            return getExpression();
        }

        /**
         * Reverse the list of bend points. This may be neces
         */
        protected void _reverse() {
            Object temp;
            // swap head and tail
            temp = _head;
            _head = _tail;
            _tail = (NamedObj) temp;
            // swap location
            temp = _headLocation;
            _headLocation = _tailLocation;
            _tailLocation = (Point2D.Double) temp;
            // swap multiport stuff
            temp = _headMultiportIndex;
            _headMultiportIndex = _tailMultiportIndex;
            _tailMultiportIndex = (int[]) temp;
            // reverse bendpoints
            _reverseCoordinateArray(_bendPoints);
        }

        ///////////////////////////////////////////////////////////////////
        ////                     private methods                       ////

        /**
         * Translate all coordinates given in this hint item. Include the
         * memorized head and tail location as well as all bend points.
         *
         * @param x amount on the x-axis to translate
         * @param y amount on the x-axis to translate
         */
        private void _translate(double x, double y) {
            _headLocation.x += x;
            _headLocation.y += y;
            _tailLocation.x += x;
            _tailLocation.y += y;
            // iterate all bend points, make sure that in a faulty odd
            // array, the last entry is ignored
            for (int i = 0; i < _bendPoints.length - 1; i += 2) {
                _bendPoints[i] += x;
                _bendPoints[i + 1] += y;
            }
        }

        /**
         * Update the layout information for head and tail of this hint that is
         * required to check validity. This is the location and channel width
         * get read from the current model objects.
         */
        private void _updateHeadTailLocations() {
            _headLocation = _getEndpointLocation(_head);
            _tailLocation = _getEndpointLocation(_tail);
            _updateChannelIndex(_head, _headMultiportIndex);
            _updateChannelIndex(_tail, _tailMultiportIndex);
        }

        /**
         * Get the width of a channel corresponding to a port. If no
         * {@link IOPort} is passed, return 0.
         *
         * @param port port for which to determine the channel width
         * @return channel width if applicable, else 0
         */
        private static int _getChannelWidth(Object port) {
            if (port instanceof IOPort) {
                return ((IOPort) port).numLinks();
                // return ((IOPort)port).getWidth();
            }
            return 0;
        }

        /**
         * Get a String name of an object that is sufficient to identify it on
         * one level of hierarchy. The name will be the name of the object
         * relative to some parent. E.g. a port "input" of a "Discard" actor
         * will result in the name "Discard.input".
         *
         * @param obj The object for which the name should be obtained
         * @return a String representation that is sufficient to identify the
         *         object
         */
        private static String _getName(NamedObj obj) {
            NamedObj parent = obj.getContainer();
            if ((obj instanceof Port || obj instanceof Vertex || obj instanceof Attribute)
                    && parent != null) {
                parent = parent.getContainer();
            }
            return obj.getName(parent);
        }

        /**
         * Determine the correct location of a link endpoint.
         *
         * @param obj an endpoint of a link
         * @return the location for the given endpoint
         */
        private static Point2D.Double _getEndpointLocation(NamedObj obj) {
            // In case of a component port, the endpoint is the external port of
            // a composite actor, so take the actor's position instead of the port's
            // internal position.
            if (obj instanceof ComponentPort) {
                return (Point2D.Double) PtolemyModelUtil._getLocationPoint(obj
                        .getContainer());
            }
            return (Point2D.Double) PtolemyModelUtil._getLocationPoint(obj);
        }

        /**
         * Update the channel width of a given object. For an IOPort, the actual
         * channel width is obtained and otherwise set it to default 1.
         *
         * @param port The port for which the update is required, may also be
         *            some other type than port, e.g. a Relation
         * @param indexLocation array with at least 2 entries in where to store
         *            the index locations
         */
        private static void _updateChannelIndex(Object port, int[] indexLocation) {
            int width = 1;
            int index = 1; // currently the index is ignored
            if (port instanceof IOPort) {
                width = ((IOPort) port).numLinks();
            }
            indexLocation[0] = index;
            indexLocation[1] = width;
        }

        /**
         * Local storage of bend points, where every 2 doubles form x and y
         * coordinates.
         */
        private double[] _bendPoints = {};
        /** Head object to identify this item. */
        private NamedObj _head = null;
        /** Coordinates for the head at which this item is only valid. */
        private Point2D.Double _headLocation = new Point2D.Double();
        /** Width and index of the multiport, if the head actually is a multiport. */
        private int[] _headMultiportIndex = { 1, 1 };
        /** Tail object to identify this item. */
        private NamedObj _tail = null;
        /** Coordinates for the tail at which this item is only valid. */
        private Point2D.Double _tailLocation = new Point2D.Double();
        /** Width and index of the multiport, if the tail actually is a multiport. */
        private int[] _tailMultiportIndex = { 1, 1 };
    }
}
