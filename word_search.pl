#!/usr/bin/perl


###	This script is part of the Zeitcrawler v1.3 (http://code.google.com/p/zeitcrawler/).
###	Copyright (C) Adrien Barbaresi 2011-2013.
###	This is free software, released under the GNU GPL v3 license (http://www.gnu.org/licenses/gpl.html).

# Function: find a word in the 'flatfile'.
# Use: without arguments


use strict;
use warnings;


# file name and path here:
my $input = "ZEIT_flatfile";

my $titles = 0;
my ($title, $excerpt, $author, $date, $url);
my $counter = 0;

# query here
my $word = "Elefant";

# loop through the file and print the results (metadata) to STDOUT
open (INPUT, '<', $input) or die;

while (<INPUT>) {
    if ($_ =~ m/^Titel: /) {
        $title = $_;
    }
    elsif ($_ =~ m/^Excerpt: /) {
        $excerpt = $_;
    }
    elsif ($_ =~ m/^Autor: /) {
        $author = $_;
    }
    elsif ($_ =~ m/^Datum: /) {
        $date = $_;
    }
    elsif ($_ =~ m/^url: /) {
        $url = $_;
    }
    elsif ($_ eq "-----\n") {
        if ($counter > 0) {
            print "----------\n" . $counter . "\n" . $title . "\n" . $excerpt . "\n" . $author . "\n" . $date . "\n" . $url . "\n";
        }
        $counter = 0;
    }
    else {
        if ($_ =~ m/$word/) {
            $counter++;
        }
    }
}


