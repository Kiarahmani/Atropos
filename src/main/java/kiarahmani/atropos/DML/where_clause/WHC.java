package kiarahmani.atropos.DML.where_clause;

import java.util.ArrayList;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.constants.E_Const_Bool;
import kiarahmani.atropos.program.Table;

public class WHC extends Expression {
	public enum WHC_Type {
		PK_SINGLE, PK_RANGE, NPK_RANGE;
	}

	private WHC_Type whc_type;
	private ArrayList<WHC_Constraint> whc_constraints;

	public WHC(FieldName is_alive, WHC_Constraint... whccs) {
		whc_constraints = new ArrayList<>();
		for (WHC_Constraint whcc : whccs)
			whc_constraints.add(whcc);
		// all where cluases included a special constraint on is_alive
		whc_constraints.add(new WHC_Constraint(whccs[0].getTableName(), is_alive, BinOp.EQ, new E_Const_Bool(true)));
	}

	public ArrayList<WHC_Constraint> getConstraints() {
		return this.whc_constraints;
	}

	public ArrayList<FieldName> getAccessedFieldNames() {
		ArrayList<FieldName> result = new ArrayList<>();
		for (WHC_Constraint whcc : this.whc_constraints)
			if (!whcc.getFieldName().isPK())
				result.add(whcc.getFieldName());
		return result;
	}

	@Override
	public String toString() {
		String constraintsList = "", delim = "";
		for (WHC_Constraint whcc : whc_constraints) {
			constraintsList += delim + whcc.toString();
			delim = " ∧ ";
		}
		constraintsList = (constraintsList.equals("")) ? "true" : constraintsList; // no constraint means true
		return "(" + constraintsList + ")";
	}
}
