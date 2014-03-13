#!/usr/bin/python

### This is part of the Zeitcrawler (http://code.google.com/p/zeitcrawler/).
### Copyright (C) Adrien Barbaresi 2014.
### This is free software, released under the GNU GPL v3 license (http://www.gnu.org/licenses/gpl.html).
### WORK IN PROGRESS ! This is an experimental software component.


from __future__ import print_function
from time import strptime
import re

# vars
total = 0
no_date = 0
invalid_cols = 0
value_errors = 0
repair_count = 0
norepairs = 0
dictdates = dict()
listerrors = list()
reference = dict()
outputlist = list()


## FUNCTIONS

# fill dates function
def filldate(year, week):
    # deal with weeks > 52
    if int(week) > 52:
        week = '52'    
    # analyze
    data = strptime(year + ' ' + week + ' 1', '%Y %W %w')
    year = str(data[0])
    if len(str(data[1])) == 1:
        month = '0' + str(data[1])
    else:
        month = str(data[1])
    if len(str(data[2])) == 1:
        day = '0' + str(data[2])
    else:
        day = str(data[2])
    return year + '-' + month + '-' + day

# '0' + month
def correct_month(month):
    # add zero
    if len(month) == 1:
        month = '0' + str(month)
    return month

# repaired count and boolean
def repaired(boolean):
    global repair_count, repair_bool
    repair_bool = True
    if boolean is True:
        repair_count += 1



## FILES

# reference dates dict
with open('sorted-dates.old', 'r') as inputfh:
    for line in inputfh:
        line = line.strip()
        columns = line.split('\t')
        reference[columns[0]] = columns[1]



## LOOP

