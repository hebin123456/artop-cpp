Installation of Latex Environment for creation of XML Persistence Mapping documentation

MacOS X
(1) Install MacTeX
    Download: http://tug.org/mactex/
    
(2) Install rail
    Download: http://www.ctan.org/tex-archive/support/rail
    
    Since no MacOS executable is contained you need to compile it yourself:
    - install XCode via Apple App Store
    - patch rail.h in order to make the code run with up to date gcc:
      after
        typedef union {
			IDTYPE *id;	/* identifier */
			int num;	/* number */
			char *text;	/* text */
			BODYTYPE *body;	/* body */
			RULETYPE *rule;	/* rule */
		} YYSTYPE;
		
		add the following #defines
		# define YYSTYPE_IS_DECLARED 1
		# define YYSTYPE_IS_TRIVIAL 0
		
		
		change add void to delete: "void delete(id)"
		change add void to freebody: "void freebody(body)"
		
	- patch "Makefile"
	  set 
        BINDIR=/usr/texbin
		TEXDIR=/usr/local/texlive/2013/texmf-dist/tex/latex/base
		MANDIR=/usr/share/man
		MANSUFFIX=1
    - patch "rail.c"
      change parameter argc to "int argc;"
      change add void to freebody: "void freebody(body)"
      change add void to delete: "void delete(id)"
	- execute "make"
	- execute "sudo make install"
	- add "/usr/texbin" to your PATH (e.g. in $HOME/.profile)
	
(3) install pygmentize
    - sudo easy_install Pygments
    
    
    
    
    
BUILD XMLPersistenceMapping.tex
   (1) execute build.sh
   
	
    