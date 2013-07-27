#!/bin/bash
## Use this script to build and compile a java project

## Jar options
JARVERSION="0.1"
JARNAME="PixelmonUSInstaller"
MAINCLASS="us.pixelmon.installer.Main"

## Path options
BASEDIR=/home/aaron/repo/github/aabmass/PixelmonUSInstaller
LISTFILE=$BASEDIR/javasrc.list
LIBDIR=$BASEDIR/lib
SRCDIR=$BASEDIR/src
BINDIR=$BASEDIR/bin
JARDIR=$BASEDIR/jar

## Array of other files you want added to the jar (ie library classes)
ADDTOJAR=("${ADDTOJAR[@]}"
          "$SRCDIR/config"
          "$LIBDIR/net")

## Formatting
_uf="\e[0m"         ##unformat clears formatting
_blue="\e[34m"      ##blue
_lc="\e[96m"        ##light cyan
_fm="${_lc}==> "    ##default formatting

ORIGDIR=`pwd`

mkdir -p {$BASEDIR,$SRCDIR,$BINDIR,$JARDIR}
cd $BASEDIR
echo -e "${_fm}Finding new java files...\n"
find -iname "*.java" | tee $LISTFILE
echo -e "\n${_fm}Beginning compilation...\n"

javac -extdirs $LIBDIR -d $BINDIR @${LISTFILE}
if [ "$?" -eq 1 ]
then
    echo -e "${_fm}Compilation was unsuccessful. Exiting..."
    exit
else
    echo -e "${_fm}Compilation was successful!"
fi

cd $BINDIR

echo -e "${_fm}Adding files specified in 'ADDTOJAR' to the jar..."
cp -R ${ADDTOJAR[@]} $BINDIR

echo -e "${_fm}Creating jar..."
jar cfe ${JARDIR}/${JARNAME}-${JARVERSION}.jar ${MAINCLASS} .
echo -e "${_fm}Done!"

cd $ORIGDIR
