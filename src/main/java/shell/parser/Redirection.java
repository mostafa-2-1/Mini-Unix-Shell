package shell.parser;

import java.nio.file.Path;
import java.util.Optional;

public final class Redirection {

	private final Path stdout;
	private final boolean stdoutAppend;

	private final Path stderr;
	private final boolean stderrAppend;

	private Redirection(Path stdout, boolean stdoutAppend,
			Path stderr, boolean stderrAppend) {
		this.stdout = stdout;
		this.stdoutAppend = stdoutAppend;
		this.stderr = stderr;
		this.stderrAppend = stderrAppend;
	}

	public static Redirection none() {
		return new Redirection(null, false, null, false);
	}

	public Optional<Path> stdout() {
		return Optional.ofNullable(stdout);
	}

	public boolean stdoutAppend() {
		return stdoutAppend;
	}

	public Optional<Path> stderr() {
		return Optional.ofNullable(stderr);
	}

	public boolean stderrAppend() {
		return stderrAppend;
	}

	public Redirection withStdout(Path path, boolean append) {
		return new Redirection(path, append, stderr, stderrAppend);
	}

	public Redirection withStderr(Path path, boolean append) {
		return new Redirection(stdout, stdoutAppend, path, append);
	}
}
