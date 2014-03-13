#!/usr/bin/python

### This is part of the Zeitcrawler (http://code.google.com/p/zeitcrawler/).
### Copyright (C) Adrien Barbaresi 2014.
### This is free software, released under the GNU GPL v3 license (http://www.gnu.org/licenses/gpl.html).
### WORK IN PROGRESS ! This is an experimental software component.


from __future__ import print_function
from __future__ import division

from collections import defaultdict

import argparse
import re
import time

# import numpy as np
import matplotlib.pyplot as plot


# time the whole script
start_time = time.time()

# argparse
parser = argparse.ArgumentParser()
parser.add_argument('--file1', dest='file1', help='name of the input file number 1', required=True)
parser.add_argument('--file2', dest='file2', help='name of the input file number 2', required=True)
args = parser.parse_args()


regex = re.compile(r'([0-9]{4})_([0-9]{2})_')
f1 = list()
f2 = list()


with open(args.file1, 'r') as file1fh:
    for line in file1fh:
        m = regex.match(line)
        if m:
            f1.append((int(m.group(1)), int(m.group(2))))


with open(args.file2, 'r') as file2fh:
    for line in file2fh:
        m = regex.match(line)
        if m:
            f2.append((int(m.group(1)), int(m.group(2))))


print ('total length file 1:', len(f1))
print ('total length file 2:', len(f2))


total_vals = list()
weeks_of_year1 = defaultdict(int)
weeks_of_year2 = defaultdict(int)
weeks_list = list()
week_counter = 1
xticks = list()

for i in range(1946, 2013):
    weeks_of_year1 = defaultdict(int)
    weeks_of_year2 = defaultdict(int)
    for item in f1:
        if item[0] == i:
            weeks_of_year1[item[1]] += 1
    for item in f2:
        if item[0] == i:
            weeks_of_year2[item[1]] += 1

    xticks.append(i)
    for j in range(2, 52):
            xticks.append('')
    for j in range(1, 52):
        diff = weeks_of_year2[j] - weeks_of_year1[j]
        total_vals.append(diff)
        weeks_list.append(week_counter)
        week_counter += 1

print (sum(total_vals)/len(total_vals))
print (min(total_vals))
print (max(total_vals))



## PLOT
plot.bar(range(len(total_vals)), total_vals, linewidth=0)
plot.axis([0, 3484, min(total_vals), max(total_vals)])
plot.xticks(range(len(xticks)), xticks)
locs, labels = plot.xticks()
plot.setp(labels, rotation=90)
plot.subplots_adjust(left=0.03, bottom=0.1, right=0.99, top=0.9, wspace=0.2, hspace=0.2)
# plot.grid(True)
plot.show()


## END
print ('Execution time (secs): {0:.2f}' . format(time.time() - start_time))
