package ptolemy.lang;

public class MutableBoolean {

  public MutableBoolean() {
    this(Boolean.FALSE);
  }

  public MutableBoolean(boolean b) {
    this(new Boolean(b));
  }

  public MutableBoolean(Boolean b) {
    _bval = b;
  }

  public boolean getValue() {
    return _bval.booleanValue();
  }

  public void setValue(boolean b) {
    _bval = new Boolean(b);
  }

  public String toString() {
    return _bval.toString();
  }

  protected Boolean _bval;
}