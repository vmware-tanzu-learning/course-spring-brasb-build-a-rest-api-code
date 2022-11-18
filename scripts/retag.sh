#!/bin/sh

repository=$(git remote -v | head -n1 | perl -lwne 'm{\b([^/:]+/[^/]+).git\s} and print $1')

git log \
    --date=iso8601-strict \
    --pretty=format:"{%n  \"commit\": \"%H\",%n  \"author\": \"%aN <%aE>\",%n  \"date\": \"%ad\",%n  \"timestamp\": %at,%n  \"message\": \"%s\",%n  \"repo\": \"$repository\"%n}," \
    "$@" | \
    perl -pe 'BEGIN{print "["}; END{print "]\n"}' | \
    perl -pe 's/},]/}]/' | \
    perl -pe 's{\\}{\\\\}g' | \
    jq -r '.[] | select(.message|test("<")) | (.message) + " | " + (.commit)' | \
    sed -E 's/^.*<([^>]+)>.* | (.*)$/\1 | \2/g' | \
    perl -E '
use strict;
    
sub trim
{
    my $string = shift;
    $string =~ s/^\s+//;
    $string =~ s/\s+$//;
    return $string;
}
my %tag_sha;
my @tags_in_order;
for(<>) {
    my ($tags, $sha) = /^(.*)? \| (.*)$/;
    trim $tags;
    my @tags = split /\s*,\s*/, $tags;
    foreach my $tag (@tags) {
        $tag_sha{$tag} = $sha;
        push @tags_in_order, $tag;
    }
}
say "";
say "# Delete any existing local tags";

foreach my $tag (@tags_in_order) {
    say "git tag -d $tag";
}
say "";
say "# Create tags locally";

foreach my $tag (@tags_in_order) {
    say "git tag $tag $tag_sha{$tag}";
}
say "############### DANGER WILL ROBINSON";
say "### Delete any existing remote tags";

foreach my $tag (@tags_in_order) {
    say "# git push --delete origin $tag";
}
say "";
say "### Push the branch and the tags";
say "# git push && git push --tags";
'