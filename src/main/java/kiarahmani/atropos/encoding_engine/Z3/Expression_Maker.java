package kiarahmani.atropos.encoding_engine.Z3;

import java.util.ArrayList;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Quantifier;
import com.microsoft.z3.Sort;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.dependency.Conflict;
import kiarahmani.atropos.dependency.DAI;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.program.Transaction;
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

	public Quantifier mk_cycle_exists(int dependency_length) {
		// assert (dependency_length > txn_instances) : "Cannot form a cycle of length
		// L, within T transaction instances, where T>=L";
		Expr[] Ts = new Expr[dependency_length];
		Expr[] POs = new Expr[dependency_length];
		for (int i = 0; i < dependency_length; i++) {
			Ts[i] = ctx.mkFreshConst("txn", objs.getSort("Txn"));
			POs[i] = ctx.mkFreshConst("po", objs.getEnum("Po"));
		}
		BoolExpr[] edges = new BoolExpr[dependency_length - 3];
		for (int i = 0; i < dependency_length - 3; i++) {
			if (i % 2 == 0) {
				edges[i] = (BoolExpr) ctx.mkApp(objs.getfuncs("dep_st"), Ts[i + 1], POs[i + 1], Ts[i + 2], POs[i + 2]);
			} else {
				edges[i] = (BoolExpr) ctx.mkApp(objs.getfuncs("dep"), Ts[i + 1], POs[i + 1], Ts[i + 2], POs[i + 2]);
			}
		}
		BoolExpr base_txn_pos = ctx.mkDistinct(POs[0], POs[dependency_length - 1]);
		BoolExpr base_txn = ctx.mkEq(Ts[0], Ts[dependency_length - 1]);
		BoolExpr base_edge_1 = (BoolExpr) ctx.mkApp(objs.getfuncs("dep"), Ts[0], POs[0], Ts[1], POs[1]);
		BoolExpr base_edge_2 = (BoolExpr) ctx.mkApp(objs.getfuncs("dep"), Ts[dependency_length - 2],
				POs[dependency_length - 2], Ts[dependency_length - 1], POs[dependency_length - 1]);
		Expr body = ctx.mkAnd(base_txn_pos, base_txn, base_edge_1, base_edge_2, ctx.mkAnd(edges));
		Expr[] result = new Expr[dependency_length + dependency_length];
		System.arraycopy(Ts, 0, result, 0, dependency_length);
		System.arraycopy(POs, 0, result, dependency_length, dependency_length);
		return ctx.mkExists(result, body, 1, null, null, null, null);
	}

	public Quantifier mk_cycle_exists_constrained(int dependency_length, DAI dai, Conflict c1, Conflict c2) {
		// declare new bound variables (base transaction will be assigned 2 POs)
		Expr[] Ts = new Expr[dependency_length - 1];
		Expr[] POs = new Expr[dependency_length];
		// initialize variable arrays
		for (int i = 0; i < dependency_length - 1; i++) {
			Ts[i] = ctx.mkFreshConst("txn" + i, objs.getSort("Txn"));
			POs[i] = ctx.mkFreshConst("po" + i, objs.getEnum("Po"));
		}
		POs[dependency_length - 1] = ctx.mkFreshConst("po" + dependency_length, objs.getEnum("Po"));
		// declare dependency edges (other then the 3 edge on/from/to the base
		// transaction
		BoolExpr[] edges = new BoolExpr[dependency_length - 3];
		for (int i = 0; i < dependency_length - 3; i++)
			if (i % 2 == 0) {
				edges[i] = (BoolExpr) ctx.mkApp(objs.getfuncs("dep_st"), Ts[i + 1], POs[i + 1], Ts[i + 2], POs[i + 2]);
			} else {
				edges[i] = (BoolExpr) ctx.mkApp(objs.getfuncs("dep"), Ts[i + 1], POs[i + 1], Ts[i + 2], POs[i + 2]);
			}

		// assertions regarding base transaction
		BoolExpr base_txn_type = ctx.mkEq(ctx.mkApp(objs.getfuncs("txn_type"), Ts[0]),
				objs.getEnumConstructor("TxnType", dai.getTransaction().getName()));
		BoolExpr base_txn_po_1 = ctx.mkEq(POs[0], objs.getEnumConstructor("Po", "po_" + dai.getQuery(1).getPo()));
		BoolExpr base_txn_po_2 = ctx.mkEq(POs[dependency_length - 1],
				objs.getEnumConstructor("Po", "po_" + dai.getQuery(2).getPo()));

		// assertions regarding the first neighbour
		BoolExpr first_txn_type = ctx.mkEq(ctx.mkApp(objs.getfuncs("txn_type"), Ts[1]),
				objs.getEnumConstructor("TxnType", c1.getTransaction(2).getName()));
		BoolExpr first_txn_po_1 = ctx.mkEq(POs[1], objs.getEnumConstructor("Po", "po_" + c1.getQuery(2).getPo()));

		// assertions regarding the last neighbour
		BoolExpr last_txn_type = ctx.mkEq(ctx.mkApp(objs.getfuncs("txn_type"), Ts[dependency_length - 2]),
				objs.getEnumConstructor("TxnType", c2.getTransaction(2).getName()));
		BoolExpr last_txn_po_1 = ctx.mkEq(POs[dependency_length - 2],
				objs.getEnumConstructor("Po", "po_" + c2.getQuery(2).getPo()));

		// constrain the type of dependency for the edge between the base and first
		// neighbour
		String base_edge_1_func_name = "";
		if (!dai.getQuery(1).isWrite()) {
			assert (c1.getQuery(2).isWrite()) : "cannot form dependency between two selet queries";
			base_edge_1_func_name = "rw_on_" + c1.getTableName().getName();
		} else {
			if (!c1.getQuery(2).isWrite()) {
				base_edge_1_func_name = "wr_on_" + c1.getTableName().getName();
			} else {
				base_edge_1_func_name = "ww_on_" + c1.getTableName().getName();
			}
		}

		// constrain the type of dependency for the edge between the base and last
		// neighbour
		String base_edge_2_func_name = "";
		if (!dai.getQuery(2).isWrite()) {
			assert (c2.getQuery(2).isWrite()) : "cannot form dependency between two selet queries";
			base_edge_2_func_name = "rw_on_" + c1.getTableName().getName();
		} else {
			if (!c2.getQuery(2).isWrite()) {
				base_edge_2_func_name = "wr_on_" + c1.getTableName().getName();
			} else {
				base_edge_2_func_name = "ww_on_" + c1.getTableName().getName();
			}
		}

		// assertions regarding the edge between the base and first neighbour
		BoolExpr base_edge_1 = (BoolExpr) ctx.mkApp(objs.getfuncs(base_edge_1_func_name), Ts[0], POs[0], Ts[1], POs[1]);
		// assertions regarding the edge between the base and last neighbour
		BoolExpr base_edge_2 = (BoolExpr) ctx.mkApp(objs.getfuncs(base_edge_2_func_name), Ts[dependency_length - 2],
				POs[dependency_length - 2], Ts[0], POs[dependency_length - 1]);

		// edges[0] = (dependency_length == 4) ? ctx.mkEq(Ts[1], Ts[2]) : edges[0];

		Expr body = ctx.mkAnd(base_txn_type, base_txn_po_1, base_txn_po_2, first_txn_type, first_txn_po_1,
				last_txn_type, last_txn_po_1, base_edge_1, base_edge_2, ctx.mkAnd(edges));
		Expr[] result = new Expr[dependency_length + dependency_length - 1];
		System.arraycopy(Ts, 0, result, 0, dependency_length - 1);
		System.arraycopy(POs, 0, result, dependency_length - 1, dependency_length);
		return ctx.mkExists(result, body, 1, null, null, null, null);
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
		Quantifier x = ctx.mkForall(new Expr[] { rec1, txn1, po1, txn2, po2 }, body, 1, null, null, null, null);
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
