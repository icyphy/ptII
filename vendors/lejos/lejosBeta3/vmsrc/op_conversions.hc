/**
 * This is included inside a switch statement.
 */

case OP_I2B:
  just_set_top_word ((JBYTE) word2jint(get_top_word()));
  goto LABEL_ENGINELOOP;
case OP_I2S:
case OP_I2C:
  just_set_top_word ((JSHORT) word2jint(get_top_word()));
  goto LABEL_ENGINELOOP;   
case OP_I2F:
  // Arguments: 0
  // Stack: -1 +1
  just_set_top_word (jfloat2word ((JFLOAT) word2jint(get_top_word())));
  goto LABEL_ENGINELOOP;
case OP_I2D:
  // Arguments: 0
  // Stack: -1 +2
  push_word (jfloat2word ((JFLOAT) word2jint(get_top_word())));
  goto LABEL_ENGINELOOP;
case OP_F2I:
  // Arguments: 0
  // Stack: -1 +1
  just_set_top_word ((JINT) word2jfloat(get_top_word()));
  goto LABEL_ENGINELOOP;
case OP_F2D:
  // Arguments: 0
  // Stack: -1 +2
  // Temporary is necessary because these are macros
  tempStackWord = get_top_word();
  push_word (tempStackWord);
  goto LABEL_ENGINELOOP;
case OP_D2I:
  // Arguments: 0
  // Stack: -2 +1  
  // Temporary is necessary because mixing macros is bad!
  tempStackWord = (JINT) word2jfloat (pop_word());
  just_set_top_word (tempStackWord);
  goto LABEL_ENGINELOOP;
case OP_D2F:
case OP_L2I:
  // Arguments: 0
  // Stack: -2 +1
  // Temporary is necessary because mixing macros is bad!
  tempStackWord = pop_word();
  just_set_top_word (tempStackWord);
  goto LABEL_ENGINELOOP;
case OP_I2L:
  tempStackWord = get_top_word();
  just_set_top_word (0);
  push_word (tempStackWord);
  goto LABEL_ENGINELOOP;
case OP_L2F:
  tempStackWord = pop_word();
  just_set_top_word (jfloat2word ((JFLOAT) tempStackWord));
  goto LABEL_ENGINELOOP;
case OP_L2D:
  just_set_top_word (jfloat2word ((JFLOAT) get_top_word()));
  goto LABEL_ENGINELOOP;
case OP_F2L:
  tempStackWord = get_top_word();
  just_set_top_word (0);
  push_word ((JINT) word2jfloat(tempStackWord));
  goto LABEL_ENGINELOOP;
case OP_D2L:
  tempStackWord = pop_word();
  just_set_top_word (0);
  push_word ((JINT) word2jfloat(tempStackWord));
  goto LABEL_ENGINELOOP;

/*end*/

