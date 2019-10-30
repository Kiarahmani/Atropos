package kiarahmani.atropos.encoding_engine.Z3;

import java.util.ArrayList;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Constructor;
import com.microsoft.z3.Context;
import com.microsoft.z3.DatatypeSort;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Quantifier;
import com.microsoft.z3.Sort;
import com.microsoft.z3.Symbol;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.utils.Constants;

public class Program_Relations {
	Program program;
	Context ctx;
	DeclaredObjects objs;
	Expr qry1, qry2, rec1, rec2, time1, time2;

	public Program_Relations(Program program, Context ctx, DeclaredObjects objs) {

		this.program = program;
		this.ctx = ctx;
		this.objs = objs;
		qry1 = ctx.mkFreshConst("qry", objs.getSort("Qry"));
		qry2 = ctx.mkFreshConst("qry", objs.getSort("Qry"));
		rec1 = ctx.mkFreshConst("rec", objs.getSort("Rec"));
		rec2 = ctx.mkFreshConst("rec", objs.getSort("Rec"));
		time1 = ctx.mkFreshConst("time", objs.getSort("Int"));
		time2 = ctx.mkFreshConst("time", objs.getSort("Int"));
	}

	public BoolExpr mk_qry_types_to_txn_types(String name, String stmtName) {
		BoolExpr rhs = ctx.mkEq(ctx.mkApp(objs.getfuncs("txn_type"), ctx.mkApp(objs.getfuncs("parent"), qry1)),
				ctx.mkApp(objs.getConstructor("TxnType", name)));

		BoolExpr lhs = (BoolExpr) ctx.mkEq(ctx.mkApp(objs.getfuncs("qry_type"), qry1),
				ctx.mkApp(objs.getConstructor("QryType", stmtName)));
		BoolExpr body = ctx.mkImplies(lhs, rhs);
		Quantifier x = ctx.mkForall(new Expr[] { qry1 }, body, 1, null, null, null, null);
		return x;
	}

	private DatatypeSort mkDataType(String name, String[] consts) {
		Symbol[] head_tail = new Symbol[] {};
		Sort[] sorts = new Sort[] {};
		int[] sort_refs = new int[] {};
		Constructor[] constructors = new Constructor[consts.length];
		for (int i = 0; i < consts.length; i++) {
			constructors[i] = ctx.mkConstructor(ctx.mkSymbol(consts[i]), ctx.mkSymbol("is_" + consts[i]), head_tail,
					sorts, sort_refs);
		}
		try {

			DatatypeSort result = ctx.mkDatatypeSort(name, constructors);
			return result;
		} catch (com.microsoft.z3.Z3Exception e) {
			throw new com.microsoft.z3.Z3Exception("No Txn with SQL operations found");
		}
	}

	public void addRecFldDataTypes() {
		Z3Logger.LogZ3("\n;; records and fields data types");
		objs.addDataType("RecType", mkDataType("RecType", program.getAllTableNames()));
	}

	public void addTxnOpDataTypes() {
		Z3Logger.LogZ3("\n;; transactions and operations data types");
		objs.addDataType("TxnType", mkDataType("TxnType", program.getAllTxnNames()));
		objs.addDataType("QryType", mkDataType("QryType", program.getAllStmtTypes()));
	}

	public void addTypingFuncs() {
		Z3Logger.LogZ3(";; type assignment for records, operations and transactions typs");
		objs.addFunc("txn_type", ctx.mkFuncDecl("txn_type", objs.getSort("Txn"), objs.getDataTypes("TxnType")));
		objs.addFunc("qry_type", ctx.mkFuncDecl("qry_type", objs.getSort("Qry"), objs.getDataTypes("QryType")));
		objs.addFunc("rec_type", ctx.mkFuncDecl("rec_type", objs.getSort("Rec"), objs.getDataTypes("RecType")));
	}

	public void addExecutionFuncs() {
		objs.addFunc("qry_time", ctx.mkFuncDecl("qry_time", objs.getSort("Qry"), objs.getSort("Int")));
		objs.addFunc("qry_part", ctx.mkFuncDecl("qry_part", objs.getSort("Qry"), objs.getSort("Int")));
	}

