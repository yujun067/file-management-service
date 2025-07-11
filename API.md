# File Management Service API Documentation

## Endpoint

- **URL:** `/api/v1/files`
- **Method:** `POST`
- **Content-Type:** `application/json`
- **Protocol:** JSON-RPC 2.0

All requests must follow the [JSON-RPC 2.0 specification](https://www.jsonrpc.org/specification).

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

## Batch Request Support

- The API supports batch requests by sending an array of JSON-RPC request objects in a single POST.
- If any request in the batch encounters an error, processing will stop immediately.
- The response will be an array containing only the responses for the requests that were processed up to and including the error.
- Remaining requests in the batch will not be processed.

---

## Methods

### 1. `getFileInfo`
- **Description:** Get file or directory information.
- **Params:**
  ```json
  { "path": "<relative-path>" }
  ```
- **Returns:**
  ```json
  {
    "name": "<string>",
    "path": "<string>",
    "size": <long>,
    "directory": <boolean>
  }
  ```

### 2. `listDirectoryChildren`
- **Description:** List children of a directory.
- **Params:**
  ```json
  { "path": "<relative-path>" }
  ```
- **Returns:**
  ```json
  {
    "fileInfos": [
      {
        "name": "<string>",
        "path": "<string>",
        "size": <long>,
        "directory": <boolean>
      },
      ...
    ]
  }
  ```

### 3. `createEntry`
- **Description:** Create a file or folder.
- **Params:**
  ```json
  { "path": "<relative-path>", "type": "file" | "folder" }
  ```
- **Returns:**
  ```json
  {
    "name": "<string>",
    "path": "<string>",
    "size": <long>,
    "directory": <boolean>
  }
  ```

### 4. `deleteEntry`
- **Description:** Delete a file or folder.
- **Params:**
  ```json
  { "path": "<relative-path>" }
  ```
- **Returns:**
  ```json
  { "path": "<string>" }
  ```

### 5. `moveEntry`
- **Description:** Move a file or folder.
- **Params:**
  ```json
  { "sourcePath": "<string>", "targetPath": "<string>" }
  ```
- **Returns:**
  ```json
  { "sourcePath": "<string>", "targetPath": "<string>" }
  ```

### 6. `copyEntry`
- **Description:** Copy a file or folder.
- **Params:**
  ```json
  { "sourcePath": "<string>", "targetPath": "<string>" }
  ```
- **Returns:**
  ```json
  { "sourcePath": "<string>", "targetPath": "<string>" }
  ```

### 7. `readFileSegment`
- **Description:** Read a segment of a file (base64 encoded).
- **Params:**
  ```json
  { "path": "<string>", "offset": <long>, "length": <int> }
  ```
- **Returns:**
  ```json
  { "data": "<base64 string>" }
  ```

### 8. `appendDataToFile`
- **Description:** Append data to a file (base64 encoded).
- **Params:**
  ```json
  { "path": "<string>", "data": "<base64 string>" }
  ```
- **Returns:**
  ```json
  { "path": "<string>", "appendLength": <int> }
  ```

---

## Error Codes

| Code      | Name              | Description                                      | Example Scenario                        |
|-----------|-------------------|--------------------------------------------------|-----------------------------------------|
| -32601    | METHOD_NOT_FOUND  | Method not found                                 | Unknown method name                     |
| -32602    | INVALID_PARAMS    | Invalid parameters                               | Path outside root, bad type, bad offset |
| -32603    | INTERNAL_ERROR    | Internal server error                            | Unhandled exception                     |
| -32000    | FILE_NOT_FOUND    | File or directory not found                      | Path does not exist                     |
| -32001    | FILE_OPP_ERROR    | File operation error (e.g., permission denied, disk error) | File system operation fails             |
| -32002    | CONFLICT_ERROR    | Conflict error (e.g., target already exists, self-contained move/copy) | Target already exists, move/copy to subdirectory |

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
- **-32001 (FILE_OPP_ERROR):** A file operation error occurred (e.g., permission denied, disk full, IO error, etc.).
- **-32002 (CONFLICT_ERROR):** A conflict occurred (e.g., target already exists, move/copy to subdirectory).

---

## Example Error Scenarios

- **Path outside root:**  
  Returns `-32602` with message containing "outside root".
- **File not found:**  
  Returns `-32000` with message containing "File not found".
- **Invalid type for createEntry:**  
  Returns `-32602` with message containing "type must be 'file' or 'folder'".
- **I/O error (e.g., permission denied):**  
  Returns `-32001` with message containing the I/O error details.
- **Target already exists:**  
  Returns `-32002` with message containing "target already exists".
- **Move/copy to subdirectory of itself:**  
  Returns `-32002` with message containing "own subdirectories".

---

## Notes

- All file paths are relative to the configured root directory.
- All data for file read/write is base64 encoded.
- The `id` field in the response matches the request. 