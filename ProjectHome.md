### DESCRIPTION ###

Starting from the front page or from a given list of links, **the crawler retrieves newspaper articles and gathers new links to explore as it goes**, stripping the text of each article out of the HTML formatting and saving it into a raw text file.

Due to its specialization it is able to **build a reliable corpus consisting of texts and relevant metadata** (title, author, excerpt, date and url). The URL list which comes with the software features about 130.000 articles, which enables to gather more than 100 millions of tokens.

As you can't republish anything but quotations of the texts, the purpose of this tool is to **enable others to make their own version of the corpus**, as crawling is not explicitly forbidden by the right-holders of _Die Zeit_.

The crawler does not support multi-threading as this may not be considered a fair use, it takes the links one by one and **it is not set up for speed**. It may take two or three days to gather a corpus of more than 100.000 articles.

The **export in XML format** accounts for the compatibility with other software designed to complete a further analysis of the texts, for example the textometry software [TXM](http://txm.sourceforge.net/).

An experimental version of scripts enabling a conversion to the CWB format is now included.


### FILES & USAGE ###

The current release of the software includes most notably **the crawler itself, a list of URLs as well as scripts to convert raw data into the XML format** for further use with natural language processing tools.

For more information please refer to the [README file](http://code.google.com/p/zeitcrawler/source/browse/trunk/README).


### RESTRICTIONS ###

**The texts gathered using this software are for personal (or academic) use only**, you are not allowed to republish them. The cases which allow for a free use of the texts are listed on [this page](http://www.zeit-verlagsgruppe.de/presse/rechte-und-lizenzen/freie-nutzung/) (in German).

The crawler was designed to get as little noise as possible. However, it is **highly dependent on the content management system and on the HTML markup** used by the newspaper _Die Zeit_. It worked by July 1st 2013, but it could break on future versions of the website and it may not be updated on a regular basis.

All the scripts should work correctly on **UNIX-like systems** if you set the right permissions. They may need a software like [Cygwin](http://www.cygwin.com/) to run on Windows, this case was not tested.


### LICENSE ###

This is free software, released under the GNU GPL v3 license.

Copyright (C) [Adrien Barbaresi](http://perso.ens-lyon.fr/adrien.barbaresi/) 2011-2013.


### CHANGELOG ###

17th August 2013: v1.3 - Better memory management, xml sitemap crawler added.

22nd March 2013: v1.2 - Small efficiency improvements, script to remove duplicates added, experimental version of a conversion to CWB format added, updated list of links.

1st August 2012: v1.1 – Speed and memory use improvements for the crawler : replacement of the CRC function, better and reordered regular expressions, use of a hash to remove duplicates, the text is only re-encoded when written to a file, a few bugs removed.