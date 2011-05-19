;; Ptolemy II Java coding Style
;; $Id$

;; This file contains Emacs definitions for the Ptolemy II coding
;; style that is defined in $PTII/doc/coding/style.htm

;; To use this file, place the following into your $HOME/.emacs file
;;(load-file (expand-file-name (concat 
;;			      (getenv "PTII") 
;;			      "/util/lisp/ptjavastyle.el")))
		   
;;;;;;;;;;;;;;;;;;;;;;;;;; C++MODE
(autoload 'c++-mode  "cc-mode" "C++ Editing Mode" t)
(autoload 'c-mode    "cc-mode" "C Editing Mode" t)
(add-hook 'c-mode-common-hook '(lambda ()
				(setq c-basic-offset 4)
				(setq c-indent-comments-syntactically-p t)
				(setq c-hanging-comment-starter-p nil)
				(setq c-hanging-comment-ender-p nil)
				;; Indent long lines eight chars
				;; See ~ptdesign/tycho/doc/coding/style.html
				(c-set-offset 'arglist-cont-nonempty '++)
				(c-set-offset 'arglist-cont 0)
				(c-set-offset 'arglist-intro '++)
				(c-set-offset 'func-decl-cont '++)
				(c-set-offset 'case-label 0)
				(if (string-match "^20\." emacs-version) 
				    ;; 
				    (c-set-offset 'c 'pt-c-lineup-C-comments))
				(c-set-offset 'topmost-intro-cont 0)

				(setq tab-width 8
				      ;; this will make sure spaces are
				      ;; used instead of tabs
				      indent-tabs-mode nil)
				;;(font-lock-mode)
				))

;; Emacs 21.1 and later does not require pt-c-lineup-C-comments
(defun pt-c-lineup-C-comments (langelem)
  ;; line up C block comment continuation lines
  (save-excursion
    (let ((here (point))
	  (stars (progn (back-to-indentation)
			(skip-chars-forward "*")))
	  (langelem-col (progn (goto-char (cdr langelem))
			    (current-column))))
;;	  (langelem-col (c-langelem-col langelem)))
      (back-to-indentation)
      (if (not (re-search-forward "/\\([*]+\\)" (c-point 'eol) t))
	  (progn
	    (if (not (looking-at "[*]+"))
		(progn
		  ;; we now have to figure out where this comment begins.
		  (goto-char here)
		  (back-to-indentation)
		  (if (looking-at "[*]+/")
		      (progn (goto-char (match-end 0))
			     (forward-comment -1))
		    (goto-char (cdr langelem))
		    (back-to-indentation))))
	    (- (current-column) langelem-col))
	(if (zerop stars)
	    (progn
	      (skip-chars-forward " \t")
	      (- (current-column) langelem-col))
	  ;; how many stars on comment opening line?  if greater than
	  ;; on current line, align left.  if less than or equal,
	  ;; align right.  this should also pick up Javadoc style
	  ;; comments.
	  (if (> (length (match-string 1)) stars)
	      (progn
		(back-to-indentation)
		(- (current-column) -1 langelem-col))
	    (- (current-column) stars langelem-col))
	  )))))
