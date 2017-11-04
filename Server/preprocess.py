# -*- coding: utf-8 -*-
# preprocess.py : Preprocess sensor data
# author : Antoine Passemiers

from dataflow import *

import numpy as np
import matplotlib.pyplot as plt


def interpolate_3d_samples(data):
    timestamps = data["timestamp"]
    start, end = timestamps[0], timestamps[-1]
    interpolated = list()
    xp = np.arange(start, end, 60)
    for dim_name in ["X", "Y", "Z"]:
        interpolated.append(np.interp(xp, timestamps, data[dim_name]))
    return np.asarray(interpolated).T


if __name__ == "__main__":
    mdcar = MDCAR.from_file("files/20171104-001745.mdcar")
    
    acc_data = mdcar.sensor1_data
    gyr_data = mdcar.sensor2_data

    print(acc_data["X"])
    plt.plot(gyr_data["X"])
    plt.show()

    gyr_data = interpolate_3d_samples(gyr_data)
    print(gyr_data)