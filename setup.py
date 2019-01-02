import sys
import os

from setuptools import setup, find_packages

src_path = os.path.dirname(os.path.abspath(sys.argv[0]))
old_path = os.getcwd()
os.chdir(src_path)
sys.path.insert(0, src_path)

kwargs = {
    'name': 'epl.protobuf',
    'description': 'Echo Park Labs Geometry Client',
    'long_description': open('README.md').read(),
    'author': 'Echo Park Labs',
    'author_email': 'david@echoparklabs.com',
    'url': 'https://bitbucket.org/davidraleigh/geometry-client-python',
    'version': open('VERSION').read(),
    "namespace_package": ['epl'],
    'packages': ['epl.protobuf'],
    'zip_safe': False
}

clssfrs = [
    "Programming Language :: Python",
    "Programming Language :: Python :: 2.7",
    "Programming Language :: Python :: 3",
    "Programming Language :: Python :: 3.3",
    "Programming Language :: Python :: 3.4",
    "Programming Language :: Python :: 3.5",
    "Programming Language :: Python :: 3.6",
]
kwargs['classifiers'] = clssfrs

setup(**kwargs)
