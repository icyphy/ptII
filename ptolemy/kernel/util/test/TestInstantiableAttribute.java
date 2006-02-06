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

public class TestInstantiableAttribute extends Attribute implements
        Instantiable {

    public TestInstantiableAttribute() {
        super();
    }

    public TestInstantiableAttribute(Workspace workspace) {
        super(workspace);
    }

    public TestInstantiableAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
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
        NamedObj container = getContainer();
        if (container instanceof Instantiable) {
            return (Instantiable) container;
        }
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
