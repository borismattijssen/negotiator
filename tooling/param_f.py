#!/usr/bin/env python

import matplotlib.pyplot as plt
import numpy as np
import sys

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

if len(sys.argv) < 2:
    print('Please specify base path like so: param_e.py <base-path>')
    sys.exit(1)

es = [0.5,0.6,0.7,0.75,0.8,0.85,0.9,0.95,0.99,1]
file_tmpl = sys.argv[1] + "/f_{}.csv"
agents = 4
y = {}
for e in es:
    utils = sim_utils(file_tmpl.format(e), agents)
    for agent in utils:
        if agent not in y:
            y[agent] = []
        y[agent].append(utils[agent])

for agent in y:
    plt.plot(es, y[agent], label=agent)

# Add a legend
plt.legend()

plt.title("Parameter f_min")

# Show the plot
plt.show()
