#!/bin/bash

ABBR_LIST=~/Desktop/DArbeit/korpora/Zprog/share/abbrev.lex
TOKDIP=~/Desktop/DArbeit/korpora/Zprog/tokenize-DIPPER.pl
TAGGER=~/Desktop/DArbeit/werkzeuge/TreeTagger/bin/tree-tagger
OPTIONS="-token -lemma -no-unknown"
PARMFILE=~/Desktop/DArbeit/werkzeuge/TreeTagger/lib/german-utf8.par


perl FU-doc-trenn.pl

#read -p "DOCNR ? -> " DOCNR
#for f in docs/text-*
#for i in {$START..$DOCNR}

perl $TOKDIP -abbrev $ABBR_LIST docs/text temp
< docs/temp perl -pe 's/\n/\nSPSPSP\n.\n/g' > temp1
< temp1 sed -e 's/[„“”‚’]/\n"\n/g' -e 's/\([A-Za-zÄÖÜäöüß]\)\([,;:\.!?"]\)/\1 \2/g' -e 's/\n/\nSPSPSP\n.\n/g' -e 's/ /\n/g' -e '/^$/d' | $TAGGER $OPTIONS $PARMFILE > docs/tags
rm docs/temp
#-e 's/„/\n"\n/g' -e 's/“/\n"\n/g' -e 's/&amp;/&/g'

perl join.pl




