- Problem: "DB complains about missing constrain between role and user table". The error is returned as the api response:"error": "Internal Server Error" "message": "User Role not set.", 
    - solution: INSERT INTO roles(name) VALUES('ROLE_FAMILY');
                INSERT INTO roles(name) VALUES('admin');
                INSERT INTO roles(name) VALUES('ROLE_FRIEND');
                INSERT INTO roles(name) VALUES('ROLE_OUTSIDER');

- How to run the app, use different profile(dev, prod) and override the properties in the app.properties file?
    - sol: java -jar -Dspring.profiles.active=dev target/authapi-0.0.1-production-candidate.jar --app.adminpassword=raining
    - alt sol: mvn -Dspring.profiles.active=dev spring-boot:run -Dspring-boot.run.arguments="--app.adminpassword=raining"
- How to run "application-dev.properties", 指定dev env? 和 pass in program arguments in commandline?
    - In "Edit configuration -> 'vm option' ": -Dspring.profiles.active=dev 
    - In "Edit configuration -> 'program arguments'": --server.port=8083 --app.admin.username=jylkelvin --app.admin.password=productionpwd --app.pedestrian.username=pedestrian --app.pedestrian.password=productionpwd