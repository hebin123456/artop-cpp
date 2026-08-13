#*******************************************************************************
# Copyright (c) 2014 itemis AG and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
#
# Contributors:
#     Mark Broerkens (itemis AG) - initial API and implementation
#*******************************************************************************
#!/bin/sh
#clean
rm *.aux
rm *.bbl
rm *.bcf
rm *.blg
rm *.idx
rm *.ilg
rm *.ind
rm *.log
rm *.out
rm *.pyg
rm *.rai
rm *.rao
rm *.run.xml
rm *.toc

touch run.out
tail -f run.out&
pdflatex -shell-escape XMLPersistenceMapping.tex 1>run.out 2>run.err
rail XMLPersistenceMapping 1>>run.out 2>>run.err
makeindex XMLPersistenceMapping 1>>run.out 2>>run.err
biber XMLPersistenceMapping 1>>run.out 2>>run.err
pdflatex -shell-escape XMLPersistenceMapping.tex 1>>run.out 2>>run.err
pdflatex -shell-escape XMLPersistenceMapping.tex 1>>run.out 2>>run.err
cat run.err
