#!/usr/bin/perl


###	This script is part of the zeitcrawler (http://code.google.com/p/zeitcrawler/).
###	It is brought to you by Adrien Barbaresi.
###	It is freely available under the GNU GPL v3 license (http://www.gnu.org/licenses/gpl.html).

# Function : check the flatfile for duplicates, displays the number of different items.
# Use : without arguments.


use strict;
use List::MoreUtils qw(uniq);
use Digest::CRC qw(crc32);


my $input = "ZEIT_flatfile";
my $titles = 0;
my (@titles, @excerpts, @urls, @buffer, @crc);

open (INPUT, '<', $input) or die;

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
