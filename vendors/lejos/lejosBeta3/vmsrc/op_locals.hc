/**
 * This is included inside a switch statement.
 */

case OP_ILOAD:
case OP_FLOAD:
  push_word (get_local_word(*pc++));
  goto LABEL_ENGINELOOP;
case OP_ALOAD:
  // Arguments: 1
  // Stack: +1
  push_ref (get_local_ref(*pc++));
  goto LABEL_ENGINELOOP;
case OP_ILOAD_0:
case OP_ILOAD_1:
case OP_ILOAD_2:
case OP_ILOAD_3:
  // Arguments: 0
  // Stack: +1

  push_word (get_local_word(*(pc-1)-OP_ILOAD_0));
  goto LABEL_ENGINELOOP;
case OP_FLOAD_0:
case OP_FLOAD_1:
case OP_FLOAD_2:
case OP_FLOAD_3:
  // Arguments: 0
  // Stack: +1
  push_word (get_local_word(*(pc-1)-OP_FLOAD_0));
  goto LABEL_ENGINELOOP;
case OP_ALOAD_0:
case OP_ALOAD_1:
case OP_ALOAD_2:
case OP_ALOAD_3:
  // Arguments: 0
  // Stack: +1

  //printf ("### aload_x: %d\n", (int) get_local_word(*(pc-1)-OP_ILOAD_0));

  push_ref (get_local_ref(*(pc-1)-OP_ALOAD_0));
  goto LABEL_ENGINELOOP;
case OP_LLOAD:
case OP_DLOAD:
  // Arguments: 1
  // Stack: +2
  push_word (get_local_word(pc[0]));
  push_word (get_local_word(pc[0]+1));
  pc++;
  goto LABEL_ENGINELOOP;
case OP_LLOAD_0:
case OP_LLOAD_1:
case OP_LLOAD_2:
case OP_LLOAD_3:
  // Arguments: 0
  // Stack: +2
  tempByte = *(pc-1) - OP_LLOAD_0;
  goto LABEL_DLOAD_COMPLETE; // below
  //push_word (get_local_word(tempByte++));
  //push_word (get_local_word(tempByte));
  //goto LABEL_ENGINELOOP;
case OP_DLOAD_0:
case OP_DLOAD_1:
case OP_DLOAD_2:
case OP_DLOAD_3:
  // Arguments: 0
  // Stack: +2
  tempByte = *(pc-1) - OP_DLOAD_0;
 LABEL_DLOAD_COMPLETE:
  push_word (get_local_word(tempByte++));
  push_word (get_local_word(tempByte));
  goto LABEL_ENGINELOOP;
case OP_ISTORE:
case OP_FSTORE:
  // Arguments: 1
  // Stack: -1
  set_local_word(*pc++, pop_word());
  goto LABEL_ENGINELOOP;  
case OP_ASTORE:
  // Arguments: 1
  // Stack: -1

  set_local_ref(*pc++, pop_word());
  goto LABEL_ENGINELOOP;
case OP_ISTORE_0:
case OP_ISTORE_1:
case OP_ISTORE_2:
case OP_ISTORE_3:
  // Arguments: 0
  // Stack: -1
  set_local_word(*(pc-1)-OP_ISTORE_0, pop_word());
  goto LABEL_ENGINELOOP;
case OP_FSTORE_0:
case OP_FSTORE_1:
case OP_FSTORE_2:
case OP_FSTORE_3:
  // Arguments: 0
  // Stack: -1
  set_local_word(*(pc-1)-OP_FSTORE_0, pop_word());
  goto LABEL_ENGINELOOP;
case OP_ASTORE_0:
case OP_ASTORE_1:
case OP_ASTORE_2:
case OP_ASTORE_3:
  // Arguments: 0
  // Stack: -1

  //printf ("### astore_x: %d\n", (int) get_top_word());

  set_local_ref(*(pc-1)-OP_ASTORE_0, pop_word());
  goto LABEL_ENGINELOOP;
case OP_LSTORE:
case OP_DSTORE:
  // Arguments: 1
  // Stack: -1
  set_local_word (pc[0]+1, pop_word());
  set_local_word (pc[0], pop_word());
  pc++;
  goto LABEL_ENGINELOOP;
case OP_LSTORE_0:
case OP_LSTORE_1:
case OP_LSTORE_2:
case OP_LSTORE_3:
  tempByte = *(pc-1) - OP_LSTORE_0;
  goto LABEL_DSTORE_END;
  //set_local_word (tempByte+1, pop_word());
  //set_local_word (tempByte, pop_word());
  //goto LABEL_ENGINELOOP;
case OP_DSTORE_0:
case OP_DSTORE_1:
case OP_DSTORE_2:
case OP_DSTORE_3:
  tempByte = *(pc-1) - OP_DSTORE_0;
 LABEL_DSTORE_END:
  set_local_word (tempByte+1, pop_word());
  set_local_word (tempByte, pop_word());
  goto LABEL_ENGINELOOP;
case OP_IINC:
  // Arguments: 2
  // Stack: +0
  inc_local_word (pc[0], byte2jint(pc[1]));
  pc += 2; 
  goto LABEL_ENGINELOOP;

// Notes:
// - OP_WIDE is unexpected in TinyVM and CompactVM.

/*end*/







