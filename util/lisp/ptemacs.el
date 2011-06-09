;; $Id$
;; $PTII/util/lisp/ptemacs.el
;; This is a sample $HOME/.emacs file.
;; To use it, do:
;; cp $PTII/util/lisp/ptemacs.el $HOME/.emacs
;; Note that your $HOME and $PTII environment variables should be set
;; see $PTII/doc/install.htm and $PTII/doc/coding/debugging.htm for details.
;;

;; Set the load path to include the $PTII/util/lisp directory.
(setq load-path (append
                 (list
                  (expand-file-name 
		   (concat (getenv "PTII") "/util/lisp")))
                 load-path
                 ))

;; Set up shared memory attachment, see 
;; $PTII/doc/coding/debugging.htm for details
(setq gud-jdb-command-name "jdb -attach javadebug")

;; Load the Ptolemy II coding style that is defined
;; in $PTII/doc/coding/style.htm
(load-file (expand-file-name (concat 
			      (getenv "PTII") 
			      "/util/lisp/ptjavastyle.el")))

;; Turn on colorization of files
(global-font-lock-mode 1)

;; Set up to use bash as your shell

;; This assumes that Cygwin is installed in C:\cygwin (the
;; default) and that C:\cygwin\bin is not already in your
;; Windows Path (it generally should not be).
;;
;; As an alternative, Zoltan Kemenczy suggests setting the SHELL
;; environment variable to the windows path to bash.exe.
;; If cygwin\bin is in your DOS path, then setting SHELL to
;; bash should work.
;;
(setq exec-path (cons "C:/cygwin/bin" exec-path))
(setenv "PATH" (concat "C:\\cygwin\\bin;" (getenv "PATH")))

;;
;; NT-emacs assumes a Windows command shell, which you change
;; here.
;;
(setq process-coding-system-alist '(("bash" . undecided-unix)))
(if (boundp 'w32-quote-process-args)
  (setq w32-quote-process-args ?\")) ;; Include only for MS Windows.
(setq shell-file-name "bash")
(setenv "SHELL" shell-file-name) 
(setq explicit-shell-file-name shell-file-name) 
;;
;; This removes unsightly ^M characters that would otherwise
;; appear in the output of java applications.
;;
(add-hook 'comint-output-filter-functions
	  'comint-strip-ctrl-m)
