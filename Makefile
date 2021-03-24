JAVAC  := javac
JAVACC := javacc

default : compiler

compiler : check.jar

# options: c--create; f--name of jar file; e--entry point
check.jar :
	$(JAVAC) -classpath .:$(SUPPORT) */*.java
	jar cvmf META-INF/MANIFEST.MF check.jar Lexer/*.class semanticAnalysis/*.class main/*.class errors/*.class

clean :
	/bin/rm -f check.jar
	/bin/rm -f */*.class