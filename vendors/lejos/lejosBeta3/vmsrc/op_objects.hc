/**
 * This is included inside a switch statement.
 */

case OP_NEW:
  // Stack: +1
  // Arguments: 2
  // Hi byte unused
  tempBytePtr = (byte *) new_object_checked (pc[1], pc - 1);
  if (tempBytePtr != JNULL)
  { 
    #if 0
    trace (-1, (short) pc[1], 1);
    trace (-1, (short) tempBytePtr, 2);
    trace (-1, get_class_index((Object *) tempBytePtr), 3);
    #endif
    push_ref (ptr2ref(tempBytePtr));
    pc += 2;
  }
  goto LABEL_ENGINELOOP;
case OP_GETSTATIC:
case OP_PUTSTATIC:
  // Stack: +1 or +2 for GETSTATIC, -1 or -2 for PUTSTATIC
  {
    STATICFIELD fieldRecord;
    byte *fieldBase;
    byte fieldType;
    byte fieldSize;
    boolean wideWord;
    boolean isRef;

    #if DEBUG_FIELDS
    printf ("---  GET/PUTSTATIC --- (%d, %d)\n", (int) pc[0], (int) pc[1]);
    #endif

    if (dispatch_static_initializer (get_class_record (pc[0]), pc - 1))
      goto LABEL_ENGINELOOP;
    fieldRecord = ((STATICFIELD *) get_static_fields_base())[pc[1]];

    fieldType = (fieldRecord >> 12) & 0x0F;
    isRef = (fieldType == T_REFERENCE);
    fieldSize = typeSize[fieldType];
    wideWord = (fieldSize > 4);


    if (wideWord)
      fieldSize = 4;
    fieldBase = get_static_state_base() + get_static_field_offset (fieldRecord);

    #if DEBUG_FIELDS
    printf ("fieldSize  = %d\n", (int) fieldSize);
    printf ("fieldBase  = %d\n", (int) fieldBase);
    #endif

    if (*(pc-1) == OP_GETSTATIC)
    {
      make_word (fieldBase, fieldSize, &tempStackWord);
      push_word_or_ref (tempStackWord, isRef);
      if (wideWord)
      {
        make_word (fieldBase + 4, 4, &tempStackWord);
        push_word (tempStackWord);
      }
    }
    else
    {
      if (wideWord)
        store_word (fieldBase + 4, 4, pop_word());
      store_word (fieldBase, fieldSize, pop_word_or_ref (isRef));
    }
    pc += 2;
  }
  goto LABEL_ENGINELOOP;
case OP_GETFIELD:
  {
    byte *fieldBase;
    byte fieldType;
    byte fieldSize;
    boolean wideWord;

    tempStackWord = get_top_ref();
    if (tempStackWord == JNULL)
    {
      throw_exception (nullPointerException);
      goto LABEL_ENGINELOOP;
    }
    fieldType = get_pgfield_type(pc[0]);
    fieldSize = typeSize[fieldType];
    wideWord = (fieldSize > 4);
    if (wideWord)
      fieldSize = 4;
    fieldBase = ((byte *) word2ptr (tempStackWord)) + 
                get_pgfield_offset(pc[0], pc[1]);

    #ifdef DEBUG_FIELDS
    printf ("--- PUTFIELD ---\n");
    printf ("fieldType: %d\n", (int) fieldType);
    printf ("fieldSize: %d\n", (int) fieldSize);
    printf ("wideWord: %d\n", (int) wideWord);
    printf ("reference: %d\n", (int) tempStackWord);
    #endif

    make_word (fieldBase, fieldSize, &tempStackWord);
    //printf ("### get_field base=%d size=%d pushed=%d\n", (int) fieldBase, (int) fieldSize, (int) tempStackWord);
    set_top_word_or_ref (tempStackWord, (fieldType == T_REFERENCE));
    if (wideWord)
    {
      make_word (fieldBase + 4, 4, &tempStackWord);
      push_word (tempStackWord);
    }
    pc += 2;
  }
  goto LABEL_ENGINELOOP;
case OP_PUTFIELD:
  {
    byte *fieldBase;
    byte fieldType;
    byte fieldSize;
    boolean wideWord;

    fieldType = get_pgfield_type(pc[0]);
    fieldSize = typeSize[fieldType];
    wideWord = (fieldSize > 4);
    if (wideWord)
      fieldSize = 4;
    tempStackWord = get_ref_at (wideWord ? 2 : 1);

    #ifdef DEBUG_FIELDS
    printf ("--- PUTFIELD ---\n");
    printf ("fieldType: %d\n", (int) fieldType);
    printf ("fieldSize: %d\n", (int) fieldSize);
    printf ("wideWord: %d\n", (int) wideWord);
    printf ("reference: %d\n", (int) tempStackWord);
    #endif


    if (tempStackWord == JNULL)
    {
      throw_exception (nullPointerException);
      goto LABEL_ENGINELOOP;
    }
    fieldBase = ((byte *) word2ptr (tempStackWord)) +
                get_pgfield_offset (pc[0], pc[1]); 
    if (wideWord)
      store_word (fieldBase + 4, 4, pop_word());
    //printf ("### put_field base=%d size=%d stored=%d\n", (int) fieldBase, (int) fieldSize, (int) get_top_word());
    store_word (fieldBase, fieldSize, pop_word_or_ref (fieldType == T_REFERENCE));
    just_pop_ref();
    pc += 2;
  }
  goto LABEL_ENGINELOOP;
case OP_INSTANCEOF:
  // Stack: unchanged
  // Arguments: 2
  // Ignore hi byte
  set_top_word (instance_of (word2obj (get_top_ref()),  pc[1]));
  pc += 2;
  goto LABEL_ENGINELOOP;
case OP_CHECKCAST:
  // Stack: -1 +1 (same)
  // Arguments: 2
  // Ignore hi byte
  pc++;
  tempStackWord = get_top_ref();
  if (tempStackWord != JNULL && !instance_of (word2obj (tempStackWord), pc[0]))
    throw_exception (classCastException);
  pc++;
  goto LABEL_ENGINELOOP;

// Notes:
// - NEW, INSTANCEOF, CHECKCAST: 8 bits ignored, 8-bit class index
// - GETSTATIC and PUTSTATIC: 8-bit class index, 8-bit static field record
// - GETFIELD and PUTFIELD: 4-bit field type, 12-bit field data offset

/*end*/








