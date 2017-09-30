# -*- coding: utf-8 -*-
# dataflow.py : Dealing with mobile sensor data types
# author : Antoine Passemiers

import os
import numpy as np
import time


DATA_PATH = "files"

class MDCAR:

	SAMPLE_T = np.dtype([
		("X", np.float32), 
		("Y", np.float32),
		("Z", np.float32),
		("timestamp", np.int64)])

	def __init__(self, ha_id, sensor1_data, sensor2_data):
		self.ha_id = ha_id
		self.sensor1_data = sensor1_data
		self.sensor2_data = sensor2_data

	@staticmethod
	def random_data(n_samples):
		stdv = float(np.random.randint(1, 100, size = 1))
		offset = float(np.random.randint(-50, 50, size = 1))
		return np.asarray(stdv * np.random.rand(n_samples) + offset, dtype = np.float32)
	
	@staticmethod
	def to_bytes(data, n_acc_samples, n_gyr_samples):
		header = np.asarray([n_acc_samples, n_gyr_samples], dtype = np.int64)
		bytes = np.fromstring(header.tobytes() + data.tobytes())
		return bytes

	@staticmethod
	def random_file(filepath, n_acc_samples, n_gyr_samples):
		n_samples = n_acc_samples + n_gyr_samples
		data = np.empty(n_samples, dtype = MDCAR.SAMPLE_T)
		# data["X"] = MDCAR.random_data(n)
		data["X"][:n_acc_samples] = MDCAR.random_data(n_acc_samples)
		data["Y"][:n_acc_samples] = MDCAR.random_data(n_acc_samples)
		data["Z"][:n_acc_samples] = MDCAR.random_data(n_acc_samples)
		data["timestamp"][:n_acc_samples] = np.arange(n_acc_samples)
		data["X"][n_acc_samples:] = MDCAR.random_data(n_gyr_samples)
		data["Y"][n_acc_samples:] = MDCAR.random_data(n_gyr_samples)
		data["Z"][n_acc_samples:] = MDCAR.random_data(n_gyr_samples)
		data["timestamp"][n_acc_samples:] = np.arange(n_gyr_samples)

		bytes = MDCAR.to_bytes(data, n_acc_samples, n_gyr_samples)
		bytes.tofile(filepath)
		return data
	
	@staticmethod
	def from_string(rawstring):
		filepath = time.strftime("%Y%m%d-%H%M%S")
		with open(filepath, "w") as f:
			f.write(rawstring)
			f.flush()
		bytes = np.fromfile(filepath, dtype = np.uint8, count = len(rawstring))

		header = np.frombuffer(bytes[:8], dtype = np.int32)

		n_acc_samples = header[0]
		n_gyr_samples = header[1]
		data = np.frombuffer(bytes[8:], dtype = MDCAR.SAMPLE_T)
		return n_acc_samples,n_gyr_samples,data

	@staticmethod
	def from_file(filepath):
		bytes = np.fromfile(filepath)
		header = np.frombuffer(bytes[:2], dtype = np.int64)
		n_acc_samples = header[0]
		n_gyr_samples = header[1]
		data = np.frombuffer(bytes[2:], dtype = MDCAR.SAMPLE_T)
		return data

def test_mdcar():
	encoded = MDCAR.random_file("temp.mdcar", 40, 50)
	decoded = MDCAR.from_file("temp.mdcar")
	assert((encoded == decoded).all())

assert(MDCAR.SAMPLE_T.itemsize == 20)

if __name__ == "__main__":
	filepath = os.path.join(DATA_PATH, "random.mdcar")
	encoded = MDCAR.random_file(filepath, 5000, 5000)
	print("Finished")
