#!/bin/bash

set -e

sleep 10  # TODO: wait on something instead of just sleeping...

echo `df`

createdb -w -U postgres -h postgres galaxy
export GALAXY_VIRTUAL_ENV=/galaxy_env
export GALAXY_CONFIG_OVERRIDE_MASTER_API_KEY=`date --rfc-3339=ns | md5sum | cut -f 1 -d ' '`
echo `ls /galaxy`
GALAXY_RUN_ALL=1 bash /galaxy/run.sh --daemon --wait
export GALAXY_USER=$USER
export GALAXY_USER_PASSWD=`date --rfc-3339=ns | md5sum | cut -f 1 -d ' '`
git clone https://github.com/galaxyproject/bioblend.git /bioblend
cd /bioblend
. /bioblend-venv/bin/activate && python setup.py install
export BIOBLEND_GALAXY_API_KEY=`. /bioblend-venv/bin/activate && python docs/examples/create_user_get_api_key.py $BIOBLEND_GALAXY_URL $GALAXY_MASTER_API_KEY $GALAXY_USER $GALAXY_USER_EMAIL $GALAXY_USER_PASSWD`
. /bioblend-venv/bin/activate && tox -e py27
