#!/usr/bin/perl -w

use strict;

if (eval {require LWP::Simple;}) {
}
else {
#	print "You need to install the Perl LWP module!<br />";
	exit;
}

my $request_method = $ENV {'REQUEST_METHOD'};
my $form_info;

if ($request_method eq "GET") {
	$form_info = $ENV {'QUERY_STRING'};
}
else {
	my $size_form = $ENV {'CONTENT_LENGTH'};
	read (STDIN, $form_info, $size_form);
}

my $title;
my $type;
my $temp1;
my $temp2;

($temp1, $title, $temp2, $type) = split (/=|&/, $form_info);

if ($type eq "All+Types") {
	$type = "feature,tv_series,game";
}
elsif ($type eq "Feature+Film") {
	$type = "feature";
}
elsif ($type eq "TV+Series") {
	$type = "tv_series";
}
elsif ($type eq "Video+Game") {
	$type = "game";
}

$title =~ s/\%([A-Fa-f0-9]{2})/pack('C', hex($1))/seg;
$title =~ s/\+/ /g;
$title =~ s/ /+/g;
$title =~ s/([^A-Za-z0-9\+-])/sprintf("%%%02X", ord($1))/seg;

my $url = "http://www.imdb.com/search/title?title=$title&title_type=$type";

my $content = LWP::Simple::get($url);
die "Couldn't get url" unless defined $content;

my @poster;
my @result_title;
my @year;
my @rate;
my @director;
my @detail;

my $temp;
my $count = 0;
my @match;
my @match_rate;
my @dir;
my @test;

my $match1;
my $match2;
my $match3;

my $t1;
my $t2;

my $num;
my $maxcount;

$title =~ s/\%([A-Fa-f0-9]{2})/pack('C', hex($1))/seg;
$title =~ s/\+/ /g;

if ($content =~ /No results/) {
	$maxcount = 0;
	print "Content-type: application/xml\n\n";

	print "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	print "<rsp stat=\"ok\">\n";
	print "<results total=\"$maxcount\">";
	print "</results>\n";
	print "</rsp>\n";
}
elsif ($content =~ /<h1>Error<\/h1>/) {
	$maxcount = 0;
	print "Content-type: application/xml\n\n";

	print "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	print "<rsp stat=\"error\">\n";
	print "<results total=\"$maxcount\">";
	print "</results>\n";
	print "</rsp>\n";
}
else {
	@test = $content =~ /(<a href=\"\/title.*?\"certificate\")/sg;
	#@match = $content =~ /(<a href=\"\/title.*><img src=\".*\">)/g;
	#@match_rate = $content =~ /(title=\"Users.*\/10)/g;
	#@dir = $content =~ /(Dir:\s<a.*a>)/g;
	$num = () =$content =~ /(<a href=\"\/title.*?\"certificate\")/sg;

	if ($num <=5) {
		$maxcount = $num;
	}
	else {
		$maxcount = 5;
	}

	while ($count<$maxcount) {
		$test[$count] =~ /(<a href=\"\/title.*><img src=\".*\">)/;
		$match1 = $1;
		#$temp = $match[$count];
		$temp = $match1;
		$temp =~ /(<img src=\")(.*)(\" height)/;
		$poster[$count] = $2;
		$temp =~ /(title=\")(.*)(\(.{4})(.*\)\"><)/;
		$result_title[$count] = $2;
		$temp =~ /(\()(.{4})(.*\))/;
		$year[$count] = $2;
		$temp =~ /(<a href=\")(.*\/)(\"\st)/;
		$detail[$count] = $2;

		if ($test[$count] =~ /(title=\"Users.*\/10)/) {
			$match2 = $1;
			#$temp = $match_rate[$count];
			$temp = $match2;
			$temp =~ /(this\s)(.*)(\/10)/;
			$rate[$count] = $2;
		}
		else {
			if ($test[$count] =~ /Awaiting enough ratings/) {
				$rate[$count] = "-";
			}
			else {
				$rate[$count] = "N/A";
			}
		}

		if ($test[$count] =~ /(Dir:\s<a.*a>)/) {
			$match3 = $1;
			#$temp = $dir[$count];
			$temp = $match3;
			if ($temp =~ />,\s</) {
				$temp =~ /(<a href=.*\/\">)(.*)(<\/a>\,)/;
				$director[$count] = $2;
				my @dir_list = $temp =~ /(\,\s<a href=.*<\/a>)/;
				for(@dir_list) {
					$_ =~ /(\,\s<a href=.*\/\">)(.*)(<\/a>)/;
					$director[$count] .= ", $2";
				}
			}
			else {
				$temp =~ /(Dir:\s.*\/\">)(.*)(<\/a>)/;
				$director[$count] = $2;
			}
		}
		else {
			$director[$count] = "N/A";
		}
		$count++;
	}

	$count=0;
	my $detail_link;

	#binmode (STDOUT, ":utf8");

	print "Content-type: application/xml\n\n";

	print "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	print "<rsp stat=\"ok\">\n";
	print "<results total=\"$maxcount\">";
	while ($count<$maxcount) {
		$detail_link = "http://www.imdb.com$detail[$count]";
		print "<result cover=\"$poster[$count]\" title=\"$result_title[$count]\" year=\"$year[$count]\" director=\"$director[$count]\" rating=\"$rate[$count]\" details=\"$detail_link\" \/>\n";
		$count++;
	}
	print "</results>\n";
	print "</rsp>\n";
}
