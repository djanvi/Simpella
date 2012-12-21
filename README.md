Simpella
========

Implementation of a distributive search and file sharing protocol



Readme :


Directions to run the program:
Go to the directory Simpella and run the program from there (cd Simpella)
Do ‘make’
Then run the program using the below command: 
 java -cp ./src core.Simpella <port1> <port2>

Below sequence of commands is necessary:
Open
Share
Scan
Update

Each user must share a directory which will be used to keep the downloaded file alongwith the files to be shared.
Without ‘update’ command, command ‘info’ will not give accurate results.

Downloading status can be seen by info d command.

Rest of the commands are implemented as in the project requirement.

Since the program extensively uses many data structures and threads, it is memory intensive. More than 4 instances of a program give exception.
Once a connection is closed, other end throws exception since conenction is closed.

  

