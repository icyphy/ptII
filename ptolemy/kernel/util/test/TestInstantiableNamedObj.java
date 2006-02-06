package ptolemy.kernel.util.test;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Instantiable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

public class TestInstantiableNamedObj extends NamedObj implements Instantiable {

    public TestInstantiableNamedObj() {
        super();
    }

    public TestInstantiableNamedObj(String name) throws IllegalActionException {
        super(name);
    }

    public TestInstantiableNamedObj(Workspace workspace) {
        super(workspace);
    }

    public TestInstantiableNamedObj(Workspace workspace, String name)
            throws IllegalActionException {
        super(workspace, name);
    }

    public List getChildren() {
        List results = new LinkedList();
        Iterator attributes = attributeList().iterator();
        while (attributes.hasNext()) {
            Attribute attribute = (Attribute) attributes.next();
            results.add(new WeakReference(attribute));
        }
        return results;
    }

    public Instantiable getParent() {
        return null;
    }

    public Instantiable instantiate(NamedObj container, String name)
            throws CloneNotSupportedException, IllegalActionException,
            NameDuplicationException {
        return null;
    }

    public boolean isClassDefinition() {
        return false;
    }

}
