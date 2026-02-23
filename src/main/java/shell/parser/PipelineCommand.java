package shell.parser;

import java.util.List;

public final class PipelineCommand implements Command{

	private final List<SimpleCommand> stages;

	public PipelineCommand(List<SimpleCommand> stages) {
		if (stages == null || stages.size() < 2) {
			throw new IllegalArgumentException("Pipeline must have at least 2 stages");
		}
		this.stages = List.copyOf(stages);
	}

	public List<SimpleCommand> stages() {
		return stages;
	}

}
