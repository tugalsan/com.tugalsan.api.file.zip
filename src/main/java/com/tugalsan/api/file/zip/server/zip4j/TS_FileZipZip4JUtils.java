package com.tugalsan.api.file.zip.server.zip4j;

import module com.tugalsan.api.file;
import module com.tugalsan.api.function;
import module com.tugalsan.api.log;
import module com.tugalsan.api.list;
import module com.tugalsan.api.thread;
import module com.tugalsan.api.union;
import module zip4j;
import java.nio.file.*;
import java.util.*;

public class TS_FileZipZip4JUtils {

    final private static TS_Log d = TS_Log.of(TS_FileZipZip4JUtils.class);

    public static void zipFile(TS_ThreadSyncTrigger servletKillTrigger, Path sourceFile, Path targetZipFile) {
        d.ci("zipFile", sourceFile, targetZipFile);
        TS_DirectoryUtils.createDirectoriesIfNotExists(targetZipFile.getParent());
        zipList(servletKillTrigger, TGS_ListUtils.of(sourceFile), null, targetZipFile);
    }

    public static void zipFolder(TS_ThreadSyncTrigger servletKillTrigger, Path sourceDirectory, Path targetZipFile) {
        d.ci("zipFolder", sourceDirectory, targetZipFile);
        TS_DirectoryUtils.createDirectoriesIfNotExists(targetZipFile.getParent());
        zipList(servletKillTrigger, null, TGS_ListUtils.of(sourceDirectory), targetZipFile);
    }

    public static TGS_UnionExcuseVoid zipList(TS_ThreadSyncTrigger servletKillTrigger, List<Path> sourceFiles, List<Path> sourceDirectories, Path targetZipFile) {
        return TGS_FuncMTCUtils.call(() -> {
            d.ci("zipList", sourceFiles, sourceDirectories, targetZipFile);
            TS_DirectoryUtils.createDirectoriesIfNotExists(targetZipFile.getParent());
            TS_FileUtils.deleteFileIfExists(targetZipFile);
            var zipFile = new ZipFile(targetZipFile.toAbsolutePath().toString());
            if (sourceFiles != null) {
                sourceFiles.stream().forEachOrdered(p -> {
                    if (servletKillTrigger.hasTriggered()) {
                        return;
                    }
                    TGS_FuncMTCUtils.run(() -> {
                        if (p == null) {
                            return;
                        }
                        zipFile.addFile(p.toFile());
                    });
                });
            }
            if (sourceDirectories != null) {
                sourceDirectories.stream().forEachOrdered(p -> {
                    if (servletKillTrigger.hasTriggered()) {
                        return;
                    }
                    TGS_FuncMTCUtils.run(() -> {
                        if (p == null) {
                            return;
                        }
                        zipFile.addFolder(p.toFile());
                    });
                });
            }
            return TGS_UnionExcuseVoid.ofVoid();
        }, e -> TGS_UnionExcuseVoid.ofExcuse(e));
    }

    public static void unzip(Path sourceZipFile, Path destinationDirectory) {
        TGS_FuncMTCUtils.run(() -> {
            d.ci("unzip", sourceZipFile, destinationDirectory);
//            TS_DirectoryUtils.deleteDirectoryIfExists(destinationDirectory);//DONT!!!
            TS_DirectoryUtils.createDirectoriesIfNotExists(destinationDirectory);
            var zipFile = new ZipFile(sourceZipFile.toAbsolutePath().toString());
            zipFile.extractAll(destinationDirectory.toAbsolutePath().toString());
        });
    }
}
