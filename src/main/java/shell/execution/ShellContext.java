package shell.execution;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Holds mutable shell state – primarily the current working directory.
 * Instances are shared by the executor, builtins and pipelines.
 */
public final class ShellContext {
    private Path currentDirectory;

    public ShellContext(Path initialDirectory) {
        this.currentDirectory = initialDirectory.toAbsolutePath().normalize();
    }

    public Path getCurrentDirectory() {
        return currentDirectory;
    }

    /**
     * Change directory, performing ~‑expansion, relative‑to‑cwd resolution
     * and canonicalisation exactly like the original monolithic shell.
     *
     * @param raw the raw argument supplied by the user (may contain ~)
     * @throws IllegalArgumentException if the target does not exist or is not a directory
     */
    public void changeDirectory(String raw) {
        File target;
        if (raw.equals("~")) {
            target = new File(System.getenv("HOME"));
        } else if (raw.startsWith("~" + File.separator)) {
            target = new File(System.getenv("HOME"), raw.substring(2));
        } else if (raw.startsWith("/")) {
            target = new File(raw);
        } else {
            target = new File(currentDirectory.toFile(), raw);
        }

        try {
            target = target.getCanonicalFile();
        } catch (IOException e) {
            throw new IllegalArgumentException("cd: " + raw + ": No such file or directory", e);
        }

        if (!target.exists() || !target.isDirectory()) {
            throw new IllegalArgumentException("cd: " + raw + ": No such file or directory");
        }

        currentDirectory = target.toPath();
    }

    /** Resolve a possibly relative path against the current directory. */
    public Path resolve(String maybeRelative) {
        if (maybeRelative == null || maybeRelative.isEmpty()) {
            return currentDirectory;
        }
        if (maybeRelative.startsWith("~")) {
            String home = System.getenv("HOME");
            if (home != null) {
                if (maybeRelative.equals("~")) {
                    return Path.of(home);
                } else if (maybeRelative.startsWith("~/" + File.separator)
                        || maybeRelative.startsWith("~" + File.separator)) {
                    return Path.of(home, maybeRelative.substring(2));
                }
            }
        }
        return currentDirectory.resolve(maybeRelative).normalize();
    }
}