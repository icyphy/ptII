/**
 * This is included inside a switch statement.
 */

case OP_ISHL:
  // Arguments: 0
  // Stack: -2 +1
  tempStackWord = pop_word();
  just_set_top_word (word2jint(get_top_word()) << (tempStackWord & 0x1F));
  goto LABEL_ENGINELOOP;
case OP_ISHR:
  // Arguments: 0
  // Stack: -2 +1
  tempStackWord = pop_word();
  just_set_top_word (word2jint(get_top_word()) >> (tempStackWord & 0x1F));
  goto LABEL_ENGINELOOP;
case OP_IUSHR:
  // Arguments: 0
  // Stack: -2 +1
  tempStackWord = pop_word();
  just_set_top_word (get_top_word() >> (tempStackWord & 0x1F));
  goto LABEL_ENGINELOOP;
case OP_IAND:
  tempStackWord = pop_word();
  just_set_top_word (get_top_word() & tempStackWord);
  goto LABEL_ENGINELOOP;
case OP_IOR:
  tempStackWord = pop_word();
  just_set_top_word (get_top_word() | tempStackWord);
  goto LABEL_ENGINELOOP;
case OP_IXOR:
  tempStackWord = pop_word();
  just_set_top_word (get_top_word() ^ tempStackWord);
  goto LABEL_ENGINELOOP;

// Notes:
// - Not supported: LSHL, LSHR, LAND, LOR, LXOR

/*end*/







