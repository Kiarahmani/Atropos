package kiarahmani.atropos.encoding_engine.Z3;

import java.util.ArrayList;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Constructor;
import com.microsoft.z3.Context;
import com.microsoft.z3.DatatypeSort;
import com.microsoft.z3.Expr;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Quantifier;
import com.microsoft.z3.Sort;
import com.microsoft.z3.Symbol;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.utils.Constants;

public class Expression_Maker {
	Program program;
	Context ctx;
	DeclaredObjects objs;
	// TODO: remove qry1 and qry2 once done with updating this class
	Expr qry1, qry2, rec1, rec2, txn1, txn2, time1, time2, po1, po2, part1, part2, arg1, arg2, fld1, fld2, order1,
			order2;

	public Expression_Maker(Program program, Context ctx, DeclaredObjects objs) {

		this.program = program;
		this.ctx = ctx;
		this.objs = objs;
		txn1 = ctx.mkFreshConst("txn", objs.getSort("Txn"));
		txn2 = ctx.mkFreshConst("txn", objs.getSort("Txn"));
		rec1 = ctx.mkFreshConst("rec", objs.getSort("Rec"));
		rec2 = ctx.mkFreshConst("rec", objs.getSort("Rec"));
		time1 = ctx.mkFreshConst("time", objs.getEnum("Time"));
		time2 = ctx.mkFreshConst("time", objs.getEnum("Time"));
		po1 = ctx.mkFreshConst("po", objs.getEnum("Po"));
		po2 = ctx.mkFreshConst("po", objs.getEnum("Po"));
		arg1 = ctx.mkFreshConst("arg", objs.getSort("Arg"));
		arg2 = ctx.mkFreshConst("arg", objs.getSort("Arg"));
		fld1 = ctx.mkFreshConst("fld", objs.getSort("Fld"));
		fld2 = ctx.mkFreshConst("fld", objs.getSort("Fld"));
		part1 = ctx.mkFreshConst("part", objs.getEnum("Part"));
		part2 = ctx.mkFreshConst("part", objs.getEnum("Part"));
	}

	private String[] getTypeConsNames(String name, int size) {
		String[] result = new String[size];
		for (int i = 0; i < size; i++)
			result[i] = name + i;
		return result;
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
		objs.addDataType("RecType", mkDataType("RecType", program.getAllTableNames()));
	}

	public void addExecTypes() {
		Z3Logger.LogZ3("\n;; bounded domains for execution variables");
		objs.addDataType("TimeVal", mkDataType("TimeVal", getTypeConsNames("time", Constants._MAX_EXECECUTION_LENGTH)));
		objs.addDataType("PoVal", mkDataType("PoVal", getTypeConsNames("po", Constants._MAX_EXECUTION_PO)));
		objs.addDataType("RoVal", mkDataType("RoVal", getTypeConsNames("ro", (int) Math.pow(2, Constants._MAX_FIELD_INT))));
		objs.addDataType("PartVal", mkDataType("PartVal", getTypeConsNames("part", Constants._MAX_PARTITION_NUMBER)));
		// objs.addDataType("ArgVal", mkDataType("ArgVal", getTypeConsNames("arg",
		// Constants._MAX_ARG_INT)));
		// objs.addDataType("FldVal", mkDataType("FldVal", getTypeConsNames("fld",
		// Constants._MAX_FIELD_INT)));
	}

	public void addTxnOpDataTypes() {
		objs.addDataType("TxnType", mkDataType("TxnType", program.getAllTxnNames()));
		objs.addDataType("QryType", mkDataType("QryType", program.getAllStmtTypes()));
	}


	public Quantifier mk_uniqueness_of_time() {
		Expr int_of_time1 = ctx.mkApp(objs.getfuncs("time_to_int"), ctx.mkApp(objs.getfuncs("qry_time"), txn1, po1));
		Expr int_of_time2 = ctx.mkApp(objs.getfuncs("time_to_int"), ctx.mkApp(objs.getfuncs("qry_time"), txn2, po2));
		Expr int_of_po1 = ctx.mkApp(objs.getfuncs("po_to_int"), po1);
		Expr int_of_po2 = ctx.mkApp(objs.getfuncs("po_to_int"), po2);
		BoolExpr lhs = ctx.mkEq(int_of_time1, int_of_time2);
		BoolExpr rhs = ctx.mkAnd((ctx.mkEq(txn1, txn2)), ctx.mkEq(int_of_po1, int_of_po2));
		BoolExpr body = ctx.mkImplies(lhs, rhs);
		Quantifier result = ctx.mkForall(new Expr[] { txn1, txn2, po1, po2 }, body, 1, null, null, null, null);
		return result;
	}

