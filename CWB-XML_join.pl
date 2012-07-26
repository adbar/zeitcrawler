#!/usr/bin/perl


###	This script is part of the zeitcrawler (http://code.google.com/p/zeitcrawler/).
###	It is brought to you by Adrien Barbaresi.
###	It is freely available under the GNU GPL v3 license (http://www.gnu.org/licenses/gpl.html).

###	WORK IN PROGRESS !


use strict;
use warnings;


my $tags = "docs/tags";
open (TAGS, "<", $tags) or die "Can't open $tags: $!";



my $output = "ZEIT_crawl.xml";
open (OUTPUT, ">", $output) or die "Can't open $output: $!";

print OUTPUT "<?xml version=\"1.0\" encoding=\"utf-8\"?>
<!DOCTYPE collection [
  <!ELEMENT collection (doc+)>
  <!ELEMENT doc (#PCDATA|p|s)*>
  <!ATTLIST doc titel CDATA #REQUIRED>
  <!ATTLIST doc untertitel CDATA #REQUIRED>
  <!ATTLIST doc autor CDATA #REQUIRED>
  <!ATTLIST doc datum CDATA #REQUIRED>
  <!ATTLIST doc url CDATA #REQUIRED>
  <!ELEMENT p (s?)>
  <!ATTLIST p num CDATA #REQUIRED>
  <!ELEMENT s (#PCDATA)>
]>\n\n\n";
print OUTPUT "<collection>\n\n";

my $i = 1; my ($buffer);

while(<TAGS>) {
	if ($_ =~ m/^%%%%%/) {
		my $meta = "docs/meta-" . $i;
		open (META, "<", $meta) or die "Can't open $meta: $!";
		while (<META>) {
			print OUTPUT $_;
		}
		close(META);
		$i++;
		#$buffer =~ s/<unknown>/unknown/g;
		$buffer =~ s/SPSPSP\t.*?\tSPSPSP\n.\t\$.\t./<\/s>\n<s>/gs;
		$buffer =~ s/<s>\n<\/s>//gs;
		$buffer = xmlize($buffer);
		print OUTPUT "<s>\n" . $buffer . "</s>\n</doc>\n";
		$buffer = ();
	}
	else {$buffer .= $_}

}

close(TAGS);
print OUTPUT "</collection>";
close(OUTPUT);


sub xmlize {
	my $string = shift;
	#$string =~ s/(&#8220;|&#8221;|&#8222;|&#x201c;|&#x201e;)/&quot;/g;
	#$string =~ s/["„“”‚’]/&quot;/g;
	#$string =~ s/('|&#8216;)/&apos;/g;
	$string =~ s/&/&amp;/g;
	$string =~ s/'/&apos;/g;
	#$string =~ s/"/&quot;/g;
	#$string =~ s/&amp;quot;/&quot;/g;
	#$string =~ s/&#039;/&rsquo;/g;
	#$string =~ s/&gt;&gt;//g;
	#$string =~ s/&lt;&lt;//g;
	#$string =~ s/&#x2013;/\-\-/g;
	#$string =~ s/(&#8211;|&#8212;)/–/g;
	#$string =~ s/&#8364;/€/g;
	return $string;
}
