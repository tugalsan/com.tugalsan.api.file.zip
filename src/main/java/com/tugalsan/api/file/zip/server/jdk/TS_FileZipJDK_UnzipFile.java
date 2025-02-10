package com.tugalsan.api.file.zip.server.jdk;


import com.tugalsan.api.function.client.maythrow.checkedexceptions.TGS_FuncMTCEUtils;
import com.tugalsan.api.function.client.maythrow.uncheckedexceptions.TGS_FuncMTUCEUtils;
import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

//https://github.com/eugenp/tutorials/blob/master/core-java-modules/core-java-io/src/main/java/com/baeldung/unzip/UnzipFile.java
public class TS_FileZipJDK_UnzipFile {

    public static void unzip(Path fileZip, Path destPath) {
        TGS_FuncMTCEUtils.run(() -> {
            var destDir = destPath.toFile();
            var buffer = new byte[1024];
            try ( var zis = new ZipInputStream(Files.newInputStream(fileZip))) {
                ZipEntry zipEntry;
                while ((zipEntry = zis.getNextEntry()) != null) {
                    var newFile = newFile(destDir, zipEntry);
                    if (zipEntry.isDirectory()) {
                        if (!newFile.mkdirs()) {
                            TGS_FuncMTUCEUtils.thrw(TS_FileZipJDK_UnzipFile.class.getSimpleName(), "unzip", "Failed to create directory " + newFile);
                        }
                    } else {
                        var parent = newFile.getParentFile();
                        if (!parent.isDirectory() && !parent.mkdirs()) {
                            TGS_FuncMTUCEUtils.thrw(TS_FileZipJDK_UnzipFile.class.getSimpleName(), "unzip", "Failed to create directory " + parent);
                        }

                        try ( var fos = new FileOutputStream(newFile)) {
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                    }
                    zis.closeEntry();
                }
            }
        });
    }

    /**
     * @see https://snyk.io/research/zip-slip-vulnerability
     */
    public static File newFile(File destinationDir, ZipEntry zipEntry) {
        return TGS_FuncMTCEUtils.call(() -> {
            var destFile = new File(destinationDir, zipEntry.getName());
            var destDirPath = destinationDir.getCanonicalPath();
            var destFilePath = destFile.getCanonicalPath();
            if (!destFilePath.startsWith(destDirPath + File.separator)) {
                TGS_FuncMTUCEUtils.thrw(TS_FileZipJDK_UnzipFile.class.getSimpleName(), "newFile", "Entry is outside of the target dir: " + zipEntry.getName());
            }
            return destFile;
        });
    }
}
