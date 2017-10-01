# -*- coding: utf-8 -*-
# dataflow.py : Dealing with mobile sensor data types
# author : Antoine Passemiers

import os
import numpy as np
import time


DATA_PATH = "files"

"""
import numpy as np


s = np.array([0, 0, 0, 1, 0, 0, 0, 1], dtype = np.uint8)

be_int32 = np.dtype(np.int32).newbyteorder('>')
p = s.view(dtype = be_int32)
print(p)


be_float32 = np.dtype(np.float32).newbyteorder('>')
s = np.fromstring("CUff", dtype = be_float32)
print(s)
"""

BE_INT32_T = np.dtype(np.int32).newbyteorder('>')
BE_INT64_T = np.dtype(np.int64).newbyteorder('>')
BE_FLOAT32_T = np.dtype(np.float32).newbyteorder('>')


SAMPLE_T = np.dtype([
	("X", BE_FLOAT32_T), 
	("Y", BE_FLOAT32_T),
	("Z", BE_FLOAT32_T),
	("timestamp", BE_INT64_T)])
assert(SAMPLE_T.itemsize == 20)

class MDCAR:
	def __init__(self, ha_id, sensor1_data, sensor2_data):
		self.ha_id = ha_id
		self.sensor1_data = sensor1_data
		self.sensor2_data = sensor2_data

	@staticmethod
	def random_data(n_samples):
		stdv = float(np.random.randint(1, 100, size = 1))
		offset = float(np.random.randint(-50, 50, size = 1))
		return np.asarray(stdv * np.random.rand(n_samples) + offset, dtype = BE_FLOAT32_T)
	
	@staticmethod
	def to_bytes(data, n_acc_samples, n_gyr_samples):
		header = np.asarray([n_acc_samples, n_gyr_samples], dtype = BE_INT64_T)
		bytes = np.fromstring(header.tobytes() + data.tobytes())
		return bytes

	@staticmethod
	def random_file(filepath, n_acc_samples, n_gyr_samples):
		n_samples = n_acc_samples + n_gyr_samples
		data = np.empty(n_samples, dtype = SAMPLE_T)
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
	def from_file(filepath):
		with open(filepath, "r") as f:
			rawstring = f.read()
			print(len(rawstring))
			header = np.fromstring(rawstring[:8], dtype = BE_INT32_T)
			print(header)
			n_acc_samples = header[0]
			n_gyr_samples = header[1]
			data = np.fromstring(rawstring[8:], dtype = SAMPLE_T)
			return data

	@staticmethod
	def save_from_string(rawstring, filepath):
		with open(filepath, "w") as f:
			f.write(rawstring)
			f.flush()


def test_mdcar():
	encoded = MDCAR.random_file("temp.mdcar", 40, 50)
	decoded = MDCAR.from_file("temp.mdcar")
	assert((encoded == decoded).all())
	print("Tests: success")


decoded = MDCAR.from_file("files/20171001-172911.mdcar")
# test_mdcar()