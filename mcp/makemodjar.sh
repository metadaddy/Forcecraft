#!/bin/bash
./recompile.sh 
./reobfuscate.sh
cd reobf/minecraft

# Need to include our dependencies
for f in ../../lib/*.jar
do 
	jar xvf $f
done

# Extract version number...
VERSION=`egrep -o 'version="\d+.\d+.\d+"' ../../src/minecraft/us/forcecraft/Forcecraft.java | cut -f2 -d \"`
jar cvf ../../../Forcecraft-v${VERSION}.jar *
