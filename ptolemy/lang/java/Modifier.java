package ptolemy.lang.java;

public class Modifier implements JavaStaticSemanticConstants {

  public static final String toString(final int modifier) {
    StringBuffer modString = new StringBuffer();

    if (modifier == NO_MOD) return "";

    if ((modifier & PUBLIC_MOD) != 0)
       modString.append("public ");

    if ((modifier & PROTECTED_MOD) != 0)
       modString.append("protected ");

    if ((modifier & PRIVATE_MOD) != 0)
       modString.append("private ");

    if ((modifier & ABSTRACT_MOD) != 0)
       modString.append("abstract ");

    if ((modifier & FINAL_MOD) != 0)
       modString.append("final ");

    if ((modifier & NATIVE_MOD) != 0)
       modString.append("native ");

    if ((modifier & SYNCHRONIZED_MOD) != 0)
       modString.append("synchronized ");

    if ((modifier & TRANSIENT_MOD) != 0)
       modString.append("transient ");

    if ((modifier & VOLATILE_MOD) != 0)
       modString.append("volatile ");

    if ((modifier & STATIC_MOD) != 0)
       modString.append("static ");

    if ((modifier & STRICTFP_MOD) != 0)
       modString.append("strictfp ");

    return modString.toString();
  }

  public static final void checkClassModifiers(final int modifiers) {
    if ((modifiers &
        ~(PUBLIC_MOD | PROTECTED_MOD | PRIVATE_MOD | FINAL_MOD |
          ABSTRACT_MOD | STATIC_MOD | STRICTFP_MOD)) != 0) {
       throw new RuntimeException("Illegal class modifier: " +
        toString(modifiers));
    }
  }

  public static final void checkInterfaceModifiers(final int modifiers) {
    if ((modifiers &
        ~(PUBLIC_MOD | PROTECTED_MOD | PRIVATE_MOD | FINAL_MOD |
          ABSTRACT_MOD | STATIC_MOD | STRICTFP_MOD)) != 0) {
       throw new RuntimeException("Illegal interface modifier: " +
        toString(modifiers));
    }
  }


  public static final void checkConstructorModifiers(final int modifiers) {
    if ((modifiers &
        ~(PUBLIC_MOD | PROTECTED_MOD | PRIVATE_MOD)) != 0) {
       throw new RuntimeException("Illegal constructor modifier: " +
        toString(modifiers));

    }
  }

  public static final void checkConstantFieldModifiers(final int modifiers) {
    if ((modifiers &
        ~(PUBLIC_MOD | STATIC_MOD | FINAL_MOD)) != 0) {
       throw new RuntimeException("Illegal constant field modifier: " +
        toString(modifiers));
    }
  }

  public static final void checkFieldModifiers(final int modifiers) {
    if ((modifiers &
        ~(PUBLIC_MOD | PROTECTED_MOD | PRIVATE_MOD | STATIC_MOD |
          FINAL_MOD | TRANSIENT_MOD | VOLATILE_MOD)) != 0) {
       throw new RuntimeException("Illegal field modifier : " +
        toString(modifiers));
    }
  }

  public static final void checkLocalVariableModifiers(final int modifiers) {
    if ((modifiers & ~(FINAL_MOD)) != 0) {
       throw new RuntimeException("Illegal local variable modifier : " +
        toString(modifiers));
    }
  }


  public static final void checkMethodModifiers(final int modifiers) {
    if ((modifiers &
        ~(PUBLIC_MOD | PROTECTED_MOD | PRIVATE_MOD | STATIC_MOD | FINAL_MOD |
          ABSTRACT_MOD | NATIVE_MOD | SYNCHRONIZED_MOD | STRICTFP_MOD)) != 0) {
       throw new RuntimeException("Illegal method modifier: " +
        toString(modifiers));
    }
  }

  public static final void checkMethodSignatureModifiers(final int modifiers) {
    if ((modifiers &
        ~(PUBLIC_MOD | ABSTRACT_MOD)) != 0) {
       throw new RuntimeException("Illegal method signature modifier: " +
        toString(modifiers));
    }
  }

  public static final void checkParameterModifiers(final int modifiers) {
    if ((modifiers & ~(FINAL_MOD)) != 0) {
       throw new RuntimeException("Illegal parameter modifier : " +
        toString(modifiers));
    }
  }
}
