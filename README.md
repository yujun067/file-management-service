# File Management Service

## Overview
This project implements an HTTP JSON-RPC 2.0 file management service based on Java (Spring Boot), allowing clients to perform file system operations within a configurable root directory. All file paths are relative to this root directory. The service supports file info retrieval, directory listing, creation, deletion, moving/copying, data appending, and reading file contents.

---

## Features
- Retrieve file/directory information (name, path, size, type)
- List children of a folder
- Create an empty file or folder
- Delete a file or folder
- Move or copy a file/folder
- Append data to a file (concurrent write isolation)
- Read file content by offset
- Thread-safe concurrent writes
- HTTP JSON-RPC 2.0 API
- Docker containerization support
- Helm Chart deployment to Kubernetes
- Unit testing (JUnit)

---

## Technology Stack
| Layer         | Technology                                      |
|--------------|-------------------------------------------------|
| Language     | Java 17                                         |
| Framework    | Spring Boot                                     |
| API Protocol | JSON-RPC 2.0 (Jackson + custom controller)      |
| Build Tool   | Maven                                           |
| Testing      | JUnit 5, Mockito                                |
| Concurrency  | Java `ReentrantLock` |
| Logging      | log4j2                                          |
| Container    | Docker                                          |
| Deployment   | Helm Chart for Kubernetes                       |

---

## Project Structure

```
file-management-service/
├── API.md                  # API documentation
├── README.md
├── docker-compose.yml
├── Dockerfile
├── file_locking_strategy.md
├── data/                   # Default data mount directory
├── config/                 # Optional custom config mount directory
├── file-management-service/ # Helm Chart directory
│   ├── Chart.yaml
│   ├── values.yaml
│   └── templates/
├── src/
│   ├── main/
│   │   ├── java/com/jetbrains/filesystem/
│   │   │   ├── api/         # FileManager interface
│   │   │   ├── config/      # Configuration classes
│   │   │   ├── controller/  # JSON-RPC controller
│   │   │   ├── dto/         # Data transfer objects
│   │   │   │   ├── file/
│   │   │   │   └── rpc/
│   │   │   ├── exception/   # Exception definitions
│   │   │   ├── handler/     # JSON-RPC method handlers
│   │   │   ├── lock/        # File lock implementations
│   │   │   ├── registry/    # Handler registry
│   │   │   ├── service/     # File management logic
│   │   │   ├── storage/     # File storage implementations
│   │   │   └── util/        # Utility classes
│   │   └── resources/
│   │       ├── application.yaml
│   │       └── log4j2.xml
│   └── test/
│       └── java/com/jetbrains/filesystem/
│           ├── controller/
│           ├── handler/
│           ├── manager/
│           └── util/
├── mvnw
├── mvnw.cmd
└── pom.xml
```

---

## Configuration
Configuration files are located in `src/main/resources/application.yaml` and its profile versions. Example:

```yaml
spring:
  application:
    name: filesystem

fileservice:
  rootFolder: /tmp/my-root

server:
  port: 8081
```

- `fileservice.rootFolder`: The root directory for all file operations (default `/tmp/my-root`).
- `server.port`: The port the service listens on (default `8081`).

---

## File Locking Strategy

For details about file locking and concurrency control, see [file_locking_strategy.md](./file_locking_strategy.md).

---

## Building and Deployment

### Docker

1. **Build the image:**
   ```sh
   docker build -t file-management-service .
   ```
2. **Run the container:**
   ```sh
   docker run -p 8081:8081 -d file-management-service
   ```

### Docker Compose

A sample `docker-compose.yml` is provided. Start the service:
```sh
docker-compose up -d --build
```

### Kubernetes (Helm)

The Helm chart is located in the `file-management-service/` directory. To install:

```sh
helm install file-service ./helm
```

You can customize parameters in `values.yaml` for your environment.

---

## API Documentation

For complete JSON-RPC API details (including endpoints, request/response structures, methods, error codes, etc.), see [API.md](./API.md).

---

## Testing

Unit tests are located in `src/test/java/com/jetbrains/filesystem/`.

To run all tests:

```sh
mvn clean test
```

Or without a local Maven installation:

```sh
./mvnw clean test
```