/**
 * This is included inside a switch statement.
 */

case OP_ISUB:
  // Arguments: 0
  // Stack: -2 +1
  just_set_top_word (-word2jint(get_top_word()));
  // Fall through!
case OP_IADD:
  // Arguments: 0
  // Stack: -2 +1
  tempStackWord = pop_word();
  just_set_top_word (word2jint(get_top_word()) + word2jint(tempStackWord));
  goto LABEL_ENGINELOOP;
case OP_IMUL:
  // Arguments: 0
  // Stack: -2 +1
  tempStackWord = pop_word();
  just_set_top_word (word2jint(get_top_word()) * word2jint(tempStackWord));
  goto LABEL_ENGINELOOP;
case OP_IDIV:
case OP_IREM:
  tempInt = word2jint(pop_word());
  if (tempInt == 0)
  {
    throw_exception (arithmeticException);
    goto LABEL_ENGINELOOP;
  }
  just_set_top_word ((*(pc-1) == OP_IDIV) ? word2jint(get_top_word()) / tempInt :
                                            word2jint(get_top_word()) % tempInt);
  goto LABEL_ENGINELOOP;
case OP_INEG:
  just_set_top_word (-word2jint(get_top_word()));
  goto LABEL_ENGINELOOP;
case OP_FSUB:
  just_set_top_word (jfloat2word(-word2jfloat(get_top_word())));
  // Fall through!
case OP_FADD:
  tempStackWord = pop_word();
  just_set_top_word (jfloat2word(word2jfloat(get_top_word()) + 
                     word2jfloat(tempStackWord)));
  goto LABEL_ENGINELOOP;
case OP_FMUL:
  tempStackWord = pop_word();
  just_set_top_word (jfloat2word(word2jfloat(get_top_word()) * 
                     word2jfloat(tempStackWord)));
  goto LABEL_ENGINELOOP;
case OP_FDIV:
  // TBD: no division by zero?
  tempStackWord = pop_word();
  just_set_top_word (jfloat2word(word2jfloat(get_top_word()) / 
                     word2jfloat(tempStackWord)));
  goto LABEL_ENGINELOOP;
case OP_FNEG:
case OP_DNEG:
  just_set_top_word (jfloat2word(-word2jfloat(get_top_word())));
  goto LABEL_ENGINELOOP;
case OP_DSUB:
  just_set_top_word (jfloat2word(-word2jfloat(get_top_word())));
  // Fall through!
case OP_DADD:
  tempStackWord = get_top_word();
  pop_words(2);
  just_set_top_word (jfloat2word(word2jfloat(get_top_word()) +
                    word2jfloat(tempStackWord)));
  goto LABEL_ENGINELOOP;
case OP_DMUL:
  tempStackWord = get_top_word();
  pop_words(2);
  just_set_top_word (jfloat2word(word2jfloat(get_top_word()) *
                    word2jfloat(tempStackWord)));
  goto LABEL_ENGINELOOP;
case OP_DDIV:
  // TBD: no division by zero?
  tempStackWord = get_top_word();
  pop_words(2);
  just_set_top_word (jfloat2word(word2jfloat(get_top_word()) /
                    word2jfloat(tempStackWord)));
  goto LABEL_ENGINELOOP;

// Notes:
// - Not supported: LADD, LSUB, LMUL, LREM, FREM, DREM
// - Operations on doubles are truncated to low float

/*end*/







