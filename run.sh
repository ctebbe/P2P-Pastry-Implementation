#!/bin/bash

HOST=$(hostname)

ant clean

# discovery node start-up
gnome-terminal -e 'bash -c "ant discovery; bash"'

sleep 3 # allow the discovery node time to spin up

while read PEERNODE
do
    echo 'sshing into '${PEERNODE}
    gnome-terminal -x bash -c "ssh -t ${PEERNODE} 'cd '~/workspace/cs555/A02'; echo $PEERNODE;
    ant -Darg0=${HOST} peer; bash'" #&
    sleep 2 # allow the discovery node time to spin up
done < peernodes


# data node startup
#gnome-terminal -x bash -c "ssh -t cheyenne 'cd '~/workspace/cs555/A02'; echo $(hostname); ant -Darg0=${HOST} data; bash'" #&
