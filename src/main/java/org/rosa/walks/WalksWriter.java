package org.rosa.walks;

import org.rosa.util.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

/**
 * Concurrent writer of the walks into a file.
 *
 * @author Emir Munoz
 * @version 0.0.1
 * @since 01/09/2016
 */
public class WalksWriter {

    /** class logger */
    private static final transient Logger _log = LoggerFactory.getLogger(WalksWriter.class.getSimpleName());
    private static final WalksWriter instance = new WalksWriter();
    private static BufferedWriter writer;
    private String filename;
    private String encoding;
    private int batchSize;
    private int entitiesCount;
    private int walksCount;
    private int linesCount;
    private int filesCounter;
    private long starTime;

    /**
     * Class constructor.
     */
    private WalksWriter() {
        _log.info("Creating singleton files writer");
        starTime = System.currentTimeMillis();
        filesCounter = 1;
    }

    /**
     * Get singleton instance.
     *
     * @return singleton instance.
     */
    public static WalksWriter getInstance() {
        return instance;
    }

    /**
     * Get instance using parameters.
     *
     * @param filename
     *         Path to the output file.
     * @param encoding
     *         Encoding of the file.
     * @param batchSize
     *         Size of batches.
     * @return singleton instance.
     */
    public static WalksWriter getInstance(String filename, String encoding, int batchSize) {
        instance.filename = filename;
        instance.encoding = encoding;
        instance.batchSize = batchSize;
        _log.info("Creating output file '{}'", filename);
        writer = FileSystem.createWriter(filename, encoding);

        return instance;
    }

    /**
     * Close file.
     */
    public static void closeWriter() {
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            _log.error("Error closing the walks writer");
            e.printStackTrace();
        }
    }

    /**
     * Write a list of elements to the file. Once that a file reach the
     * batch size, it is closed, and a new file is opened.
     *
     * @param items
     *         List of strings.
     */
    public synchronized void write(final List<String> items) {
        this.entitiesCount++;
        this.walksCount += items.size();
        for (String item : items) {
            try {
                writer.append(item);
                writer.newLine();
            } catch (IOException e) {
                _log.error("Error writing walks");
                e.printStackTrace();
            }
        }
        this.linesCount += items.size();
        _log.info("Entity counter = {}", this.entitiesCount);
        if (this.linesCount % this.batchSize == 0) {
            _log.info("Walks counter = {}", this.walksCount);
            _log.info("Elapsed time = {} seconds", (System.currentTimeMillis() - starTime) / 1000.0);
        }
        if (this.linesCount > this.batchSize) {
            this.linesCount = 0;
            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                _log.error("Error closing the walks writer");
                e.printStackTrace();
            }
            String nextFilename = this.filename.replace(".txt.gz", "-" + (++filesCounter) + ".txt.gz");
            _log.info("Creating output file '{}'", nextFilename);
            writer = FileSystem.createWriter(nextFilename, this.encoding);
        }
    }

}
