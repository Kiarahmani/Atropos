package kiarahmani.atropos.DML.where_clause;

import java.util.ArrayList;
import java.util.HashSet;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.E_Proj;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.constants.E_Const_Bool;

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

	public WHC(FieldName is_alive, ArrayList<WHC_Constraint> whccs) {
		this.whc_constraints = whccs;
		// all where cluases included a special constraint on is_alive
		whc_constraints
				.add(new WHC_Constraint(whccs.get(0).getTableName(), is_alive, BinOp.EQ, new E_Const_Bool(true)));
	}

	public WHC(WHC_Constraint... whccs) {
		whc_constraints = new ArrayList<>();
		for (WHC_Constraint whcc : whccs)
			whc_constraints.add(whcc);
		// all where cluases included a special constraint on is_alive
	}

	public ArrayList<WHC_Constraint> getConstraints() {
		return this.whc_constraints;
	}

	public WHC_Constraint getConstraintByFieldName(FieldName fn) {
		for (WHC_Constraint whcc : this.whc_constraints)
			if (whcc.getFieldName() == fn)
				return whcc;
		assert (false) : "unexepected field name " + fn;
		return null;
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

	public HashSet<Variable> getAllRefferencedVars() {
		HashSet<Variable> result = new HashSet<>();
		for (WHC_Constraint whcc : this.whc_constraints)
			result.addAll(whcc.getAllRefferencedVars());
		return result;
	}
	
	
	
	public HashSet<E_Proj> getAllProjExps() {
		HashSet<E_Proj> result = new HashSet<>();
		for (WHC_Constraint whcc : this.whc_constraints)
			result.addAll(whcc.getAllProjExps());
		return result;
	}
	
	

	public boolean isAtomic(FieldName SK) {
		for (WHC_Constraint whcc : whc_constraints)
			if (whcc.getOp() == BinOp.EQ && (whcc.getFieldName().equals(SK))) {
				return true;
			}
		return false;
	}

	public boolean hasOnlyEq() {
		for (WHC_Constraint whcc : whc_constraints)
			if (whcc.getOp() != BinOp.EQ) 
				return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * kiarahmani.atropos.DML.expression.Expression#redirectProjs(kiarahmani.atropos
	 * .DML.Variable, kiarahmani.atropos.DDL.FieldName,
	 * kiarahmani.atropos.DML.Variable, kiarahmani.atropos.DDL.FieldName)
	 */
	@Override
	public void redirectProjs(Variable oldVar, FieldName oldFn, Variable newVar, FieldName newFn) {
		for (WHC_Constraint whcc : whc_constraints)
			whcc.redirectProjs(oldVar, oldFn, newVar, newFn);
	}
}
