#!/usr/bin/python3
# -*- coding: utf-8 -*-

### This is part of the Zeitcrawler (http://code.google.com/p/zeitcrawler/).
### Copyright (C) Adrien Barbaresi 2014.
### This is free software, released under the GNU GPL v3 license (http://www.gnu.org/licenses/gpl.html).
### WORK IN PROGRESS ! This is an experimental software component.


from __future__ import print_function
from os import walk
from os.path import join
from collections import defaultdict
import re
import time


start_time = time.time()
lastseen = ''

filelist = list()
print_d = defaultdict(int)
online_d = defaultdict(int)
dpa_d = defaultdict(int)


for (dirpath, dirnames, filenames) in walk('/home/adrien/Arbeitsfläche/ZEIT_export/'):
    for dirname in dirnames:
        for (dirpath2, dirnames2, filenames2) in walk(join(dirpath, dirname)):
            for filename in filenames2:
                filelist.append(join(dirpath2,filename))
    break

print ('#', len(filelist))

# year
def findyear(string):      
    m_year = re.search(r'<year>([0-9]+)</year>', string)
    if m_year:
        return m_year.group(1)
    else:
        return '0'


for filename in filelist:
    with open(filename, 'r') as inputfh:
        year, date, metatype = ['0']*3
        slurp = inputfh.read()
        # date
        m_date = re.search(r'<date>([0-9-]+)</date>', slurp)
        if m_date:
            date = m_date.group(1)
            m_year = re.search(r'^([0-9]{4})-', date)
            if m_year:
                year = m_year.group(1)
            else:
                year = findyear(slurp)
        else:
            year = findyear(slurp)

        # type
        m_metatype = re.search(r'<metatype>([A-Za-z]+)</metatype>', slurp)
        if m_metatype:
            metatype = m_metatype.group(1)
        # print
        print (date, year, metatype, sep='\t')

        # store
        if metatype is 'DPA':
            dpa_d[year] += 1
        elif metatype is 'print':
            print_d[year] += 1
        elif metatype is 'online':
            online_d[year] += 1

def printdict(dictname, printname):
    print ('#', printname)
    for key in dictname:
        print ('#', key, dictname[key])

printdict(print_d, 'print')
printdict(online_d, 'online')
printdict(dpa_d, 'DPA')

## END
print ('Execution time (secs): {0:.2f}' . format(time.time() - start_time))