	public Quantifier mk_bound_on_qry_time() {
		ArithExpr ret_val = (ArithExpr) ctx.mkApp(objs.getfuncs("qry_time"), txn1, po1);
		BoolExpr bodyGT = ctx.mkGt(ret_val, ctx.mkInt(0));
		BoolExpr bodyLT = ctx.mkLt(ret_val, ctx.mkInt(Constants._MAX_EXECECUTION_LENGTH));
		BoolExpr body = ctx.mkAnd(bodyGT, bodyLT);
		Quantifier result = ctx.mkForall(new Expr[] { txn1, po1 }, body, 1, null, null, null, null);
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
		ArithExpr ret_val = (ArithExpr) ctx.mkApp(objs.getfuncs("qry_part"), txn1, po1);
		BoolExpr bodyGT = ctx.mkGt(ret_val, ctx.mkInt(0));
		BoolExpr bodyLT = ctx.mkLt(ret_val, ctx.mkInt(Constants._MAX_PARTITION_NUMBER));
		BoolExpr body = ctx.mkAnd(bodyGT, bodyLT);
		Quantifier result = ctx.mkForall(new Expr[] { txn1, po1 }, body, 1, null, null, null, null);
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


	public BoolExpr mk_po_to_qry_type(String txn_name, String qry_name, int po) {
		Expr po_val = objs.getEnum("Po").getConsts()[po];
		Expr txn_type = objs.getEnumConstructor("TxnType", txn_name);
		Expr expected_qry_type = objs.getEnumConstructor("QryType", qry_name);
		BoolExpr lhs1 = (BoolExpr) ctx.mkEq(ctx.mkApp(objs.getfuncs("txn_type"), txn1), txn_type);
		BoolExpr lhs2 = (BoolExpr) ctx.mkEq(po1, po_val);
		BoolExpr lhs = ctx.mkAnd(lhs1, lhs2);
		BoolExpr rhs = ctx.mkEq(ctx.mkApp(objs.getfuncs("qry_type"), txn1, po1), expected_qry_type);
		BoolExpr body = ctx.mkImplies(lhs, rhs);
		Quantifier x = ctx.mkForall(new Expr[] { txn1, po1 }, body, 1, null, null, null, null);
		return x;
	}

	public Expr intToConstructor(String type_name, int i) {
		return ctx.mkApp(objs.getConstructor(type_name + "Val", type_name.toLowerCase() + i));
	}

	public BoolExpr mk_qry_time_respects_po() {
		Expr local_time1 = ctx.mkApp(objs.getfuncs("qry_time"), txn1, po1);
		Expr local_time2 = ctx.mkApp(objs.getfuncs("qry_time"), txn1, po2);
		ArithExpr int_of_local_time1 = (ArithExpr) ctx.mkApp(objs.getfuncs("time_to_int"), local_time1);
		ArithExpr int_of_local_time2 = (ArithExpr) ctx.mkApp(objs.getfuncs("time_to_int"), local_time2);
		ArithExpr int_of_po1 = (ArithExpr) ctx.mkApp(objs.getfuncs("po_to_int"), po1);
		ArithExpr int_of_po2 = (ArithExpr) ctx.mkApp(objs.getfuncs("po_to_int"), po2);

		BoolExpr lhs = ctx.mkGt(int_of_po2, int_of_po1);
		BoolExpr rhs = ctx.mkGt(int_of_local_time2, int_of_local_time1);

		BoolExpr body = ctx.mkImplies(lhs, rhs);
		Quantifier x = ctx.mkForall(new Expr[] { txn1, po1, po2 }, body, 1, null, null, null, null);
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
				objs.getEnumConstructor("RecType", table_name));
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
		eqs[i++] = ctx.mkEq(ctx.mkApp(objs.getfuncs("rec_type"), rec1), objs.getEnumConstructor("RecType", table_name));
		eqs[i++] = ctx.mkEq(ctx.mkApp(objs.getfuncs("rec_type"), rec2), objs.getEnumConstructor("RecType", table_name));
		BoolExpr lhs = ctx.mkAnd(eqs);
		BoolExpr body = ctx.mkEq(rec1, rec2);
		Quantifier x = ctx.mkForall(new Expr[] { rec1, rec2, time1 }, ctx.mkImplies(lhs, body), 1, null, null, null,
				null);
		return x;
	}

}
