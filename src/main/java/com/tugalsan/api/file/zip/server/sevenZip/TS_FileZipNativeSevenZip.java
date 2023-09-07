package com.tugalsan.api.file.zip.server.sevenZip;

import java.nio.file.*;
import java.util.*;
import com.tugalsan.api.file.server.*;
import com.tugalsan.api.log.server.*;
import com.tugalsan.api.os.server.*;
import com.tugalsan.api.string.client.*;
import com.tugalsan.api.coronator.client.*;

public class TS_FileZipNativeSevenZip {

    final private static TS_Log d = TS_Log.of(TS_FileZipNativeSevenZip.class);

    public static Path sevenZipExe = TGS_Coronator.of(Path.class)
            .anoint(val -> Path.of("C:\\Program Files\\7-Zip\\7z.exe"))
            .anointIf(val -> TS_FileUtils.isExistFile(Path.of("D:\\bin\\7z\\App\\7-Zip64\\7z.exe")), val -> Path.of("D:\\bin\\7z\\App\\7-Zip64\\7z.exe"))
            .coronate();

    public static void zipFile(Path sourceFile, Path targetZipFile) {//"C:\Program Files\7-Zip\7z.exe" a "D:\a\zipne.zip" "D:\b\zipne.txt"
        var cmd = TGS_StringUtils.concat(
                "\"", sevenZipExe.toAbsolutePath().toString(), "\" a -tzip \"",
                targetZipFile.toAbsolutePath().toString(), "\" \"",
                sourceFile.toAbsolutePath().toString(), "\""
        );
        d.ci("zipFile", "cmd", cmd);
        TS_OsProcess.of(cmd);
    }

    public static void zipList(List<Path> sourceFiles, Path targetZipFile) {//7z a -tzip DestinyTest.zip destiny1.txt destiny4.txt destiny6.txt
        var sb = new StringBuilder();
        sourceFiles.stream().forEachOrdered(file -> {
            sb.append(" \"").append(file.toAbsolutePath().toString()).append("\"");
        });
        var cmd = TGS_StringUtils.concat(
                "\"", sevenZipExe.toAbsolutePath().toString(), "\" a -tzip \"",
                targetZipFile.toAbsolutePath().toString(), "\" ", sb.toString()
        );
        d.ci("zipFile", "cmd", cmd);
        TS_OsProcess.of(cmd);
    }

    public static void zipFolder(Path sourceDirectory, Path targetZipFile) {//7z a myzip ./MyFolder/*
        var driveLetter = TS_PathUtils.getDriveLetter(targetZipFile);
        var bat = new StringJoiner("\n");
        bat.add(driveLetter + ":");
        bat.add("cd " + targetZipFile.getParent().toAbsolutePath().toString());
        bat.add(TGS_StringUtils.concat(
                "\"", sevenZipExe.toAbsolutePath().toString(), "\" a -tzip \"",
                targetZipFile.toAbsolutePath().toString(), "\"",
                sourceDirectory.toAbsolutePath().toString(), "\\*\""
        ));
        TS_OsProcess.of(driveLetter);
    }

    public static void unzip(Path sourceZipFile, Path destinationDirectory) {//"C:\Program Files\7-Zip\7z.exe" x -y "D:\xampp\168063.zip" -o"d:\zip"
        TS_OsProcess.of(TGS_StringUtils.concat(
                "\"", sevenZipExe.toAbsolutePath().toString(), "\" x -y \"",
                sourceZipFile.toAbsolutePath().toString(), "\" -o\"",
                destinationDirectory.toAbsolutePath().toString(), "\""
        ));
    }

    public static void unzipFileFlattened(Path sourceZipFile, Path destinationDirectory) {//"C:\Program Files\7-Zip\7z.exe" x -y "D:\xampp\168063.zip" -o"d:\zip"
        var cmd = TGS_StringUtils.concat(
                "\"", sevenZipExe.toAbsolutePath().toString(), "\" e -y \"",
                sourceZipFile.toAbsolutePath().toString(), "\" -o\"",
                destinationDirectory.toAbsolutePath().toString(), "\""
        );
        d.ci("unzipFileFlattened", "cmd", cmd);
        TS_OsProcess.of(cmd);
    }

    public static void unzipDirectoryFlattened(Path zipDirectory) {
        var batCode = new StringJoiner("\n");
        batCode.add(TS_PathUtils.getDriveLetter(zipDirectory) + ":");
        batCode.add("cd " + zipDirectory.toAbsolutePath().toString());
        batCode.add("FOR /F \"usebackq\" %%a in (`DIR /s /b *.zip`) do \"" + sevenZipExe.toAbsolutePath().toString() + "\" e %%a");
        d.cr("unzipDirectoryFlattened", "batCode", batCode);
        TS_OsProcess.ofCode(batCode.toString(), TS_OsProcess.CodeType.BAT);
    }
}
