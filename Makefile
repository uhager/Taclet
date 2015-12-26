## Makefile for part of Taclet
## author: Ulrike Hager
##
## to generate taclet.jar, run
## $ make
## $ jar vcf taclet.jar *.class

JAVAC = javac
   CLASS_FILES =  \
	GlobalVars.class \
	HistBar.class    \
	HistCircle.class \
	EnergyHistogram.class  \
	OneDHistogramFixed.class \
	OutputPanel.class  \
	Sector.class       \
	TacticViewer.class \
	TwoDHistogramFixed.class \


%.class: %.java
	$(JAVAC) $<

default: $(CLASS_FILES)

clean:
	rm *.class
