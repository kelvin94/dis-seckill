- Problem: "DB complains about missing constrain between role and user table". The error is returned as the api response:"error": "Internal Server Error" "message": "User Role not set.", 
    - solution: INSERT INTO roles(name) VALUES('ROLE_USER');
                INSERT INTO roles(name) VALUES('ROLE_ADMIN');
                