package medicalendra.common;

import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Deterministic path resolver for MediCalendra plain-text persistence files.
 * Guarantees that servlets, background threads, and tests resolve the exact same
 * canonical project data directory under SmartTomcat, standalone Tomcat, or CLI.
 */
public final class DataPathResolver {

    private static final String DATA_DIR_NAME = "data";
    private static final String CANONICAL_PROJECT_DATA = "G:/MediCalendra/data";

    private DataPathResolver() {}

    /**
     * Resolves the deterministic data directory path.
     */
    public static Path resolveDataDir(ServletContext ctx) {
        // 1. Explicit system property override
        String sysProp = System.getProperty("medicalendra.data.dir");
        if (sysProp != null && !sysProp.isBlank()) {
            Path p = Paths.get(sysProp).toAbsolutePath().normalize();
            if (Files.exists(p)) {
                return p;
            }
        }

        // 2. From ServletContext real path (under SmartTomcat: docBase is src/main/webapp)
        if (ctx != null) {
            String realPath = ctx.getRealPath("/");
            if (realPath != null) {
                Path rp = Paths.get(realPath).toAbsolutePath().normalize();
                // When docBase is src/main/webapp -> ../../../data
                Path candidateFromWebapp = rp.resolve("../../../" + DATA_DIR_NAME).normalize();
                if (Files.exists(candidateFromWebapp)) {
                    return candidateFromWebapp;
                }
                // When docBase is target/MediCalendra -> ../../data
                Path candidateFromTarget = rp.resolve("../../" + DATA_DIR_NAME).normalize();
                if (Files.exists(candidateFromTarget)) {
                    return candidateFromTarget;
                }
                // Direct data subdir in docBase
                Path candidateInDocBase = rp.resolve(DATA_DIR_NAME).normalize();
                if (Files.exists(candidateInDocBase)) {
                    return candidateInDocBase;
                }
            }
        }

        // 3. User working directory (when running Maven, tests, or CLI from project root)
        String userDir = System.getProperty("user.dir");
        if (userDir != null) {
            Path ud = Paths.get(userDir).toAbsolutePath().normalize();
            Path candidateUserDir = ud.resolve(DATA_DIR_NAME).normalize();
            if (Files.exists(candidateUserDir)) {
                return candidateUserDir;
            }
        }

        // 4. Default canonical project directory
        Path canonical = Paths.get(CANONICAL_PROJECT_DATA).toAbsolutePath().normalize();
        if (Files.exists(canonical)) {
            return canonical;
        }

        // 5. Fallback: create in canonical or user directory
        try {
            Files.createDirectories(canonical);
            return canonical;
        } catch (IOException ignored) {
            Path fallback = Paths.get(userDir != null ? userDir : ".", DATA_DIR_NAME).toAbsolutePath().normalize();
            try {
                Files.createDirectories(fallback);
            } catch (IOException e) {
                throw new RuntimeException("Could not create data directory", e);
            }
            return fallback;
        }
    }

    /**
     * Resolves a named data file within the deterministic data directory.
     */
    public static Path resolveDataFile(ServletContext ctx, String filename) {
        Path dir = resolveDataDir(ctx);
        return dir.resolve(filename).toAbsolutePath().normalize();
    }
}
