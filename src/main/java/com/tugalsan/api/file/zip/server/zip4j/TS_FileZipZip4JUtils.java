package com.tugalsan.api.file.zip.server.zip4j;

import java.nio.file.*;
import java.util.*;
import net.lingala.zip4j.*;
import com.tugalsan.api.file.server.*;
import com.tugalsan.api.log.server.*;
import com.tugalsan.api.list.client.*;
import com.tugalsan.api.union.client.TGS_Union;
import net.lingala.zip4j.exception.ZipException;

public class TS_FileZipZip4JUtils {

    final private static TS_Log d = TS_Log.of(TS_FileZipZip4JUtils.class);

    public static void zipFile(Path sourceFile, Path targetZipFile) {
        d.ci("zipFile", sourceFile, targetZipFile);
        TS_DirectoryUtils.createDirectoriesIfNotExists(targetZipFile.getParent());
        zipList(TGS_ListUtils.of(sourceFile), null, targetZipFile);
    }

    public static void zipFolder(Path sourceDirectory, Path targetZipFile) {
        d.ci("zipFolder", sourceDirectory, targetZipFile);
        TS_DirectoryUtils.createDirectoriesIfNotExists(targetZipFile.getParent());
        zipList(null, TGS_ListUtils.of(sourceDirectory), targetZipFile);
    }

    public static TGS_Union<Boolean> zipList(List<Path> sourceFiles, List<Path> sourceDirectories, Path targetZipFile) {
        d.ci("zipList", sourceFiles, sourceDirectories, targetZipFile);
        TS_DirectoryUtils.createDirectoriesIfNotExists(targetZipFile.getParent());
        TS_FileUtils.deleteFileIfExists(targetZipFile);
        var zipFile = new ZipFile(targetZipFile.toAbsolutePath().toString());
        if (sourceFiles != null) {
            for (var p : sourceFiles) {
                if (p == null) {
                    continue;
                }
                try {
                    zipFile.addFile(p.toFile());
                } catch (ZipException ex) {
                    return TGS_Union.ofExcuse(ex);
                }
            }
        }
        if (sourceDirectories != null) {
            for (var p : sourceDirectories) {
                if (p == null) {
                    continue;
                }
                try {
                    zipFile.addFolder(p.toFile());
                } catch (ZipException ex) {
                    return TGS_Union.ofExcuse(ex);
                }
            }
        }
        return TGS_Union.of(true);
    }

    public static TGS_Union<Boolean> unzip(Path sourceZipFile, Path destinationDirectory) {
        try {
            d.ci("unzip", sourceZipFile, destinationDirectory);
//            TS_DirectoryUtils.deleteDirectoryIfExists(destinationDirectory);//DONT!!!
            TS_DirectoryUtils.createDirectoriesIfNotExists(destinationDirectory);
            var zipFile = new ZipFile(sourceZipFile.toAbsolutePath().toString());
            zipFile.extractAll(destinationDirectory.toAbsolutePath().toString());
            return TGS_Union.of(true);
        } catch (ZipException ex) {
            return TGS_Union.ofExcuse(ex);
        }
    }
}
