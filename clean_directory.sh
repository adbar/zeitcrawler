#!/bin/bash

###	This shell script is part of the Zeitcrawler v1.3 (http://code.google.com/p/zeitcrawler/).
###	Copyright (C) Adrien Barbaresi 2011-2013.
###	This is free software, released under the GNU GPL v3 license (http://www.gnu.org/licenses/gpl.html).


# To use after a succesful crawl.
# Function: creates a compressed backup of the crawl files and deletes them.
# Should work on all UNIX-like systems.

tar -cvjf ZEIT_crawl_`date +%F`.tar.bz2 ZEIT_*
rm ZEIT_*
