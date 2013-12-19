package ptolemy.domains.metroII.kernel;

import java.util.List;

import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.domains.metroII.gui.MappingEditorGUIFactory;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class MappingEditor extends Attribute {

    public MappingEditor(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:11; font-family:SansSerif; fill:white\">"
                + "Double click to\nedit mapping.</text></svg>");

        new MappingEditorGUIFactory(this, "_mappingEditorGUIFactory");
    }

}
