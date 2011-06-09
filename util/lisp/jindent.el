;; jindent.el, used by $PTII/util/testsuite/jindent to
;; indent Java files to the Ptolemy II standard
;; Version: $Id$

(load (expand-file-name
       (concat (getenv "PTII") "/util/lisp/ptjavastyle.el")))

(defun jindent ()
  (java-mode)
  (indent-region (point-min) (point-max) 'nil)
  (save-buffer)
)
