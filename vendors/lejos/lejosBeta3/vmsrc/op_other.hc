/**
 * This is included inside a switch statement.
 */

case OP_ATHROW:
  tempStackWord = pop_ref();
  if (tempStackWord == JNULL)
  {
    throw_exception (nullPointerException);
    goto LABEL_ENGINELOOP;
  }
  throw_exception (word2obj (tempStackWord));
  goto LABEL_ENGINELOOP;
case OP_MONITORENTER:
  enter_monitor (word2obj(pop_ref()));
  goto LABEL_ENGINELOOP;
case OP_MONITOREXIT:
  exit_monitor (word2obj(pop_ref()));
  goto LABEL_ENGINELOOP;

// Notes:
// - Not supported: BREAKPOINT

/*end*/


