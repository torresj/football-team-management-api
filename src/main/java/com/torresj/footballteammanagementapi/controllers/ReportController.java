package com.torresj.footballteammanagementapi.controllers;

import com.itextpdf.text.DocumentException;
import com.torresj.footballteammanagementapi.services.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;

@RestController
@RequestMapping("v1/reports")
@Slf4j
@RequiredArgsConstructor
public class ReportController {

  private final ReportService reportService;

  @Operation(summary = "")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = {
              @Content(
                  mediaType = "application/PDF")
            }),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
      })
  @SecurityRequirement(name = "Bearer Authentication")
  @GetMapping("/balance_pdf")
  ResponseEntity<byte[]> balancePDF() throws DocumentException {
    log.info("[REPORTS] Generating balance in PDF");
    byte[] data = reportService.getBalancePDF();
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=balance.pdf");
    log.info("[REPORTS] Balance in PDF generated");
    return ResponseEntity.ok()
            .headers(headers)
            .contentType(MediaType.APPLICATION_PDF)
            .body(data);
  }
}
