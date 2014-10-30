PAXChecker
==========


Features include:

-PAX Prime, East, South and Aus scanning capability

-Get into queue before the Twitter notification! (This may change at any given time, but as far as I know you most likely will)

-Text as many phones as you want when queue is found (along with a link for if you don't have access to your computer!) (Standard messaging rates probably apply, but it's basically just a text message -- if you have unlimited texting you're most likely good)

-Can use any cell carrier that supports email text messages

-Program supports Yahoo! and GMail for sending texts (GMail highly recommended. Other emails may be used -- see the in-program Instructions for how to set this up)

-Specify how often to check for tickets (10-60 seconds, to reduce request spamming and data usage)

-Track how much data is used


Command Line Args:

-alertfile <file of emails to alert>
	A file of emails to alert upon finding tickets, formated with one line per email, and no blank lines
-delay <Period between checking for tickets>
	Set how long to wait between checking for tickets
-email <email address to send alerts from>
	Set the email to alert emails from
-password <password to email address to send alerts from>
	Set the password of the email to send alerts from
-expo <Which PAX Expo to check>
	Which expo to check, currently only supports checking one expo
-nopax
	Do not check the PAX website for tickets
-noshowclix
	Do not check the showclix website for tickets
-notify <email(s) to alert>
	Set emails to alert directly in command line
-v 
	verbose
-help
	pull up help doc

Input for while program is running:

exit - exits the program
testalert - sends a test email to all addresses
check - force a check for tickets

Usage Examples:

Say you want to check the showclick and pax website for PAX East tickets, and you have a text document of email addresses to alert, you would run...

java -jar paxchecker.jar -expo east -email <email to send from> -password <password for email> -alertfile <files of emails to alert>

Say you want to check the showclick and pax website for PAX East tickets, and just want to have an email sent to you, you would run...

java -jar paxchecker.jar -expo east -email <email to send from> -password <password for email> -notify <the email to notify>
