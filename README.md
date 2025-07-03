# File System JSON-RPC Service

## Overview

This project implements an HTTP JSON-RPC 2.0 service in Java, allowing clients to perform file system operations within a configurable root directory.

All file paths are treated as relative paths inside this root folder.  
The service will expose operations such as file info retrieval, directory listing, creation, deletion, file moving/copying, data appending, and reading file contents.

---

## Functional Requirements

- ✅ Retrieve file information (name, path, size)
- ✅ List children of a folder
- ✅ Create an empty file or folder
- ✅ Delete a file or folder
- ✅ Move or copy a file/folder
- ✅ Append data to a file (with concurrent writes isolation)
- ✅ Read N bytes from a file at a specific offset

---

## Non-Functional Requirements

- ✅ Concurrent writes to a file (append) must be thread-safe
- ✅ Expose as HTTP JSON-RPC 2.0 API
- ✅ Containerized (Docker)
- ✅ Deployable via Helm chart to Kubernetes
- ✅ Unit-tested (JUnit)

---

## Initial Technology Stack

| Layer | Technology                                                 |
|---|------------------------------------------------------------|
| Language | Java (version 17)                                          |
| Framework | Spring Boot                                                |
| API Protocol | JSON-RPC 2.0 (using Jackson + custom controller)           |
| Build Tool | Maven                                                      |
| Testing | JUnit 5, Mockito                                           |
| Concurrency | Java `synchronized`, `ReentrantLock`, or FileChannel locks |
| Logging | log4j2                                                     |
| Containerization | Docker                                                     |
| Deployment | Helm Chart for Kubernetes                                  |

---

## Estimated Timeline

| Phase | Task                                | Estimated Time |
|---|-------------------------------------|---|
| Phase 1 | Project Skeleton & JSON-RPC Setup   | 0.5 day |
| Phase 2 | Implement File Operations (CRUD)    | 1 day |
| Phase 3 | Implement Concurrency-safe Function | 1 day |
| Phase 4 | Unit Testing & Error Handling       | 1 day |
| Phase 5 | Containerization & Helm Deployment  | 1 day |

---
## Current progress:
✅ Phase 4 ongoing

## JSON-RPC Request Introduction


### Function 1: getFileInfo

JSON Request Body

```json
{
  "jsonrpc": "2.0",
  "method": "getFileInfo",
  "params": {
    "path": "myfolder/file01.txt"
  },
  "id": 1
}
```

JSON Resonse Body

```json
{
  "jsonrpc": "2.0",
  "result": {
    "name": "myfile.txt",
    "path": "myfolder/file01.txt",
    "size": 13
  },
  "id": 1
}
```

### Function 2: listDirectoryChildren

JSON Request Body

```json
{
  "jsonrpc": "2.0",
  "method": "listDirectoryChildren",
  "params": {
    "path": "myfolder"
  },
  "id": 2
}
```

JSON Resonse Body

```json
{
  "jsonrpc": "2.0",
  "result": [{
    "path": "myfolder/file02.txt",
    "name": "file2.txt",
    "size": 10,
    "directory": false
  }, {
    "path": "myfolder/fold02",
    "name": "fold02",
    "size": 64,
    "directory": true
  }],
  "id": 2
}
```

### Function 3: createEntry

case 1: JSON Request Body 

```json
{
  "jsonrpc": "2.0",
  "method": "createEntry",
  "params": {
    "path": "myfolder/file03.txt",
    "type": "file"
  },
  "id": 3
}

```

case 1: JSON Resonse Body

```json
{
  "jsonrpc": "2.0",
  "result": {
    "name": "file03.txt",
    "path": "myfolder/file03.txt",
    "size": 0,
    "directory": false
  },
  "id": 3
}

```


case 2: JSON Request Body

```json
{
  "jsonrpc": "2.0",
  "method": "createEntry",
  "params": {
    "path": "myfolder/folder03",
    "type": "folder"
  },
  "id": 3
}

```


case 2: JSON Resonse Body

```json
{
  "jsonrpc": "2.0",
  "result": {
    "name": "folder03",
    "path": "myfolder/folder03",
    "size": 64,
    "directory": true
  },
  "id": 3
}
```

### Function 4: deleteEntry

JSON Request Body

```json
{
  "jsonrpc": "2.0",
  "method": "deleteEntry",
  "params": {
    "path": "folder03"
  },
  "id": 4
}
```

JSON Resonse Body

```json
{
  "jsonrpc": "2.0",
  "result": {
    "path": "folder03"
  },
  "id": 4
}
```

### Function 5: moveEntry
JSON Request Body

```json
{
  "jsonrpc": "2.0",
  "method": "moveEntry",
  "params": {
    "sourcePath": "folder01/file1.txt",
    "targetPath": "folder02/file5.txt"
  },
  "id": 5
}
```

JSON Resonse Body

```json
{
  "jsonrpc": "2.0",
  "result": {
    "sourcePath": "folder01/file1.txt",
    "targetPath": "folder02/file5.txt"
  },
  "id": 5
}
```

### Function 6: copyEntry
JSON Request Body

```json
{
  "jsonrpc": "2.0",
  "method": "copyEntry",
  "params": {
    "sourcePath": "folder01/file1.txt",
    "targetPath": "folder02/file5.txt"
  },
  "id": 6
}
```

JSON Resonse Body

```json
{
  "jsonrpc": "2.0",
  "result": {
    "sourcePath": "folder01/file1.txt",
    "targetPath": "folder02/file5.txt"
  },
  "id": 6
}
```

### Function 7: readFileSegment
JSON Request Body

file text is "Hello World!"
the data field in response is base64 encoded

```json
{
  "jsonrpc": "2.0",
  "method": "readFileSegment",
  "params": {
    "path": "folder07/file07.txt",
    "offset": 0,
    "length": 100
  },
  "id": 7
}
```

JSON Resonse Body

```json
{
  "jsonrpc": "2.0",
  "result": {
    "data": "SGVsbG8gV29ybGQhCg=="
  },
  "id": 7
}
```

### Function 8: appendDataToFile
JSON Request Body

the data field in request is base64 encoded

```json
{
  "jsonrpc": "2.0",
  "method": "appendDataToFile",
  "params": {
    "path": "folder08/file08.txt",
    "data": "MTIz"  
  },
  "id": 8
}
```

JSON Resonse Body

```json
{
  "jsonrpc": "2.0",
  "result": {
    "path": "folder08/file08.txt",
    "appendLength": 3
  },
  "id": 8
}
```

# Test Structure

- `FileServiceTest`: normal use cases (happy path)
- `FileService*ExceptionTest`: edge cases & error handling
