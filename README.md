# Messing Backend

Backend of **Messing**, a real-time chat application inspired by Discord.
Built with **Spring Boot 4**, **Kotlin**, **Spring Security**, **Spring Data JPA**, **WebSocket/STOMP**, and **MariaDB**.

## Overview

The backend is responsible for:

- user authentication and authorization
- server, channel, and member management
- chat message persistence and delivery
- invite creation and invite acceptance
- file and image upload handling
- voice / presence / signaling support
- websocket-based realtime communication
- validation and global error handling

## Tech stack

- Spring Boot 4.0.4
- Kotlin 2.2.21
- Spring Security
- Spring Data JPA
- Spring Web MVC
- Spring WebSocket
- Spring Session Data Redis
- Bean Validation
- MariaDB
- Cloudinary
- JWT (`jjwt`)
- GraphQL code generation via Netflix DGS plugin

## Requirements

- Java 17+ runtime target
- Maven
- MariaDB
- Redis if session/data caching is enabled in your environment

> The project is configured with `java.version=22`, `maven.compiler.release=17`, and `kotlin.compiler.jvmTarget=17`.
> In practice, use a JDK compatible with the build configuration used by your environment.

## Project setup

From the `Messing` directory:

```sh
mvn clean install
```

This will compile the backend and run the test phase if your environment is configured correctly.

## Run locally

Start the backend with Maven:

```sh
mvn spring-boot:run
```

Or run the generated jar after building:

```sh
java -jar target/Messing-0.0.1-SNAPSHOT.jar
```

## Main responsibilities by layer

### Controllers

Controllers expose REST endpoints and websocket entry points for the frontend.

Common responsibilities:

- login / register
- server and channel CRUD
- message APIs
- invite acceptance
- call signaling
- profile updates

### Services

Services contain the business rules.

Examples:

- auth and JWT handling
- server membership validation
- channel permission checks
- invite expiry / cleanup
- file upload coordination
- presence and call state management

### Repositories

Repositories handle database access for:

- users
- servers
- channels
- messages
- members
- invites

### Entities

Entities model the persisted domain:

- `User`
- `Server`
- `Channel`
- `Message`
- `ServerMember`
- `ServerInvite`

### Config

Configuration classes manage:

- security filters and auth rules
- websocket/STOMP setup
- static resources / upload paths
- CORS / infrastructure behavior

## Runtime configuration

The backend typically needs configuration for:

- MariaDB connection
- JWT secret and token settings
- Cloudinary credentials
- Redis connection if session support is active
- websocket endpoints and allowed origins
- upload paths / static content mapping

Check `src/main/resources/application.properties` for the current runtime settings.

## Realtime features

Messing backend supports realtime communication through websocket/STOMP.

This is used for:

- chat message delivery
- presence updates
- voice / call signaling

Relevant components include:

- `WebSocketConfig`
- `ChatController`
- `CallSignalingController`
- `WebSocketPresenceListener`
- `CallPresenceService`

## GraphQL code generation

The backend includes the Netflix DGS GraphQL codegen plugin.

Generated sources are configured from:

- `src/main/resources/graphql-client/`

If you add a remote GraphQL schema, place it in that folder and run the Maven generation phase.

## Documentation guide

If you want to understand the backend quickly, read the project docs in this order:

1. `../PROJECT_README_ROADMAP.md`
2. `../ARCHITECTURE_SUMMARY.md`
3. `../DETAILED_FILE_INDEX.md`
4. `../MODULE_GUIDE.md`
5. `../FLOW_GUIDE.md`

## Suggested backend reading order

1. `src/main/resources/application.properties`
2. `src/main/kotlin/com/example/messing/config`
3. `src/main/kotlin/com/example/messing/entity`
4. `src/main/kotlin/com/example/messing/repository`
5. `src/main/kotlin/com/example/messing/service`
6. `src/main/kotlin/com/example/messing/controller`
7. `src/main/kotlin/com/example/messing/dto`
8. `src/main/kotlin/com/example/messing/exception`

## Notes

- Keep controller logic thin.
- Put business rules in services.
- Keep DTOs separate from entities.
- Validate file upload and websocket state carefully.
- Use the global exception handler to keep API errors consistent.
