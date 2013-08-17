#!/bin/bash


###	This script is part of the Zeitcrawler v1.3 (http://code.google.com/p/zeitcrawler/).
###	Copyright (C) Adrien Barbaresi 2011-2013.
###	This is free software, released under the GNU GPL v3 license (http://www.gnu.org/licenses/gpl.html).

## WORK IN PROGRESS ! This is not a mature script.
## Please check what this script does before executing it.

# requires the TreeTagger and a "docs" folder


TAGGER=~TreeTagger/bin/tree-tagger
OPTIONS="-token -lemma -no-unknown"
PARMFILE=~TreeTagger/lib/german-utf8.par


perl CWB-XML_doc-trenn.pl

< docs/text sed -e 's/[„“”‚’]/\n"\n/g' -e 's/\([«»]\)/\n\1\n/g' -e 's/\([A-Za-zÄÖÜäöüß]\)\([,;:\.!?"]\)/\1 \2/g' -e 's/ /\n/g' -e '/^$/d' | $TAGGER $OPTIONS $PARMFILE > docs/tags

perl CWB-XML_join.pl




