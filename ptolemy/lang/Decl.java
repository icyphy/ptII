package ptolemy.lang;

abstract class Decl extends PropertyMap {

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

  // These should be moved to a JavaDecl class
  public static final int CG_CLASS = 1;        // Type ClassDecl representing a class
  public static final int CG_INTERFACE = 2;	// Type ClassDecl representing an interface
  public static final int CG_FIELD = 4;		// Type FieldDecl
  public static final int CG_METHOD = 8;		// Type MethodDecl
  public static final int CG_CONSTRUCTOR = 16;	// Type MethodDecl
  public static final int CG_LOCALVAR = 32;	// Type LocalVarDecl
  public static final int CG_FORMAL = 64;		// Type FormalParameterDecl
  public static final int CG_PACKAGE = 128;	// Type PackageDecl
  public static final int CG_STMTLABEL = 256;	// Type StmtLblDecl
  public static final int CG_PRIMITIVE = 512;  // Type PrimitiveDecl
  public static final int CG_ANY = 0xFFFFFFFF; // Any type

  public static final int CG_USERTYPE = CG_CLASS | CG_INTERFACE;

  public static final String ANY_NAME = "*";
}
