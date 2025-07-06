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

## API Documentation

For complete details about the JSON-RPC API, including endpoints, request and response structures, available methods, and error codes, please refer to the [API.md](./API.md) file.

---

## Request Structure

```json
{
  "jsonrpc": "2.0",
  "method": "<methodName>",
  "params": { ... },
  "id": "<client-generated-id>"
}
```

---

## Response Structure

**Success:**
```json
{
  "jsonrpc": "2.0",
  "result": { ... },
  "id": "<same-as-request>"
}
```

**Error:**
```json
{
  "jsonrpc": "2.0",
  "error": {
    "code": <int>,
    "message": "<error description>"
  },
  "id": "<same-as-request>"
}
```

---

## Methods

### 1. `getFileInfo`
- **Description:** Get file or directory information.
- **Params:** `{ "path": "<relative-path>" }`
- **Returns:** `{ "name": "...", "path": "...", "size": <bytes> }`

### 2. `listDirectoryChildren`
- **Description:** List children of a directory.
- **Params:** `{ "path": "<relative-path>" }`
- **Returns:** `[{ "name": "...", "path": "...", "size": <bytes>, "directory": <bool> }, ...]`

### 3. `createEntry`
- **Description:** Create a file or folder.
- **Params:** `{ "path": "<relative-path>", "type": "file" | "folder" }`
- **Returns:** `{ "name": "...", "path": "...", "size": <bytes>, "directory": <bool> }`

### 4. `deleteEntry`
- **Description:** Delete a file or folder.
- **Params:** `{ "path": "<relative-path>" }`
- **Returns:** `{ "path": "..." }`

### 5. `moveEntry`
- **Description:** Move a file or folder.
- **Params:** `{ "sourcePath": "...", "targetPath": "..." }`
- **Returns:** `{ "sourcePath": "...", "targetPath": "..." }`

### 6. `copyEntry`
- **Description:** Copy a file or folder.
- **Params:** `{ "sourcePath": "...", "targetPath": "..." }`
- **Returns:** `{ "sourcePath": "...", "targetPath": "..." }`

### 7. `readFileSegment`
- **Description:** Read a segment of a file (base64 encoded).
- **Params:** `{ "path": "...", "offset": <int>, "length": <int> }`
- **Returns:** `{ "data": "<base64 string>" }`

### 8. `appendDataToFile`
- **Description:** Append data to a file (base64 encoded).
- **Params:** `{ "path": "...", "data": "<base64 string>" }`
- **Returns:** `{ "path": "...", "appendLength": <int> }`

---

## Error Codes

| Code      | Name              | Description                                      | Example Scenario                        |
|-----------|-------------------|--------------------------------------------------|-----------------------------------------|
| -32601    | METHOD_NOT_FOUND  | Method not found                                 | Unknown method name                     |
| -32602    | INVALID_PARAMS    | Invalid parameters                               | Path outside root, bad type, bad offset |
| -32603    | INTERNAL_ERROR    | Internal server error                            | Unhandled exception                     |
| -32000    | FILE_NOT_FOUND    | File or directory not found                      | Path does not exist                     |
| -32001    | IO_ERROR          | IO Error (e.g., permission denied, disk error)   | File system operation fails             |

**Error Response Example:**
```json
{
  "jsonrpc": "2.0",
  "error": {
    "code": -32000,
    "message": "File not found: nonexistent.txt"
  },
  "id": "case-1"
}
```

### Error Code Details

- **-32601 (METHOD_NOT_FOUND):** The requested method does not exist or is not available.
- **-32602 (INVALID_PARAMS):** The parameters are invalid (e.g., missing, wrong type, path outside root, invalid file type, etc.).
- **-32603 (INTERNAL_ERROR):** An unexpected server error occurred.
- **-32000 (FILE_NOT_FOUND):** The specified file or directory does not exist.
- **-32001 (IO_ERROR):** An I/O error occurred (e.g., permission denied, disk full, etc.).

---

## Example Error Scenarios

- **Path outside root:**  
  Returns `-32602` with message containing "Outside root folder".
- **File not found:**  
  Returns `-32000` with message containing "File not found".
- **Invalid type for createEntry:**  
  Returns `-32602` with message containing "type must be 'file' or 'folder'".
- **I/O error (e.g., permission denied):**  
  Returns `-32001` with message containing the I/O error details.

---

## Notes

- All file paths are relative to the configured root directory.
- All data for file read/write is base64 encoded.
- The `id` field in the response matches the request.

---

## Testing

Unit tests are located in `src/test/java/com/jetbrains/filesystem/`.

To run all tests, use:

```sh
mvn clean test
```

or run all tests without maven installed.

```sh
./mvnw clean test
```

## Deployment

### Docker

You can deploy the service as a Docker container:

1. Build the Docker image:
   ```sh
   docker build -t file-management-service .
   ```
2. Run the container:
   ```sh
   docker run -p 8081:8081 file-management-service
   ```
   - Use `-v` to mount a custom config or data directory if needed.

### Docker Compose

A sample `docker-compose.yml` is provided. Start the service with:
```sh
docker-compose up --build
```

### Kubernetes (Helm)

A Helm chart is provided in the `helm/` directory for Kubernetes deployment. To install using Helm:

```sh
cd helm
helm install file-management-service .
```

You can customize values in the `values.yaml` file for your environment.
