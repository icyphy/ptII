package ptolemy.lang;



//########## SEMANTIC VALUES ##########
public class parserval implements Cloneable
{
public int ival;
public double dval;
public String sval;
public Object obj;
public parserval(int val)
{
  ival=val;
}
public parserval(double val)
{
  dval=val;
}
public parserval(String val)
{
  sval=val;
}
public parserval(Object val)
{
  obj=val;
}

public Object clone() throws CloneNotSupportedException
{
  return super.clone();
}
}//end class
