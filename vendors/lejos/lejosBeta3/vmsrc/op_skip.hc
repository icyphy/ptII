/**
 * This is included inside a switch statement.
 */

case OP_NOP:
  goto LABEL_ENGINELOOP;

#if !FP_ARITHMETIC

default:
  throw_exception (noSuchMethodError);
  goto LABEL_ENGINELOOP;  

#endif FP_ARITHMETIC
  
/*end*/
