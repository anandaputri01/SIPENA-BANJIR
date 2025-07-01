package services;

import models.Report;
import repositories.ReportRepository;

import java.util.List;

public class ReportService {
    private final ReportRepository reportRepository;

    public ReportService() {
        this.reportRepository = new ReportRepository();
    }

    public boolean createReport(Report report) {
        return reportRepository.create(report);
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public List<Report> getUserReports(int userId) {
        return reportRepository.findByUserId(userId);
    }

    public boolean updateReport(Report report) {
        return reportRepository.update(report);
    }

    public boolean updateReportStatus(int reportId, String status, String adminNotes) {
        return reportRepository.updateStatus(reportId, status, adminNotes);
    }

    public boolean deleteReport(int reportId) {
        return reportRepository.delete(reportId);
    }
}