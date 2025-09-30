package com.tugalsan.api.file.zip.server.jdk;

import module com.tugalsan.api.log;
import module com.tugalsan.api.file;
import module com.tugalsan.api.function;
import java.net.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.zip.*;

@Deprecated
public class TS_FileZipJDKUtils {

    final private static TS_Log d = TS_Log.of(TS_FileZipJDKUtils.class);

    private static class zipDirectory_DirectoryVisitor extends SimpleFileVisitor<Path> implements AutoCloseable {

        @Override
        public void close() {
            TGS_FuncMTCUtils.run(() -> zos.close(), e -> TGS_FuncMTU.empty.run());
        }
        private final ZipOutputStream zos;
        final private Path sourceDir;

        public zipDirectory_DirectoryVisitor(Path sourceDir, ZipOutputStream zos) {
            this.sourceDir = sourceDir;
            this.zos = zos;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
            return TGS_FuncMTCUtils.call(() -> {
                var targetFile = sourceDir.relativize(file);
                zos.putNextEntry(new ZipEntry(targetFile.toString()));
                var bytes = Files.readAllBytes(file);
                zos.write(bytes, 0, bytes.length);
                zos.closeEntry();
                return FileVisitResult.CONTINUE;
            });
        }
    }

    @Deprecated
    public static void zipDirectory(Path sourceDirectory, Path targetZipFile) {
        TGS_FuncMTCUtils.run(() -> {
            d.cr("zipDirectory", sourceDirectory.toAbsolutePath().toString(), targetZipFile.toAbsolutePath().toString());
            TS_FileUtils.deleteFileIfExists(targetZipFile);
            try (var fos = Files.newOutputStream(targetZipFile); var zos = new ZipOutputStream(fos); var zdv = new zipDirectory_DirectoryVisitor(sourceDirectory, zos)) {
                Files.walkFileTree(sourceDirectory, zdv);
            }
        });
    }

    @Deprecated//FOR PARALLEL, STUDY https://stackoverflow.com/questions/54624695/how-to-implement-parallel-zip-creation-with-scatterzipoutputstream-with-zip64-su
    public static void zipFiles(Path[] sourceFiles, Path targetZipFile) {
        TGS_FuncMTCUtils.run(() -> {
            d.cr("zipFiles", sourceFiles, targetZipFile.toAbsolutePath().toString());
            try (var fos = Files.newOutputStream(targetZipFile); var zos = new ZipOutputStream(fos);) {
                Arrays.stream(sourceFiles).forEachOrdered(sourceFile -> zipFiles_for(sourceFile, zos));
            }
        });
    }

    private static void zipFiles_for(Path sourceFile, ZipOutputStream zos) {//I KNOW
        TGS_FuncMTCUtils.run(() -> {
            d.ci("zipFiles_for", sourceFile.toAbsolutePath().toString());
            zos.putNextEntry(new ZipEntry(sourceFile.getFileName().toString()));
            var bytes = Files.readAllBytes(sourceFile);
            zos.write(bytes, 0, bytes.length);
            zos.closeEntry();
        });
    }

    @Deprecated
    public static void unzipFile(Path zipFile, Path targetDirectory) {
        TGS_FuncMTCUtils.run(() -> {
            d.ci("unzipFile", zipFile.toAbsolutePath().toString(), targetDirectory.toAbsolutePath().toString());
            TS_DirectoryUtils.createDirectoriesIfNotExists(targetDirectory);
            try (var zipInputStream = new ZipInputStream(Files.newInputStream(zipFile))) {
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    var toPath = targetDirectory.resolve(entry.getName());
                    if (entry.isDirectory()) {
                        Files.createDirectory(toPath);
                    } else {
                        Files.copy(zipInputStream, toPath);
                    }
                    zipInputStream.closeEntry();
                }
            }
        });
    }

    @Deprecated
    public static void unzipURL(URL zipURL, Path targetDirectory) {
        TGS_FuncMTCUtils.run(() -> {
            d.cr("unzipFile", zipURL.toExternalForm(), targetDirectory.toAbsolutePath().toString());
            try (var zipInputStream = new ZipInputStream(Channels.newInputStream(Channels.newChannel(zipURL.openStream())))) {
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    var toPath = targetDirectory.resolve(entry.getName());
                    if (entry.isDirectory()) {
                        Files.createDirectory(toPath);
                    } else try (var fileChannel = FileChannel.open(toPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE/*, DELETE_ON_CLOSE*/); var nc = Channels.newChannel(zipInputStream)) {
                        fileChannel.transferFrom(nc, 0, Long.MAX_VALUE);
                    }
                    zipInputStream.closeEntry();
                }
            }
        });
    }

    //TODO_MORE @ https://www.baeldung.com/java-compress-and-uncompress
    public static void zipFile(Path inputFile, Path zipFile) {
        TGS_FuncMTCUtils.run(() -> {
            var bytes = new byte[1024];
            int length;
            try (var fos = Files.newOutputStream(zipFile)) {
                try (var zipOut = new ZipOutputStream(fos)) {
                    try (var fis = Files.newInputStream(inputFile)) {
                        zipOut.putNextEntry(new ZipEntry(inputFile.toFile().getName()));
                        while ((length = fis.read(bytes)) >= 0) {
                            zipOut.write(bytes, 0, length);
                        }
                    }
                }
            }
        });
    }
}
