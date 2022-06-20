package com.tugalsan.api.file.zip.server;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import com.tugalsan.api.file.server.*;
import com.tugalsan.api.list.client.*;
import com.tugalsan.api.log.server.*;
import com.tugalsan.api.os.server.*;
import com.tugalsan.api.file.zip.server.sevenZip.*;
import com.tugalsan.api.file.zip.server.zip4j.*;

public class TS_FileZipUtils {

    final private static TS_Log d = TS_Log.of(TS_FileZipUtils.class.getSimpleName());

    public static void zipFile(Path sourceFile, Path targetZipFile) {
        TS_FileZipZip4JUtils.zipFile(sourceFile, targetZipFile);
    }

    public static void zipFolder(Path sourceDirectory, Path targetZipFile) {
        TS_FileZipZip4JUtils.zipFolder(sourceDirectory, targetZipFile);
    }

    public static void zipList(List<Path> sourceFiles, Path targetZipFile) {
        TS_FileZipZip4JUtils.zipList(sourceFiles, null, targetZipFile);
    }

    public static boolean isOSHasDeleteBugAfterUnzip() {
        return TS_OSUtils.getName().startsWith("windows server 200");
    }

    public static void unzipFileFlattened(Path sourceZipFile, Path destinationDirectory) {
        d.ci("unzipFileFlattened", sourceZipFile, destinationDirectory);
        TS_FileZipNativeSevenZip.unzipFileFlattened(sourceZipFile, destinationDirectory);
    }

    public static void unzipDirectoryFlattened(Path zipDirectory) {
        TS_FileZipNativeSevenZip.unzipDirectoryFlattened(zipDirectory);
    }

    public static void unzipListFlattened(List<Path> sourceZipFiles, Path destinationDirectory) {
        d.ci("unzipDirectoryFlattened", sourceZipFiles.size(), destinationDirectory);
        sourceZipFiles.parallelStream().forEach(zip -> unzipFileFlattened(zip, destinationDirectory));
    }

    public static void unzipFile(Path sourceZipFiles, Path destinationDirectory) {
        TS_FileZipNativeSevenZip.unzip(sourceZipFiles, destinationDirectory);
    }

    public static List<Path> getZipFiles(Path parentDirectory) {
        return TS_DirectoryUtils.subFiles(parentDirectory, "*.zip", false, false);
    }

    public static List<String> getFilePaths(Path sourceZipFile) {
        List<String> list = TGS_ListUtils.of();
        try ( var fis = new FileInputStream(sourceZipFile.toFile());  var zipIs = new ZipInputStream(new BufferedInputStream(fis));) {
            ZipEntry zEntry;
            while ((zEntry = zipIs.getNextEntry()) != null) {
                list.add(zEntry.getName());
            }
            zipIs.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return list;
    }
}
