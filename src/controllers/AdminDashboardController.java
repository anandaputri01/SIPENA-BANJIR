package controllers;

import models.Message;
import models.Report;
import utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Controller untuk dashboard admin.
 * Mengimplementasikan AdminReportActionCallback untuk menerima aksi (edit status, hapus)
 * dari setiap item dalam ListView.
 */
public class AdminDashboardController extends BaseController implements ReportItemController.AdminReportActionCallback {

    @FXML
    private ListView<Report> reportsListView;

    /**
     * Metode ini dipanggil secara otomatis setelah file FXML dimuat.
     * Digunakan untuk menginisialisasi controller.
     */
    @FXML
    public void initialize() {
        loadAllReports();
    }

    /**
     * Memuat semua laporan dari database dan menampilkannya di ListView.
     * Menggunakan cell factory kustom yang menghubungkan aksi tombol di setiap item
     * ke metode di controller ini (onEditStatus, onDeleteReport).
     */
    private void loadAllReports() {
        List<Report> reports = reportService.getAllReports();
        ObservableList<Report> observableList = FXCollections.observableArrayList(reports);
        reportsListView.setItems(observableList);

        // Menghubungkan controller ini (sebagai callback) ke setiap sel di list view.
        // Ini memungkinkan tombol di dalam sel memanggil metode di controller ini.
        reportsListView.setCellFactory(ReportListCell.forAdminListView(this));
    }

    /**
     * Menangani aksi logout.
     * @param event Aksi dari tombol menu.
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        authService.logout();
        // Menggunakan reportsListView sebagai node referensi untuk mendapatkan stage saat ini.
        loadScene("/views/login.fxml", reportsListView);
    }

    //--- IMPLEMENTASI METODE CALLBACK DARI AdminReportActionCallback ---

    /**
     * Dipanggil ketika tombol "Ubah Status" pada sebuah item laporan diklik.
     * @param report Objek laporan yang akan diubah.
     */
    @Override
    public void onEditStatus(Report report) {
        if (report == null) {
            AlertUtils.showError("Laporan tidak valid.");
            return;
        }
        showEditStatusDialog(report);
    }
    @Override
    public void onViewDetail(Report report) {
        if (report == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/report_detail_dialog.fxml"));
            Parent parent = loader.load();

            ReportDetailDialogController controller = loader.getController();
            controller.setReport(report);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Detail Laporan: " + report.getFloodLocation());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            // Inisialisasi pemilik window agar dialog muncul di atas window utama
            Stage ownerStage = (Stage) reportsListView.getScene().getWindow();
            dialogStage.initOwner(ownerStage);

            Scene scene = new Scene(parent, 900, 600); // Atur ukuran dialog
            // Jika Anda punya file CSS, tambahkan di sini
            // scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            dialogStage.setScene(scene);

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Gagal membuka detail laporan.");
        }
    }


    /**
     * Dipanggil ketika tombol "Hapus" pada sebuah item laporan diklik.
     * @param report Objek laporan yang akan dihapus.
     */
    @Override
    public void onDeleteReport(Report report) {
        if (report == null) {
            AlertUtils.showError("Laporan tidak valid.");
            return;
        }

        // Tampilkan dialog konfirmasi untuk mencegah penghapusan yang tidak disengaja.
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Konfirmasi Hapus Laporan");
        confirmationAlert.setHeaderText("Anda yakin ingin menghapus laporan ini secara permanen?");
        confirmationAlert.setContentText("Laporan ID: " + report.getId() + "\nLokasi: " + report.getFloodLocation() + "\n\nAksi ini tidak dapat dibatalkan.");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Jika admin menekan OK, lanjutkan penghapusan.
            if (reportService.deleteReport(report.getId())) {
                AlertUtils.showInfo("Laporan berhasil dihapus.");
                loadAllReports(); // Muat ulang daftar untuk menampilkan perubahan.
            } else {
                // Pesan ini mungkin muncul jika laporan sudah dihapus atau statusnya bukan PENDING.
                AlertUtils.showError("Gagal menghapus laporan. Laporan mungkin sudah tidak ada.");
            }
        }
    }

    //--- DIALOG LOGIC ---

    /**
     * Menampilkan dialog untuk mengubah status laporan.
     * Catatan admin yang dimasukkan di sini juga akan dikirim sebagai pesan ke pelapor.
     * @param report Laporan yang akan di-edit.
     */
    private void showEditStatusDialog(Report report) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Ubah Status Laporan");
        dialog.setHeaderText("Laporan ID: " + report.getId() + " - " + report.getFloodLocation());

        ButtonType saveButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("PENDING", "REVIEWED", "RESOLVED");
        statusComboBox.setValue(report.getStatus());

        TextArea adminNotesArea = new TextArea(report.getAdminNotes());
        adminNotesArea.setPromptText("Catatan Admin (akan dikirim sebagai pesan ke pelapor)");
        adminNotesArea.setPrefRowCount(4);

        grid.add(new Label("Status:"), 0, 0);
        grid.add(statusComboBox, 1, 0);
        grid.add(new Label("Catatan Admin:"), 0, 1);
        grid.add(adminNotesArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Pair<>(statusComboBox.getValue(), adminNotesArea.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent(statusAndNotes -> {
            String newStatus = statusAndNotes.getKey();
            String adminNotes = statusAndNotes.getValue();

            // 1. Update status dan catatan admin di tabel laporan.
            if (reportService.updateReportStatus(report.getId(), newStatus, adminNotes)) {
                AlertUtils.showInfo("Status laporan berhasil diubah.");

                // 2. Jika ada catatan, kirim catatan tersebut sebagai pesan ke pelapor.
                if (adminNotes != null && !adminNotes.trim().isEmpty()) {
                    Message message = new Message(0, report.getId(), authService.getCurrentUser().getId(), adminNotes.trim(), true, LocalDateTime.now());
                    if (messageService.sendMessage(message)) {
                        System.out.println("Catatan admin berhasil dikirim sebagai pesan.");
                    } else {
                        // Gagal mengirim pesan tidak menghentikan alur utama, hanya dicatat di konsol.
                        System.err.println("Peringatan: Gagal mengirim catatan admin sebagai pesan.");
                    }
                }

                loadAllReports(); // Muat ulang daftar untuk menampilkan status baru.
            } else {
                AlertUtils.showError("Gagal mengubah status laporan.");
            }
        });
    }
}