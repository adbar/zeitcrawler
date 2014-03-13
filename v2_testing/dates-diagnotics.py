#!/usr/bin/python

### This is part of the Zeitcrawler (http://code.google.com/p/zeitcrawler/).
### Copyright (C) Adrien Barbaresi 2014.
### This is free software, released under the GNU GPL v3 license (http://www.gnu.org/licenses/gpl.html).
### WORK IN PROGRESS ! This is an experimental software component.

## + URL !


from __future__ import print_function
import re

total = 0
no_date = 0
invalid_cols = 0
norepairs = 0
dictdates = dict()
listerrors = list()

# loop
with open('dates', 'r') as inputfh:
    for line in inputfh:
        total += 1
        repair = False
        line = line.strip()
        # repair broken lines
        test = line.split(' ')
        if len(test) > 3:
            invalid_cols += 1
        else:
            # fill hash table
            columns = line.split('\t')
            # take valid dates only
            if str(columns[2]) is '0':
                no_date += 1
            else:
                try:
                    # plausible dates
                    if int(columns[0]) > 1945 and int(columns[0]) < 2020 and int(columns[1]) > 0 and int(columns[1]) < 60 and re.search(r'[0-9]{4}-[0-9]{2}-[0-9]{2}', columns[2]):
                        # add zero
                        if len(columns[1]) == 1:
                            columns[1] = '0' + str(columns[1])
                        # build strings
                        key = str(columns[0]) + '-' + str(columns[1])
                        value = columns[2]
                        year = int(columns[2].split('-')[0])
                        # does the date match the year ?
                        if year == int(columns[0]):
                            if key not in dictdates:
                                dictdates[key] = value
                            else:
                                # replace the reference date with any older one
                                if value is not dictdates[key]:
                                    dparse = value.split('-')
                                    value_month = int(dparse[1])
                                    value_day = int(dparse[2])
                                    dparse = dictdates[key].split('-')
                                    dict_month = int(dparse[1])
                                    dict_day = int(dparse[2])
                                    if value_month < dict_month:
                                        dictdates[key] = value
                                    elif value_month == dict_month and value_day < dict_day:
                                        dictdates[key] = value

                except ValueError:
                    repair = False
                else:
                    repair = True

                if repair is False:
                # tricky cases
                    # double year in first column
                    if re.match(r'[0-9]+ [0-9]+$', columns[0]):
                        temp = columns[0].split(' ')
                        if int(temp[0]) == int(temp[1]):
                            columns[0] = temp[0]
                            repair = True
                    # dots instead of hyphens in date
                    if re.match(r'[0-9]+\.[0-9]+\.[0-9]$', columns[2]):
                        temp = columns[2].split(' ')
                        repaired_date = str(temp[2]) + str(temp[1]) + str(temp[0])
                        repair = True
                    # same date twice in second column
                    if re.match(r'[0-9]+ [0-9]+$', columns[1]):
                        temp = columns[1].split(' ')
                        if int(temp[0]) == int(temp[1]):
                            columns[1] = temp[0]
                            repair = True
                        else:
                            repair = False

                # rare cases where year == 20
                if not re.search(r'[0-9]{4}', columns[0]) and not re.search(r'^0$', columns[0]):
                    if re.search(r'[0-9]{4}-[0-9]{2}-[0-9]{2}', columns[2]):
                        # extract year
                        dparse = columns[2].split('-')
                        year = dparse[0]
                        repair = True
                    else:
                        repair = False
                    
                # flag
                if repair is False:
                    norepairs += 1
                    listerrors.append(line)
                    # print ('warning:', key, value, dictdates[key])



print ('Total:', total)
print ('Date NA:', no_date)
print ('Invalid column number:', invalid_cols)
print ('Repair failed:', norepairs)

with open('sorted-dates', 'w') as outputfh:
    for item in sorted(dictdates):
        outputfh.write(item + '\t' + dictdates[item] + '\n')

with open('dates-errors', 'w') as outputfh:
    for item in listerrors:
        outputfh.write(item + '\n')

