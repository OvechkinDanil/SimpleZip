public interface IPipelineStep extends IExecutable {
	RetCode.SetterCode setConsumer(IExecutable c);
	RetCode.SetterCode setProducer(IExecutable p);
}