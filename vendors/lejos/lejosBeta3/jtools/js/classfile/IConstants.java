package js.classfile;

public interface IConstants
{
  public static final boolean DEBUG_READ = Boolean.getBoolean ("trace.cfread");

  public static final int T_REFERENCE = 0;
  public static final int T_STACKFRAME = 1;
  public static final int T_BOOLEAN = 4;
  public static final int T_CHAR = 5;
  public static final int T_FLOAT = 6;
  public static final int T_DOUBLE = 7;
  public static final int T_BYTE = 8;
  public static final int T_SHORT = 9;
  public static final int T_INT = 10;
  public static final int T_LONG = 11;
}
