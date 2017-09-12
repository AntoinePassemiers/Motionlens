from flask import Flask, request
from werkzeug.utils import secure_filename
import os

ROOT_FOLDER = os.getcwd()
UPLOAD_FOLDER = "files"
ALLOWED_EXTENSIONS = {"bonjour"}

app = Flask(__name__)

@app.route("/file", methods = ["POST"])
def upload():
	f = request.files["data"]
	secureFilename = secure_filename(f.filename)
	filePath = os.path.join(ROOT_FOLDER,UPLOAD_FOLDER,secureFilename)
	#secureFilePath = secure_filename(filePath)
	print("________",filePath)
	f.save(filePath)
	return str({"yo":"ok"})

if __name__ == "__main__":
	app.run(debug = True)