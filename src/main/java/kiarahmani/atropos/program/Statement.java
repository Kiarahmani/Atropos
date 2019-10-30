package kiarahmani.atropos.program;

public abstract class Statement {
	public abstract void printStatemenet(String indent);

	public abstract void printStatemenet();

	public abstract String getId();

	public abstract String[] getAllQueryIds(); 
}
