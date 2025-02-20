package dev.sbytmacke.onlyprofit.constants;

public enum PathConstants {
    // CSS
    COMBO_BOX_CSS("/dev/sbytmacke/onlyprofit/css/comboBox.css"),
    TABLE_USERS_CSS("/dev/sbytmacke/onlyprofit/css/tableUsers.css"),
    PIE_CHART_CSS("/dev/sbytmacke/onlyprofit/css/pieChart.css"),

    // ICONS
    MAIN_ICON("/dev/sbytmacke/onlyprofit/icons/main_icon.png"),
    REFRESH_ICON("/dev/sbytmacke/onlyprofit/icons/refresh.png"),
    UPDATE_ICON("/dev/sbytmacke/onlyprofit/icons/update.png"),
    DELETE_ICON("/dev/sbytmacke/onlyprofit/icons/borrar.png"),

    // SCRIPT BACKUP
    SCRIPT_BACKUP("/dev/sbytmacke/onlyprofit/scripts/backup.ps1");

    private final String path;

    PathConstants(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return path;
    }
}

