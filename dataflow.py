import numpy as np


class HAData:

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
	def from_file(filepath):
		pass
	def to_file(filepath, data):
		raw_sensor1 = self.sensor1_data.tobytes()
		raw_sensor2 = self.sensor2_data.tobytes()

if __name__ == "__main__":
	a = np.empty(40, dtype = HAData.SAMPLE_T)
	b = np.empty(40, dtype = HAData.SAMPLE_T)
	print(type(a[0]))
	print("Finished")