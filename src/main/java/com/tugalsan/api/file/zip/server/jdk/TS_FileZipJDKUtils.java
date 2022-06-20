package com.tugalsan.api.file.zip.server.jdk;

import java.net.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.zip.*;
import com.tugalsan.api.log.server.*;
import com.tugalsan.api.file.server.*;

@Deprecated
public class TS_FileZipJDKUtils {

    final private static TS_Log d = TS_Log.of(TS_FileZipJDKUtils.class.getSimpleName());

    private static class zipDirectory_DirectoryVisitor extends SimpleFileVisitor<Path> implements AutoCloseable {

        @Override
        public void close() throws Exception {
            try {
                zos.close();
            } catch (Exception e) {
            }
        }
        private ZipOutputStream zos;
        final private Path sourceDir;

        public zipDirectory_DirectoryVisitor(Path sourceDir, ZipOutputStream zos) {
            this.sourceDir = sourceDir;
            this.zos = zos;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
            try {
                var targetFile = sourceDir.relativize(file);
                zos.putNextEntry(new ZipEntry(targetFile.toString()));
                var bytes = Files.readAllBytes(file);
                zos.write(bytes, 0, bytes.length);
                zos.closeEntry();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return FileVisitResult.CONTINUE;
        }
    }

    @Deprecated
    public static void zipDirectory(Path sourceDirectory, Path targetZipFile) {
        d.cr("zipDirectory", sourceDirectory.toAbsolutePath().toString(), targetZipFile.toAbsolutePath().toString());
        TS_FileUtils.deleteFileIfExists(targetZipFile);
        try ( var fos = Files.newOutputStream(targetZipFile);  var zos = new ZipOutputStream(fos);  var zdv = new zipDirectory_DirectoryVisitor(sourceDirectory, zos)) {
            Files.walkFileTree(sourceDirectory, zdv);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated//FOR PARALLEL, STUDY https://stackoverflow.com/questions/54624695/how-to-implement-parallel-zip-creation-with-scatterzipoutputstream-with-zip64-su
    public static void zipFiles(Path[] sourceFiles, Path targetZipFile) {
        d.cr("zipFiles", sourceFiles, targetZipFile.toAbsolutePath().toString());
        try ( var fos = Files.newOutputStream(targetZipFile);  var zos = new ZipOutputStream(fos);) {
            Arrays.stream(sourceFiles).forEachOrdered(sourceFile -> zipFiles_for(sourceFile, zos));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void zipFiles_for(Path sourceFile, ZipOutputStream zos) {//I KNOW
        d.ci("zipFiles_for", sourceFile.toAbsolutePath().toString());
        try {
            zos.putNextEntry(new ZipEntry(sourceFile.getFileName().toString()));
            var bytes = Files.readAllBytes(sourceFile);
            zos.write(bytes, 0, bytes.length);
            zos.closeEntry();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public static void unzipFile(Path zipFile, Path targetDirectory) {
        d.ci("unzipFile", zipFile.toAbsolutePath().toString(), targetDirectory.toAbsolutePath().toString());
        TS_DirectoryUtils.createDirectoriesIfNotExists(targetDirectory);
        try ( var zipInputStream = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                final Path toPath = targetDirectory.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectory(toPath);
                } else {
                    Files.copy(zipInputStream, toPath);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public static void unzipURL(URL zipURL, Path targetDirectory) {
        d.cr("unzipFile", zipURL.toExternalForm(), targetDirectory.toAbsolutePath().toString());
        try ( var zipInputStream = new ZipInputStream(Channels.newInputStream(Channels.newChannel(zipURL.openStream())))) {
            for (var entry = zipInputStream.getNextEntry(); entry != null; entry = zipInputStream.getNextEntry()) {
                var toPath = targetDirectory.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectory(toPath);
                } else try ( var fileChannel = FileChannel.open(toPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE/*, DELETE_ON_CLOSE*/);  var nc = Channels.newChannel(zipInputStream)) {
                    fileChannel.transferFrom(nc, 0, Long.MAX_VALUE);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
