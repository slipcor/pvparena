package net.slipcor.pvparena.updater;

public enum UpdateMode {
    OFF, ANNOUNCE, DOWNLOAD;

    public static UpdateMode getBySetting(final String setting) {
        if (ANNOUNCE.name().equalsIgnoreCase(setting)) {
            return ANNOUNCE;
        }
        if (DOWNLOAD.name().equalsIgnoreCase(setting)) {
            return DOWNLOAD;
        }
        //Retro-compatibility
        if ("both".equalsIgnoreCase(setting)) {
            return DOWNLOAD;
        }
        return OFF;
    }
}