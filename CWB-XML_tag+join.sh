#!/bin/bash

###	This script is part of the zeitcrawler (http://code.google.com/p/zeitcrawler/).
###	It is brought to you by Adrien Barbaresi.
###	It is freely available under the GNU GPL v3 license (http://www.gnu.org/licenses/gpl.html).

###	WORK IN PROGRESS !


TAGGER=~/Desktop/DArbeit/werkzeuge/TreeTagger/bin/tree-tagger
OPTIONS="-token -lemma -no-unknown"
PARMFILE=~/Desktop/DArbeit/werkzeuge/TreeTagger/lib/german-utf8.par


perl FU-doc-trenn.pl

< docs/text sed -e 's/[„“”‚’]/\n"\n/g' -e 's/\([«»]\)/\n\1\n/g' -e 's/\([A-Za-zÄÖÜäöüß]\)\([,;:\.!?"]\)/\1 \2/g' -e 's/ /\n/g' -e '/^$/d' | $TAGGER $OPTIONS $PARMFILE > docs/tags

perl join.pl




