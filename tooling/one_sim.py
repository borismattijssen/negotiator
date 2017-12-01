#!/usr/bin/env python

import matplotlib.pyplot as plt
import numpy as np
import sys

def sim_utils( file, no_of_agents ):
    r = {}
    d = np.genfromtxt(file, dtype=None, skip_header=1, delimiter=';', names=True, comments='####')
    init = True
    for x in d:
        if init:
            for i in range(1,no_of_agents+1):
                agent_name = x["Agent_{}".format(i)]
                agent_name = agent_name.split('@')[0]
                r[agent_name] = []
            init = False

        for i in range(1,no_of_agents+1):
            agent_name = x["Agent_{}".format(i)]
            agent_name = agent_name.split('@')[0]
            r[agent_name].append(x["Utility_{}".format(i)])

    return r

if len(sys.argv) < 2:
    print('Please specify a simulationf file: one_sim.py <base-path>')
    sys.exit(1)

agents = 4
utils = sim_utils(sys.argv[1], agents)
for agent in utils:
	mean = np.mean(np.array(utils[agent]))
	std = np.std(np.array(utils[agent]))
	print("{}\t{:.8f}\t{:.8f}".format(agent, mean, std))


d = np.genfromtxt(sys.argv[1], dtype=None, skip_header=1, delimiter=';', names=True, comments='####')
print("sw: {}".format(np.mean(d['Social_Welfare'])))
print("sw: {}".format(np.mean(d['Dist_to_Nash'])))
