### 1. Setting Security Config
- Add next to src/main/resources/application-dev.properties
    - Modify the database source and email account information to your own. 
```properties
# 개발할 때에만 create-drop 또는 update를 사용하고 운영 환경에서는 validate를 사용합니다.
spring.jpa.hibernate.ddl-auto=update
spring.datasource.url=jdbc:postgresql://localhost:5432/testdb
spring.datasource.username=testuser
spring.datasource.password=testpass


spring.mail.host=smtp.gmail.com
spring.mail.port=587
# 본인 gmail 계정으로 바꾸세요.
spring.mail.username=[mail account]@gmail.com
# 위에서 발급받은 App 패스워드로 바꾸세요.
spring.mail.password=[password]
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.starttls.enable=true
```

### 2. Setting Database
```shell
# 1. Install postgresql Database
brew install postgres

# 2. Start Service
brew services start postgresql

# 3. Create User & Database
psql -h localhost -d postgres
postgres=# CREATE USER testuser PASSWORD 'testpass' SUPERUSER;
postgres=# CREATE DATABASE testdb OWNER testuser;
```

-----------------------------------------------------

### Project Overview

![image](https://user-images.githubusercontent.com/30730212/107880768-46aa3e00-6f24-11eb-9852-7fd03f043d62.png)
