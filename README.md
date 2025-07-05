# File Management Service

## Overview
This project implements an HTTP JSON-RPC 2.0 service in Java (Spring Boot), allowing clients to perform file system operations within a configurable root directory. All file paths are treated as relative paths inside this root folder. The service exposes operations such as file info retrieval, directory listing, creation, deletion, moving/copying, data appending, and reading file contents.

---

## Features
- Retrieve file information (name, path, size)
- List children of a folder
- Create an empty file or folder
- Delete a file or folder
- Move or copy a file/folder
- Append data to a file (with concurrent writes isolation)
- Read N bytes from a file at a specific offset
- Thread-safe concurrent writes
- HTTP JSON-RPC 2.0 API
- Containerized (Docker)
- Deployable via Helm chart to Kubernetes
- Unit-tested (JUnit)

---

## Technology Stack
| Layer           | Technology                                      |
|-----------------|-------------------------------------------------|
| Language        | Java 17                                         |
| Framework       | Spring Boot                                     |
| API Protocol    | JSON-RPC 2.0 (Jackson + custom controller)      |
| Build Tool      | Maven                                           |
| Testing         | JUnit 5, Mockito                                |
| Concurrency     | Java `synchronized`, `ReentrantLock`, FileLocks |
| Logging         | log4j2                                          |
| Containerization| Docker                                          |
| Deployment      | Helm Chart for Kubernetes                       |

---

## Project Structure
```
main/
  java/com/jetbrains/filesystem/
    controller/   # JSON-RPC controller
    service/      # File management logic
    dto/          # Data transfer objects
    config/       # Configuration classes
  resources/
    application.yaml  # Main config
    application-dev.yaml
    application-prod.yaml
```

---

## Configuration
Configuration is managed via `src/main/resources/application.yaml` (and profile-specific YAMLs). Example:

```yaml
spring:
  application:
    name: filesystem
  profiles:
    active: dev

fileservice:
  rootFolder: /tmp/my-root

server:
  port: 8081
```

- `fileservice.rootFolder`: The root directory for all file operations (default: `/tmp/my-root`).
- `server.port`: The port the service listens on (default: `8081`).
- Profiles: Use `dev` or `prod` for different environments.

You can override these at runtime using environment variables or by mounting a custom YAML file (see Docker Compose section).

---

## Building and Running

### With Docker
1. **Build the image:**
   ```sh
   docker build -t file-management-service .
   ```
2. **Run the container:**
   ```sh
   docker run -p 8081:8081 file-management-service
   ```
   - Use `-v` to mount a custom config or data directory if needed.

### With Docker Compose
A sample `docker-compose.yml`:
```yaml
version: '3.8'
services:
  file-management-service:
    build: .
    container_name: file-management-service
    ports:
      - "8081:8081"
    environment:
      SPRING_PROFILE: "dev"
      JAVA_OPTS: "-Xms512m -Xmx1024m"
    volumes:
      - ./data:/tmp/my-root
      - ./config:/app/config
    command: >
      sh -c "java $JAVA_OPTS -jar app.jar --spring.config.location=classpath:/,file:/app/config/application-${SPRING_PROFILE}.yml"
```
- To override configuration, place your YAML in `./config` and set `SPRING_PROFILE` accordingly.

---

## API Usage (JSON-RPC 2.0)
All requests are POSTed to `/filemanage` with a JSON-RPC 2.0 body. Example methods:

### 1. getFileInfo
Request:
```json
{
  "jsonrpc": "2.0",
  "method": "getFileInfo",
  "params": { "path": "myfolder/file01.txt" },
  "id": 1
}
```
Response:
```json
{
  "jsonrpc": "2.0",
  "result": { "name": "myfile.txt", "path": "myfolder/file01.txt", "size": 13 },
  "id": 1
}
```

### 2. listDirectoryChildren
Request:
```json
{
  "jsonrpc": "2.0",
  "method": "listDirectoryChildren",
  "params": { "path": "myfolder" },
  "id": 2
}
```
Response:
```json
{
  "jsonrpc": "2.0",
  "result": [
    { "path": "myfolder/file02.txt", "name": "file2.txt", "size": 10, "directory": false },
    { "path": "myfolder/fold02", "name": "fold02", "size": 64, "directory": true }
  ],
  "id": 2
}
```

### 3. createEntry
Request (file):
```json
{
  "jsonrpc": "2.0",
  "method": "createEntry",
  "params": { "path": "myfolder/file03.txt", "type": "file" },
  "id": 3
}
```
Request (folder):
```json
{
  "jsonrpc": "2.0",
  "method": "createEntry",
  "params": { "path": "myfolder/folder03", "type": "folder" },
  "id": 3
}
```

### 4. deleteEntry
Request:
```json
{
  "jsonrpc": "2.0",
  "method": "deleteEntry",
  "params": { "path": "folder03" },
  "id": 4
}
```

### 5. moveEntry
Request:
```json
{
  "jsonrpc": "2.0",
  "method": "moveEntry",
  "params": { "sourcePath": "folder01/file1.txt", "targetPath": "folder02/file5.txt" },
  "id": 5
}
```

### 6. copyEntry
Request:
```json
{
  "jsonrpc": "2.0",
  "method": "copyEntry",
  "params": { "sourcePath": "folder01/file1.txt", "targetPath": "folder02/file5.txt" },
  "id": 6
}
```

### 7. readFileSegment
Request:
```json
{
  "jsonrpc": "2.0",
  "method": "readFileSegment",
  "params": { "path": "folder07/file07.txt", "offset": 0, "length": 100 },
  "id": 7
}
```
- Response `data` is base64 encoded.

### 8. appendDataToFile
Request:
```json
{
  "jsonrpc": "2.0",
  "method": "appendDataToFile",
  "params": { "path": "folder08/file08.txt", "data": "MTIz" },
  "id": 8
}
```
- Request `data` is base64 encoded.

---

## Testing
- Unit tests are in `src/test/java/com/jetbrains/filesystem/`
- Run all tests:
  ```sh
  mvn clean test
  ```

---

## Deployment
- **Docker:** See above for build/run instructions.
- **Kubernetes:** A Helm chart is provided in the `helm/` directory for easy deployment.

---

