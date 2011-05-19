package ptolemy.apps.ptalon.model;

import ptolemy.actor.TypedIOPort;

public class TransparentRelation {
    public TransparentRelation(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    public boolean hasInitialPortBeenSet() {
        return _port != null;
    }

    public TypedIOPort getPort() {
        return _port;
    }

    public void setInitialPort(TypedIOPort port) {
        _port = port;
    }

    public boolean equals(Object obj) {
        if (obj instanceof TransparentRelation) {
            return ((TransparentRelation) obj).getName().equals(_name);
        }
        return super.equals(obj);
    }

    public int hashCode() {
        return _name.hashCode();
    }

    private String _name;

    private TypedIOPort _port = null;
}
