- Problem: "DB complains about missing constrain between role and user table". The error is returned as the api response:"error": "Internal Server Error" "message": "User Role not set.", 
    - solution: INSERT INTO roles(name) VALUES('ROLE_FAMILY');
                INSERT INTO roles(name) VALUES('ROLE_ADMIN');
                INSERT INTO roles(name) VALUES('ROLE_FRIEND');
                INSERT INTO roles(name) VALUES('ROLE_OUTSIDER');

                