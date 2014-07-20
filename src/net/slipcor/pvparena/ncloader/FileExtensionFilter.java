package net.slipcor.pvparena.ncloader;

import java.io.File;
import java.io.FileFilter;

final class FileExtensionFilter implements FileFilter {

    private final String extension;

    public FileExtensionFilter(final String extension) {
        this.extension = extension;
    }

    @Override
    public boolean accept(final File file) {
        return file.getName().endsWith(extension);
    }
}