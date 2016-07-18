package org.rosa.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

/**
 * @author Emir Munoz
 * @since 29/05/2016
 */
public class FileSystem {

    /** class logger */
    private final static transient Logger _log = LoggerFactory.getLogger(FileSystem.class.getSimpleName());

    /**
     * Determines if a given file exists in disk.
     *
     * @param path Path to the file.
     * @return true if the file exists; false otherwise.
     */
    public static boolean existsFile(final Path path) {
        return Files.exists(path, LinkOption.NOFOLLOW_LINKS);
    }

    /**
     * Generate the directory if does not exists.
     *
     * @param dirPath Path to directory.
     */
    public static void setUpFolder(final Path dirPath) {
        File evalDirPath = new File(dirPath.toString());
        if (!evalDirPath.exists()) {
            try {
                Files.createDirectory(dirPath);
            } catch (IOException e) {
                _log.error("Error creating the evaluation directory '{}'", dirPath);
                e.printStackTrace();
            }
        }
    }

    /**
     * Generate directories for a given path.
     *
     * @param file File path.
     */
    public static void setUpFolder(final File file) {
        if (!FileSystem.existsFile(file.getParentFile().toPath())) {
            if (!file.getParentFile().mkdirs()) {
                _log.error("Cannot create directories for path {}", file.getName());
                System.exit(1);
            }
        }
    }

}
