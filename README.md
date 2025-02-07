# SpringBootExample
Basic CRUD (Create, Read, Update and Delete) application made in SpringBoot with a simple html frontend.
## Brief Overview
### How it works
This is a very rough prototype of a disaster relief web app. Users can send request through the request page of the front end and an email of confirmation will be sent.
### Frontend
Users can load info and send a post request to the backend at the request page by clicking the send button.
### Backend
1. Use of MySQL database for data storage.
2. Use of Jackson-DataBind to handle entity mapping and smooth interaction with the MySQL database.
3. Use of Google's SMTP service to automate the email sending service.

PS: the database service is just a mockup, please implement your own database service based on your needs and store all the credentials in application.properties!
### Port Forwarding
You can use port fowarding services like Serveo to expose your port to the public instead of sharing your IP address :)
