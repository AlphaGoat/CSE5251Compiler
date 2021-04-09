JAVAC  := javac
JAVACC := javacc

GRAMMARDIR := semanticAnalysis

default : compiler

compiler : translate.jar

# options: c--create; f--name of jar file; e--entry point
translate.jar :
	$(JAVACC) -OUTPUT_DIRECTORY=$(GRAMMARDIR) semanticAnalysis/*.jj
	$(JAVAC) -classpath .:$(SUPPORT) */*.java
	jar cvmf COMPILEPLS.MF translate.jar semanticAnalysis/*.class main/*.class errors/*.class

clean :
	/bin/rm -f translate.jar
	/bin/rm -f */*.class