	public void addProgramOrderFunc() {
		objs.addFunc("qry_po", ctx.mkFuncDecl("qry_po", objs.getSort("Qry"), objs.getSort("Int")));
	}

	public void addParentFunc() {
		objs.addFunc("parent", ctx.mkFuncDecl("parent", objs.getSort("Qry"), objs.getSort("Txn")));
	}

	public Quantifier mk_uniqueness_of_time() {
		BoolExpr rhs = (BoolExpr) ctx.mkNot(
				ctx.mkEq(ctx.mkApp(objs.getfuncs("qry_time"), qry1), ctx.mkApp(objs.getfuncs("qry_time"), qry2)));
		BoolExpr lhs = ctx.mkDistinct(qry1, qry2);// ctx.mkNot(ctx.mkEq(qry1, qry2));

		BoolExpr body = ctx.mkImplies(lhs, rhs);
		Quantifier result = ctx.mkForall(new Expr[] { qry1, qry2 }, body, 1, null, null, null, null);
		return result;
	}

	public Quantifier mk_bound_on_qry_time() {
		ArithExpr ret_val = (ArithExpr) ctx.mkApp(objs.getfuncs("qry_time"), qry1);
		BoolExpr bodyGT = ctx.mkGt(ret_val, ctx.mkInt(0));
		BoolExpr bodyLT = ctx.mkLt(ret_val, ctx.mkInt(Constants._MAX_EXECECUTION_LENGTH));
		BoolExpr body = ctx.mkAnd(bodyGT, bodyLT);
		Quantifier result = ctx.mkForall(new Expr[] { qry1 }, body, 1, null, null, null, null);
		return result;
	}

	public BoolExpr mk_bound_on_txn_instances(int current_cycle_length) {
		int limit = (Constants._MAX_TXN_INSTANCES == -1) ? (current_cycle_length - 1) : Constants._MAX_TXN_INSTANCES;
		Expr[] Ts = new Expr[limit + 1];
		for (int i = 0; i < limit + 1; i++)
			Ts[i] = ctx.mkFreshConst("txn", objs.getSort("Txn"));
		Expr body = ctx.mkNot(ctx.mkDistinct(Ts));
		Quantifier x = ctx.mkForall(Ts, body, 1, null, null, null, null);
		return x;
	}

	public Quantifier mk_bound_on_qry_part() {
		ArithExpr ret_val = (ArithExpr) ctx.mkApp(objs.getfuncs("qry_part"), qry1);
		BoolExpr bodyGT = ctx.mkGt(ret_val, ctx.mkInt(0));
		BoolExpr bodyLT = ctx.mkLt(ret_val, ctx.mkInt(Constants._MAX_PARTITION_NUMBER));
		BoolExpr body = ctx.mkAnd(bodyGT, bodyLT);
		Quantifier result = ctx.mkForall(new Expr[] { qry1 }, body, 1, null, null, null, null);
		return result;
	}

	public Quantifier mk_bound_on_po_part() {
		ArithExpr ret_val = (ArithExpr) ctx.mkApp(objs.getfuncs("qry_po"), qry1);
		BoolExpr bodyGT = ctx.mkGt(ret_val, ctx.mkInt(0));
		BoolExpr bodyLT = ctx.mkLt(ret_val, ctx.mkInt(program.getMaxQueryCount() + 1));
		BoolExpr body = ctx.mkAnd(bodyGT, bodyLT);
		Quantifier result = ctx.mkForall(new Expr[] { qry1 }, body, 1, null, null, null, null);
		return result;
	}

	public BoolExpr mk_qry_type_to_po(String qry_name, int po) {
		BoolExpr lhs = (BoolExpr) ctx.mkEq(ctx.mkApp(objs.getfuncs("qry_type"), qry1),
				ctx.mkApp(objs.getConstructor("QryType", qry_name)));
		BoolExpr rhs = ctx.mkEq((ArithExpr) ctx.mkApp(objs.getfuncs("qry_po"), qry1), ctx.mkInt(po));
		BoolExpr body = ctx.mkImplies(lhs, rhs);
		Quantifier x = ctx.mkForall(new Expr[] { qry1 }, body, 1, null, null, null, null);
		return x;
	}

