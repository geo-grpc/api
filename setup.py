import sys
import os

from setuptools import setup, find_packages

src_path = os.path.dirname(os.path.abspath(sys.argv[0]))
old_path = os.getcwd()
os.chdir(src_path)
sys.path.insert(0, src_path)

kwargs = {
    'name': 'epl.geometry',
    'description': 'geometry library for gRPC remote geometry functions',
    'url': 'https://github.com/geo-grpc/geometry-client-python',
    'long_description': "geometry library for gRPC remote geometry functions",
    'author': 'David Raleigh',
    'author_email': 'davidraleigh@geometry.com',
    'license': 'Apache 2.0',
    'version': '0.3.27',
    'namespace_package': ['epl'],
    'python_requires': '>3.5.2',
    'packages': ['epl.geometry'],
    'install_requires': [
        'grpcio-tools',
        'protobuf',
        'shapely',
        'epl.protobuf'
    ],
    'zip_safe': False
}

clssfrs = [
    'Programming Language :: Python',
    'Programming Language :: Python :: 3',
    'Programming Language :: Python :: 3.5',
    'Programming Language :: Python :: 3.6',
]
kwargs['classifiers'] = clssfrs

setup(**kwargs)
