# Compiling the program

To compile the program, make sure the 3 jar files included in this directory is in the class path.
- slf4j-api-1.7.36.jar
- slf4j-nop-1.6.1.jar
- sqlite-jdbc-3.44.1.0.jar

# Log in

User must log in before accessing any of the features. 

New users can enter `B` (case insensitive) at the command line when prompted to make a new account. Existing user scan enter `A` (case insensitive) at the command line when prompted to log in. 

Your account information will be saved upon registration, hence exiting the program will not affect your data. You can log in again upon rerunning the program.

# Acount Menu

Upon logging in, the user has 7 options:

1. **Change password**: To change their password, the user must enter their current password. Entering the wrong password 3 times will terminate the process. When the correct password is entered, they can enter their new password.
2. **View your sets**: View all study sets the user has previously made. New user will have 0 sets.
3. **Make new set**: Make a new empty set by filling out the subject and the name of the set. The set will be automatically given an ID for future access.
4. **Go to set (Add/Delete/Print/Study/Test)**: When prompted, enter the ID of an available set to go into the set and access more features. Non-numeric entries or invalid ID will cause the program to reprompt the user until a valid ID is given.
5. **Delete set**: Delete an available set. Be careful: Deleting a set will delete all of its content. If you only want to delete a few terms in the set, select option **Go to set** and delete the terms instead.
6. **Log out**: Log out of the current account.
7. **Exit**: Terminate the program.

To choose your option, enter a number from 1 to 7 at the command line. Invalid entries will cause the program to reprompt until a valid entry is given.

# Set Menu

When the user successfully go to a set, they can choose from 7 options:

1. **View all cards in this set**: View available terms and definitions in the set.
2. **Study cards in set**: The user will first be prompted to configure the study modes: study definitions (user will input the definitions) or study terms (user will input the terms), case sensitive or case insensitive. 
- For each card in the set, the user must enter the correct term/definition provided at random until all cards have been tested. 
- When the user enter the wrong answer, they will be reprompted to answer until the correct answer is given. The correct answer will be given on the fourth try, which the user must input to move onto the next card. The term/definition will be given again at random.
3. **Test cards in set**: Similar to study mode. However, the user can only enter the answer to each term/definition once. A final score will be calculated based on the percentage of correct terms/definitions overall.
4. **Add cards**: Add terms to the set. The first prompt will ask for the number of terms the user want to add. After a valid positive number is given, the user will be prompted to enter the term(s) and definition(s) until the number of terms have been reached.
5. **Delete card**: Delete a term from the set. This option will only delete 1 term at a time. The user must enter the term (case sensitive) they want to remove from the set.
6. **Go back to main menu**: Go back to account menu.
7. **Exit**: Terminate the program.

To choose your option, enter a number from 1 to 7 at the command line. Invalid entries will cause the program to reprompt until a valid entry is given.

## Further instruction
- You will **not** be able to change your username after registration! Choose carefully. The username can also be changed via the back end by modifying `flashcards.db` in the `\data` directory.
- Your password will be hashed on the back-end for privacy. Only you will know your password. Make sure to remember it well. Changing password without your old password can only be done via the back end.
- To terminate the program at any time (especially during an active process): `Ctrl C`

**Have fun studying!**