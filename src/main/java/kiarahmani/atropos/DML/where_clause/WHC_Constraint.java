package kiarahmani.atropos.DML.where_clause;

import java.util.HashSet;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.Expression;

public class WHC_Constraint {
	private TableName tn;
	private FieldName f;
	private BinOp op;
	private Expression exp;

	public WHC_Constraint(TableName tn, FieldName f, BinOp op, Expression exp) {
		this.tn = tn;
		this.f = f;
		this.op = op;
		this.exp = exp;
	}

	public TableName getTableName() {
		return this.tn;
	}

	public Expression getExpression() {
		return this.exp;
	}

	public BinOp getOp() {
		return this.op;
	}

	public FieldName getFieldName() {
		return this.f;
	}

	@Override
	public String toString() {
		return this.f + BinOp.BinOpToString(op) + exp.toString();
	}

	public HashSet<Variable> getAllRefferencedVars() {
		return this.exp.getAllRefferencedVars();
	}

}