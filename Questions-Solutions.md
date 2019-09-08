- Problem: "DB complains about missing constrain between role and user table". The error is returned as the api response:"error": "Internal Server Error" "message": "User Role not set.", 
    - solution: INSERT INTO roles(name) VALUES('ROLE_FAMILY');
                INSERT INTO roles(name) VALUES('admin');
                INSERT INTO roles(name) VALUES('ROLE_FRIEND');
                INSERT INTO roles(name) VALUES('ROLE_OUTSIDER');

- How to run the app, use different profile(dev, prod) and override the properties in the app.properties file?
    - sol: java -jar -Dspring.profiles.active=dev target/authapi-0.0.1-production-candidate.jar --app.adminpassword=raining
    - alt sol: mvn -Dspring.profiles.active=dev spring-boot:run -Dspring-boot.run.arguments="--app.adminpassword=raining"