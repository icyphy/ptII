

//########## SEMANTIC VALUES ##########
package ptolemy.lang.java;

public class JavaParserval implements Cloneable
{
public int ival;
public double dval;
public String sval;
public Object obj;
public JavaParserval() {}
public JavaParserval(int val)
{
  ival=val;
}
public JavaParserval(double val)
{
  dval=val;
}
public JavaParserval(String val)
{
  sval=val;
}
public JavaParserval(Object val)
{
  obj=val;
}
public Object clone() throws CloneNotSupportedException
{
  return super.clone();
}
}//end class
