#!/usr/bin/perl


###	This shell script is part of the Zeitcrawler v1.3 (http://code.google.com/p/zeitcrawler/).
###	Copyright (C) Adrien Barbaresi 2011-2013.
###	This is free software, released under the GNU GPL v3 license (http://www.gnu.org/licenses/gpl.html).

# Function: check the flatfile for duplicates, displays the number of different items.
# Use: without arguments.


use strict;

# not the fastest way to uniq a list in Perl
use List::MoreUtils qw(uniq);
# may not be the fastest and most efficient hashing algorithm
use String::CRC32;

# input file here:
my $input = "ZEIT_flatfile";

my $titles = 0;
my (@titles, @excerpts, @urls, @buffer, @crc);

open (INPUT, '<', $input) or die;

# loop through the file, store the metadata and hash the content
while (<INPUT>) {
	if ($_ =~ m/^Titel: /) {
		push (@titles, $_);
	}
	elsif ($_ =~ m/^Excerpt/) {
		push (@excerpts, $_);
	}
	elsif ($_ =~ m/^url: /) {
		push (@urls, $_);
	}
	if ($_ eq "-----\n") {
		push (@crc, crc32(join("",@buffer)));
		@buffer= ();
	}
	else {
		if ( ($_ !~ m/^Autor: /) && ($_ !~ m/^Datum: /) ) {
		push (@buffer, $_);
		}
	}
}

print "titles:\t\t" . scalar (@titles) . "\t" . scalar (uniq @titles) . "\n";
print "excerpts:\t" . scalar (@excerpts) . "\t" . scalar (uniq @excerpts) . "\n";
print "urls:\t\t" . scalar (@urls) . "\t" . scalar (uniq @urls) . "\n";
print "text crc:\t" .scalar (@crc) . "\t" . scalar (uniq @crc) . "\n";
