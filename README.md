###1. Setting Security Config
- Add next to src/main/resources/application-dev.properties
    - Modify the database source and email account information to your own. 
```properties
spring.datasource.url=dbc:h2:tcp://localhost/~/study-olleh
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver

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