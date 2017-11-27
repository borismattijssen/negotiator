#!/usr/bin/env python

# Import the necessary packages and modules
import matplotlib.pyplot as plt
import numpy as np
import sys

folder = "../log/"
m = {
    0.5: 'our_2017.11.27_14.46.55',
    1: 'our_2017.11.27_14.51.34',
    2: 'our_2017.11.27_14.56.17',
    4: 'our_2017.11.27_15.00.55',
    6: 'our_2017.11.27_15.05.34',
    8: 'our_2017.11.27_14.18.31',
    10: 'our_2017.11.27_14.23.23',
    12: 'our_2017.11.27_14.27.50',
    14: 'our_2017.11.27_14.32.38',
}

plt.figure(1)
sub_base = 330
count = 1
for key in m:
    d = np.genfromtxt("{}{}.csv".format(folder,m[key]), delimiter=';', names=['x','y'])

    # Plot the data
    # plt.subplot(sub_base+count)
    plt.plot(d['x'], d['y'], label=key)

    # Add a legend
    plt.legend()

    # increase counter
    count = count+1

# Show the plot
plt.show()
