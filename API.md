# File Management Service API Documentation

## Endpoint

- **URL:** `/filemanage`
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