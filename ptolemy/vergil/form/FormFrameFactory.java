/* A menu item factory that creates the port/parameter/property editing form.

 Copyright (c) 1999-2000 The Regents of the University of California.
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

@ProposedRating Red (Ed.Willink@rrl.co.uk)
@AcceptedRating Red (Ed.Willink@rrl.co.uk)
*/

package ptolemy.vergil.form;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.Documentation;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.Location;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.toolbox.EditorIcon;
import ptolemy.vergil.toolbox.MenuItemFactory;

import diva.gui.toolbox.JContextMenu;

import java.awt.event.ActionEvent;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// FormFrameFactory
/**
A factory that adds an action to the given context menu that will configure
parameters on the given object.

@author Edward D. Willink
@version $Id$
*/
public class FormFrameFactory implements MenuItemFactory {

    /** Construct a factory with the default name, "Edit Parameters".
     */
    public FormFrameFactory() {
        this("Edit Form");
    }

    /** Construct a factory with the specified name.  This name
     *  will typically appear in a menu.
     *  @param name The name of the factory.
     */
    public FormFrameFactory(String name) {
        _name = name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** Add an item to the given context menu that will configure the
     *  parameters on the given target.
     *  @param menu The context menu.
     *  @param object The object whose parameters are being configured.
     */
    public JMenuItem create(JContextMenu menu, NamedObj object) {

        // Removed this method since it was never used. EAL
	// final NamedObj target = _getItemTargetFromMenuTarget(object);
        final NamedObj target = object;

	// ensure that we actually have a target.
	if (target == null) return null;
	Action action = new AbstractAction(_name)
        {
	    public void actionPerformed(ActionEvent e)
            {
		// Create a dialog for configuring the object.
                // FIXME: First argument below should be a parent window
                // (a JFrame).
                FormFrame theForm = new FormDialog(target);
 	    }
	};
        return menu.add(action, _name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////

    // The name of this factory.
    private String _name;
}

/** FormDialog provides a non-modal 2D port/parameter/property editing form for a NamdObj.
 * It refines FormFrame to maintain the connection between the underlying editor and the MoML model */
class FormDialog extends FormFrame implements ChangeListener
{
    /** Construct and display a FormFrame tailored to display the attributes (and ports) of aTarget. */
    public FormDialog(NamedObj aTarget)
    {
        super();
        _target = aTarget;
        _target.addChangeListener(this);
        setColumnModel(_createColumnModel());
        _setContents();
        setPreferredScrollableViewportSize(new Dimension(700, 200));
        pack();
        // Center on screen.  According to the Java docs,
        // passing null to setLocationRelationTo() _may_ result
        // in centering on the screen, but it is not required to.
        Toolkit tk = Toolkit.getDefaultToolkit();
        setLocation((tk.getScreenSize().width - getSize().width)/2, (tk.getScreenSize().height - getSize().height)/2);
        super.setVisible(true);                         // Bypass duplication of _setContents()
    }
    /** Notification of a successful change causes the form to be recreated.
     * .bugbug A smart update could be so much more efficient. */
    public void changeExecuted(ChangeRequest changeRequest)
    {
        System.out.println("Executed ChangeRequest for " + _target + "\n" + changeRequest.getDescription());
        _setContents();
    }
    /** Notification of an unsuccessful change is diagnosed. */
    public void changeFailed(ChangeRequest changeRequest, Exception exception)
    {
        if ((changeRequest.getSource() == this) && !changeRequest.isErrorReported())
        {
            MessageHandler.error("Change failed: ", exception);
            changeRequest.setErrorReported(true);
        }
    }
    /** Setting the form visibility is intercepted to ensure changes are listened to when and only when visible,
     * and to recreate the form when it becomes visible in order to catch any missed changes. */ 
    public void setVisible(boolean isVisible)
    {
        boolean wasVisible = isVisible();
        if (isVisible)
        {
            if (!wasVisible)
            {
                _setContents();
                _target.addChangeListener(this);
            }
        }
        else
        {
            if (wasVisible)
                _target.removeChangeListener(this);
        }           
        super.setVisible(isVisible);
    }
    
 
// Protected methods   
    /** Add the rows describing the attributes to the form. */
    protected void _addAttributeRows()
    {
        Iterator attributes = _target.attributeList().iterator();
        while (attributes.hasNext())
        {
            Attribute attribute = (Attribute)attributes.next();
            FormRow theRow = addRow();
            theRow.setCellValue("key", "parameter");
            theRow.setCellValue("name", attribute.getName());
            theRow.setCellValue("class", attribute.getClass().getName());
            if (attribute instanceof StringAttribute)
            {
                StringAttribute theString = (StringAttribute)attribute;
                theRow.setCellValue("value", theString.getExpression());
            }
            else if (attribute instanceof LibraryAttribute)
            {
                LibraryAttribute theLibrary = (LibraryAttribute)attribute;
                try { theRow.setCellValue("value", theLibrary.getLibrary()); }
                catch(Exception e) { }
            }
            else if (attribute instanceof Documentation)
            {
                Documentation theText = (Documentation)attribute;
                theRow.setCellValue("value", theText.getValue());
            }
            else if (attribute instanceof EditorIcon)
            {
                EditorIcon theIcon = (EditorIcon)attribute;
                theRow.setCellValue("value", theIcon.getFullName());
            }
            else if (attribute instanceof Location)
            {
                Location theLocation = (Location)attribute;
                double[] xy = theLocation.getLocation();
                theRow.setCellValue("value", xy[0] + "," + xy[1]);
            }
            else if (attribute instanceof Parameter)
            {
                Parameter theParameter = (Parameter)attribute;
                try
                {
                    Object theValue = theParameter.getToken();
                    if (theValue != null)
                        theRow.setCellValue("value", theValue.toString());
                }
                catch (IllegalActionException ex) {}
                theRow.setCellValue("type", theParameter.getType().toString());
            }
        }
    }

    /** Add the rows describing the ports of anEntity to the form. */
    protected void _addPortRows(Entity anEntity)
    {
        Iterator ports = anEntity.portList().iterator();
        while (ports.hasNext())
        {
            Port port = (Port)ports.next();
            FormRow theRow = addRow();
            theRow.setCellValue("key", "parameter");
            theRow.setCellValue("name", port.getName());
            theRow.setCellValue("class", port.getClass().getName());
            if (port instanceof IOPort)
            {
                IOPort ioPort = (IOPort)port;
                boolean isInput = ioPort.isInput();
                boolean isOutput = ioPort.isOutput();
                boolean isMultiport = ioPort.isMultiport();
                String portKey;
                if (isInput)
                {
                    if (isOutput) portKey = isMultiport ? "inout[]" : "inout";
                    else portKey = isMultiport ? "in[]" : "in";
                }
                else
                {
                    if (isOutput) portKey = isMultiport ? "out[]" : "out";
                    else portKey = isMultiport ? "port[]" : "port";
                }
                theRow.setCellValue("key", portKey);
            }
            if (port instanceof TypedIOPort)
            {
                TypedIOPort thePort = (TypedIOPort)port;
                theRow.setCellValue("type", thePort.getType().toString());
            }
        }
    }

    /** Create the column model - the list of editable columns.*/
    protected static FormColumnModel _createColumnModel()
    {
        FormKeyModel keyModel = new FormKeyModel();
        keyModel.add(new PortKey("in", true, false, false));
        keyModel.add(new PortKey("out", false, true, false));
        keyModel.add(new PortKey("inout", true, true, false));
        keyModel.add(new PortKey("port", false, false, false));
        keyModel.add(new PortKey("in[]", true, false, true));
        keyModel.add(new PortKey("out[]", false, true, true));
        keyModel.add(new PortKey("inout[]", true, true, true));
        keyModel.add(new PortKey("port[]", false, false, true));
        keyModel.add(new ParameterKey("parameter"));
        FormColumnModel columnModel = new FormColumnModel();
        columnModel.addColumn(new ControlCell());
        columnModel.addColumn(new KeyCell(keyModel, "in"));
        columnModel.addColumn(new NameCell(""));
        FormCell valueCell = new StringCell("value", null);
        valueCell.setEnabled(false);
        valueCell.setSettable(false);
        columnModel.addColumn(valueCell);
        FormCell typeCell = new StringCell("type", null);
        typeCell.setEnabled(false);
        typeCell.setSettable(false);
        columnModel.addColumn(typeCell);
        FormCell classCell = new StringCell("class", "ptolemy.actor.TypedIOPort");
        classCell.setEnabled(false);
        classCell.setSettable(false);
        columnModel.addColumn(classCell);
        return columnModel;
    }

    /** The execute call-back is intercepted to dispatch momlText as a MoMLChangeRequest. */
    protected void _executeMoml(String momlText)
    {
        MoMLChangeRequest changeRequest = new MoMLChangeRequest(this, _target, momlText, null);
        System.out.println("Executing ChangeRequest for " + _target + "\n" + changeRequest.getDescription());
        _target.requestChange(changeRequest);
    }

    /** Transfer the context of the MoML model to the form. */
    protected void _setContents()
    {
        setTitle(_target.getFullName()  + " of " + _target);
        eraseRows();
        if (_target instanceof Entity)
            _addPortRows((Entity)_target);
        _addAttributeRows();
        cancelChanges();
    }
// Private data
    /** The target of any edits. */
    private final NamedObj _target;
};
