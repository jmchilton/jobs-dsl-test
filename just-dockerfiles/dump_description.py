import json
import os
import sys
import yaml

ROOT_DIRECTORY = "."
DEFAULT_RECIPE_DIRECTORY = "recipes"
RECIPE_DIRECTORY = os.environ.get("RECIPE_DIRECTORY", DEFAULT_RECIPE_DIRECTORY)

with open("%s/%s/def.yml" % (RECIPE_DIRECTORY, sys.argv[1])) as ins:
    with open("%s/%s/def.json" % (RECIPE_DIRECTORY, sys.argv[1]), "w") as outs:
        json.dump(yaml.load(ins), outs)
