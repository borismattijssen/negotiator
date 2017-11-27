#!/usr/bin/env python

import matplotlib.pyplot as plt
import numpy as np

def sim_utils( file, no_of_agents ):
    r = {}
    d = np.genfromtxt(file, dtype=None, skip_header=1, delimiter=';', names=True, comments='####')
    init = True
    rounds = 0
    for x in d:
        if init:
            for i in range(1,no_of_agents+1):
                agent_name = x["Agent_{}".format(i)]
                agent_name = agent_name.split('@')[0]
                r[agent_name] = 0
            init = False

        for i in range(1,no_of_agents+1):
            agent_name = x["Agent_{}".format(i)]
            agent_name = agent_name.split('@')[0]
            r[agent_name] += x["Utility_{}".format(i)]

        rounds += 1

    for key in r:
        r[key] /= rounds

    return r


es = [0.5,1,2,4,6,8,10,12,14]
file_tmpl = "../logs/e_{}.csv"
agents = 4
y = []
for e in es:
    utils = sim_utils(file_tmpl.format(e), agents)
    y.append(utils['Group1'])

plt.plot(es, y, label='Group1')

# Add a legend
plt.legend()

plt.title("Conceding parameter e")

# Show the plot
plt.show()
