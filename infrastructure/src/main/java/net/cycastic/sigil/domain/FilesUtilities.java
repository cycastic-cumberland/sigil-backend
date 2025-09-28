package net.cycastic.sigil.domain;

import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.UUID;

public class FilesUtilities {
    public static Path getTempFile(){
        var tempDir = System.getProperty("java.io.tmpdir");
        var fileName = UUID.randomUUID().toString();
        return Paths.get(tempDir, fileName);
    }

    @SneakyThrows
    public static void deleteRecursively(Path root) {
        if (!Files.exists(root)) {
            return;
        }

        var stack = new ArrayDeque<Path>();
        stack.push(root);

        while (!stack.isEmpty()) {
            var current = stack.peek();
            if (Files.isDirectory(current)) {
                try (var ds = Files.newDirectoryStream(current)) {
                    var hasChildren = false;
                    for (var child : ds) {
                        stack.push(child);
                        hasChildren = true;
                    }
                    if (!hasChildren) {
                        stack.pop();
                        Files.delete(current);
                    }
                }
            } else {
                stack.pop();
                Files.delete(current);
            }
        }
    }
}
