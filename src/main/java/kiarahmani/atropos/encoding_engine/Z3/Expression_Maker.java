package kiarahmani.atropos.encoding_engine.Z3;

import java.util.ArrayList;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Quantifier;
import com.microsoft.z3.Sort;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.utils.Constants;

public class Expression_Maker {
	Program program;
	Context ctx;
	DeclaredObjects objs;
	Expr rec1, rec2, txn1, txn2, txn3, time1, time2, po1, po2, po3, po4;

	public Expression_Maker(Program program, Context ctx, DeclaredObjects objs) {
		this.program = program;
		this.ctx = ctx;
		this.objs = objs;
		txn1 = ctx.mkFreshConst("txn", objs.getSort("Txn"));
		txn2 = ctx.mkFreshConst("txn", objs.getSort("Txn"));
		txn3 = ctx.mkFreshConst("txn", objs.getSort("Txn"));
		rec1 = ctx.mkFreshConst("rec", objs.getSort("Rec"));
		rec2 = ctx.mkFreshConst("rec", objs.getSort("Rec"));
		// time1 = ctx.mkFreshConst("time", objs.getEnum("Time"));
		// time2 = ctx.mkFreshConst("time", objs.getEnum("Time"));
		po1 = ctx.mkFreshConst("po", objs.getEnum("Po"));
		po2 = ctx.mkFreshConst("po", objs.getEnum("Po"));
		po3 = ctx.mkFreshConst("po", objs.getEnum("Po"));
		po4 = ctx.mkFreshConst("po", objs.getEnum("Po"));
	}

	public Quantifier mk_uniqueness_of_time() {
		Expr int_of_time1 = ctx.mkApp(objs.getfuncs("time_to_int"), ctx.mkApp(objs.getfuncs("qry_time"), txn1, po1));
		Expr int_of_time2 = ctx.mkApp(objs.getfuncs("time_to_int"), ctx.mkApp(objs.getfuncs("qry_time"), txn2, po2));
		Expr int_of_po1 = ctx.mkApp(objs.getfuncs("po_to_int"), po1);
		Expr int_of_po2 = ctx.mkApp(objs.getfuncs("po_to_int"), po2);
		BoolExpr rhs = ctx.mkNot(ctx.mkEq(int_of_time1, int_of_time2));
		BoolExpr lhs = ctx.mkOr(ctx.mkNot(ctx.mkEq(txn1, txn2)), ctx.mkNot(ctx.mkEq(int_of_po1, int_of_po2)));
		BoolExpr body = ctx.mkImplies(lhs, rhs);
		Quantifier result = ctx.mkForall(new Expr[] { txn1, txn2, po1, po2 }, body, 1, null, null, null, null);
		return result;
	}

	public Quantifier mk_bound_on_txn_instances(int current_cycle_length) {
		int limit = (Constants._MAX_TXN_INSTANCES == -1) ? (current_cycle_length - 1) : Constants._MAX_TXN_INSTANCES;
		Expr[] Ts = new Expr[limit + 1];
		for (int i = 0; i < limit + 1; i++)
			Ts[i] = ctx.mkFreshConst("txn", objs.getSort("Txn"));
		Expr body = ctx.mkNot(ctx.mkDistinct(Ts));
		Quantifier x = ctx.mkForall(Ts, body, 1, null, null, null, null);
		return x;
	}

	public Quantifier mk_minimum_txn_instances_with_type(int limit, String... types) {
		Expr[] Ts = new Expr[limit];
		BoolExpr[] txn_types = new BoolExpr[limit];
		for (int i = 0; i < limit; i++) {
			Ts[i] = ctx.mkFreshConst("txn", objs.getSort("Txn"));
			txn_types[i] = ctx.mkEq(ctx.mkApp(objs.getfuncs("txn_type"), Ts[i]),
					objs.getEnumConstructor("TxnType", types[i]));
		}
		Expr body = ctx.mkAnd(ctx.mkDistinct(Ts), ctx.mkAnd(txn_types));
		return ctx.mkExists(Ts, body, 1, null, null, null, null);
	}

	public Quantifier mk_minimum_txn_instances(int limit) {
		Expr[] Ts = new Expr[limit];
		for (int i = 0; i < limit; i++)
			Ts[i] = ctx.mkFreshConst("txn", objs.getSort("Txn"));
		Expr body = ctx.mkAnd(ctx.mkDistinct(Ts));
		return ctx.mkExists(Ts, body, 1, null, null, null, null);
	}

	public Quantifier mk_cycle_exists() {
		BoolExpr conf1 = (BoolExpr) ctx.mkApp(objs.getfuncs("dep"), txn1, po1, txn2, po3);
		BoolExpr conf2 = (BoolExpr) ctx.mkApp(objs.getfuncs("dep"), txn2, po4, txn1, po2);
		Expr body = ctx.mkAnd(ctx.mkDistinct(txn1, txn2), conf1, conf2);
		return ctx.mkExists(new Expr[] { txn1, txn2, po1, po2, po3, po4 }, body, 1, null, null, null, null);
	}

	public Quantifier mk_qry_time_respects_po() {
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

	public BoolExpr mk_pk_must_not_change(Table t) {
		String table_name = t.getTableName().getName();
		ArrayList<FieldName> pk_fields = (ArrayList<FieldName>) t.getPKFields();
		BoolExpr[] eqs = new BoolExpr[pk_fields.size()];
		int i = 0;
		for (FieldName fn : pk_fields) {
			String funcName = "proj_" + table_name + "_" + fn.getName();
			eqs[i++] = ctx.mkEq(ctx.mkApp(objs.getfuncs(funcName), rec1, txn1, po1),
					ctx.mkApp(objs.getfuncs(funcName), rec1, txn2, po2));
		}
		BoolExpr lhs = ctx.mkEq(ctx.mkApp(objs.getfuncs("rec_type"), rec1),
				objs.getEnumConstructor("RecType", table_name));
		BoolExpr body = ctx.mkAnd(eqs);
		Quantifier x = ctx.mkForall(new Expr[] { rec1, txn1, po1, txn2, po2 }, ctx.mkImplies(lhs, body), 1, null, null,
				null, null);
		return x;
	}

	public BoolExpr mk_eq_pk_eq_rec(Table t) {
		String table_name = t.getTableName().getName();
		ArrayList<FieldName> pk_fields = (ArrayList<FieldName>) t.getPKFields();
		BoolExpr[] eqs = new BoolExpr[pk_fields.size() + 2];
		int i = 0;
		for (FieldName fn : pk_fields) {
			String funcName = "proj_" + table_name + "_" + fn.getName();
			eqs[i++] = ctx.mkEq(ctx.mkApp(objs.getfuncs(funcName), rec1, txn1, po1),
					ctx.mkApp(objs.getfuncs(funcName), rec2, txn1, po1));
		}
		eqs[i++] = ctx.mkEq(ctx.mkApp(objs.getfuncs("rec_type"), rec1), objs.getEnumConstructor("RecType", table_name));
		eqs[i++] = ctx.mkEq(ctx.mkApp(objs.getfuncs("rec_type"), rec2), objs.getEnumConstructor("RecType", table_name));
		BoolExpr lhs = ctx.mkAnd(eqs);
		BoolExpr body = ctx.mkEq(rec1, rec2);
		Quantifier x = ctx.mkForall(new Expr[] { rec1, rec2, txn1, po1 }, ctx.mkImplies(lhs, body), 1, null, null, null,
				null);
		return x;
	}

}
