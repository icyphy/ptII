package ptolemy.actor.gt.controller;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Initializable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class ModelAttribute extends Attribute implements Initializable {

    public ModelAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    public void addInitializable(Initializable initializable) {
        if (_initializables == null) {
            _initializables = new LinkedList<Initializable>();
        }
        _initializables.add(initializable);
    }

    public CompositeEntity getModel() {
        return _model;
    }

    public void initialize() throws IllegalActionException {
        // Invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.initialize();
            }
        }
    }

    public void preinitialize() throws IllegalActionException {
        _model = null;
        // Invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.preinitialize();
            }
        }
    }

    public void removeInitializable(Initializable initializable) {
        if (_initializables != null) {
            _initializables.remove(initializable);
            if (_initializables.size() == 0) {
                _initializables = null;
            }
        }
    }

    public void setContainer(NamedObj container) throws IllegalActionException,
    NameDuplicationException {
        NamedObj oldContainer = getContainer();
        if (oldContainer instanceof Initializable) {
            ((Initializable) oldContainer).removeInitializable(this);
        }
        super.setContainer(container);
        if (container instanceof Initializable) {
            ((Initializable) container).addInitializable(this);
        }
    }

    public void setModel(CompositeEntity model) {
        _model = model;
    }

    public void wrapup() throws IllegalActionException {
        // Invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.wrapup();
            }
        }
        _model = null;
    }

    /** List of objects whose (pre)initialize() and wrapup() methods should be
     *  slaved to these.
     */
    private transient List<Initializable> _initializables;

    private CompositeEntity _model;
}
