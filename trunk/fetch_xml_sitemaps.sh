#!/bin/bash


###	This script is part of the Zeitcrawler v1.3 (http://code.google.com/p/zeitcrawler/).
###	Copyright (C) Adrien Barbaresi 2011-2013.
###	This is free software, released under the GNU GPL v3 license (http://www.gnu.org/licenses/gpl.html).


## Purpose: crawl the sitemaps of the website to find all the article URLs.
## Execute the file without arguments.


wget http://www.zeit.de/gsitemaps/index.xml -O sitemap1.xml

grep -Po 'http://.+?page=[0-9]+' sitemap1.xml > sitemap-list

while read line
do
    filename="sitemaps/p"$(echo $line | grep -Po '[0-9]+')
    wget $line -O $filename
    grep -Po "http://www\.zeit\.de/.+?(?=(<))" $filename >> URLs
    sleep 2
done < sitemap-list
