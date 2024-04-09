package com.tugalsan.api.file.zip.server.jdk;

import com.tugalsan.api.union.client.TGS_Union;
import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

//https://github.com/eugenp/tutorials/blob/master/core-java-modules/core-java-io/src/main/java/com/baeldung/unzip/UnzipFile.java
public class TS_FileZipJDK_UnzipFile {

    public static TGS_Union<Boolean> unzip(Path fileZip, Path destPath) {
        var destDir = destPath.toFile();
        var buffer = new byte[1024];
        try (var zis = new ZipInputStream(Files.newInputStream(fileZip))) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                var u_newFile = newFile(destDir, zipEntry);
                if (u_newFile.isExcuse()) {
                    return TGS_Union.ofExcuse(u_newFile.excuse());
                }
                var newFile = u_newFile.value();
                if (zipEntry.isDirectory()) {
                    if (!newFile.toFile().mkdirs()) {
                        return TGS_Union.ofExcuse(TS_FileZipJDK_UnzipFile.class.getSimpleName(), "unzip", "Failed to create directory " + newFile);
                    }
                } else {
                    var parent = newFile.toFile().getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        return TGS_Union.ofExcuse(TS_FileZipJDK_UnzipFile.class.getSimpleName(), "unzip", "Failed to create directory " + parent);
                    }

                    try (var fos = new FileOutputStream(newFile.toFile())) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
            return TGS_Union.of(true);
        } catch (IOException ex) {
            return TGS_Union.ofExcuse(ex);
        }
    }

    /**
     * @see https://snyk.io/research/zip-slip-vulnerability
     */
    public static TGS_Union<Path> newFile(File destinationDir, ZipEntry zipEntry) {
        try {
            var destFile = new File(destinationDir, zipEntry.getName());
            var destDirPath = destinationDir.getCanonicalPath();
            var destFilePath = destFile.getCanonicalPath();
            if (!destFilePath.startsWith(destDirPath + File.separator)) {
                return TGS_Union.ofExcuse(TS_FileZipJDK_UnzipFile.class.getSimpleName(), "newFile", "Entry is outside of the target dir: " + zipEntry.getName());
            }
            return TGS_Union.of(destFile.toPath());
        } catch (IOException ex) {
            return TGS_Union.ofExcuse(ex);
        }
    }
}
