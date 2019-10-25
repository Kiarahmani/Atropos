package kiarahmani.atropos.program.statements;

import java.util.ArrayList;

import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.program.Statement;

public class If_Statement extends Statement {
	private Expression condition;
	private ArrayList<Statement> enclosed_statements;
}
