#!/bin/bash
#
# Copyright 2011-2013 eBusiness Information, Groupe Excilys (www.ebusinessinformation.fr)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# 		http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

run=$(date +%Y%m%d_%H%M%S)
target=../results/$run

mkdir -p $target/loadtest4
mkdir -p $target/loadtest5
mkdir -p $target/loadtest6
mkdir -p $target/loadtest7

rsync -az -v --chmod=go+r --progress -e "ssh -i /home/andy/andy.pem" --remove-source-files andy@loadtest-panda-4.use1a.apelabs.net:~/gatling-bundle-2.0.0-SPACEAPE/results $target/loadtest4
rsync -az -v --chmod=go+r --progress -e "ssh -i /home/andy/andy.pem" --remove-source-files andy@loadtest-panda-5.use1a.apelabs.net:~/gatling-bundle-2.0.0-SPACEAPE/results $target/loadtest5
rsync -az -v --chmod=go+r --progress -e "ssh -i /home/andy/andy.pem" --remove-source-files andy@loadtest-panda-6.use1a.apelabs.net:~/gatling-bundle-2.0.0-SPACEAPE/results $target/loadtest6
rsync -az -v --chmod=go+r --progress -e "ssh -i /home/andy/andy.pem" --remove-source-files andy@loadtest-panda-7.use1a.apelabs.net:~/gatling-bundle-2.0.0-SPACEAPE/results $target/loadtest7


echo "processing $target"
find $target -name '*.log' -printf "%f:%p\n" | while IFS=":" read FNAME FPATH
do

let "index++"
mv $FPATH $target/simulation_$index.log
echo $target/simulation_$index.log

done

echo ./gatling.sh -ro $run
./gatling.sh -ro $run

if [ $? -eq 0 ]
then
  echo "Publishing $target"
  echo "Enter runame:"
  read name
  mkdir -p /usr/share/nginx/www/loadtest/$name
  mv $target /usr/share/nginx/www/loadtest/$name
fi
