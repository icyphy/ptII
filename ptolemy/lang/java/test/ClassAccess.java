package ptolemy.lang.java.test;

class ClassAccess {
  public ClassAccess() {
    Class c;
    c = int.class;
    c = ClassAccess.class;
    c = ClassAccess[].class;
    c = char[].class;
    c = ptolemy.lang.ApplicationUtility.class;
    int a = c.hashCode();
    boolean b = ClassAccess[].class.isArray();
    
  }

}