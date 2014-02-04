/*
Below is the copyright agreement for the Ptolemy II system.
Version: $Id$

Copyright (c) 2007-2009 The Regents of the University of California.
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
*/
package doc.tutorial.gui;

import java.awt.Container;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import ptolemy.actor.gui.AbstractPlaceableActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

public class TableDisplay extends AbstractPlaceableActor {
    protected JTable _table;
    private Tableau _tableau;

    public TableDisplay(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    public void initialize() throws IllegalActionException {
        super.initialize();
        if (_table == null) {
            Effigy containerEffigy = Configuration.findEffigy(toplevel());
            try {
                _tableau = new Tableau(containerEffigy, "tableau");
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(this, e,
                        "Failed to create tableau.");
            }
            _frame = new TableauFrame(_tableau);
            setFrame(_frame);
            _tableau.setFrame(_frame);
            place(_frame.getContentPane());
            _frame.pack();
        }
        if (_frame != null) {
            ((TableauFrame) _frame).show();
            _frame.toFront();
        }
    }

    public void place(Container container) {
        if (container == null) {
            if (_frame != null) {
                _frame.dispose();
            }
            _frame = null;
            _table = null;
            if (_tableau != null) {
                try {
                    _tableau.setContainer(null);
                } catch (Exception e) {
                    throw new InternalErrorException(e);
                }
            }
            _tableau = null;
        } else {
            @SuppressWarnings("serial")
			TableModel dataModel = new AbstractTableModel() {
                public int getColumnCount() {
                    return 10;
                }

                public int getRowCount() {
                    return 10;
                }

                public Object getValueAt(int row, int col) {
                    return Integer.valueOf(row * col);
                }
            };
            _table = new JTable(dataModel);
            container.add(_table);
        }
    }
}
