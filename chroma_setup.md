# ChromaDB Setup Guide

This guide outlines how to set up **ChromaDB** locally to serve as the persistent Vector Database for the Log Bot.

## 1. Prerequisites
- Docker installed and running.

## 2. Run ChromaDB via Docker
Run the following command to start a ChromaDB server instance.

```bash
docker run -d --name chromadb \
  -p 8000:8000 \
  chromadb/chroma:0.4.24
```

*   **Port 8000**: The default port for the ChromaDB API.
*   **Version**: We use `0.4.24` for compatibility with LangChain4j 0.35.0.
*   **Persistence**: To persist data between restarts, mount a volume:
    ```bash
    docker run -d --name chromadb \
      -v $(pwd)/chroma_data:/chroma/chroma \
      -p 8000:8000 \
      chromadb/chroma:0.4.24
    ```

## 3. Verify Connection
You can verify Chroma is running by identifying its heartbeat endpoint:

```bash
curl http://localhost:8000/api/v1/heartbeat
```
Expected Output:
```json
{"nanosecond heartbeat": ...}
```

## 4. Configuration for Log Bot
Once running, update your `application.properties` (or simpler, the `EmbeddingConfiguration.java` as we will implement) to point to this instance.

- **Base URL**: `http://localhost:8000`
- **Collection Name**: `log-embeddings`

## 5. Inspecting Data
You can use `curl` to inspect the data inside ChromaDB.

### List Collections
Check if your `log-embeddings` collection was created.
```bash
curl http://localhost:8000/api/v1/collections
```

### Count Documents
Once you have the **Collection ID** (e.g., `a1b2c3...`) from the list command:
```bash
curl http://localhost:8000/api/v1/collections/{collection_id}/count
```

### Peek at Data
To see the actual data (first 5 entries):
```bash
curl -X POST http://localhost:8000/api/v1/collections/{collection_id}/get \
  -H "Content-Type: application/json" \
  -d '{"limit": 5}'
```
