package ptolemy.lang;

public abstract class Decl extends PropertyMap {

  protected Decl(String name, int category0) {
    _name = name;
    category = category0;
  }

  public final boolean matches(String name, int mask) {
    if ((category & mask) != 0) {
       return (name.equals(ANY_NAME) || name.equals(_name));
    }
    return false;
  }

  public final String getName() { return _name; }

  public final void setName(String name) { _name = name; }

  public final int category;

  protected String _name;

  public static final int CG_ANY = 0xFFFFFFFF; // Any category
  public static final String ANY_NAME = "*";
}
