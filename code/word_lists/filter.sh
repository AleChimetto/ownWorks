#!/bin/sh

wordlist=joined.txt
brands=brands.csv # From https://github.com/MatthiasWinkelmann/english-words-names-brands-places

output=filtered_brands.txt

cd $(dirname $0)

sort --ignore-case "$wordlist" "$brands" | uniq --ignore-case --repeated > "$output"

echo "$(wc -l "$output" | awk '{print $1}') resulting brands."

