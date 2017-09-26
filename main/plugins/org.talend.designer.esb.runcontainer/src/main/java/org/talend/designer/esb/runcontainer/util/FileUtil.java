// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.esb.runcontainer.util;

import static java.nio.file.attribute.PosixFilePermission.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.IProgressMonitor;

public class FileUtil {

    private static final List<String> CONTAINER_FILES;
   

    static {
        final List<String> containerFiles = new ArrayList<>();

        containerFiles.add("bin/trun");
        containerFiles.add("bin/setenv");
        containerFiles.add("etc");
        containerFiles.add("system/org/apache/karaf");
        containerFiles.add("system/org/talend/esb");
        containerFiles.add("lib/boot");
        CONTAINER_FILES = Collections.unmodifiableList(containerFiles);
    }

    private FileUtil() {
    }

    public static boolean isValidLocation(String rtHome) {
        return getValidLocation(rtHome) != null;
    }
    
    public static String getValidLocation(String rtHome) {
        if (rtHome == null) {
            throw new IllegalArgumentException("rtHome cannot be null");
        }
        // validate, 1st version, 2nd etc
        rtHome = rtHome.trim();
        if (rtHome.isEmpty()) {
            return null;
        }

        File rtDir = new File(rtHome);
        // find version.txt, 1st in root, second check in container folder
        if (!rtDir.isDirectory()) {
            return null;
        }

        File version = new File(rtHome + "/version.txt");
        String ver = "";
        try {
            if (version.exists()) {
                ver = Files.readAllLines(version.toPath()).get(0);
            } else {
                version = new File(rtHome + "/container/version.txt");
                if (version.exists()) {
                    ver = Files.readAllLines(version.toPath()).get(0);
                    rtHome += "/container";
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        if (ver.isEmpty()) {
            return null;
        }

        for (String f : CONTAINER_FILES) {
            File resFile = new File(rtHome, f);
            if (!resFile.exists()) {
                return null;
            }
        }

        return rtHome;
    }

    public static void copyContainer(String from, String to, IProgressMonitor monitor) throws IOException {
        Path fromPath = Paths.get(from);
        Path toPath = Paths.get(to);
        Path bakPath = null;
        monitor.beginTask("Copy to target", 100);
        if (Files.exists(toPath)) {
            bakPath = toPath.getParent().resolve(toPath.getFileName().toString() + ".bak");
            deleteDir(bakPath);
            Files.move(toPath, bakPath, StandardCopyOption.REPLACE_EXISTING);
        }

        Files.createDirectories(toPath);

        try {
            copyDir(fromPath, toPath, monitor);
        } catch (IOException e) {
            deleteDir(toPath);
            if (bakPath != null) {
                Files.move(bakPath, toPath, StandardCopyOption.REPLACE_EXISTING);
            }
            throw e;
        } finally {
            if (bakPath != null) {
                deleteDir(bakPath);
            }
            monitor.done();
        }
    }

    public static boolean isContainerArchive(String archive) {
        try {
            return getPathPrefixInArchive(archive) != null;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void unzipContainer(String from, String to, IProgressMonitor monitor) throws IOException {
        monitor.beginTask("Unzipping " + from + " into " + to, 100);
        final String prefix = getPathPrefixInArchive(from);
        if (prefix == null) {
            throw new IllegalArgumentException("Not Talend ESB Runtime archive");
        }

        final Path toPath = Paths.get(to);
        Path bakPath = null;

        if (Files.exists(toPath)) {
            bakPath = toPath.getParent().resolve(toPath.getFileName().toString() + ".bak");
            deleteDir(bakPath);
            Files.move(toPath, bakPath, StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.createDirectories(toPath);
        }

        try (final ZipInputStream zip = new ZipInputStream(new FileInputStream(from))) {
            ZipEntry zentry = zip.getNextEntry();

            while (zentry != null && !monitor.isCanceled()) {
                final String originalPath = zentry.getName();
                if (originalPath.startsWith(prefix)) {
                    final String newPath = originalPath.substring(prefix.length() + 1);
                    final Path destPath = toPath.resolve(newPath);
                    if (zentry.isDirectory()) {
                        Files.createDirectories(destPath);
                    } else {
                        Files.createDirectories(destPath.getParent());
                        Files.copy(zip, destPath);
                    }
                }
                monitor.setTaskName(originalPath);
                monitor.worked(1);

                zentry = zip.getNextEntry();
            }
        } catch (IOException e) {
            deleteDir(toPath);
            if (bakPath != null) {
                Files.move(bakPath, toPath, StandardCopyOption.REPLACE_EXISTING);
            }
            throw e;
        } finally {
            if (bakPath != null) {
                deleteDir(bakPath);
            }
            monitor.done();
        }
        setFileExecPerm(toPath.resolve("bin/trun"));
        setFileExecPerm(toPath.resolve("bin/client"));
    }

    public static String getPathPrefixInArchive(String zip) throws IOException {
        try (ZipInputStream zipStream = new ZipInputStream(new FileInputStream(zip), Charset.forName("UTF-8"))) {
            ZipEntry zentry = zipStream.getNextEntry();
            if (zentry == null) {
                return null;
            }

            String name = zentry.getName();

            Path path = Paths.get(name);

            String prefix = path.getRoot() == null ? path.toString() : path.getRoot().toString();

            boolean isRuntime = false;
            boolean isESB = false;

            Set<Path> toLookFor = new HashSet<>();
            for (String contF : CONTAINER_FILES) {
                toLookFor.add(Paths.get(contF));
            }

            while (zentry != null && !toLookFor.isEmpty()) {
                Path zpath = Paths.get(zentry.getName());

                if (zpath.getNameCount() < 2) {
                    zentry = zipStream.getNextEntry();
                    continue;
                }

                if (toLookFor.remove(zpath.subpath(1, zpath.getNameCount()))) {
                    isRuntime = true;
                } else if ("container".equals(zpath.getName(1).toString()) && zpath.getNameCount() > 2
                        && toLookFor.remove(zpath.subpath(2, zpath.getNameCount()))) {
                    isESB = true;
                }

                zentry = zipStream.getNextEntry();
            }

            if (!toLookFor.isEmpty()) {
                System.out.println("Some elements were not found: " + toLookFor);
                return null;
            }

            if (isESB == isRuntime) {
                System.out.println("ESB and Runtime is the same: " + isESB);
                return null;
            }

            return isESB ? Paths.get(prefix, "container").toString().replace('\\', '/') : prefix;
        }
    }

    private static void deleteDir(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }

        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                if (e == null) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                } else {
                    throw e;
                }
            }
        });
    }

    private static void copyDir(Path from, Path to, IProgressMonitor monitor) throws IOException {
        Files.walkFileTree(from, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetdir = to.resolve(from.relativize(dir));
                try {
                    Files.copy(dir, targetdir);
                    monitor.subTask(dir.toString());
                    monitor.worked(1);
                } catch (FileAlreadyExistsException e) {
                    if (!Files.isDirectory(targetdir))
                        throw e;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, to.resolve(from.relativize(file)));
                monitor.subTask(file.toString());
                monitor.worked(1);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void deleteFolder(String folder) throws IOException {
        deleteDirectory(new File(folder));
    }

    private static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
        }
        return (directory.delete());
    }

    public static void setFileExecPerm(Path file) throws IOException {
        System.out.println("Setting exec file permissions for " + file);

        try {
            Set<PosixFilePermission> perms = Files.getPosixFilePermissions(file);
            
            perms.add(OWNER_READ);
            perms.add(OWNER_EXECUTE);
            
            if (perms.contains(GROUP_READ)) {
                perms.add(GROUP_EXECUTE);
            }
            
            if (perms.contains(OTHERS_READ)) {
                perms.add(OTHERS_EXECUTE);
            }
            
            Files.setPosixFilePermissions(file, perms);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } catch (UnsupportedOperationException e) {
            System.out.println("File " + file + " is located on FS which doesn't support POSIX attributes.");
        }
    }
}
