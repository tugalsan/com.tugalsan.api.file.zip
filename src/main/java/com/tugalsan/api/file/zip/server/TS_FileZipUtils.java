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
import com.tugalsan.api.union.client.TGS_UnionExcuse;
import com.tugalsan.api.union.client.TGS_UnionExcuseVoid;

public class TS_FileZipUtils {

    final private static TS_Log d = TS_Log.of(TS_FileZipUtils.class);

    public static TGS_UnionExcuseVoid zipFile(Path sourceFile, Path targetZipFile) {
        return TS_FileZipZip4JUtils.zipFile(sourceFile, targetZipFile);
    }

    public static TGS_UnionExcuseVoid zipFolder(Path sourceDirectory, Path targetZipFile) {
        return TS_FileZipZip4JUtils.zipFolder(sourceDirectory, targetZipFile);
    }

    public static TGS_UnionExcuseVoid zipList(List<Path> sourceFiles, Path targetZipFile) {
        return TS_FileZipZip4JUtils.zipList(sourceFiles, null, targetZipFile);
    }

    public static boolean isOSHasDeleteBugAfterUnzip() {
        return TS_OsPlatformUtils.getName().startsWith("windows server 200");
    }

    public static TGS_UnionExcuse<TS_OsProcess> unzipFileFlattened(Path sourceZipFile, Path destinationDirectory) {
        d.ci("unzipFileFlattened", sourceZipFile, destinationDirectory);
        return TS_FileZipNativeSevenZip.unzipFileFlattened(sourceZipFile, destinationDirectory);
    }

    public static TGS_UnionExcuse<TS_OsProcess> unzipDirectoryFlattened(Path zipDirectory) {
        return TS_FileZipNativeSevenZip.unzipDirectoryFlattened(zipDirectory);
    }

    public static TGS_UnionExcuseVoid unzipListFlattened(List<Path> sourceZipFiles, Path destinationDirectory) {
        for (var zipFile : sourceZipFiles) {
            var u = unzipFileFlattened(zipFile, destinationDirectory);
            if (u.isExcuse()) {
                return u.toExcuseVoid();
            }
        }
        return TGS_UnionExcuseVoid.ofVoid();
    }

    public static TGS_UnionExcuse<TS_OsProcess> unzipFile(Path sourceZipFiles, Path destinationDirectory) {
        return TS_FileZipNativeSevenZip.unzip(sourceZipFiles, destinationDirectory);
    }

    public static TGS_UnionExcuse<List<Path>> getZipFiles(Path parentDirectory) {
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
