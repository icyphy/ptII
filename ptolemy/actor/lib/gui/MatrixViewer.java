/* A graphical component displaying matrix contents.

 Copyright (c) 1997-2000 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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

@ProposedRating Green (kienhuis@eecs.berkeley.edu)
@AcceptedRating Green (kienhuis@eecs.berkeley.edu)

*/

package ptolemy.actor.lib.gui;

import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.gui.Placeable;
import ptolemy.data.expr.Parameter;

import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.math.Complex;
import ptolemy.actor.lib.*;

//////////////////////////////////////////////////////////////////////////
//// MatrixViewer
/**

A graphical component that displays the contents of a Matrix. This
actor has a single input port, which only accepts MatrixTokens. One
token is consumed per firing.  The data in the MatrixToken is
displayed in a table format with scrollbars, using the swing JTable
class.

@author Bart Kienhuis
@version $Id$
*/

public class MatrixViewer extends Sink implements Placeable {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MatrixViewer(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  
        {
            super(container, name);
            input.setMultiport(false);
            input.setTypeEquals(BaseType.MATRIX);

            // set the parameters
            width = new Parameter(this, "width", new IntToken(500));
            width.setTypeEquals(BaseType.INT);
            height = new Parameter(this, "height", new IntToken(300));
            height.setTypeEquals(BaseType.INT);

        }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The width of the table (an integer). */
    public Parameter width;

    /** The height of the table (an integer). */
    public Parameter height;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Notification that an attribute has changed.
     *  @exception IllegalActionException If the expression of the
     *   attribute cannot be parsed or cannot be evaluated.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException 
        {
            System.out.println(" attribute change " + attribute.toString());
            if (attribute == width) {                
                _width = ((IntToken)width.getToken()).intValue();
            } else if (attribute == height) {
                _height = ((IntToken)height.getToken()).intValue();
            } else {
                super.attributeChanged(attribute);
            }
        }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new parameters.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has an
     *   attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        MatrixViewer newobj = (MatrixViewer)super.clone(ws);
        newobj.width = (Parameter)newobj.getAttribute("width");
        newobj.height = (Parameter)newobj.getAttribute("height");
        return newobj;
    }

    /** Consume a token from the <i>input</i> port when present and
     *  converted the content of the token into a table when it is of
     *  type Matrix. If not token is available, do nothing. The table
     *  that is generated is an instance of a swing class JTable.
     *
     *  @exception IllegalActionException If there is no director, or
     *  if the base class throws it.
     */
    public void fire() throws IllegalActionException {
        Token in = null;
        if (input.hasToken(0)) {
            in = input.get(0);
            _matrixTable = new MatrixAsTable((MatrixToken) in);
            _table.setModel(_matrixTable);
        }
    }

    /** Get the preferred size of this component.  This is simply the
     *  dimensions specified by the parameters <i>height</i> and
     *  <i>width</i> that represent respectively the height and width
     *  of the table representing the Matrix. The default value for
     *  preferred size is a height of 300 pixels and a width of 500
     *  pixels.
     *
     *  @return The preferred size.
     */
    public Dimension getPreferredSize() {
        return new Dimension( _width, _height );
    }

    /** Initialize this matrix viewer. If a table hasn't been created
     *  yet, create one by calling the method <i>place</i> on the
     *  container. If no container yet exists (i.e., the container is
     *  null), the place method will create a table in its own window,
     *  otherwise the table is place in the given
     *  container. Furthermore, if a frame has been created in the
     *  place method, it is explicitly made visible.
     * 
     *  @exception IllegalActionException If the parent class
     *  throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (_table == null) {
            place(_container);
        }
        if (_frame != null) {
            _frame.setVisible(true);
        }
    }

    /** Specify the container in which the data should be displayed.
     *  An instance of JTable will be added to that container. The
     *  table is configured such that a user cannot reorder the
     *  columns of the table. Also, the table maintains a fixed
     *  preferred size, and will emply scrollbars if the table is
     *  larger than the preferred size. This method needs to be called
     *  before the first call to initialize(). Otherwise, an instance
     *  of JTable will be placed in its own frame. The table is also
     *  placed in its own frame if this method is called with a null
     *  argument. The background of the table is set equal to that of
     *  the container (unless it is null).
     *
     *  @param container The container into which to place the table.
     */
    public void place(Container container) {
        _container = container;

        try {
            _width = ((IntToken)width.getToken()).intValue();
            _height = ((IntToken)height.getToken()).intValue();
            //attributeChanged( height );
            //attributeChanged( width );
        } catch ( Exception e ){
        }
    
        // create a table
        _table = new JTable();
        // Avoid reordering of the tables by the user
        _table.getTableHeader().setReorderingAllowed(false);            
        // Do not adjust column widths automatically, use a scrollbar
        _table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // add scroll pane to the table
        _scrollPane = new JScrollPane(_table);
        _scrollPane.setPreferredSize( getPreferredSize() );
       
        if (_container == null) {
            System.out.println("Container is null");
            // place the table in its own frame.
            _frame = new JFrame(getFullName());
            _frame.getContentPane().add(_scrollPane, BorderLayout.CENTER);
        } else {
            System.out.println("Container is exists");
            // place the table in supplied container.
            _table.setBackground(_container.getBackground());
            _container.add(_scrollPane, BorderLayout.CENTER);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Container into which this plot should be placed */
    private Container _container = null; 

    /** Frame into which plot is placed, if any. */
    private JFrame _frame = null;

    /** The Abstract Table Model of a Matrix. */
    private MatrixAsTable _matrixTable = null;

    /** The scroll pane of the panel. */
    private JScrollPane _scrollPane;

    /** The table representing the matrix. */
    private  JTable _table;

    /** Width of the matrix viewer in pixels. */
    private int _width;

    /** Height of the matrix viewer in pixels. */
    private int _height;

    ///////////////////////////////////////////////////////////////////
    ////                         Inner Class                       ////

    /** This class provides the implementations of the methods in the
     *  TableModel interface. To create a concrete TableModel of a
     *  Matrix as a subclass of AbstractTableModel, the class provides
     *  implementations for most of the methods.
     */
    private class MatrixAsTable extends AbstractTableModel {

        /** Construct for a specific matrix.
            @param matrix The matrix.
        */
        MatrixAsTable(MatrixToken matrix) {
            _matrix = matrix;
        }

        /** Get the row count of the Matrix.
            @return the row count.
        */
        public int getRowCount() {
            return _matrix.getRowCount();
        }

        /** Get the column count of the Matrix.
            @return the column count.
        */
        public int getColumnCount() {
            return _matrix.getColumnCount();
        }

        /** Get a specific entry from the matrix given by the row and
            column number.
            @param row The row number.
            @param column The column number.
            @return The object stored in the matrix at the specified location.
        */
        public Object getValueAt(int row, int column) {
            return (Object) (_matrix.getElementAsToken(row, column)).
                toString();
        }

        /** Get column names of the Matrix.
            @return the column names.
        */
        public String getColumnName(int columnIndex ) {
            return Integer.toString(columnIndex + 1);
        }

        /** The Matrix for which a Table Model is created. */
        private MatrixToken _matrix = null;

    }

}