with open('dates', 'r') as inputfh:
    for line in inputfh:
        total += 1
        repair_bool = False
        mismatch = False
        line = line.strip()

        # split in columns
        columns = line.split('\t')
        if len(columns) != 5:
            invalid_cols += 1
            print (line)
            continue

        ## dictionary
        # put only plausible dates in dictionary
        try:
            if mismatch is False and int(columns[0]) > 1945 and int(columns[0]) < 2020 and int(columns[1]) > 0 and int(columns[1]) < 60 and re.search(r'[0-9]{4}-[0-9]{2}-[0-9]{2}', columns[2]):
                # build strings
                key = str(columns[0]) + '-' + correct_month(columns[1])
                value = columns[2]
                year = int(columns[2].split('-')[0])
                # does the date match the year ?
                if year == int(columns[0]):
                    repaired(True)
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
                else:
                    mismatch = True

        except ValueError:
            value_errors += 1


        ## Corrections and normalization
        # correct month
        columns[1] = correct_month(columns[1])

        # correct news URLs
        if re.match(r'[0-9]{4}/[0-9]{2} [0-9]+$', columns[3]):
            columns[3] = re.sub(r' [0-9]+$', '', columns[3])

        # double year in first column
        if re.match(r'[0-9]+ [0-9]+$', columns[0]):
            temp = columns[0].split(' ')
            if int(temp[0]) == int(temp[1]):
                columns[0] = temp[0]
                repaired(True)
            else:
                mismatch = True
        # same date twice in second column
        if re.match(r'[0-9]+ [0-9]+$', columns[1]):
            temp = columns[1].split(' ')
            if int(temp[0]) == int(temp[1]):
                columns[1] = temp[0]
                repaired(True)
            else:
                mismatch = True
        # dots instead of hyphens in date
        if re.match(r'[0-9]+\.[0-9]+\.[0-9]+$', columns[2]):
            temp = columns[2].split('.')
            columns[2] = str(temp[2]) + '-' + str(correct_month(temp[1])) + '-' + str(correct_month(temp[0]))
            if re.match(r'[0-9]{4}$', columns[0]):
                if temp[2] == columns[0]:
                    repaired(True)
                else:
                    mismatch = True
            else:
                # force replacement of year
                columns[2] = str(temp[2]) + '-' + str(correct_month(temp[1])) + '-' + str(correct_month(temp[0]))
                columns[0] = temp[2]
                repaired(True)

        ## Repair fields
        # fill date if necessary
        if str(columns[2]) is '0':
            if re.match(r'[0-9]{4}$', columns[0]) and re.match(r'[0-9]{2}$', columns[1]):
                slug = columns[0] + '-' + columns[1]
                if slug in reference:
                    columns[2] = reference[slug]
                    repaired(True)
                else:
                    columns[2] = filldate(columns[0], columns[1])
                    repaired(True)
            else:
                # no date at all
                if columns[0] is '0' and re.match(r'0+', columns[1]) and columns[3] is '0':
                    no_date += 1
                    columns[1] = '0'
                    listerrors.append('\t'.join(columns))
                    continue
                # double year + double month
                elif re.match(r'[0-9]{4} [0-9]{4}$', columns[0]) and re.match(r'[0-9]{2} [0-9]{2}$', columns[1]) and re.match(r'[0-9]{4}/[0-9]{2}$', columns[3]):
                    # week could be reliable
                    temp = columns[3].split('/')
                    columns[0] = temp[0]
                    columns[1] = temp[1]
                    columns[2] = filldate(temp[0], temp[1])
                    repaired(True)
                else:
                    # use URL
                    if re.match(r'[0-9]{4}/[0-9]{2}$', columns[3]):
                        temp = columns[3].split('/')
                        columns[0] = temp[0]
                        columns[1] = temp[1]
                        columns[2] = filldate(temp[0], temp[1])
                        repaired(True)
                    else:
                        no_date += 1
                        listerrors.append('\t'.join(columns))
                        continue
        # no date in URL
        elif str(columns[3]) is '0':
            # news with just date
            if columns[0] is '0' and re.match(r'0+', columns[1]) and re.match(r'[0-9]{4}-[0-9]{2}-[0-9]{2}$', columns[2]) and columns[3] is '0':
                temp = columns[2].split('-')
                columns[0] = temp[0]
                columns[1] = '0'
                repaired(False)
            # no week
            elif re.match(r'0+$', columns[1]):
                temp = columns[2].split('-')
                columns[0] = temp[0]
                columns[1] = '0'
                repaired(False)
            # try to repair
            else:
                temp = columns[2].split('-')
                # probably not a problem
                if temp[0] == columns[0]:
                    repaired(False)
                # repair
                else:
                    try:
                        columns[0] = temp[0]
                        columns[1] = temp[1]
                        columns[2] = filldate(columns[0], columns[1])
                        repaired(True)
                    except IndexError:
                        print ('#####')
                        print (line)
                        print ('\t'.join(columns))
                        print ('#####')
        # easy cases
        else:
            # 0/1900 news with no week
            if re.match(r'1?9?0+$', columns[0]) and re.match(r'0+', columns[1]) and re.match(r'[0-9]{4}-[0-9]{2}-[0-9]{2}$', columns[2]) and re.match(r'[0-9]{4}-[0-9]{2}$', columns[3]):
                temp = columns[2].split('-')
                columns[0] = temp[0]
                columns[1] = '0'
                repaired(False)
                outputlist.append('\t'.join(columns))
                continue

            # no week
            elif re.match(r'[0-9]{4}$', columns[0]) and re.match(r'0+', columns[1]) and re.match(r'[0-9]{4}-[0-9]{2}-[0-9]{2}$', columns[2]) and re.match(r'[0-9]{4}[/-][0-9]{2}$', columns[3]):
                # use URL
                mismatch = True
            # rare cases where year == 20
            if not re.match(r'[0-9]{4}$', columns[0]) and not re.match(r'0$', columns[0]):
                if re.match(r'[0-9]{4}-[0-9]{2}-[0-9]{2}', columns[2]):
                    # extract year
                    columns[0] = columns[2].split('-')[0]
                    # year = dparse[0]
                    repaired(True)
            # catch the rest (news)
            if columns[0] is '0' and re.match(r'0+$', columns[1]):
                temp = columns[2].split('-')
                columns[0] = temp[0]
                columns[1] = '0'
                repaired(True)
            

        # year columns do not match
        if mismatch is True:
            # take URL as source
            if re.match(r'[0-9]{4}/[0-9]{2}$', columns[3]):
                temp = columns[3].split('/')
                columns[0] = temp[0]
                columns[1] = temp[1]
                columns[2] = filldate(columns[0], columns[1])
                repaired(True)
            elif re.match(r'[0-9]{4}-[0-9]{2}$', columns[3]):
                temp = columns[3].split('-')
                if columns[0] == temp[0]:
                    # week could be reliable
                    columns[2] = filldate(columns[0], columns[1])
                    repaired(True)
                else:
                    # replace date by 1st of January
                    columns[0] = temp[0]
                    columns[1] = '01'
                    columns[2] = str(temp[0]) + '-01-02'
                    # columns[4] = 'GAGA'
                    repaired(True)

        # catchall: expeditive fix for dates that do not match
        if re.match(r'[0-9]{4}_[0-9]{2}$', columns[3]):
            if columns[0] is '0': # does not work
                temp = columns[3].split('_')
                columns[0] = temp[0]
                columns[1] = temp[1]
            columns[2] = filldate(columns[0], columns[1])
            repaired(True)
        if re.search(r' -$', columns[2]):
            columns[2] = filldate(columns[0], columns[1])
            repaired(True)

        # flag
        if repair_bool is False:
            norepairs += 1
            listerrors.append('\t'.join(columns)) # line
            # print ('warning:', key, value, dictdates[key])
        else:
            # output
            outputlist.append('\t'.join(columns))



print ('Total:\t\t', total)
print ('Invalid columns:', invalid_cols)
print ('Value errors:\t', value_errors)
print ('Repaired:\t', repair_count)
print ('Date NA:\t', no_date)
print ('Repair failed:\t', norepairs)

with open('sorted-dates', 'w') as outputfh:
    for item in sorted(dictdates):
        outputfh.write(item + '\t' + dictdates[item] + '\n')

with open('dates-errors', 'w') as outputfh:
    for item in listerrors:
        outputfh.write(item + '\n')

with open('output-dates', 'w') as outputfh:
    for item in outputlist:
        outputfh.write(item + '\n')
