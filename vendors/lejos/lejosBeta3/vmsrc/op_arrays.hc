/**
 * This is included inside a switch statement.
 */

case OP_NEWARRAY:
  // Stack size: unchanged
  // Arguments: 1
  set_top_ref (obj2ref(new_primitive_array (*pc++, get_top_word())));
  // Exceptions are taken care of
  goto LABEL_ENGINELOOP;
case OP_MULTIANEWARRAY:
  // Stack size: -N + 1
  // Arguments: 3
  tempByte = pc[2] - 1;
  tempBytePtr = (byte *) new_multi_array (pc[0], pc[1], pc[2], get_stack_ptr() - tempByte);
  pop_words (tempByte);
  set_top_ref (ptr2ref (tempBytePtr));
  pc += 3;
  goto LABEL_ENGINELOOP;
case OP_AALOAD:
  // Stack size: -2 + 1
  // Arguments: 0
  if (!array_load_helper())
    goto LABEL_ENGINELOOP;
  // tempBytePtr and tempInt set by call above
  set_top_ref (word_array(tempBytePtr)[tempInt]);
  goto LABEL_ENGINELOOP;
case OP_IALOAD:
case OP_FALOAD:
  // Stack size: -2 + 1
  // Arguments: 0
  if (!array_load_helper())
    goto LABEL_ENGINELOOP;
  set_top_word (word_array(tempBytePtr)[tempInt]);
  goto LABEL_ENGINELOOP;
case OP_CALOAD:
case OP_SALOAD:
  if (!array_load_helper())
    goto LABEL_ENGINELOOP;
  set_top_word (jshort_array(tempBytePtr)[tempInt]);
  goto LABEL_ENGINELOOP;
case OP_BALOAD:
  if (!array_load_helper())
    goto LABEL_ENGINELOOP;
  set_top_word (jbyte_array(tempBytePtr)[tempInt]);
  goto LABEL_ENGINELOOP;
case OP_LALOAD:
case OP_DALOAD:
  // Stack size: -2 + 2
  // Arguments: 0
  if (!array_load_helper())
    goto LABEL_ENGINELOOP;
  tempInt *= 2;
  set_top_word (word_array(tempBytePtr)[tempInt++]);
  push_word (word_array(tempBytePtr)[tempInt]);
  goto LABEL_ENGINELOOP;
case OP_AASTORE:
  // Stack size: -3
  tempStackWord = pop_ref();
  if (!array_store_helper())
    goto LABEL_ENGINELOOP;
  ref_array(tempBytePtr)[tempInt] = tempStackWord;
  goto LABEL_ENGINELOOP;
case OP_IASTORE:
case OP_FASTORE:
  // Stack size: -3
  tempStackWord = pop_word();
  if (!array_store_helper())
    goto LABEL_ENGINELOOP;
  jint_array(tempBytePtr)[tempInt] = tempStackWord;
  goto LABEL_ENGINELOOP;
case OP_CASTORE:
case OP_SASTORE:
  // Stack size: -3
  tempStackWord = pop_word();
  if (!array_store_helper())
    goto LABEL_ENGINELOOP;
  jshort_array(tempBytePtr)[tempInt] = tempStackWord;
  goto LABEL_ENGINELOOP;
case OP_BASTORE:
  // Stack size: -3
  tempStackWord = pop_word();
  if (!array_store_helper())
    goto LABEL_ENGINELOOP;
  jbyte_array(tempBytePtr)[tempInt] = tempStackWord;
  goto LABEL_ENGINELOOP;
case OP_DASTORE:
case OP_LASTORE:
  // Stack size: -4
  {
    STACKWORD tempStackWord2;

    tempStackWord2 = pop_word();
    tempStackWord = pop_word();
    if (!array_store_helper())
      goto LABEL_ENGINELOOP;
    tempInt *= 2;
    jint_array(tempBytePtr)[tempInt++] = tempStackWord;
    jint_array(tempBytePtr)[tempInt] = tempStackWord2;
  }
  goto LABEL_ENGINELOOP;
case OP_ARRAYLENGTH:
  // Stack size: -1 + 1
  // Arguments: 0
  {
    REFERENCE tempRef;

    tempRef = get_top_ref();
    
    //printf ("ARRAYLENGTH for %d\n", (int) tempRef); 
    
    if (tempRef == JNULL)
      throw_exception (nullPointerException);
    else     
      set_top_word (get_array_length (word2obj (tempRef)));
  }
  goto LABEL_ENGINELOOP;


// Notes:
// * OP_ANEWARRAY is changed to OP_NEWARRAY of data type 0, plus a NOP.

/*end*/







