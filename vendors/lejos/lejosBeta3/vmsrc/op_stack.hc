/**
 * This is included inside a switch statement.
 */

case OP_BIPUSH:
  // Stack size: +1
  // Arguments: 1
  // TBD: check negatives
  push_word ((JBYTE) (*pc++));
  goto LABEL_ENGINELOOP;
case OP_SIPUSH:
  // Stack size: +1
  // Arguments: 2
  #if 0
  printf ("  OP_SIPUSH args: %d, %d (%d)\n", (int) pc[0], (int) pc[1], (int) pc[2]);
  #endif
  push_word ((JSHORT) (((TWOBYTES) pc[0] << 8) | pc[1]));
  pc += 2;
  goto LABEL_ENGINELOOP;
case OP_LDC:
  // Stack size: +1
  // Arguments: 1
  tempConstRec = get_constant_record (*pc++);
  switch (tempConstRec->constantType)
  {
    case T_REFERENCE:

      // T_REFERENCE is actually String

      tempWordPtr = (void *) create_string (tempConstRec, pc - 2);
      if (tempWordPtr == JNULL)
        goto LABEL_ENGINELOOP;
      push_ref (ptr2word (tempWordPtr));
      break;
    case T_INT:
    case T_FLOAT:
      make_word (get_constant_ptr(tempConstRec), 4, &tempStackWord);
      //printf ("### LDC offset=%d 0x%X (%f)\n", (int) tempConstRec->offset, (int) tempStackWord, word2jfloat(tempStackWord));
      push_word (tempStackWord);
      break;
    #ifdef VERIFY
    default:
      assert (false, INTERPRETER0);
    #endif
  }
  goto LABEL_ENGINELOOP;

case OP_LDC2_W:
  // Stack size: +2
  // Arguments: 2
  tempConstRec = get_constant_record (((TWOBYTES) pc[0] << 8) | pc[1]);

  #ifdef VERIFY
  assert (tempConstRec->constantSize == 8, INTERPRETER6);
  #endif VERIFY

  tempBytePtr = get_constant_ptr (tempConstRec);
  make_word (tempBytePtr, 4, &tempStackWord);
  push_word (tempStackWord);
  make_word (tempBytePtr + 4, 4, &tempStackWord);
  push_word (tempStackWord);

  pc += 2;
  goto LABEL_ENGINELOOP;

case OP_ACONST_NULL:
  // Stack size: +1
  // Arguments: 0
  push_ref (JNULL);
  goto LABEL_ENGINELOOP;

case OP_ICONST_M1:
case OP_ICONST_0:
case OP_ICONST_1:
case OP_ICONST_2:
case OP_ICONST_3:
case OP_ICONST_4:
case OP_ICONST_5:
  // Stack size: +1
  // Arguments: 0
  push_word ((JINT) (*(pc-1) - OP_ICONST_0));
  goto LABEL_ENGINELOOP;
case OP_LCONST_0:
case OP_LCONST_1:
  // Stack size: +2
  // Arguments: 0
  push_word (0);
  push_word (*(pc-1) - OP_LCONST_0);
  goto LABEL_ENGINELOOP;
case OP_DCONST_0:
  push_word (0);
  // Fall through!
case OP_FCONST_0:
  push_word (0);
  goto LABEL_ENGINELOOP;  
case OP_POP2:
  // Stack size: -2
  // Arguments: 0
  just_pop_word();
  // Fall through
case OP_POP:
  // Stack size: -1
  // Arguments: 0
  just_pop_word();
  goto LABEL_ENGINELOOP;
case OP_DUP:
  // Stack size: +1
  // Arguments: 0
  dup();
  goto LABEL_ENGINELOOP;
case OP_DUP2:
  // Stack size: +2
  // Arguments: 0
  dup2();
  goto LABEL_ENGINELOOP;
case OP_DUP_X1:
  // Stack size: +1
  // Arguments: 0
  dup_x1();
  goto LABEL_ENGINELOOP;
case OP_DUP2_X1:
  // Stack size: +2
  // Arguments: 0
  dup2_x1();
  goto LABEL_ENGINELOOP;
case OP_DUP_X2:
  // Stack size: +1
  // Arguments: 0
  dup_x2();
  goto LABEL_ENGINELOOP;
case OP_DUP2_X2:
  // Stack size: +2
  // Arguments: 0
  dup2_x2();
  goto LABEL_ENGINELOOP;
case OP_SWAP:
  swap(); 
  goto LABEL_ENGINELOOP;

#if FP_ARITHMETIC
  
case OP_FCONST_1:
  push_word (jfloat2word((JFLOAT) 1.0));
  goto LABEL_ENGINELOOP;
case OP_FCONST_2:
  push_word (jfloat2word((JFLOAT) 2.0));
  goto LABEL_ENGINELOOP;
case OP_DCONST_1:
  // Stack size: +2
  // Arguments: 0
  push_word (0);
  push_word (jfloat2word((JFLOAT) 1.0));
  goto LABEL_ENGINELOOP;

#endif FP_ARITHMETIC

  
// Notes:
// - LDC_W should not occur in TinyVM or CompactVM.
// - Arguments of LDC and LDC2_W are postprocessed.
// - NOP is in op_skip.hc.

/*end*/







