package com.torresj.footballteammanagementapi.services;

import com.itextpdf.text.DocumentException;

import java.io.InputStream;

public interface ReportService {
    byte[] getBalancePDF() throws DocumentException;
}
