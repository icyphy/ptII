
// Basic object creation and field access

import tinyvm.rcx.*;

public class Test04
{
  static Test04 iObj2;
  static int iStatic;
  byte iByteField;
  int iIntField;
  byte iByteField2;
  long iLongField;
  short iShortField;

  public static void main (String[] aArg)
  {
    Test04 pObj = new Test04();
    iObj2 = new Test04();
    Test04.iStatic = 4490; 
    pObj.iByteField = 100; 
    pObj.iIntField = 1000000; 
    pObj.iByteField2 = 80; 
    pObj.iShortField = 4000;
    iObj2.iByteField = 125;
    iObj2.iShortField = 4175;
    LCD.showNumber (pObj.iIntField + pObj.iByteField - 999999);
    int k = pObj.iStatic;   
    LCD.showNumber (k);
    Test04.iStatic = 4491;
    int lvar = pObj.iShortField + pObj.iByteField2 + pObj.iIntField + 
               pObj.iByteField;   
    LCD.showNumber (lvar - 999999);
    LCD.showNumber (iObj2.iByteField + iObj2.iShortField);
    LCD.showNumber (iObj2.iShortField + iObj2.iByteField2);
  }
}


