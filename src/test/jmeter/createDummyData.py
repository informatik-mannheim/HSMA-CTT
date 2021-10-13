import csv

usersFileStudenten1 = open('/home/emely/Schreibtisch/CTT/Studenten1.csv', 'w', newline='')
usersFileStudenten2 = open('/home/emely/Schreibtisch/CTT/Studenten2.csv', 'w', newline='')
usersFileGäste1 = open('/home/emely/Schreibtisch/CTT/Gäste1.csv', 'w', newline='')
usersFileGäste2 = open('/home/emely/Schreibtisch/CTT/Gäste2.csv', 'w', newline='')
writerStudenten1 = csv.writer(usersFileStudenten1)
writerStudenten2 = csv.writer(usersFileStudenten2)
writerGäste1 = csv.writer(usersFileGäste1)
writerGäste2 = csv.writer(usersFileGäste2)


students =[]
count = 0
amountStudents = 1000

while count < amountStudents:
    count += 1
    students.append(f'{count}@stud.hs-mannheim.de')
for idx1, student in enumerate(students):
    if idx1 < (amountStudents / 2):
        writerStudenten1.writerow([student])
    else:
        writerStudenten2.writerow([student])
usersFileStudenten1.close()
usersFileStudenten2.close()

print('Students are done')

guests =[]
count = 0
amountGuests = 1000

while count < amountGuests:
    count += 1
    guest = f'{count}@gmail.com'
    guest += f',sample name {count}'
    guest += f',samplestreet'
    guest += f',123{count}'
    guests.append(guest)
for idx2, guest in enumerate(guests):
    if idx2 < (amountGuests / 2):
        writerGäste1.writerow([guest])
    else:
        writerGäste2.writerow([guest])

usersFileGäste1.close()
usersFileGäste2.close()

print('Guests are done')