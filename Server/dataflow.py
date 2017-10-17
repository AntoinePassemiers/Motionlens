# -*- coding: utf-8 -*-
# dataflow.py : Dealing with mobile sensor data types
# author : Antoine Passemiers

import os
import numpy as np
import time


DATA_PATH = "files"


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
	HEADER_N_BYTES = 16
	BAD_FILE = "bad mdcar"

	def __init__(self, device_id, ha_id, sensor1_data, sensor2_data):
		self.device_id = device_id
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
		return MDCAR(0, 0, data[:n_acc_samples], data[n_acc_samples:])

	@staticmethod
	def from_string(rawstring):
		if (len(rawstring) - MDCAR.HEADER_N_BYTES) % SAMPLE_T.itemsize != 0:
			return MDCAR.BAD_FILE
		header = np.fromstring(rawstring[:MDCAR.HEADER_N_BYTES], dtype = BE_INT32_T)
		ha_id         = header[0]
		device_id     = header[1]
		n_acc_samples = header[2]
		n_gyr_samples = header[3]
		data = np.fromstring(rawstring[MDCAR.HEADER_N_BYTES:], dtype = SAMPLE_T)
		if len(data) != n_acc_samples + n_gyr_samples:
			return MDCAR.BAD_FILE
		acc_data = data[:n_acc_samples]
		gyr_data = data[n_acc_samples:]
		return MDCAR(device_id, ha_id, acc_data, gyr_data)

	@staticmethod
	def from_file(filepath):
		with open(filepath, "rb") as f:
			rawstring = f.read()
		return MDCAR.from_string(rawstring)

	@staticmethod
	def save_from_string(rawstring, filepath):
		with open(filepath, "wb") as f:
			f.write(rawstring)
			f.flush()