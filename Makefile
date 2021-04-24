JAVAC  := javac
JAVACC := javacc

GRAMMARDIR := semanticAnalysis

default : compiler

compiler : compile.jar

# options: c--create; f--name of jar file; e--entry point
compile.jar :
	$(JAVACC) -OUTPUT_DIRECTORY=$(GRAMMARDIR) semanticAnalysis/*.jj
	$(JAVAC) -classpath .:$(SUPPORT) */*.java
	jar cvmf COMPILEPLS.MF compile.jar */*.class	
	sparc-linux-gcc -c runtime.c -o runtime.o
	chmod u+x compile.sh

clean :
	/bin/rm -f compile.jar
	/bin/rm -f */*.class