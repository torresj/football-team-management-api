package com.torresj.footballteammanagementapi.scheduledTasks;

import com.torresj.footballteammanagementapi.services.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class MatchTasks {

    private final MatchService matchService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void closeMatch(){
        log.info("Closing all matches before today");
        matchService.closePastMatches();
    }
}
