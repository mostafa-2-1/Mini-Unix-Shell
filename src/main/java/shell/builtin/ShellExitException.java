package shell.builtin;



public final class ShellExitException extends RuntimeException {
	private final int status;

	public ShellExitException() {
		this(0);
	}

	public ShellExitException(int status) {
		super("Shell exiting with status " + status);
		this.status = status;
	}

	public int status() {
		return status;
	}
}