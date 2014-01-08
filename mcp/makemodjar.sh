#!/bin/bash
./recompile.sh 
./reobfuscate.sh
cd reobf/minecraft

# Need to include our dependencies
for f in ../../lib/*.jar
do 
	jar xvf $f
done

# Need to bump version number...
jar cvf ../../../Forcecraft-v0.1.0.jar *