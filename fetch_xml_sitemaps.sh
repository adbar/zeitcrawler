#!/bin/bash


###			ZEITCRAWLER v1.3		###
###		http://code.google.com/p/zeitcrawler/ 		###

###	This script is brought to you by Adrien Barbaresi.
###	It is freely available under the GNU GPL v3 license (http://www.gnu.org/licenses/gpl.html).

###	The crawler does not support multi-threading, as this may not be considered a fair use.
###	The gathered texts are for personal (or academic) use only, you cannot republish them.
###	The cases which allow for a free use of the texts are listed on this page (in German) :
###	http://www.zeitverlag.de/presse/rechte-und-lizenzen/freie-nutzung/


## Purpose : crawl the sitemaps of the website to find all the article URLs.
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
