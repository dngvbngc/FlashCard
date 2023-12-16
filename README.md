# CSC120-FinalProject

## Deliverables:
 - Final codebase
 - Annotated architecture diagram (see architecture_diagram.jpg)
 - Design justification (below)
 - `cheatsheet.md`
 - `rubric.md`
  
## Design justification
 - This application has 2 main classes, User and Set, alongside a helper class Hash (adopted from https://www.geeksforgeeks.org/md5-hash-in-java/) which implements the MD5 Hashing Algorithm to encrypt user's password on the backend, thus preventing one user to access another's account. 

 - This design is simplified from a former architecture that includes 4 main classes: User, Subject, Set, and Card. In this former design, a user must create a Subject before creating a Set within that Subject. The Subject can have multiple Sets in it, and a Set can have any number of Cards. However, since the former Card class includes only a `term` and a `definition` attributes, I concluded that it would be unnecessary to create this class and instead used Java's own Hashtable class with built-in key and value attributes, as well as a method to loop over each key-value pairs useful to implement the study and test options in a Set. I also opted to remove the Subject class because the process of creating a Subject -> Set -> Add term was rather too many steps for the user. Instead, the user can note down the subject of a Set when creating that Set.

 - As for User and Set, I decided to remove as many attributes as possible, keeping only `id` and `currentSet` for User, and `id` and `name` for Set. This is because most methods of these 2 sets only require the `id` attribute to retrieve data from the database. 

## Additional Reflection Questions
 - What was your **overall approach** to tackling this project?
   - First, I implemented a simpler version of the application without a database. For instance, I used ArrayList to store Cards in a Set. This helps to quickly build up an effective architecture for the application, where I could test and review the basic functions and flow of the program without dealing with database-related errors. When I have refined the architecture to only the neccessary classes, I began integrating the database into the classes' methods.

 - What **new thing(s)** did you learn / figure out in completing this project?
    - Using JDBC in Java
    - Adding JAR files to class path

 - Is there anything that you wish you had **implemented differently**?
    - Adding some features mentioned in the answer to the question below.

 - If you had **unlimited time**, what additional features would you implement?
    1. Ability to input non-alphabetical characters (e.g Chinese characters)
    2. Ability to quickly convert CSV file into a new set (The user can type in the path to a CSV file on their computer and have it converted into a set, but most users would have problem with finding the correct file  paths)
    3. An ability to terminate the current process (e.g make new set, add cards, delete card) using `.exit` at the input prompt, rather than having to kill the entire program using `Ctrl C`
    4. Not showing a user's password when they type in a new or old password, instead adding a confirmation prompt for all password input.

 - What was the most helpful **piece of feedback** you received while working on your project? Who gave it to you?
    1. Avoid creating multiple scanners reading from System.in. This prevent the ability to close any of those scanners without disrupting the whole program. Another student asked this question on the CSC120 Discord group and prompted me to consolidate all my scanners.
    2. On demo day, I noticed many testers was surprised that the prompt for `subject` appeared after the prompt for `set name`, because they had already put the name of the subject in the set name. Therefore, I moved the prompt for the `subject` before the prompt for the `set name`.

 - If you could go back in time and give your past self some **advice** about this project, what hints would you give?
    - Remember to close all instances of Connection after using them. Forgetting to close a connection might yeild unexpected SQL-BUSY Exceptions. 
