#!/usr/bin/env python

# Import the necessary packages and modules
import matplotlib.pyplot as plt
import numpy as np
import sys

if len(sys.argv) < 2:
    print('Please specify base path like so: plotownutil_f.py <base-path>')
    sys.exit(1)
file_tmpl = sys.argv[1] + "/f_{}/our.csv"
m = [0.5, 0.6,0.7,0.75,0.8,0.85,0.9,0.95,0.99,1]

plt.figure(1)
sub_base = 330
count = 1
for key in m:
    d = np.genfromtxt(file_tmpl.format(key), delimiter=';', names=['x','y'])

    # Plot the data
    # plt.subplot(sub_base+count)
    plt.plot(d['x'], d['y'], label=key)

    # Add a legend
    plt.legend(loc='upper left')

    # increase counter
    count = count+1

# Show title
plt.title("Our utility per parameter f")

# Show the plot
plt.show()