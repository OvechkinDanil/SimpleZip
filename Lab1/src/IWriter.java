import java.io.FileOutputStream;

public interface IWriter extends IConfigurable, IPipelineStep {
	RetCode.SetterCode setOutputStream(FileOutputStream fos);
}
