"""Entry point for defining py.test tests for this project."""
from __future__ import print_function

import functools
import json
import os
import subprocess
import uuid

JUST_DOCKERFILES_CONFIG_PATH = os.getenv("JUST_DOCKERFILES_CONFIG", "./just-dockerfiles.json")
PROJECT_DIRECTORY = os.path.abspath(os.path.dirname(JUST_DOCKERFILES_CONFIG_PATH))
DEFAULT_RECIPE_DIRECTORY = os.path.join(PROJECT_DIRECTORY, "recipes")
RECIPE_DIRECTORY = os.environ.get("JUST_DOCKERFILES_RECIPES", DEFAULT_RECIPE_DIRECTORY)


with open(JUST_DOCKERFILES_CONFIG_PATH, "r") as f:
    JUST_DOCKERFILES_CONFIG = json.load(f)


def _create_function_for_recipe(recipe_name):
    recipe_path = os.path.join(RECIPE_DIRECTORY, recipe_name)
    the_t_function = functools.partial(_t_function, recipe_path)
    the_t_function.__name__ = "test_%s" % recipe_name
    the_t_function.__description__ = "Auto-generated test for %s" % recipe_name
    return the_t_function


def _t_function(path):
    recipe_config_path = os.path.join(path, "def.yml")
    if os.path.exists(recipe_config_path):
        with open(recipe_config_path, "r") as f:
            recipe_config = json.load(f)
    else:
        recipe_config = {}

    target_path = _get_defaultable_option(recipe_config, "targetPath", "/app")
    target_root = os.getenv("TARGET_ROOT", os.path.join(PROJECT_DIRECTORY, ".."))

    docker_image_id = str(uuid.uuid4())
    _check_call(["docker", "build", "-t", docker_image_id, "."], cwd=path)
    _check_call(["docker", "run", "-t", docker_image_id, "-v", "%s:%s" % (target_root, target_path)], cwd=path)


def _check_call(cmd, cwd):
    print("Executing test command [%s]" % " ".join(cmd))
    ret = subprocess.check_call(cmd, cwd=cwd, shell=False)
    print("Command exited with return code [%s]" % ret)


def _get_defaultable_option(recipe_config, key, default):
    default_key = key[0].upper() + key[1:]
    if key in recipe_config:
        return recipe_config.get(key)
    elif default_key in JUST_DOCKERFILES_CONFIG:
        return JUST_DOCKERFILES_CONFIG.get(default_key)
    else:
        return default


for recipe_name in os.listdir(RECIPE_DIRECTORY):
    the_t_function = _create_function_for_recipe(recipe_name)
    globals()[the_t_function.__name__] = the_t_function
