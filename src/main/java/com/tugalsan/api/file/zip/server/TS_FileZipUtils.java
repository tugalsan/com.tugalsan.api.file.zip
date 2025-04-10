package com.tugalsan.api.file.zip.server;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import com.tugalsan.api.file.server.*;
import com.tugalsan.api.file.zip.server.jdk.TS_FileZipJDKUtils;
import com.tugalsan.api.list.client.*;
import com.tugalsan.api.log.server.*;
import com.tugalsan.api.os.server.*;
import com.tugalsan.api.file.zip.server.sevenZip.*;
import com.tugalsan.api.file.zip.server.zip4j.*;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import com.tugalsan.api.union.client.TGS_UnionExcuseVoid;

public class TS_FileZipUtils {

    final private static TS_Log d = TS_Log.of(TS_FileZipUtils.class);

    public static void zipFile(Path sourceFile, Path targetZipFile) {
        TS_FileZipJDKUtils.zipFile(sourceFile, targetZipFile);
//        TS_FileZipZip4JUtils.zipFile(servletKillTrigger, sourceFile, targetZipFile);
    }

    public static void zipFolder(TS_ThreadSyncTrigger servletKillTrigger, Path sourceDirectory, Path targetZipFile) {
        TS_FileZipZip4JUtils.zipFolder(servletKillTrigger, sourceDirectory, targetZipFile);
    }

    public static TGS_UnionExcuseVoid zipList(TS_ThreadSyncTrigger servletKillTrigger, List<Path> sourceFiles, Path targetZipFile) {
        return TS_FileZipZip4JUtils.zipList(servletKillTrigger, sourceFiles, null, targetZipFile);
    }

    public static boolean isOSHasDeleteBugAfterUnzip() {
        return TS_OsPlatformUtils.getName().startsWith("windows server 200");
    }

    public static void unzipFileFlattened(Path sourceZipFile, Path destinationDirectory) {
        d.ci("unzipFileFlattened", sourceZipFile, destinationDirectory);
        TS_FileZipNativeSevenZip.unzipFileFlattened(sourceZipFile, destinationDirectory);
    }

    public static void unzipDirectoryFlattened(Path zipDirectory) {
        TS_FileZipNativeSevenZip.unzipDirectoryFlattened(zipDirectory);
    }

    public static void unzipListFlattened(List<Path> sourceZipFiles, Path destinationDirectory, boolean parallel) {
        d.ci("unzipDirectoryFlattened", sourceZipFiles.size(), destinationDirectory);
        (parallel ? sourceZipFiles.parallelStream() : sourceZipFiles.stream()).forEach(zip -> {
            unzipFileFlattened(zip, destinationDirectory);
        });
    }

    public static void unzipFile(Path sourceZipFiles, Path destinationDirectory) {
        TS_FileZipNativeSevenZip.unzip(sourceZipFiles, destinationDirectory);
    }

    public static List<Path> getZipFiles(Path parentDirectory) {
        return TS_DirectoryUtils.subFiles(parentDirectory, "*.zip", false, false);
    }

    public static List<String> getFilePaths(Path sourceZipFile) {
        List<String> list = TGS_ListUtils.of();
        try (var fis = new FileInputStream(sourceZipFile.toFile()); var zipIs = new ZipInputStream(new BufferedInputStream(fis));) {
            ZipEntry zEntry;
            while ((zEntry = zipIs.getNextEntry()) != null) {
                list.add(zEntry.getName());
                zipIs.closeEntry();
            }
        } catch (IOException e) {
            return null;
        }
        return list;
    }
}
