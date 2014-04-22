#!/bin/bash
#generate_codes.sh 1 500 salty-poop dangerous_codes.txt theSponsor
#start, finish, salt, file_name, sponsor
for i in $(seq $1 $2)
do
   md5value=$(md5sum <<< "$i $3")
   echo "${md5value// -/}" >> ./"$4"
   ./push_code.sh "${md5value// -/}" $5
done
