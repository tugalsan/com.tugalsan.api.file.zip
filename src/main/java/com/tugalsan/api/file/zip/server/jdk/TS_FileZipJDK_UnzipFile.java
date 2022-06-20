package com.tugalsan.api.file.zip.server.jdk;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

//https://github.com/eugenp/tutorials/blob/master/core-java-modules/core-java-io/src/main/java/com/baeldung/unzip/UnzipFile.java
public class TS_FileZipJDK_UnzipFile {

    public static void unzip(Path fileZip, Path destPath) {
        try {
            var destDir = destPath.toFile();
            var buffer = new byte[1024];
            try ( var zis = new ZipInputStream(Files.newInputStream(fileZip))) {
                var zipEntry = zis.getNextEntry();
                while (zipEntry != null) {
                    var newFile = newFile(destDir, zipEntry);
                    if (zipEntry.isDirectory()) {
                        if (!newFile.isDirectory() && !newFile.mkdirs()) {
                            throw new IOException("Failed to create directory " + newFile);
                        }
                    } else {
                        var parent = newFile.getParentFile();
                        if (!parent.isDirectory() && !parent.mkdirs()) {
                            throw new IOException("Failed to create directory " + parent);
                        }

                        try ( var fos = new FileOutputStream(newFile)) {
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                    }
                    zipEntry = zis.getNextEntry();
                }
                zis.closeEntry();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see https://snyk.io/research/zip-slip-vulnerability
     */
    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        try {
            var destFile = new File(destinationDir, zipEntry.getName());
            var destDirPath = destinationDir.getCanonicalPath();
            var destFilePath = destFile.getCanonicalPath();
            if (!destFilePath.startsWith(destDirPath + File.separator)) {
                throw new RuntimeException("Entry is outside of the target dir: " + zipEntry.getName());
            }
            return destFile;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
