# -*- coding: utf-8 -*-
# dataflow.py : Dealing with mobile sensor data types
# author : Antoine Passemiers

import numpy as np


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
	def random_data(self, n_samples):
		stdv = float(np.random.randint(1, 100, size = 1))
		intercept = float(np.random.randint(-50, 50, size = 1))
		return stdv * np.random.rand(n_samples, dtype = np.float32) + intercept
	
	@staticmethod
	def random_file(filepath, n_acc_samples, n_gyr_samples):
		data = np.empty(n_acc_samples + n_gyr_samples, dtype = MDCAR.SAMPLE_T)
		# data["X"] = MDCAR.random_data(n)
		data["X"][:n_acc_samples] = MDCAR.random_data(n_acc_samples)
		data["Y"][:n_acc_samples] = MDCAR.random_data(n_acc_samples)
		data["Z"][:n_acc_samples] = MDCAR.random_data(n_acc_samples)
		print(data["X"][:n_acc_samples])

if __name__ == "__main__":
	MDCAR.random_file("coucou.mdcar", 40, 50)
	print("Finished")