	public BoolExpr mk_qry_time_respects_po() {
		ArithExpr time1 = (ArithExpr) ctx.mkApp(objs.getfuncs("qry_time"), qry1);
		ArithExpr time2 = (ArithExpr) ctx.mkApp(objs.getfuncs("qry_time"), qry2);
		ArithExpr po1 = (ArithExpr) ctx.mkApp(objs.getfuncs("qry_po"), qry1);
		ArithExpr po2 = (ArithExpr) ctx.mkApp(objs.getfuncs("qry_po"), qry2);

		BoolExpr lhs1 = ctx.mkGt(po1, po2);
		BoolExpr lhs2 = ctx.mkEq(ctx.mkApp(objs.getfuncs("parent"), qry1), ctx.mkApp(objs.getfuncs("parent"), qry2));
		BoolExpr rhs = ctx.mkGt(time1, time2);

		BoolExpr body = ctx.mkImplies(ctx.mkAnd(lhs1, lhs2), rhs);
		Quantifier x = ctx.mkForall(new Expr[] { qry1, qry2 }, body, 1, null, null, null, null);
		return x;
	}

	public BoolExpr mk_eq_types_and_eq_par_then_eq() {
		BoolExpr lhs1 = ctx.mkEq(ctx.mkApp(objs.getfuncs("parent"), qry1), ctx.mkApp(objs.getfuncs("parent"), qry2));
		BoolExpr lhs2 = ctx.mkEq(ctx.mkApp(objs.getfuncs("qry_type"), qry1),
				ctx.mkApp(objs.getfuncs("qry_type"), qry2));
		BoolExpr lhs = ctx.mkAnd(lhs1, lhs2);
		BoolExpr rhs = ctx.mkEq(qry1, qry2);
		BoolExpr body = ctx.mkImplies(lhs, rhs);
		Quantifier x = ctx.mkForall(new Expr[] { qry1, qry2 }, body, 1, null, null, null, null);
		return x;
	}

	public BoolExpr mk_pk_must_not_change(Table t) {
		String table_name = t.getTableName().getName();
		ArrayList<FieldName> pk_fields = (ArrayList<FieldName>) t.getPKFields();
		BoolExpr[] eqs = new BoolExpr[pk_fields.size()];
		int i = 0;
		for (FieldName fn : pk_fields) {
			String funcName = "proj_" + table_name + "_" + fn.getName();
			eqs[i++] = ctx.mkEq(ctx.mkApp(objs.getfuncs(funcName), rec1, time1),
					ctx.mkApp(objs.getfuncs(funcName), rec1, time2));
		}

		BoolExpr lhs = ctx.mkEq(ctx.mkApp(objs.getfuncs("rec_type"), rec1),
				ctx.mkApp(objs.getConstructor("RecType", table_name)));
		BoolExpr body = ctx.mkAnd(eqs);
		Quantifier x = ctx.mkForall(new Expr[] { rec1, time1, time2 }, ctx.mkImplies(lhs, body), 1, null, null, null,
				null);
		return x;
	}

	public BoolExpr mk_eq_pk_eq_rec(Table t) {
		String table_name = t.getTableName().getName();
		ArrayList<FieldName> pk_fields = (ArrayList<FieldName>) t.getPKFields();
		BoolExpr[] eqs = new BoolExpr[pk_fields.size() + 2];
		int i = 0;
		for (FieldName fn : pk_fields) {
			String funcName = "proj_" + table_name + "_" + fn.getName();
			eqs[i++] = ctx.mkEq(ctx.mkApp(objs.getfuncs(funcName), rec1, time1),
					ctx.mkApp(objs.getfuncs(funcName), rec2, time1));
		}
		eqs[i++] = ctx.mkEq(ctx.mkApp(objs.getfuncs("rec_type"), rec1),
				ctx.mkApp(objs.getConstructor("RecType", table_name)));
		eqs[i++] = ctx.mkEq(ctx.mkApp(objs.getfuncs("rec_type"), rec2),
				ctx.mkApp(objs.getConstructor("RecType", table_name)));
		BoolExpr lhs = ctx.mkAnd(eqs);
		BoolExpr body = ctx.mkEq(rec1, rec2);
		Quantifier x = ctx.mkForall(new Expr[] { rec1, rec2, time1 }, ctx.mkImplies(lhs, body), 1, null, null, null,
				null);
		return x;
	}

}
