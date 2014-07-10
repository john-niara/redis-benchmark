#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

installdir=${1:-$HOME}

cd $installdir
rm -rf redis-stable.tar.gz
wget http://download.redis.io/redis-stable.tar.gz
tar xzf redis-stable.tar.gz
cd redis-stable
make
sudo make test
echo "Redis server is ready to start. Try:"
echo "src/redis-server $DIR/../conf/redis.conf"
