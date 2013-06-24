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


rm -rf ~/gatling-bundle-2.0.0-SPACEAPE/results/*

rsync -az -v --chmod=go+r --progress -e "ssh -i /home/andy/andy.pem" --delete ~/gatling-bundle-2.0.0-SPACEAPE andy@loadtest-panda-4.use1a.apelabs.net:~

rsync -az -v --chmod=go+r --progress -e "ssh -i /home/andy/andy.pem" --delete ~/gatling-bundle-2.0.0-SPACEAPE andy@loadtest-panda-5.use1a.apelabs.net:~

rsync -az -v --chmod=go+r --progress -e "ssh -i /home/andy/andy.pem" --delete ~/gatling-bundle-2.0.0-SPACEAPE andy@loadtest-panda-6.use1a.apelabs.net:~

rsync -az -v --chmod=go+r --progress -e "ssh -i /home/andy/andy.pem" --delete ~/gatling-bundle-2.0.0-SPACEAPE andy@loadtest-panda-7.use1a.apelabs.net:~

