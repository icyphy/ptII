package js.classfile;

import java.util.*;

public class JClassName
implements IConstants
{
  public static String getQualifiedName (String aClassName)
  {
    return aClassName.replace ('.', '/');
  }

  public static String getMethodDescriptor (String aParamDescriptors[],
                       String aRetTypeDescriptor)
  {
    StringBuffer pStr = new StringBuffer();
    pStr.append ("(");
    for (int pIndex = 0; pIndex < aParamDescriptors.length; pIndex++)
      pStr.append (aParamDescriptors[pIndex]);
    pStr.append (")");
    pStr.append (aRetTypeDescriptor);
    return pStr.toString();
  }

  public static String getMethodDescriptor (Class aParamTypes[],
                       Class aRetType)
  throws Exception
  {
    //    System.out.print ("# ParamTyps = ");
    //for (int i = 0; i < aParamTypes.length; i++)
    //  System.out.print (aParamTypes[i].getName() + ";");
    //System.out.println ("");
    String pDesc = 
      getMethodDescriptor (getDescriptorForClasses (aParamTypes),
           getDescriptorForClass (aRetType));
    //System.out.println ("# Descriptor = " + pDesc);
    return pDesc;
  }

  public static String getDescriptorForClass (String aClassName)
  {
    return "L" + aClassName.replace('.', '/') + ";";
  }

  public static String[] getDescriptorForClasses (Class aClasses[])
  throws Exception
  {
    String pParTypes[] = new String[aClasses.length];
    for (int pIndex = 0; pIndex < aClasses.length; pIndex++)
      pParTypes[pIndex] = getDescriptorForClass (aClasses[pIndex]);
    return pParTypes;
  }

  public static String getDescriptorForClass (Class aClass)
  throws Exception
  {
    if (aClass.isPrimitive())
    {
      if (aClass == byte.class)
        return getDescriptorForByte();
      else if (aClass == char.class)
        return getDescriptorForChar();
      else if (aClass == double.class)
        return getDescriptorForDouble();
      else if (aClass == float.class)
        return getDescriptorForFloat();
      else if (aClass == int.class)
        return getDescriptorForInt();
      else if (aClass == long.class)
        return getDescriptorForLong();
      else if (aClass == short.class)
        return getDescriptorForShort();
      else if (aClass == boolean.class)
        return getDescriptorForBoolean();
      else if (aClass == void.class)
        return getDescriptorForVoid();
      else
        throw new RuntimeException ("Unknown primitive class type: " +
                  aClass.getName());
    }
    else if (aClass.isArray())
    {
      return getDescriptorForArray (getDescriptorForClass (
             aClass.getComponentType()));     
    }
    else
      return getDescriptorForClass (aClass.getName());
  }

  public static String getDescriptorForArray (String aDescriptor)
  {
    return "[" + aDescriptor;
  }

  public static String getDescriptorForVoid()
  {
    return "V";
  }

  public static String getDescriptorForByte()
  {
    return "B";
  }

  public static String getDescriptorForChar()
  {
    return "C";
  }

  public static String getDescriptorForDouble()
  {
    return "D";
  }

  public static String getDescriptorForFloat()
  {
    return "F";
  }

  public static String getDescriptorForInt()
  {
    return "I";
  }

  public static String getDescriptorForLong()
  {
    return "J";
  }

  public static String getDescriptorForShort()
  {
    return "S";
  }

  public static String getDescriptorForBoolean()
  {
    return "Z";
  }

  public static String[] parseMethodParameters (JCPE_Utf8 aMethodDesc)
  {
    String pDesc = aMethodDesc.toString();
    int pIdx = pDesc.indexOf (')');
    if (pIdx == -1 || pDesc.charAt(0) != '(')
      throw new Error (pDesc);
    return parseParameters (pDesc.substring (1, pIdx));
  }

  private static String[] parseParameters (String aParams)
  {
    String pDesc = aParams;
    int pLen = pDesc.length();
    Vector pVec = new Vector();
    String pPrefix = "";
    for (int i = 0; i < pLen;)
    {
      char pChar = pDesc.charAt (i);
      switch (pChar)
      {
        case 'V':
        case 'B':
        case 'C':
        case 'D':
        case 'F':
        case 'I':
        case 'J':
        case 'S':
        case 'Z':
          pVec.addElement (pPrefix + pChar);
          pPrefix = "";
          break;
        case 'L':
          int pIdx = pDesc.indexOf (';', i+1);
          if (pIdx == -1)
            throw new Error (pDesc);
          pVec.addElement (pPrefix + pDesc.substring (i, pIdx+1));
          pPrefix = "";
          i = pIdx;
        case '[':
          pPrefix += pChar;
          break;
        default:
          throw new Error (pDesc + ": " + pChar);
      }
      i++;
    }
    String[] pArgs = new String[pVec.size()];
    pVec.copyInto (pArgs);
    return pArgs;
  }

  public static int[] getTypeAndDimensions (String aMultiArrayDesc)
  {
    int i = 0;
    while (aMultiArrayDesc.charAt (i) == '[')
      i++;
    return new int[] { 
      descriptorToType (aMultiArrayDesc.substring(i)),
      i 
    };
  }

  public static int descriptorToType (String aDesc)
  {
    switch (aDesc.charAt (0))
    {
      case 'B':
        return T_BYTE;
      case 'C':
        return T_CHAR;
      case 'D':
        return T_DOUBLE;
      case 'F':
        return T_FLOAT;
      case 'I':
        return T_INT;
      case 'J':
        return T_LONG;
      case 'S':
        return T_SHORT;
      case 'Z':
        return T_BOOLEAN;
      case 'L':
      case '[':
        return T_REFERENCE;
      default:
        throw new Error ("Bug IFR-2: " + aDesc);  
    }
  }

  public static int getTypeSize (int aType)
  {
    switch (aType)
    {
      case T_BYTE:
      case T_BOOLEAN:
        return 1;
      case T_SHORT:
      case T_CHAR:
        return 2;
      case T_INT:
      case T_REFERENCE:
      case T_FLOAT:
        return 4;
      case T_LONG:
      case T_DOUBLE:
        return 8;
      default:
        throw new Error ("Bug IFR-3: " + aType);
    }
  }


}
  






