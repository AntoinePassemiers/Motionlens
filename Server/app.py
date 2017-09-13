from flask import Flask, request
from werkzeug.utils import secure_filename
import os

ROOT_FOLDER = os.getcwd()
UPLOAD_FOLDER = "Motionlens/Server/files"
ALLOWED_EXTENSIONS = {"mdcar"}

app = Flask(__name__)

def checkExtension(filename):
	return filename.split('.')[-1] in ALLOWED_EXTENSIONS

@app.route("/file", methods = ["POST"])
def upload():
    if (request.method == "POST" and "data" in request.files):
        file = request.files["data"]
        filename = file.filename

        if (file and checkExtension(filename)):
            secureFilename = secure_filename(filename)
            filePath = os.path.join(ROOT_FOLDER,UPLOAD_FOLDER,secureFilename)
            file.save(filePath)
            return str({"yo":"ok"})

if __name__ == "__main__":
	app.run(debug = True)


#example:curl http://127.0.0.1:5000/file -F "data=@/home/rena/Documents/monFichier.mdcar" -X POST
