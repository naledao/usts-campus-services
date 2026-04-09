package hhsc.kangnasi.xyz.ustscampusservices.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.*;

@Component
public class MusicWsHandler extends AbstractWebSocketHandler {

    private static final String WS_AUTH_TOKEN = "543276dgscajhgd127rt2gfd8327tc872brx87892trcr4378brc2383rybcbvbr6734tb";
    private static final File TEMP_DIR = new File("./temp_music_b");

    static { TEMP_DIR.mkdirs(); }

    private final ObjectMapper mapper = new ObjectMapper();

    private volatile WebSocketSession workerSession;

    private final ConcurrentHashMap<String, CompletableFuture<JsonNode>> pendingJson = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, DownloadContext> pendingDownload = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // ?token=...
        String query = session.getUri() != null ? session.getUri().getQuery() : "";
        String token = "";
        if (query != null) {
            for (String kv : query.split("&")) {
                String[] arr = kv.split("=", 2);
                if (arr.length == 2 && arr[0].equals("token")) token = arr[1];
            }
        }

        if (!WS_AUTH_TOKEN.equals(token)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("bad token"));
            return;
        }

        this.workerSession = session;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        if (this.workerSession == session) {
            this.workerSession = null;
        }
    }

    // ===================== Text 控制消息 =====================
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode root = mapper.readTree(message.getPayload());
        String type = root.path("type").asText();
        String requestId = root.path("requestId").asText();
        boolean ok = root.path("ok").asBoolean(true);

        if (!ok) {
            CompletableFuture<JsonNode> f1 = pendingJson.remove(requestId);
            if (f1 != null) f1.complete(root);

            DownloadContext ctx = pendingDownload.remove(requestId);
            if (ctx != null) {
                ctx.future.completeExceptionally(
                        new RuntimeException(root.path("payload").path("message").asText("unknown error"))
                );
                ctx.closeQuietly();
            }
            return;
        }

        switch (type) {
            case "search_result" -> {
                CompletableFuture<JsonNode> f = pendingJson.remove(requestId);
                if (f != null) f.complete(root);
            }
            case "download_meta" -> {
                DownloadContext ctx = pendingDownload.get(requestId);
                if (ctx != null) {
                    ctx.filename = root.path("payload").path("filename").asText("music.mp3");
                    ctx.filesize = root.path("payload").path("filesize").asLong(-1);
                }
            }
            case "download_done" -> {
                DownloadContext ctx = pendingDownload.remove(requestId);
                if (ctx != null) {
                    try {
                        ctx.out.flush();
                        ctx.out.close();
                        ctx.future.complete(ctx.tempFile);
                    } catch (Exception e) {
                        ctx.future.completeExceptionally(e);
                    }
                }
            }
            default -> {
                // ignore
            }
        }
    }

    // ===================== Binary 分片消息（重点） =====================
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        ByteBuffer buf = message.getPayload();

        // format:
        // [36 bytes requestId ascii][4 bytes seq uint32 big-endian][chunk bytes]
        if (buf.remaining() < 36 + 4) {
            return;
        }

        byte[] ridBytes = new byte[36];
        buf.get(ridBytes);
        String requestId = new String(ridBytes, StandardCharsets.US_ASCII);

        int seq = buf.getInt(); // big-endian by default

        DownloadContext ctx = pendingDownload.get(requestId);
        if (ctx == null) {
            // 没有上下文：可能超时被移除/或 requestId 不匹配
            return;
        }

        // 剩余就是 chunk bytes
        int n = buf.remaining();
        byte[] chunk = new byte[n];
        buf.get(chunk);

        // 写文件
        ctx.out.write(chunk);

        // 可选：保存最后 seq，方便调试
        ctx.lastSeq = seq;
    }

    // ============ 给 REST 层调用的 API ============

    public JsonNode requestSearch(String keyword, long timeoutMs) throws Exception {
        WebSocketSession ws = ensureWorker();
        String requestId = UUID.randomUUID().toString();

        CompletableFuture<JsonNode> future = new CompletableFuture<>();
        pendingJson.put(requestId, future);

        String cmd = mapper.createObjectNode()
                .put("type", "search")
                .put("requestId", requestId)
                .set("payload", mapper.createObjectNode().put("keyword", keyword))
                .toString();

        ws.sendMessage(new TextMessage(cmd));
        return future.get(timeoutMs, TimeUnit.MILLISECONDS);
    }

    public DownloadResult requestDownload(String musicId, long timeoutMs) throws Exception {
        WebSocketSession ws = ensureWorker();
        String requestId = UUID.randomUUID().toString();

        File tempFile = new File(TEMP_DIR, requestId + ".mp3");
        FileOutputStream out = new FileOutputStream(tempFile);

        CompletableFuture<File> future = new CompletableFuture<>();
        DownloadContext ctx = new DownloadContext(out, tempFile, future);
        pendingDownload.put(requestId, ctx);

        String cmd = mapper.createObjectNode()
                .put("type", "download")
                .put("requestId", requestId)
                .set("payload", mapper.createObjectNode().put("music_id", musicId))
                .toString();

        ws.sendMessage(new TextMessage(cmd));

        File f = future.get(timeoutMs, TimeUnit.MILLISECONDS);

        // 这里 filename 可能已经在 download_meta 回来时填好（通常会）
        String filename = (ctx.filename != null && !ctx.filename.isBlank()) ? ctx.filename : "music.mp3";
        return new DownloadResult(f, filename);
    }

    private WebSocketSession ensureWorker() {
        WebSocketSession ws = workerSession;
        if (ws == null || !ws.isOpen()) {
            throw new IllegalStateException("Worker(A) is not connected");
        }
        return ws;
    }

    public record DownloadResult(File file, String filename) {}

    private static class DownloadContext {
        final FileOutputStream out;
        final File tempFile;
        final CompletableFuture<File> future;

        volatile String filename;
        volatile long filesize = -1;
        volatile int lastSeq = -1;

        DownloadContext(FileOutputStream out, File tempFile, CompletableFuture<File> future) {
            this.out = out;
            this.tempFile = tempFile;
            this.future = future;
        }

        void closeQuietly() {
            try { out.close(); } catch (Exception ignored) {}
        }
    }
}
