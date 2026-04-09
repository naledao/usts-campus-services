package hhsc.kangnasi.xyz.ustscampusservices.controller;

import com.fasterxml.jackson.databind.JsonNode;
import hhsc.kangnasi.xyz.ustscampusservices.handler.MusicWsHandler;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.Map;

@RestController
@RequestMapping("/music")
public class MusicController {

    // 对外的 header token 校验（和你原 Python 一样）
    private static final String VALID_MUSIC_TOKEN =
            "543276dgscajhgd127rt2gfd8327tc872brx87892trcr4378brc2383rybcbvbr6734tb";

    private final MusicWsHandler bridge;

    public MusicController(MusicWsHandler bridge) {
        this.bridge = bridge;
    }

    private void verify(String musicToken) {
        if (musicToken == null || !VALID_MUSIC_TOKEN.equals(musicToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing music_token");
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam("keyword") String keyword,
            @RequestHeader(value = "music_token", required = false) String musicToken
    ) throws Exception {
        verify(musicToken);

        JsonNode resp = bridge.requestSearch(keyword, 20_000);
        // resp: {type, requestId, ok, payload:{results:[...]}}
        return ResponseEntity.ok(Map.of(
                "code", 200,
                "data", resp.path("payload").path("results")
        ));
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> download(
            @RequestParam("music_id") String musicId,
            @RequestHeader(value = "music_token", required = false) String musicToken
    ) throws Exception {
        verify(musicToken);

        MusicWsHandler.DownloadResult r = bridge.requestDownload(musicId, 10 * 60_000);

        File file = r.file();
        if (!file.exists()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "mp3 file not found");
        }

        Resource resource = new FileSystemResource(file);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("audio/mpeg"));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(file.getName()) // 或用 r.filename()
                .build());

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}
