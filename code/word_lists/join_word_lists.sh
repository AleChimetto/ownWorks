#!/bin/sh

input=(
  american-english british-english # from /usr/share/dict/

  # Unfortunately the following contains a lot of brands which are not dictioanry words
  # words.txt # from https://github.com/dwyl/english-words/
)

output=joined.txt

cd $(dirname $0)

sort --ignore-case ${input[@]} | uniq --ignore-case > "$output"

echo "$(wc -l "$output" | awk '{print $1}') resulting words."

