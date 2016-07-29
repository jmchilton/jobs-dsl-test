"""Entry point for defining py.test tests for this project."""
from __future__ import print_function

import os
import functools
import subprocess
import uuid

JUST_DOCKERFILES_CONFIG = os.getenv("JUST_DOCKERFILES_CONFIG", "./just-dockerfiles.json")
PROJECT_DIRECTORY = os.path.dirname(JUST_DOCKERFILES_CONFIG)
DEFAULT_RECIPE_DIRECTORY = os.path.join(PROJECT_DIRECTORY, "recipes")
RECIPE_DIRECTORY = os.environ.get("JUST_DOCKERFILES_RECIPES", DEFAULT_RECIPE_DIRECTORY)


def _create_function_for_recipe(recipe_name):
    recipe_path = os.path.join(RECIPE_DIRECTORY, recipe_name)
    the_t_function = functools.partial(_t_function, recipe_path)
    the_t_function.__name__ = "test_%s" % recipe_name
    the_t_function.__description__ = "Auto-generated test for %s" % recipe_name
    return the_t_function


def _t_function(path):
    docker_image_id = str(uuid.uuid4())
    _check_call(["docker", "build", "-t", docker_image_id, "."], cwd=path)
    _check_call(["docker", "run", "-t", docker_image_id], cwd=path)


def _check_call(cmd, cwd):
    print("Executing test command [%s]" % " ".join(cmd))
    ret = subprocess.check_call(cmd, cwd=cwd, shell=False)
    print("Command exited with return code [%s]" % ret)


for recipe_name in os.listdir(RECIPE_DIRECTORY):
    the_t_function = _create_function_for_recipe(recipe_name)
    globals()[the_t_function.__name__] = the_t_function
