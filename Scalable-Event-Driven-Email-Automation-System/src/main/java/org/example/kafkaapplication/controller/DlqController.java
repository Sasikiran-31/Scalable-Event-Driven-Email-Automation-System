package org.example.kafkaapplication.Controller;


import org.example.kafkaapplication.Consumer.DlqReplayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * The REST endpoint router for handling the Replay from DLTs
 */
@RestController
@RequestMapping("/api/dlq")
public class DlqController {

    private final DlqReplayService replayService;

    @Autowired
    public DlqController(DlqReplayService replayService) {
        this.replayService = replayService;
    }

    @PostMapping("/start")
    public Map<String, String> startReplay() {
        replayService.startReplay();
        return Map.of(
                "status", "success",
                "message", "DLQ Replay Started. Dead-lettered events are flowing back to main topics."
        );
    }

    @PostMapping("/stop")
    public Map<String, String> stopReplay() {
        replayService.stopReplay();
        return Map.of(
                "status", "success",
                "message", "DLQ Replay Stopped."
        );
    }
}