import sys
import os

from setuptools import setup, find_packages

src_path = os.path.dirname(os.path.abspath(sys.argv[0]))
old_path = os.getcwd()
os.chdir(src_path)
sys.path.insert(0, src_path)

kwargs = {
    'name': 'epl.protobuf.v1',
    'description': 'generated protobufs for geometry and STAC',
    'url': 'https://github.com/geo-grpc/api',
    'long_description': 'https://github.com/geo-grpc/api',
    'author': 'David Raleigh',
    'author_email': 'davidraleigh@gmail.com',
    'license': 'Apache 2.0',
    'version': '1.0.4',
    'namespace_package': ['epl'],
    'python_requires': '>3.5.2',
    'packages': ['epl.protobuf.v1'],
    'install_requires': [
        'grpcio-tools',
        'protobuf'
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
