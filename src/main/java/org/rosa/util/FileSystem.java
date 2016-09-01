package org.rosa.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Emir Munoz
 * @version 0.1.2
 * @since 29/05/2016
 */
public class FileSystem {

    /** class logger */
    private final static transient Logger _log = LoggerFactory.getLogger(FileSystem.class.getSimpleName());

    /**
     * Determines if a given file exists in disk.
     *
     * @param path
     *         Path to the file.
     * @return true if the file exists; false otherwise.
     */
    public static boolean existsFile(final Path path) {
        return Files.exists(path, LinkOption.NOFOLLOW_LINKS);
    }

    /**
     * Generate the directory if does not exists.
     *
     * @param dirPath
     *         Path to directory.
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
     * @param file
     *         File path.
     */
    public static void setUpFolder(final File file) {
        if (!FileSystem.existsFile(file.getParentFile().toPath())) {
            if (!file.getParentFile().mkdirs()) {
                _log.error("Cannot create directories for path {}", file.getName());
                System.exit(1);
            }
        }
    }

    /**
     * Checks if a file is gzipped.
     *
     * @param file
     *         Input file.
     * @return true if file is gzip; false otherwise.
     */
    public static boolean isGZipped(File file) {
        int magic = 0;
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            magic = raf.read() & 0xff | ((raf.read() << 8) & 0xff00);
            raf.close();
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        }

        return magic == GZIPInputStream.GZIP_MAGIC;
    }

    public static BufferedWriter createWriter(final String filename, final String encoding) {
        try {
            // false will create a new file if already exists
            FileOutputStream fos = new FileOutputStream(filename, false);
            GZIPOutputStream gzip = new GZIPOutputStream(fos);
            return new BufferedWriter(new OutputStreamWriter(gzip, encoding));
        } catch (FileNotFoundException e) {
            _log.error("File not found");
            e.printStackTrace();
        } catch (IOException e) {
            _log.error("Error writing in file");
            e.printStackTrace();
        }

        return null;
    }

}
