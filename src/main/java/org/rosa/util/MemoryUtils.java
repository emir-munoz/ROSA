package org.rosa.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Memory utils.
 *
 * @author Emir Munoz
 * @version 0.0.5
 * @since 28/04/2016
 */
public class MemoryUtils {

    /** class logger */
    private final static transient Logger _log = LoggerFactory.getLogger(MemoryUtils.class.getSimpleName());

    /**
     * Returns used memory in MB
     */
    public static double usedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return usedMemory(runtime);
    }

    /**
     * Returns max memory available MB
     */
    public static double maxMemory() {
        Runtime runtime = Runtime.getRuntime();
        return maxMemory(runtime);
    }

    /**
     * Returns the used memory of a given runtime.
     * @param runtime Input runtime.
     * @return Used memory.
     */
    static double usedMemory(Runtime runtime) {
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        return (double) (totalMemory - freeMemory) / (double) (1024 * 1024);
    }

    /**
     * Returns the max memory of a given runtime.
     * @param runtime Input runtime.
     * @return Max memory.
     */
    static double maxMemory(Runtime runtime) {
        long maxMemory = runtime.maxMemory();
        return (double) maxMemory / (double) (1024 * 1024);
    }

    /**
     * Print memory information.
     */
    public static void printMemoryInfo() {
        StringBuffer buffer = getMemoryInfo();
        // StringBuffer buffer = getMemoryInfoActual();
        _log.info(buffer.toString());
    }

    /**
     * Get memory information after free memory.
     * @return Memory information.
     */
    public static StringBuffer getMemoryInfo() {
        freeMemory();

        return getMemoryInfoActual();
    }

    /**
     * Get actual (without free memory) memory information.
     * @return String with memory information.
     */
    public static StringBuffer getMemoryInfoActual() {
        StringBuffer buffer = new StringBuffer();
        Runtime runtime = Runtime.getRuntime();
        double usedMemory = usedMemory(runtime);
        double maxMemory = maxMemory(runtime);
        NumberFormat f = new DecimalFormat("###,##0.0");
        String lineSeparator = "\t"; // System.getProperty("line.separator");
        buffer.append("Used memory: ").append(f.format(usedMemory)).append("MB").append(lineSeparator);
        buffer.append("Max available memory: ").append(f.format(maxMemory)).append("MB");

        return buffer;
    }

    /**
     * Run garbage collector in the JVM.
     */
    public static void freeMemory() {
        System.gc();
        System.runFinalization();
    }

}
