package kiarahmani.atropos.program;

import java.util.ArrayList;

import kiarahmani.atropos.DML.query.Query;

public abstract class Statement {
	public abstract void printStatemenet(String indent);

	public abstract void printStatemenet();

	public abstract String getId();

	public abstract String[] getAllQueryIds();

	public abstract ArrayList<Query> getAllQueries();
}
