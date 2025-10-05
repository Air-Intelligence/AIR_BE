package air.intelligence.controller;

import air.intelligence.scheduler.WarningScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TempController {
    private final WarningScheduler warningScheduler;

    @GetMapping("/temp")
    public String temp() {
        this.warningScheduler.task();
        return "Hello, World!";
    }
}
