# -*- coding: utf-8 -*-
# test_emission.py : Testing mdcar file emission from python
# author : Antoine Passemiers

import os, sys, requests

DATA_PATH = "../files"
SERVER_URL = "http://renavspainatal.pythonanywhere.com/file"

def test_emission():
    filepath = os.path.join(DATA_PATH, "random.mdcar")
    with open(filepath, 'rb') as f: 
        r = requests.post(
            SERVER_URL, files = { "random.mdcar" : f })
        print(r.text)

if __name__ == "__main__":
    test_emission()