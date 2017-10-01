from flask import Flask, request
from werkzeug.utils import secure_filename
import os
from dataflow import *
import numpy as np

ROOT_FOLDER = os.getcwd()
UPLOAD_FOLDER = "Motionlens/Server/files"

DATA_PATH = "files"


app = Flask(__name__)


@app.route("/file", methods = ["GET"])
def get():
    return "get-ok"

@app.route("/file", methods = ["POST"])
def upload():
    rawstring = list(request.form.to_dict().keys())[0]
    fname = time.strftime("%Y%m%d-%H%M%S")
    filepath = os.path.join(UPLOAD_FOLDER, fname)
    MDCAR.save_from_string(rawstring, filepath)

	return "post - nok"


if __name__ == "__main__":
	app.run(debug = True)


#example:curl http://127.0.0.1:5000/file -F "data=@/home/rena/Documents/monFichier.mdcar" -X POST
