#!/usr/bin/perl


###	This script is part of the Zeitcrawler v1.2 (http://code.google.com/p/zeitcrawler/).
###	It is brought to you by Adrien Barbaresi.
###	It is freely available under the GNU GPL v3 license (http://www.gnu.org/licenses/gpl.html).


# Function : duplicate detection based on title and excerpt.
# Use : without arguments, change "input" and "output" values below.


use strict;
use Text::Trim;

my $input = "input";
my (@titles, @excerpts, @urls);
my (%titles, %excerpts, %urls, $url, $text);
my $duplicate = 0;

open (INPUT, '<', $input) or die;

while (<INPUT>) {

	next if ($_ =~ m/^<li/);
	next if ($_ =~ m/^\/\//);
	next if ($_ =~ m/]]>/);
	if ($_ =~ m/^Titel: /) {
		my $title = $_;
		$title =~ s/^Titel: //;
		trim($title);
		if ( (length($title) > 8 ) && (exists $titles{$title}) ) {
			$duplicate++;
		}
		else {
			$titles{$title} = ();
		}
	}
	elsif ($_ =~ m/^Excerpt: /) {
		my $excerpt = $_;
		$excerpt =~ s/^Excerpt: //;
		trim($excerpt);
		if ( (length($excerpt) > 10 ) && (exists $excerpts{$excerpt}) ) {
			$duplicate++;
		}
		else {
			$excerpts{$excerpt} = ();
		}
	}
	elsif ($_ =~ m/^url: /) {
		$url = $_;
		$url =~ s/^url: //;
		trim($url);
	}

	$text .= $_;
	if ($_ =~ m/^-----$/) {
		unless ($duplicate == 2) {
			unless (exists $urls{$url}) {
				print $text;
				push (@urls, $url);
				$urls{$url} = ();
			}
		}
		$text = (); $duplicate = 0;
	}

}

open (OUT, ">", "output") or die "Can't open $input: $!";
foreach my $line (@urls) {
	print OUT $line, "\n";
}
close(OUT);
