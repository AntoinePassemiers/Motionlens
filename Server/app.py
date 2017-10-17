from flask import Flask, request
from werkzeug.utils import secure_filename
import os
from dataflow import *
import numpy as np
import re
import time

ROOT_FOLDER = os.getcwd()
UPLOAD_FOLDER = "Motionlens/Server/files"

DATA_PATH = "files"
EXTENSION = ".mdcar"

app = Flask(__name__)


@app.route("/file", methods = ["GET"])
def get():
    return "get-ok"

@app.route("/file", methods = ["POST"])
def upload():
	"""
	pattern = r'\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00'
	for match in re.finditer(pattern,str(request.get_data())):
		print("yoyo : " + str(match.start()))
	"""
	#print(request.get_data())
	rawstring = request.get_data()
	#rawstring = s[2:-1]
	#rawstring = list(request.form.to_dict().keys())[0]
	fname = time.strftime("%Y%m%d-%H%M%S" + EXTENSION)
	filepath = os.path.join(UPLOAD_FOLDER, fname)
	MDCAR.save_from_string(rawstring, filepath)
	mdcar = MDCAR.from_file(filepath)
	print(mdcar)

	return "post - nok"


if __name__ == "__main__":
	app.run(debug = True)


#example:curl http://127.0.0.1:5000/file -F "data=@/home/rena/Documents/monFichier.mdcar" -X POST
