/**
 * This is included inside a switch statement.
 */

case OP_INVOKEVIRTUAL:
  // Stack: (see method)
  // Arguments: 2
  // Note: pc is updated by dispatch method
  dispatch_virtual (word2obj (get_ref_at (pc[0] >> 4)), 
    (TWOBYTES) pc[1] | (TWOBYTES) (pc[0] & 0x0F), pc + 2);
  goto LABEL_ENGINELOOP;
case OP_INVOKESPECIAL:
case OP_INVOKESTATIC:
  // Stack: (see method)
  // Arguments: 2
  // Note: pc is updated by dispatch method
  dispatch_special_checked (pc[0], pc[1], pc + 2, pc - 1);
  goto LABEL_ENGINELOOP;
case OP_IRETURN:
case OP_LRETURN:
case OP_FRETURN:
case OP_DRETURN:
case OP_ARETURN:
  // Stack: 1 or 2 words copied up
  // Arguments: 0
  do_return ((*(pc-1) - OP_IRETURN) % 2 + 1);
  goto LABEL_ENGINELOOP;
case OP_RETURN:
  // Stack: unchanged
  // Arguments: 0
  do_return (0);
  goto LABEL_ENGINELOOP;

// Notes:
// * INVOKEINTERFACE cannot occur because it's replaced
//   by INVOKEVIRTUAL and a couple of NOOPs.

/*end*/







