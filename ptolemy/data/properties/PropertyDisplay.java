/**
 * 
 */
package ptolemy.data.properties;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 @author Man-Kit Leung, Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.0.4
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class PropertyDisplay extends Attribute {
    /**
     * @param container The given container.
     * @param name The given name
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    public PropertyDisplay(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        JPanel fullSolverPanel = new JPanel();
        fullSolverPanel.setLayout(new BoxLayout(fullSolverPanel,
                BoxLayout.Y_AXIS));
        fullSolverPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Full Solution"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        //        _runFullSolverButton.addActionListener(this);

        //        Parameter cont = new Parameter();

        //        displayProperties = new CheckBoxStyle(cont, "Check Box");
        //        PtolemyQuery query = new PtolemyQuery(displayProperties);
        //        query.addCheckBox("Test", "testlabel", true);
        //        displayProperties.addEntry(query);

        trainingMode = new Parameter(this, "trainingMode");
        trainingMode.setTypeEquals(BaseType.BOOLEAN);
        trainingMode.setExpression("true");

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"115\" height=\"40\" "
                + "style=\"fill:grey\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\nResolve Property.</text></svg>");

        //        new PropertyDisplayGUIFactory(
        //                this, "PropertyDisplayGUIFactory");
    }

    /** The file parameter for the lattice description file.
     */

    public CheckBoxStyle displayProperties;

    public Parameter trainingMode;

}
