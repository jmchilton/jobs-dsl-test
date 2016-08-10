FROM toolshed/requirements
MAINTAINER John Chilton, jmchilton@gmail.com

RUN apt-get update && apt-get install -y postgresql-client python-pip
RUN virtualenv /bioblend-venv
RUN . /bioblend-venv/bin/activate && pip install tox mock pbr

ENV GALAXY_USER_EMAIL=testing@galaxyproject.org \
    GALAXY_CONFIG_OVERRIDE_ALLOW_USER_DATASET_PURGE=true \
    GALAXY_CONFIG_OVERRIDE_ALLOW_LIBRARY_PATH_PASTE=true \
    GALAXY_CONFIG_OVERRIDE_ENABLE_BETA_WORKFLOW_MODULES=true \
    GALAXY_CONFIG_OVERRIDE_ALLOW_USER_DELETION=true \
    GALAXY_CONFIG_OVERRIDE_DATABASE_CONNECTION=postgresql://postgres@postgres/galaxy \
    BIOBLEND_GALAXY_URL=http://localhost:8080

ADD run_tests.bash /run_tests.bash
