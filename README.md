# CPT202 Task

This project is a Spring Boot web application for a contributor application workflow. It allows users to edit their profile, submit a contributor application, track application status, and lets admins review pending applications.

## Tech Stack

- Java 21
- Spring Boot 3
- Spring MVC
- Spring Data JPA
- Thymeleaf
- MySQL
- Maven

## Features

- Edit user profile information
- Submit a contributor application with a reason
- Prevent duplicate pending applications
- View the latest application status
- Review pending applications in an admin page
- Approve or reject applications with reviewer comments

## Project Structure

```text
src/main/java/com/cpt202/task
|- controller      Web controllers for profile, application, and approval flows
|- entity          JPA entities and enums
|- repository      Database access interfaces
|- service         Business logic

src/main/resources/templates
|- profile         Profile pages
|- contributor     Contributor application pages
|- admin           Admin review pages
```

## Prerequisites

- JDK 21
- Maven 3.9+ or the included Maven wrapper
- MySQL running locally

## Configuration

The default database configuration is in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/cpt202?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=password
```

Before running the project, make sure:

1. A MySQL database named `cpt202` exists.
2. The username and password match your local MySQL setup.

## Run the Project

Using Maven wrapper:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The application starts on:

```text
http://localhost:8080
```

## Example Routes

- Edit profile: `http://localhost:8080/profile/edit?userId=1`
- Apply as contributor: `http://localhost:8080/contributor/apply?userId=1`
- View application status: `http://localhost:8080/contributor/status?userId=1`
- Review pending applications: `http://localhost:8080/admin/contributor-applications`

## Notes

- JPA is configured with `spring.jpa.hibernate.ddl-auto=update`, so tables are updated automatically based on entities.
- The current pages assume test data already exists in the database, such as users and reviewer accounts.
