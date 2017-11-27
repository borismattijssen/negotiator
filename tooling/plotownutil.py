#!/usr/bin/env python

# Import the necessary packages and modules
import matplotlib.pyplot as plt
import numpy as np
import sys

file_tmpl = "../log/{}.csv"
m = {
    0.5: 'our_2017.11.27_16.45.25',
    1: 'our_2017.11.27_16.45.42',
    2: 'our_2017.11.27_16.46.40',
    4: 'our_2017.11.27_16.46.55',
    6: 'our_2017.11.27_16.47.10',
    8: 'our_2017.11.27_16.47.25',
    10: 'our_2017.11.27_16.47.39',
    12: 'our_2017.11.27_16.47.52',
    14: 'our_2017.11.27_16.48.07',
}

plt.figure(1)
sub_base = 330
count = 1
for key in m:
    d = np.genfromtxt(file_tmpl.format(m[key]), delimiter=';', names=['x','y'])

    # Plot the data
    # plt.subplot(sub_base+count)
    plt.plot(d['x'], d['y'], label=key)

    # Add a legend
    plt.legend()

    # increase counter
    count = count+1

# Show the plot
plt.show()
