package kiarahmani.atropos.encoding_engine.Z3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.EnumSort;
import com.microsoft.z3.Expr;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Model;
import com.microsoft.z3.Params;
import com.microsoft.z3.Quantifier;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Sort;
import com.microsoft.z3.Status;
import com.microsoft.z3.Symbol;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.E_Arg;
import kiarahmani.atropos.DML.expression.*;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.constants.E_Const_Bool;
import kiarahmani.atropos.DML.expression.constants.E_Const_Num;
import kiarahmani.atropos.DML.expression.constants.E_Const_Text;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.DML.query.Select_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.DML.where_clause.WHC_Constraint;
import kiarahmani.atropos.dependency.Conflict;
import kiarahmani.atropos.dependency.Conflict_Graph;
import kiarahmani.atropos.dependency.DAI;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.program.Transaction;
import kiarahmani.atropos.utils.Constants;

public class Z3Driver {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
	Context ctx;
	Solver slv;
	Model model;
	Expression_Maker em;
	Model_Handler model_handler;
	Expr rec1, rec2, time1, time2, txn1, txn2, po1, po2, arg1, arg2, fld1, fld2, part1, part2, order1, order2, txn3,
			po3;
	DeclaredObjects objs;

	public Status generateDAI(Program program, int dependency_length, DAI dai, Conflict c1, Conflict c2) {
		HashMap<String, String> cfg = new HashMap<String, String>();
		cfg.put("model", "true");
		cfg.put("unsat_core", "true");
		ctx = new Context(cfg);
		objs = new DeclaredObjects();
		// begin encoding the context
		slv = ctx.mkSolver();
		addInitialHeader();
		addInitialStaticSorts();
		addNumericEnumSorts("Po", Constants._MAX_EXECUTION_PO);
		addNumericEnumSorts("UUID", Constants._MAX_EXECUTION_PO);
		addNumericEnumSorts("Ro", Constants._MAX_VARIABLE_RO);
		addNumericEnumSorts("Part", Constants._MAX_PARTITION_NUMBER);
		addEnumSorts("RecType", program.getAllTableNames());
		addEnumSorts("TxnType", program.getAllTxnNames());
		addTypingFuncs();
		addExecutionFuncs();
		initializeLocalVariables();
		addUUIDFunc();
		addProjFuncsAndBounds(program);
		this.em = new Expression_Maker(program, ctx, objs);
		Z3Logger.SubHeaderZ3("Properties of query functions");
		addAssertion("bound_on_txn_instances", em.mk_bound_on_txn_instances(dependency_length));
		constrainPKs(program, em);
		addArgsFuncs(program);
		addVariablesFuncs(program);
		constrainIsExecuted(program, em);
		Z3Logger.HeaderZ3(program.getName() + " reads_from and writes_to functions");
		addWritesTo(program);
		constrainWritesTo(program);
		addReadsFrom(program);
		constrainReadsFrom(program);
		constrainWrittenVals(program);
		addArFunc(program);
		constrainArFunc(program);
		addWRFuncs(program);
		constrainWRFuncs(program);
		addRWFuncs(program);
		constrainRWFuncs(program);
		addWWFuncs(program);
		constrainWWFuncs(program);
		addDepFunc(program);
		constrainDepFunc(program);
		addDepSTFunc(program);
		constrainDepSTFunc(program);
		//
		// final query
		addAssertion("cycle", em.mk_cycle_exists_constrained(dependency_length, dai, c1, c2));
		//
		//
		// check satisfiability
		Status status = slv.check();
		if (status == Status.SATISFIABLE) {
			// record the generated model
			model = slv.getModel();
			File file = new File("smt2/model.smt2");
			PrintWriter printer;
			FileWriter writer;
			try {
				writer = new FileWriter(file, false);
				printer = new PrintWriter(writer);
				printer.append(model.toString().replace("!val!", "").replace("!", ""));
				printer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return status;

	}

	private void print_result_header(Status status, long begin, long end) {
		System.out.println("==================\n" + status + " (" + (end - begin) + "ms)" + "\n==================\n\n");
	}

	private void constrainWrittenVals(Program program) {
		Z3Logger.SubHeaderZ3(";; constraints on written_val_* functions");
		for (Transaction txn : program.getTransactions()) {
			Z3Logger.LogZ3(";; Queries of txn: " + txn.getName());
			for (Query q : txn.getAllQueries()) {
				Z3Logger.LogZ3(";; " + q.getId());
				for (Table t : program.getTables())
					for (FieldName fn : t.getFieldNames())
						if (q.getWrittenFieldNames().contains(fn)) {
							BoolExpr pre_condition1 = ctx.mkEq(ctx.mkApp(objs.getfuncs("txn_type"), txn1),
									objs.getEnumConstructor("TxnType", txn.getName()));
							// potential performance bug: try replacing concrete values insteatd of
							// constrained po variable
							BoolExpr pre_condition2 = ctx.mkEq(po1, objs.getEnumConstructor("Po", "po_" + q.getPo()));
							BoolExpr pre_rectype = ctx.mkEq(ctx.mkApp(objs.getfuncs("rec_type"), rec1),
									objs.getEnumConstructor("RecType", t.getTableName().getName()));
							BoolExpr pre_condition = ctx.mkAnd(pre_condition1, pre_condition2, pre_rectype);
							Expression exp = q.getUpdateExpressionByFieldName(fn);
							String funcname = "written_val_" + t.getTableName().getName() + "_" + fn.getName();
							Expr written_val = ctx.mkApp(objs.getfuncs(funcname), rec1, txn1, po1);
							Expr current_po = ctx.mkApp(objs.getfuncs("po_from_int"), ctx.mkInt(q.getPo()));
							Expr constrained_val = translateExpressionsToZ3Expr(txn.getName(), txn1, exp, current_po);
							Quantifier result = ctx.mkForall(new Expr[] { txn1, po1, rec1 },
									ctx.mkImplies(pre_condition, ctx.mkEq(written_val, constrained_val)), 1, null, null,
									null, null);
							addAssertions(result);
						} else
							Z3Logger.LogZ3(";; 	no constraint defined for " + t.getTableName().getName() + "_"
									+ fn.getName());
			}
		}
	}

	private void constrainDepSTFunc(Program program) {
		BoolExpr exists_dep_st = (BoolExpr) ctx.mkApp(objs.getfuncs("dep_st"), txn1, po1, txn2, po2);
		BoolExpr exists_dep = (BoolExpr) ctx.mkApp(objs.getfuncs("dep"), txn1, po1, txn2, po2);
		Quantifier dep_st_conditions = ctx.mkForall(new Expr[] { txn1, txn2, po1, po2 },
				ctx.mkImplies(exists_dep_st, ctx.mkOr(ctx.mkEq(txn1, txn2), exists_dep)), 1, null, null, null, null);
		addAssertion("relating dep_st to dep and same transaction relation", dep_st_conditions);
	}

	private void constrainDepFunc(Program program) {
		String dep_funcName;
		String wr_funcName;
		String rw_funcName;
		String ww_funcName;
		Z3Logger.SubHeaderZ3("relating dep to wr/rw/ww");
		for (Table t : program.getTables()) {
			for (FieldName fn : t.getFieldNames()) {
				if (!fn.isPK()) {
					dep_funcName = "dep_on_" + t.getTableName().getName();
					wr_funcName = "wr_on_" + t.getTableName().getName();
					rw_funcName = "rw_on_" + t.getTableName().getName();
					// ww_funcName = "ww_on_" + t.getTableName().getName();
					BoolExpr exists_wr = (BoolExpr) ctx.mkApp(objs.getfuncs(wr_funcName), txn1, po1, txn2, po2);
					BoolExpr exists_rw = (BoolExpr) ctx.mkApp(objs.getfuncs(rw_funcName), txn1, po1, txn2, po2);
					// BoolExpr exists_ww = (BoolExpr) ctx.mkApp(objs.getfuncs(ww_funcName), txn1,
					// po1, txn2, po2);
					BoolExpr exists_dep = (BoolExpr) ctx.mkApp(objs.getfuncs(dep_funcName), txn1, po1, txn2, po2);
					Quantifier dep_to_wr_rw_ww = ctx.mkForall(new Expr[] { txn1, txn2, po1, po2 },
							ctx.mkImplies(exists_dep, ctx.mkOr(exists_rw, exists_wr)), 1, null, null, null, null);
					addAssertions(dep_to_wr_rw_ww);
				}
			}
		}

		BoolExpr[] array_dep_t = new BoolExpr[program.getTables().size()];
		int tbl_iter = 0;
		for (Table t : program.getTables()) {

			BoolExpr exists_dep = (BoolExpr) ctx.mkApp(objs.getfuncs("dep_on_" + t.getTableName().getName()), txn1, po1,
					txn2, po2);
			array_dep_t[tbl_iter++] = exists_dep;

		}
		String funcName = "dep";
		BoolExpr exists_dep = (BoolExpr) ctx.mkApp(objs.getfuncs(funcName), txn1, po1, txn2, po2);
		Quantifier dep_to_dep_table_field = ctx.mkForall(new Expr[] { txn1, txn2, po1, po2 },
				ctx.mkImplies(exists_dep, ctx.mkOr(array_dep_t)), 1, null, null, null, null);
		addAssertion("relating dep to dep_*_*", dep_to_dep_table_field);

	}

	private void addDepSTFunc(Program program) {
		Z3Logger.SubHeaderZ3(";; definition of generic dependency relation OR same transaction relation");
		String funcName;
		funcName = "dep_st";
		objs.addFunc(funcName,
				ctx.mkFuncDecl(funcName,
						new Sort[] { objs.getSort("Txn"), objs.getEnum("Po"), objs.getSort("Txn"), objs.getEnum("Po") },
						objs.getSort("Bool")));
	}

	private void addDepFunc(Program program) {
		Z3Logger.SubHeaderZ3(";; definition of generic dependency relation");
		String funcName;
		for (Table t : program.getTables()) {
			Z3Logger.LogZ3(";; table: " + t.getTableName().getName());
			for (FieldName fn : t.getFieldNames()) {
				if (!fn.isPK()) {
					funcName = "dep_on_" + t.getTableName().getName() + "_" + fn.getName();
					// objs.addFunc(funcName, ctx.mkFuncDecl(funcName, new Sort[] {
					// objs.getSort("Txn"),
					// objs.getEnum("Po"), objs.getSort("Txn"), objs.getEnum("Po") },
					// objs.getSort("Bool")));
				}
			}
			funcName = "dep_on_" + t.getTableName().getName();
			objs.addFunc(funcName, ctx.mkFuncDecl(funcName,
					new Sort[] { objs.getSort("Txn"), objs.getEnum("Po"), objs.getSort("Txn"), objs.getEnum("Po") },
					objs.getSort("Bool")));
		}
		funcName = "dep";
		objs.addFunc(funcName,
				ctx.mkFuncDecl(funcName,
						new Sort[] { objs.getSort("Txn"), objs.getEnum("Po"), objs.getSort("Txn"), objs.getEnum("Po") },
						objs.getSort("Bool")));
	}

	private void constrainWWFuncs(Program program) {
		int i = 0;
		for (Table t : program.getTables()) {
			int fld_cnt = t.getFieldNames().size() - t.getPKFields().size();
			int fld_iter = 0;
			BoolExpr[] array_ww_t_f = new BoolExpr[fld_cnt];
			for (FieldName fn : t.getFieldNames())
				if (!fn.isPK()) {
					String funcName = "ww_on_" + t.getTableName().getName() + "_" + fn.getName();
					String rf_funcName = "writes_to_" + t.getTableName().getName() + "_" + fn.getName();
					String wt_funcName = "writes_to_" + t.getTableName().getName() + "_" + fn.getName();
					BoolExpr writes_to = (BoolExpr) ctx.mkApp(objs.getfuncs(rf_funcName), txn1, po1, rec1);
					BoolExpr reads_from = (BoolExpr) ctx.mkApp(objs.getfuncs(wt_funcName), txn2, po2, rec1);
					BoolExpr q1_is_executed = (BoolExpr) ctx.mkApp(objs.getfuncs("qry_is_executed"), txn1, po1);
					BoolExpr q2_is_executed = (BoolExpr) ctx.mkApp(objs.getfuncs("qry_is_executed"), txn2, po2);
					BoolExpr txns_are_different = ctx.mkDistinct(txn1, txn2);
					BoolExpr exists_a_record = ctx.mkExists(new Expr[] { rec1 }, ctx.mkAnd(writes_to, reads_from), 1,
							null, null, null, null);
					BoolExpr exists_func = (BoolExpr) ctx.mkApp(objs.getfuncs(funcName), txn1, po1, txn2, po2);
					array_ww_t_f[fld_iter++] = exists_func;
					BoolExpr q1_arbit_q2 = (BoolExpr) ctx.mkApp(objs.getfuncs("arbit"), txn1, po1, txn2, po2);
					BoolExpr all_conditions = ctx.mkAnd(q1_is_executed, q2_is_executed, exists_a_record,
							txns_are_different, q1_arbit_q2);
					Z3Logger.LogZ3(
							";; constrain rw relation only if both queries are executed and a conflicting record exists");
					Quantifier no_rec_no_ww = ctx.mkForall(new Expr[] { txn1, txn2, po1, po2 },
							ctx.mkImplies(exists_func, all_conditions), 1, null, null, null, null);
					addAssertions(no_rec_no_ww);
					i++;
				}
			BoolExpr exists_ww = (BoolExpr) ctx.mkApp(objs.getfuncs("ww_on_" + t.getTableName().getName()), txn1, po1,
					txn2, po2);
			Quantifier ww_to_ww_table_field = ctx.mkForall(new Expr[] { txn1, txn2, po1, po2 },
					ctx.mkImplies(exists_ww, ctx.mkOr(array_ww_t_f)), 1, null, null, null, null);
			addAssertion("relating ww_on_" + t.getTableName().getName() + " to ww_" + t.getTableName().getName() + "_*",
					ww_to_ww_table_field);
		}
		//
		// BoolExpr[] array_ww_t_f = new BoolExpr[i];
		// i = 0;
		// for (Table t : program.getTables())
		// for (FieldName fn : t.getFieldNames())
		// if (!fn.isPK()) {
		// String funcName = "ww_on_" + t.getTableName().getName() + "_" + fn.getName();
		// array_ww_t_f[i++] = (BoolExpr) ctx.mkApp(objs.getfuncs(funcName), txn1, po1,
		// txn2, po2);
		// }
		// BoolExpr exists_ww = (BoolExpr) ctx.mkApp(objs.getfuncs("ww"), txn1, po1,
		// txn2, po2);
		// Quantifier ww_to_ww_table_field = ctx.mkForall(new Expr[] { txn1, txn2, po1,
		// po2 },
		// ctx.mkImplies(exists_ww, ctx.mkOr(array_ww_t_f)), 1, null, null, null, null);
		// addAssertion("relating ww to ww_*_*", ww_to_ww_table_field);
	}

	private void constrainRWFuncs(Program program) {
		int i = 0;
		for (Table t : program.getTables()) {
			int fld_cnt = t.getFieldNames().size() - t.getPKFields().size();
			int fld_iter = 0;
			BoolExpr[] array_rw_t_f = new BoolExpr[fld_cnt];
			for (FieldName fn : t.getFieldNames())
				if (!fn.isPK()) {
					String funcName = "rw_on_" + t.getTableName().getName() + "_" + fn.getName();
					String rf_funcName = "reads_from_" + t.getTableName().getName() + "_" + fn.getName();
					String wt_funcName = "writes_to_" + t.getTableName().getName() + "_" + fn.getName();
					BoolExpr writes_to = (BoolExpr) ctx.mkApp(objs.getfuncs(rf_funcName), txn1, po1, rec1);
					BoolExpr reads_from = (BoolExpr) ctx.mkApp(objs.getfuncs(wt_funcName), txn2, po2, rec1);
					BoolExpr q1_is_executed = (BoolExpr) ctx.mkApp(objs.getfuncs("qry_is_executed"), txn1, po1);
					BoolExpr q2_is_executed = (BoolExpr) ctx.mkApp(objs.getfuncs("qry_is_executed"), txn2, po2);
					BoolExpr txns_are_different = ctx.mkDistinct(txn1, txn2);
					BoolExpr exists_a_record = ctx.mkExists(new Expr[] { rec1 }, ctx.mkAnd(writes_to, reads_from), 1,
							null, null, null, null);
					BoolExpr exists_func = (BoolExpr) ctx.mkApp(objs.getfuncs(funcName), txn1, po1, txn2, po2);
					array_rw_t_f[fld_iter++] = exists_func;
					BoolExpr q1_arbit_q2 = (BoolExpr) ctx.mkApp(objs.getfuncs("arbit"), txn1, po1, txn2, po2);
					BoolExpr q1_not_same_partition_q2 = ctx
							.mkNot(ctx.mkEq(ctx.mkApp(objs.getfuncs("qry_part"), txn1, po1),
									ctx.mkApp(objs.getfuncs("qry_part"), txn2, po2)));
					BoolExpr not_vis = ctx.mkOr(q1_arbit_q2, q1_not_same_partition_q2);
					BoolExpr all_conditions = ctx.mkAnd(q1_is_executed, q2_is_executed, exists_a_record,
							txns_are_different, not_vis);
					Z3Logger.LogZ3(
							";; constrain rw relation only if both queries are executed and a conflicting record exists");
					Quantifier no_rec_no_rw = ctx.mkForall(new Expr[] { txn1, txn2, po1, po2 },
							ctx.mkImplies(exists_func, all_conditions), 1, null, null, null, null);
					addAssertions(no_rec_no_rw);
					i++;
				}
			BoolExpr exists_rw = (BoolExpr) ctx.mkApp(objs.getfuncs("rw_on_" + t.getTableName().getName()), txn1, po1,
					txn2, po2);
			Quantifier rw_to_rw_table_field = ctx.mkForall(new Expr[] { txn1, txn2, po1, po2 },
					ctx.mkImplies(exists_rw, ctx.mkOr(array_rw_t_f)), 1, null, null, null, null);
			addAssertion("relating rw_on_" + t.getTableName().getName() + " to rw_" + t.getTableName().getName() + "_*",
					rw_to_rw_table_field);
		}
		//
		// BoolExpr[] array_rw_t_f = new BoolExpr[i];
		// i = 0;
		// for (Table t : program.getTables())
		// for (FieldName fn : t.getFieldNames())
		// if (!fn.isPK()) {
		// String funcName = "rw_on_" + t.getTableName().getName() + "_" + fn.getName();
		// array_rw_t_f[i++] = (BoolExpr) ctx.mkApp(objs.getfuncs(funcName), txn1, po1,
		// txn2, po2);
		// }
		// BoolExpr exists_rw = (BoolExpr) ctx.mkApp(objs.getfuncs("rw"), txn1, po1,
		// txn2, po2);
		// Quantifier rw_to_rw_table_field = ctx.mkForall(new Expr[] { txn1, txn2, po1,
		// po2 },
		// ctx.mkImplies(exists_rw, ctx.mkOr(array_rw_t_f)), 1, null, null, null, null);
		// addAssertion("relating rw to rw_*_*", rw_to_rw_table_field);
	}

	private void constrainWRFuncs(Program program) {
		int i = 0;
		for (Table t : program.getTables()) {
			int fld_cnt = t.getFieldNames().size() - t.getPKFields().size();
			int fld_iter = 0;
			BoolExpr[] array_wr_t_f = new BoolExpr[fld_cnt];
			for (FieldName fn : t.getFieldNames())
				if (!fn.isPK()) {
					String funcName = "wr_on_" + t.getTableName().getName() + "_" + fn.getName();
					String rf_funcName = "reads_from_" + t.getTableName().getName() + "_" + fn.getName();
					String wt_funcName = "writes_to_" + t.getTableName().getName() + "_" + fn.getName();
					BoolExpr writes_to = (BoolExpr) ctx.mkApp(objs.getfuncs(wt_funcName), txn1, po1, rec1);
					BoolExpr reads_from = (BoolExpr) ctx.mkApp(objs.getfuncs(rf_funcName), txn2, po2, rec1);
					BoolExpr expected_rec_type = ctx.mkEq(ctx.mkApp(objs.getfuncs("rec_type"), rec1),
							objs.getEnumConstructor("RecType", t.getTableName().getName()));
					BoolExpr q1_is_executed = (BoolExpr) ctx.mkApp(objs.getfuncs("qry_is_executed"), txn1, po1);
					BoolExpr q2_is_executed = (BoolExpr) ctx.mkApp(objs.getfuncs("qry_is_executed"), txn2, po2);
					BoolExpr txns_are_different = ctx.mkDistinct(txn1, txn2);
					BoolExpr exists_a_record = ctx.mkExists(new Expr[] { rec1 }, ctx.mkAnd(writes_to, reads_from), 1,
							null, null, null, null);
					BoolExpr exists_func = (BoolExpr) ctx.mkApp(objs.getfuncs(funcName), txn1, po1, txn2, po2);
					array_wr_t_f[fld_iter++] = exists_func;
					BoolExpr q1_arbit_q2 = (BoolExpr) ctx.mkApp(objs.getfuncs("arbit"), txn1, po1, txn2, po2);
					BoolExpr q1_same_partition_q2 = (BoolExpr) ctx.mkEq(ctx.mkApp(objs.getfuncs("qry_part"), txn1, po1),
							ctx.mkApp(objs.getfuncs("qry_part"), txn2, po2));

					FuncDecl proj_at_q2 = objs.getfuncs("proj_" + t.getTableName().getName() + "_" + fn.getName());
					FuncDecl written_at_q1 = objs
							.getfuncs("written_val_" + t.getTableName().getName() + "_" + fn.getName());
					Expr val_read_by_q2 = ctx.mkApp(proj_at_q2, rec1, txn2, po2);
					Expr val_written_by_q1 = ctx.mkApp(written_at_q1, rec1, txn1, po1);
					BoolExpr q2_reads_val_from_q1 = ctx.mkEq(val_written_by_q1, val_read_by_q2);

					BoolExpr all_conditions = ctx.mkAnd(q1_is_executed, q2_is_executed, exists_a_record,
							txns_are_different, q1_arbit_q2, q1_same_partition_q2, q2_reads_val_from_q1);
					Z3Logger.LogZ3(
							";; constrain wr relation only if both queries are executed and a conflicting record exists");
					Quantifier no_rec_no_wr = ctx.mkForall(new Expr[] { txn1, txn2, po1, po2 },
							ctx.mkImplies(exists_func, all_conditions), 1, null, null, null, null);
					addAssertions(no_rec_no_wr);
					i++;
				}
			BoolExpr exists_wr = (BoolExpr) ctx.mkApp(objs.getfuncs("wr_on_" + t.getTableName().getName()), txn1, po1,
					txn2, po2);
			Quantifier wr_to_wr_table_field = ctx.mkForall(new Expr[] { txn1, txn2, po1, po2 },
					ctx.mkImplies(exists_wr, ctx.mkOr(array_wr_t_f)), 1, null, null, null, null);
			addAssertion("relating wr_on_" + t.getTableName().getName() + " to wr_" + t.getTableName().getName() + "_*",
					wr_to_wr_table_field);
		}

		//
		// BoolExpr[] array_wr_t_f = new BoolExpr[i];
		// i = 0;
		// for (Table t : program.getTables())
		// for (FieldName fn : t.getFieldNames())
		// if (!fn.isPK()) {
		// String funcName = "wr_on_" + t.getTableName().getName() + "_" + fn.getName();
		// array_wr_t_f[i++] = (BoolExpr) ctx.mkApp(objs.getfuncs(funcName), txn1, po1,
		// txn2, po2);
		// }
		// BoolExpr exists_wr = (BoolExpr) ctx.mkApp(objs.getfuncs("wr"), txn1, po1,
		// txn2, po2);
		// Quantifier wr_to_wr_table_field = ctx.mkForall(new Expr[] { txn1, txn2, po1,
		// po2 },
		// ctx.mkImplies(exists_wr, ctx.mkOr(array_wr_t_f)), 1, null, null, null, null);
		// addAssertion("relating wr to wr_*_*", wr_to_wr_table_field);
	}

	private void addWRFuncs(Program program) {
		Z3Logger.SubHeaderZ3(";; definition of write-read dependency relations");
		String funcName;
		for (Table t : program.getTables()) {
			Z3Logger.LogZ3(";; table: " + t.getTableName().getName());
			for (FieldName fn : t.getFieldNames()) {
				if (!fn.isPK()) {
					funcName = "wr_on_" + t.getTableName().getName() + "_" + fn.getName();
					objs.addFunc(funcName, ctx.mkFuncDecl(funcName, new Sort[] { objs.getSort("Txn"),
							objs.getEnum("Po"), objs.getSort("Txn"), objs.getEnum("Po") }, objs.getSort("Bool")));
				}
			}
			funcName = "wr_on_" + t.getTableName().getName();
			objs.addFunc(funcName, ctx.mkFuncDecl(funcName,
					new Sort[] { objs.getSort("Txn"), objs.getEnum("Po"), objs.getSort("Txn"), objs.getEnum("Po") },
					objs.getSort("Bool")));
		}
	}

	private void addRWFuncs(Program program) {
		Z3Logger.SubHeaderZ3(";; definition of read-write dependency relations");
		String funcName;
		for (Table t : program.getTables()) {
			Z3Logger.LogZ3(";; table: " + t.getTableName().getName());
			for (FieldName fn : t.getFieldNames()) {
				if (!fn.isPK()) {
					funcName = "rw_on_" + t.getTableName().getName() + "_" + fn.getName();
					objs.addFunc(funcName, ctx.mkFuncDecl(funcName, new Sort[] { objs.getSort("Txn"),
							objs.getEnum("Po"), objs.getSort("Txn"), objs.getEnum("Po") }, objs.getSort("Bool")));
				}
			}
			funcName = "rw_on_" + t.getTableName().getName();
			objs.addFunc(funcName, ctx.mkFuncDecl(funcName,
					new Sort[] { objs.getSort("Txn"), objs.getEnum("Po"), objs.getSort("Txn"), objs.getEnum("Po") },
					objs.getSort("Bool")));
		}
	}

	private void addWWFuncs(Program program) {
		Z3Logger.SubHeaderZ3(";; definition of write-write dependency relations");
		String funcName;
		for (Table t : program.getTables()) {
			Z3Logger.LogZ3(";; table: " + t.getTableName().getName());
			for (FieldName fn : t.getFieldNames()) {
				if (!fn.isPK()) {
					funcName = "ww_on_" + t.getTableName().getName() + "_" + fn.getName();
					objs.addFunc(funcName, ctx.mkFuncDecl(funcName, new Sort[] { objs.getSort("Txn"),
							objs.getEnum("Po"), objs.getSort("Txn"), objs.getEnum("Po") }, objs.getSort("Bool")));
				}
			}
			funcName = "ww_on_" + t.getTableName().getName();
			objs.addFunc(funcName, ctx.mkFuncDecl(funcName,
					new Sort[] { objs.getSort("Txn"), objs.getEnum("Po"), objs.getSort("Txn"), objs.getEnum("Po") },
					objs.getSort("Bool")));
		}

	}

	private void addArFunc(Program program) {
		Z3Logger.SubHeaderZ3(";; definition of arbitration function");
		String funcName = "arbit";
		objs.addFunc(funcName,
				ctx.mkFuncDecl(funcName,
						new Sort[] { objs.getSort("Txn"), objs.getEnum("Po"), objs.getSort("Txn"), objs.getEnum("Po") },
						objs.getSort("Bool")));
	}

	private void constrainArFunc(Program program) {
		String funcName = "arbit";
		BoolExpr pre1 = (BoolExpr) ctx.mkApp(objs.getfuncs(funcName), txn1, po1, txn2, po2);
		BoolExpr pre2 = (BoolExpr) ctx.mkApp(objs.getfuncs(funcName), txn2, po2, txn3, po3);
		BoolExpr post1 = (BoolExpr) ctx.mkApp(objs.getfuncs(funcName), txn1, po1, txn3, po3);
		BoolExpr body = ctx.mkImplies(ctx.mkAnd(pre1, pre2), post1);
		Quantifier result = ctx.mkForall(new Expr[] { txn1, po1, txn2, po2, txn3, po3 }, body, 1, null, null, null,
				null);
		addAssertion("transitivity of arbit function", result);

		BoolExpr loop = (BoolExpr) ctx.mkApp(objs.getfuncs(funcName), txn1, po1, txn1, po1);
		result = ctx.mkForall(new Expr[] { txn1, po1 }, ctx.mkNot(loop), 1, null, null, null, null);
		addAssertion("irreflixivity of arbit function", result);

		FuncDecl po_to_int = objs.getfuncs("po_to_int");
		pre1 = (BoolExpr) ctx.mkApp(objs.getfuncs("arbit"), txn1, po1, txn1, po2);
		post1 = ctx.mkLt((ArithExpr) ctx.mkApp(po_to_int, po1), (ArithExpr) ctx.mkApp(po_to_int, po2));
		result = ctx.mkForall(new Expr[] { txn1, po1, po2 }, ctx.mkImplies(pre1, post1), 1, null, null, null, null);
		addAssertion("arbitration respects program order", result);
	}

	private void constrainIsExecuted(Program program, Expression_Maker em) {
		for (Transaction txn : program.getTransactions()) {
			Z3Logger.SubHeaderZ3("is executed? (" + txn.getName() + ")");
			for (Query q : txn.getAllQueries())
				addQryTypeToIsExecuted(txn, q);
			addPoLargerThanQryCntIsNotExecuted(txn);
		}
	}

	private void constrainPKs(Program program, Expression_Maker em) {
		Z3Logger.SubHeaderZ3("Properties of tables");
		for (Table t : program.getTables()) {
			String table_name = t.getTableName().getName();
			addAssertion("pk_must_not_change_" + table_name, em.mk_pk_must_not_change(t));
			addAssertion("eq_pk_then_eq_" + table_name, em.mk_eq_pk_eq_rec(t));
		}
	}

	private void addRFFuncs(Program program) {
		Z3Logger.SubHeaderZ3(";; definition of RF functions");
		String funcName;
		for (Table t : program.getTables()) {
			Z3Logger.LogZ3(";; table: " + t.getTableName().getName());
			for (FieldName fn : t.getFieldNames())
				if (!fn.isPK()) {
					funcName = "RF_" + t.getTableName().getName() + "_" + fn.getName();
					objs.addFunc(funcName, ctx.mkFuncDecl(funcName, new Sort[] { objs.getSort("Txn"),
							objs.getEnum("Po"), objs.getSort("Txn"), objs.getEnum("Po") }, objs.getSort("Bool")));
				}
		}
	}

	private void addReadsFrom(Program program) {
		Z3Logger.SubHeaderZ3("\n;; definition of reads_from functions");
		// definition of projection functions
		String funcName;
		for (Table t : program.getTables()) {
			Z3Logger.LogZ3(";; table: " + t.getTableName().getName());
			for (FieldName fn : t.getFieldNames()) {
				funcName = "reads_from_" + t.getTableName().getName() + "_" + fn.getName();
				objs.addFunc(funcName,
						ctx.mkFuncDecl(funcName,
								new Sort[] { objs.getSort("Txn"), objs.getEnum("Po"), objs.getSort("Rec") },
								objs.getSort("Bool")));
			}
		}
	}

	private void addWritesTo(Program program) {
		Z3Logger.SubHeaderZ3("\n;; definition of writes_to functions");
		// definition of projection functions
		String funcName;
		for (Table t : program.getTables()) {
			Z3Logger.LogZ3(";; table: " + t.getTableName().getName());
			for (FieldName fn : t.getFieldNames()) {
				funcName = "writes_to_" + t.getTableName().getName() + "_" + fn.getName();
				objs.addFunc(funcName,
						ctx.mkFuncDecl(funcName,
								new Sort[] { objs.getSort("Txn"), objs.getEnum("Po"), objs.getSort("Rec") },
								objs.getSort("Bool")));
			}
		}
	}

	private void constrainReadsFrom(Program program) {
		Z3Logger.SubHeaderZ3(";; constraints on reads_from functions");
		for (Table t : program.getTables())
			for (FieldName fn : t.getFieldNames()) {
				// constrain the function for unrelated transactions
				BoolExpr unrelated_pre_condition1 = ctx.mkEq(ctx.mkApp(objs.getfuncs("rec_type"), rec1),
						objs.getEnumConstructor("RecType", t.getTableName().getName()));
				String unrelated_funcName = "reads_from_" + t.getTableName().getName() + "_" + fn.getName();
				BoolExpr unrelated_body = (BoolExpr) ctx.mkApp(objs.getfuncs(unrelated_funcName), txn1, po1, rec1);
				Quantifier unrelated_result = ctx.mkForall(new Expr[] { txn1, po1, rec1 },
						ctx.mkImplies(unrelated_body, unrelated_pre_condition1), 1, null, null, null, null);
				addAssertions(unrelated_result);
			}
		for (Transaction txn : program.getTransactions()) {
			Z3Logger.LogZ3(";; Queries of txn: " + txn.getName());
			for (Query q : txn.getAllQueries()) {
				Z3Logger.LogZ3(";; " + q.getId());
				for (Table t : program.getTables()) {
					for (FieldName fn : t.getFieldNames()) {
						if (q.getReadFieldNames().contains(fn)) {
							// special case: when the field is is_alive, read_from must NOT include the
							// constrain from WHC regarding is_alive itself
							if (fn.getName().contains("is_alive")) {
								BoolExpr pre_condition1 = ctx.mkEq(ctx.mkApp(objs.getfuncs("txn_type"), txn1),
										objs.getEnumConstructor("TxnType", txn.getName()));
								Expr expected_po = objs.getEnumConstructor("Po", "po_" + q.getPo());
								BoolExpr pre_condition = (pre_condition1);
								String funcName = "reads_from_" + t.getTableName().getName() + "_" + fn.getName();

								Expr validWT = ctx.mkApp(objs.getfuncs(funcName), txn1, expected_po, rec1);
								BoolExpr validRecType = ctx.mkEq(ctx.mkApp(objs.getfuncs("rec_type"), rec1),
										objs.getEnumConstructor("RecType", t.getTableName().getName()));
								BoolExpr whc_to_expr = translateWhereClauseWithoutIsAliveToZ3Expr(txn.getName(), txn1,
										q.getWHC(), rec1, expected_po);
								BoolExpr body = ctx.mkImplies((BoolExpr) validWT, ctx.mkAnd(validRecType, whc_to_expr));
								Quantifier result = ctx.mkForall(new Expr[] { txn1, rec1 },
										ctx.mkImplies(pre_condition, body), 1, null, null, null, null);
								addAssertions(result);
							} else {
								BoolExpr pre_condition1 = ctx.mkEq(ctx.mkApp(objs.getfuncs("txn_type"), txn1),
										objs.getEnumConstructor("TxnType", txn.getName()));
								Expr expected_po = objs.getEnumConstructor("Po", "po_" + q.getPo());
								BoolExpr pre_condition = (pre_condition1);
								String funcName = "reads_from_" + t.getTableName().getName() + "_" + fn.getName();

								Expr validWT = ctx.mkApp(objs.getfuncs(funcName), txn1, expected_po, rec1);
								BoolExpr validRecType = ctx.mkEq(ctx.mkApp(objs.getfuncs("rec_type"), rec1),
										objs.getEnumConstructor("RecType", t.getTableName().getName()));

								BoolExpr whc_to_expr = translateWhereClauseToZ3Expr(txn.getName(), txn1, q.getWHC(),
										rec1, expected_po);
								BoolExpr body = ctx.mkImplies((BoolExpr) validWT, ctx.mkAnd(whc_to_expr, validRecType));
								Quantifier result = ctx.mkForall(new Expr[] { txn1, rec1 },
										ctx.mkImplies(pre_condition, body), 1, null, null, null, null);
								addAssertions(result);
							}
						} else {
							Expr expected_po = objs.getEnumConstructor("Po", "po_" + q.getPo());
							BoolExpr pre_condition1 = ctx.mkEq(ctx.mkApp(objs.getfuncs("txn_type"), txn1),
									objs.getEnumConstructor("TxnType", txn.getName()));
							BoolExpr pre_condition2 = ctx.mkEq(expected_po,
									objs.getEnumConstructor("Po", "po_" + q.getPo()));
							BoolExpr pre_condition = ctx.mkAnd(pre_condition1, pre_condition2);
							String funcName = "reads_from_" + t.getTableName().getName() + "_" + fn.getName();
							BoolExpr body = ctx
									.mkNot((BoolExpr) ctx.mkApp(objs.getfuncs(funcName), txn1, expected_po, rec1));
							Quantifier result = ctx.mkForall(new Expr[] { txn1, rec1 },
									ctx.mkImplies(pre_condition, body), 1, null, null, null, null);
							addAssertions(result);
						}
					}
				}
			}
		}

	}

	private void constrainWritesTo(Program program) {
		Z3Logger.SubHeaderZ3(";; constraints on writes_to functions");
		for (Table t : program.getTables())
			for (FieldName fn : t.getFieldNames()) {
				// constrain the function for unrelated transactions
				BoolExpr unrelated_pre_condition1 = ctx.mkEq(ctx.mkApp(objs.getfuncs("rec_type"), rec1),
						objs.getEnumConstructor("RecType", t.getTableName().getName()));
				String unrelated_funcName = "writes_to_" + t.getTableName().getName() + "_" + fn.getName();
				BoolExpr unrelated_body = (BoolExpr) ctx.mkApp(objs.getfuncs(unrelated_funcName), txn1, po1, rec1);
				Quantifier unrelated_result = ctx.mkForall(new Expr[] { txn1, po1, rec1 },
						ctx.mkImplies(unrelated_body, unrelated_pre_condition1), 1, null, null, null, null);
				addAssertions(unrelated_result);
			}

		for (Transaction txn : program.getTransactions()) {
			Z3Logger.LogZ3(";; Queries of txn: " + txn.getName());
			// constrain quries of the related transactions
			for (Query q : txn.getAllQueries()) {
				Z3Logger.LogZ3(";; " + q.getId());
				for (Table t : program.getTables()) {
					for (FieldName fn : t.getFieldNames()) {
						if (q.isWrite() && q.getWrittenFieldNames().contains(fn)) {
							BoolExpr pre_condition1 = ctx.mkEq(ctx.mkApp(objs.getfuncs("txn_type"), txn1),
									objs.getEnumConstructor("TxnType", txn.getName()));
							Expr expected_po = objs.getEnumConstructor("Po", "po_" + q.getPo());
							BoolExpr pre_condition2 = ctx.mkEq(po1, objs.getEnumConstructor("Po", "po_" + q.getPo()));
							BoolExpr pre_condition = pre_condition1;
							String funcName = "writes_to_" + t.getTableName().getName() + "_" + fn.getName();
							Expr validWT = ctx.mkApp(objs.getfuncs(funcName), txn1, expected_po, rec1);
							BoolExpr validRecType = ctx.mkEq(ctx.mkApp(objs.getfuncs("rec_type"), rec1),
									objs.getEnumConstructor("RecType", t.getTableName().getName()));
							// Expr query_time = ctx.mkApp(objs.getfuncs("qry_time"), txn1, po1);
							BoolExpr whc_to_expr = translateWhereClauseToZ3Expr(txn.getName(), txn1, q.getWHC(), rec1,
									expected_po);
							BoolExpr body = ctx.mkImplies((BoolExpr) validWT, ctx.mkAnd(whc_to_expr, validRecType));
							Quantifier result = ctx.mkForall(new Expr[] { txn1, rec1 },
									ctx.mkImplies(pre_condition, body), 1, null, null, null, null);
							addAssertions(result);
						} else {
							BoolExpr pre_condition1 = ctx.mkEq(ctx.mkApp(objs.getfuncs("txn_type"), txn1),
									objs.getEnumConstructor("TxnType", txn.getName()));
							Expr expected_po = objs.getEnumConstructor("Po", "po_" + q.getPo());
							BoolExpr pre_condition2 = ctx.mkEq(po1, objs.getEnumConstructor("Po", "po_" + q.getPo()));
							BoolExpr pre_condition = pre_condition1;
							String funcName = "writes_to_" + t.getTableName().getName() + "_" + fn.getName();
							BoolExpr body = ctx
									.mkNot((BoolExpr) ctx.mkApp(objs.getfuncs(funcName), txn1, expected_po, rec1));
							Quantifier result = ctx.mkForall(new Expr[] { txn1, rec1 },
									ctx.mkImplies(pre_condition, body), 1, null, null, null, null);
							addAssertions(result);
						}
					}
				}
			}
		}

	}

	private void addUUIDFunc() {
		Z3Logger.LogZ3(";; uuid related functions");
		objs.addFunc("uuid",
				ctx.mkFuncDecl("uuid", new Sort[] { objs.getSort("Txn"), objs.getEnum("Po") }, objs.getEnum("UUID")));
		FuncDecl uuid = objs.getfuncs("uuid");
		BoolExpr pre_condition1 = ctx.mkNot(ctx.mkEq(po1, po2));
		BoolExpr pre_condition2 = ctx.mkNot(ctx.mkEq(txn1, txn2));
		BoolExpr pre_conditions = ctx.mkAnd(pre_condition1, pre_condition2);
		BoolExpr rhs = (ctx.mkEq(ctx.mkApp(uuid, txn1, po1), ctx.mkApp(uuid, txn2, po2)));
		Quantifier result = ctx.mkForall(new Expr[] { txn1, rec1 }, ctx.mkImplies(rhs, pre_conditions), 1, null, null,
				null, null);
		addAssertion("uuids must be unique", result);
	}

	private void addExecutionFuncs() {
		Z3Logger.LogZ3(";; query related functions");
		objs.addFunc("qry_part", ctx.mkFuncDecl("qry_part", new Sort[] { objs.getSort("Txn"), objs.getEnum("Po") },
				objs.getEnum("Part")));
		objs.addFunc("qry_is_executed", ctx.mkFuncDecl("qry_is_executed",
				new Sort[] { objs.getSort("Txn"), objs.getEnum("Po") }, objs.getSort("Bool")));
	}

	private void addTypingFuncs() {
		Z3Logger.LogZ3("\n;; functions for bounded type assignment for uninterpreted sorts");
		objs.addFunc("txn_type", ctx.mkFuncDecl("txn_type", objs.getSort("Txn"), objs.getEnum("TxnType")));
		objs.addFunc("rec_type", ctx.mkFuncDecl("rec_type", objs.getSort("Rec"), objs.getEnum("RecType")));
	}

	private void addInitialStaticSorts() {
		Z3Logger.HeaderZ3("Basic Sorts and Types");
		objs.addSort("Rec", ctx.mkUninterpretedSort("Rec"));
		objs.addSort("Txn", ctx.mkUninterpretedSort("Txn"));
		objs.addSort("Bool", ctx.mkBoolSort());
		objs.addSort("Int", ctx.mkIntSort());
		objs.addSort("String", ctx.mkStringSort());
		objs.addSort("Arg", ctx.mkBitVecSort(Constants._MAX_ARG_INT));
		objs.addSort("Fld", ctx.mkBitVecSort(Constants._MAX_FIELD_INT));
	}

	private void addEnumSorts(String name, String[] constructors) {
		Symbol symbol = ctx.mkSymbol(name);
		objs.addSymbol(name, symbol);
		Symbol[] array = new Symbol[constructors.length];
		for (int i = 0; i < constructors.length; i++)
			array[i] = ctx.mkSymbol(constructors[i]);
		EnumSort new_enum = ctx.mkEnumSort(symbol, array);
		objs.addEnum(name, new_enum);
	}

	private void addNumericEnumSorts(String name, int size) {
		Symbol symbol = ctx.mkSymbol(name);
		objs.addSymbol(name, symbol);
		Symbol[] array = new Symbol[size];
		for (int i = 0; i < size; i++)
			array[i] = ctx.mkSymbol(name.toLowerCase() + "_" + i);
		EnumSort new_enum = ctx.mkEnumSort(symbol, array);
		objs.addEnum(name, new_enum);

		// define toInt function for this enum sort
		String func_name = name.toLowerCase() + "_to_int";
		Z3Logger.LogZ3(";; " + func_name);
		objs.addFunc(func_name, ctx.mkFuncDecl(func_name, new Sort[] { objs.getEnum(name) }, objs.getSort("Int")));
		for (int i = 0; i < size; i++)
			addAssertions(
					ctx.mkEq(ctx.mkApp(objs.getfuncs(func_name), objs.getEnum(name).getConsts()[i]), ctx.mkInt(i)));

		// define fromInt function this enum sort
		String from_func_name = name.toLowerCase() + "_from_int";
		Z3Logger.LogZ3(";; " + from_func_name);
		objs.addFunc(from_func_name,
				ctx.mkFuncDecl(from_func_name, new Sort[] { objs.getSort("Int") }, objs.getEnum(name)));
		for (int i = 0; i < size; i++)
			addAssertions(ctx.mkEq(ctx.mkApp(objs.getfuncs(from_func_name), ctx.mkInt(i)),
					objs.getEnum(name).getConsts()[i]));
	}

	private void initializeLocalVariables() {
		txn1 = ctx.mkFreshConst("txn", objs.getSort("Txn"));
		txn2 = ctx.mkFreshConst("txn", objs.getSort("Txn"));
		txn3 = ctx.mkFreshConst("txn", objs.getSort("Txn"));
		rec1 = ctx.mkFreshConst("rec", objs.getSort("Rec"));
		rec2 = ctx.mkFreshConst("rec", objs.getSort("Rec"));
		po1 = ctx.mkFreshConst("po", objs.getEnum("Po"));
		po2 = ctx.mkFreshConst("po", objs.getEnum("Po"));
		po3 = ctx.mkFreshConst("po", objs.getEnum("Po"));
		arg1 = ctx.mkFreshConst("arg", objs.getSort("Arg"));
		arg2 = ctx.mkFreshConst("arg", objs.getSort("Arg"));
		fld1 = ctx.mkFreshConst("fld", objs.getSort("Fld"));
		fld2 = ctx.mkFreshConst("fld", objs.getSort("Fld"));
		part1 = ctx.mkFreshConst("part", objs.getEnum("Part"));
		part2 = ctx.mkFreshConst("part", objs.getEnum("Part"));
		order1 = ctx.mkFreshConst("order", objs.getEnum("Ro"));
		order2 = ctx.mkFreshConst("order", objs.getEnum("Ro"));
	}

	private void addPoLargerThanQryCntIsNotExecuted(Transaction txn) {
		int qry_cnt = txn.getAllQueries().size();
		Expr expected_txn_type = objs.getEnumConstructor("TxnType", txn.getName());
		BoolExpr lhs1 = ctx.mkGt((ArithExpr) ctx.mkApp(objs.getfuncs("po_to_int"), po1), ctx.mkInt(qry_cnt));
		BoolExpr lhs2 = ctx.mkEq(ctx.mkApp(objs.getfuncs("txn_type"), txn1), expected_txn_type);
		BoolExpr rhs = ctx.mkNot((BoolExpr) ctx.mkApp(objs.getfuncs("qry_is_executed"), txn1, po1));
		BoolExpr body = ctx.mkImplies(ctx.mkAnd(lhs1, lhs2), rhs);
		BoolExpr result = ctx.mkForall(new Expr[] { txn1, po1 }, body, 1, null, null, null, null);
		addAssertion("Program order larger than number of queries is not executed: " + txn.getName(), result);
	}

	private void addQryTypeToIsExecuted(Transaction txn, Query q) {
		Expr expected_txn_type = objs.getEnumConstructor("TxnType", txn.getName());
		// BoolExpr lhs1 = ctx.mkEq(po1, objs.getEnumConstructor("Po", "po_" +
		// q.getPo()));
		Expr current_po = ctx.mkApp(objs.getfuncs("po_from_int"), ctx.mkInt(q.getPo()));
		BoolExpr lhs2 = ctx.mkEq(ctx.mkApp(objs.getfuncs("txn_type"), txn1), expected_txn_type);
		BoolExpr rhs = ctx.mkEq(
				ctx.mkApp(objs.getfuncs("qry_is_executed"), txn1, objs.getEnumConstructor("Po", "po_" + q.getPo())),
				(BoolExpr) translateExpressionsToZ3Expr(txn.getName(), txn1, q.getPathCondition(), current_po));
		BoolExpr body = ctx.mkImplies(lhs2, rhs);
		BoolExpr result = ctx.mkForall(new Expr[] { txn1 }, body, 1, null, null, null, null);
		addAssertion("qry_type_to_is_executed_" + q.getId(), result);
	}

	private void addVariablesFuncs(Program program) {
		Z3Logger.HeaderZ3(program.getName() + " (Variables)");
		for (Transaction txn : program.getTransactions()) {
			for (Query q : txn.getAllQueries()) {
				if (!q.isWrite()) {
					Variable current_var = ((Select_Query) q).getVariable();
					String funcName = txn.getName() + "_var_" + current_var.getName();
					Z3Logger.SubHeaderZ3("Functions and properties for " + current_var.getName());

					// def main function
					Z3Logger.LogZ3("\n;; definition of var function " + funcName);
					objs.addFunc(funcName, ctx.mkFuncDecl(funcName,
							new Sort[] { objs.getSort("Txn"), objs.getEnum("Ro") }, objs.getSort("Rec")));

					// def time func
					String timeFuncName = txn.getName() + "_var_" + current_var.getName() + "_gen_time";
					objs.addFunc(timeFuncName,
							ctx.mkFuncDecl(timeFuncName, new Sort[] { objs.getSort("Txn") }, objs.getEnum("Po")));
					// def size func
					String sizeFuncName = txn.getName() + "_var_" + current_var.getName() + "_size";
					objs.addFunc(sizeFuncName,
							ctx.mkFuncDecl(sizeFuncName, new Sort[] { objs.getSort("Txn") }, objs.getEnum("Ro")));

					// properties of time func
					Expr generated_time = ctx.mkApp(objs.getfuncs(timeFuncName), txn1);
					// Expr time_of_query = po1; // ctx.mkApp(objs.getfuncs("qry_time"), txn1, po1);
					Expr expected_po = objs.getEnumConstructor("Po", "po_" + q.getPo());
					// Expr expected_txn_type = objs.getEnumConstructor("TxnType", txn.getName());
					// BoolExpr lhs1 = ctx.mkEq(po1, expected_po);
					// BoolExpr lhs2 = ctx.mkEq(ctx.mkApp(objs.getfuncs("txn_type"), txn1),
					// expected_txn_type);
					BoolExpr rhs = ctx.mkEq(generated_time, expected_po);
					// BoolExpr body = ctx.mkImplies(lhs2, rhs);
					// performance enhance: try using rhs instead of body
					Quantifier ass1 = ctx.mkForall(new Expr[] { txn1 }, rhs, 1, null, null, null, null);
					addAssertion("assertion that the time matches the select time for " + current_var.getName(), ass1);
					// properties of main func
					// prop#1: a record is in var only if satisfies the whc
					Expr the_record = ctx.mkApp(objs.getfuncs(funcName), txn1, order1);
					BoolExpr where_body = translateWhereClauseToZ3Expr(txn.getName(), txn1, q.getWHC(), the_record,
							expected_po);
					BoolExpr is_alive_body = (BoolExpr) ctx.mkApp(
							objs.getfuncs("proj_" + q.getTableName().getName() + "_is_alive"), the_record, txn1,
							expected_po);
					BoolExpr rec_type_body = ctx.mkEq(ctx.mkApp(objs.getfuncs("rec_type"), the_record),
							objs.getEnumConstructor("RecType", q.getTableName().getName()));
					Quantifier where_assertion = ctx.mkForall(new Expr[] { txn1, order1 },
							ctx.mkAnd(rec_type_body, where_body), 1, null, null, null, null);
					addAssertion("any record in " + current_var.getName()
							+ " must be alive and satisfy the associated where clause, at the time of generation",
							where_assertion);
					// prop#2 if a rec satisfies certain properties, then it must be in the var
					BoolExpr expected_rec_type = ctx.mkEq(ctx.mkApp(objs.getfuncs("rec_type"), rec1),
							objs.getEnumConstructor("RecType", q.getTableName().getName()));
					BoolExpr expected_is_alive = (BoolExpr) ctx.mkApp(
							objs.getfuncs("proj_" + q.getTableName().getName() + "_is_alive"), rec1, txn1, expected_po);
					BoolExpr where_body2 = ctx.mkAnd(expected_rec_type,
							translateWhereClauseToZ3Expr(txn.getName(), txn1, q.getWHC(), rec1, expected_po));
					BoolExpr body2 = ctx.mkEq(the_record, rec1);
					BoolExpr exists_clause = ctx.mkExists(new Expr[] { order1 }, body2, 1, null, null, null, null);
					Quantifier where_assertion2 = ctx.mkForall(new Expr[] { txn1, rec1 },
							ctx.mkImplies(where_body2, exists_clause), 1, null, null, null, null);
					// addAssertion(
					// "if an *alive* record satisfies the where clause, it must be in " +
					// current_var.getName(),
					// where_assertion2);
				}
			}
		}
	}

	public void addArgsFuncs(Program program) {
		Z3Logger.HeaderZ3(program.getName() + " (Transactrions Sorts, types and functions)");
		for (Transaction txn : program.getTransactions()) {
			Z3Logger.SubHeaderZ3("Transaction: " + txn.getName());
			for (E_Arg arg : txn.getArgs()) {
				String funcName = txn.getName() + "_arg_" + arg.getName();
				Z3Logger.LogZ3("\n;; definition of arg function " + funcName);
				switch (arg.getType()) {
				case NUM:
					objs.addFunc(funcName, ctx.mkFuncDecl(funcName, objs.getSort("Txn"), objs.getSort("Arg")));
					break;
				case TEXT:
					objs.addFunc(funcName, ctx.mkFuncDecl(funcName, objs.getSort("Txn"), objs.getSort("String")));
					// add bounds
					Expr ret_val2 = ctx.mkApp(objs.getfuncs(funcName), txn1);
					BoolExpr[] eqs = new BoolExpr[Constants._MAX_ARG_STRING];
					for (int i = 0; i < Constants._MAX_ARG_STRING; i++)
						eqs[i] = ctx.mkEq(ret_val2, ctx.MkString("string-val#" + i));
					BoolExpr body2 = ctx.mkOr(eqs);
					Quantifier result2 = ctx.mkForall(new Expr[] { txn1 }, body2, 1, null, null, null, null);
					addAssertion("bound_on_" + funcName, result2);
					break;
				case BOOL:
					objs.addFunc(funcName, ctx.mkFuncDecl(funcName, objs.getSort("Txn"), objs.getSort("Bool")));
					break;

				default:
					assert (false) : "unhandled arg type";
					break;
				}

			}
		}
	}

	private void addProjFuncsAndBounds(Program program) {
		Z3Logger.HeaderZ3(program.getName() + " (/ sorts, types and functions)");
		// Z3Logger.LogZ3("\n;; definition of is_alive projection function for all
		// tables");
		String funcName = "";
		String writen_proj_func = "";
		// objs.addFunc(funcName, ctx.mkFuncDecl(funcName,
		// new Sort[] { objs.getSort("Rec"), objs.getSort("Txn"), objs.getEnum("Po") },
		// objs.getSort("Bool")));
		// definition of projection functions
		for (Table t : program.getTables()) {
			Z3Logger.SubHeaderZ3(t.getTableName().getName().toUpperCase());
			for (FieldName fn : t.getFieldNames()) {
				funcName = "proj_" + t.getTableName().getName() + "_" + fn.getName();
				writen_proj_func = "written_val_" + t.getTableName().getName() + "_" + fn.getName();
				Z3Logger.LogZ3(";; definition of projection functions " + funcName);
				switch (fn.getType()) {
				case NUM:
					// define function1
					objs.addFunc(funcName,
							ctx.mkFuncDecl(funcName,
									new Sort[] { objs.getSort("Rec"), objs.getSort("Txn"), objs.getEnum("Po") },
									objs.getSort("Fld")));
					// define function2
					objs.addFunc(writen_proj_func,
							ctx.mkFuncDecl(writen_proj_func,
									new Sort[] { objs.getSort("Rec"), objs.getSort("Txn"), objs.getEnum("Po") },
									objs.getSort("Fld")));

					// define bounds1
					Z3Logger.LogZ3(";; constrain the values of " + funcName + " for unrelated tables");
					Expr func_ret_val = ctx.mkApp(objs.getfuncs(funcName), rec1, txn1, po1);
					int number_of_unrelated_tables = program.getTables().size() - 1;
					BoolExpr[] num_eqs = new BoolExpr[number_of_unrelated_tables];
					int iter = 0;
					for (Table unrelated_table : program.getTables())
						if (!unrelated_table.getTableName().getName().contains(t.getTableName().getName()))
							num_eqs[iter++] = ctx.mkEq(ctx.mkApp(objs.getfuncs("rec_type"), rec1),
									objs.getEnumConstructor("RecType", unrelated_table.getTableName().getName()));
					BoolExpr num_body = ctx.mkEq(func_ret_val, ctx.mkBV(0, Constants._MAX_FIELD_INT));
					Quantifier num_result2 = ctx.mkForall(new Expr[] { rec1, txn1, po1 },
							ctx.mkImplies(ctx.mkOr(num_eqs), num_body), 1, null, null, null, null);

					addAssertions(num_result2);

					Z3Logger.LogZ3(";; constrain the values of " + writen_proj_func + " for unrelated tables");
					Expr func_ret_val2 = ctx.mkApp(objs.getfuncs(writen_proj_func), rec1, txn1, po1);
					int number_of_unrelated_tables2 = program.getTables().size() - 1;
					BoolExpr[] num_eqs2 = new BoolExpr[number_of_unrelated_tables2];
					int iter2 = 0;
					for (Table unrelated_table2 : program.getTables())
						if (!unrelated_table2.getTableName().getName().contains(t.getTableName().getName()))
							num_eqs2[iter2++] = ctx.mkEq(ctx.mkApp(objs.getfuncs("rec_type"), rec1),
									objs.getEnumConstructor("RecType", unrelated_table2.getTableName().getName()));
					BoolExpr num_body2 = ctx.mkEq(func_ret_val2, ctx.mkBV(0, Constants._MAX_FIELD_INT));
					Quantifier num_result22 = ctx.mkForall(new Expr[] { rec1, txn1, po1 },
							ctx.mkImplies(ctx.mkOr(num_eqs2), num_body2), 1, null, null, null, null);
					addAssertions(num_result22);

					break;
				case TEXT:
					// define function
					objs.addFunc(funcName,
							ctx.mkFuncDecl(funcName,
									new Sort[] { objs.getSort("Rec"), objs.getSort("Txn"), objs.getEnum("Po") },
									objs.getSort("String")));
					// define function
					objs.addFunc(writen_proj_func,
							ctx.mkFuncDecl(writen_proj_func,
									new Sort[] { objs.getSort("Rec"), objs.getSort("Txn"), objs.getEnum("Po") },
									objs.getSort("String")));
					// define bounds
					Expr ret_val2 = ctx.mkApp(objs.getfuncs(funcName), rec1, txn1, po1);
					BoolExpr[] eqs = new BoolExpr[Constants._MAX_FIELD_STRING];
					for (int i = 0; i < Constants._MAX_FIELD_STRING; i++)
						eqs[i] = ctx.mkEq(ret_val2, ctx.MkString("string-val#" + i));
					BoolExpr body2 = ctx.mkOr(eqs);
					Quantifier result2 = ctx.mkForall(new Expr[] { rec1, txn1, po1 }, body2, 1, null, null, null, null);
					addAssertions(result2);
					break;
				case BOOL:
					objs.addFunc(funcName,
							ctx.mkFuncDecl(funcName,
									new Sort[] { objs.getSort("Rec"), objs.getSort("Txn"), objs.getEnum("Po") },
									objs.getSort("Bool")));
					objs.addFunc(writen_proj_func,
							ctx.mkFuncDecl(writen_proj_func,
									new Sort[] { objs.getSort("Rec"), objs.getSort("Txn"), objs.getEnum("Po") },
									objs.getSort("Bool")));
					break;
				default:
					assert (false) : "unhandled field type";
				}

			}
		}
	}

	private void addInitialHeader() {
		Z3Logger.LogZ3(";; ATROPOS");
		Z3Logger.LogZ3(";; Representation of the encoding in SMTLIB2");
	}

	public void closeCtx() {
		this.ctx.close();
	}

	private void addAssertion(String name, BoolExpr ass) {
		Z3Logger.LogZ3("\n;; " + name);
		objs.addAssertion(name, ass);
		slv.add(ass);
	}

	private void addAssertions(BoolExpr ass) {
		objs.addAssertion("", ass);
		slv.add(ass);
	}

	private BoolExpr translateWhereClauseToZ3Expr(String txnName, Expr transaction, WHC input_whc, Expr record,
			Expr po) {
		ArrayList<WHC_Constraint> constraints = input_whc.getConstraints();
		BoolExpr[] result = new BoolExpr[constraints.size()];
		for (int i = 0; i < constraints.size(); i++)
			result[i] = translateWhereConstraintToZ3Expr(txnName, transaction, constraints.get(i), record, po);
		return ctx.mkAnd(result);
	}

	private BoolExpr translateWhereClauseWithoutIsAliveToZ3Expr(String txnName, Expr transaction, WHC input_whc,
			Expr record, Expr po) {
		ArrayList<WHC_Constraint> constraints = input_whc.getConstraints();
		BoolExpr[] result = new BoolExpr[constraints.size() - 1];
		int i = 0;
		for (WHC_Constraint constraint : constraints) {
			if (!constraint.getFieldName().getName().contains("is_alive"))
				result[i++] = translateWhereConstraintToZ3Expr(txnName, transaction, constraint, record, po);
		}
		return ctx.mkAnd(result);
	}

	private Expr translateExpressionsToZ3Expr(String txnName, Expr transaction, Expression input_expr, Expr po) {
		switch (input_expr.getClass().getSimpleName()) {
		case "E_Arg":
			E_Arg exp = (E_Arg) input_expr;
			Expr result = ctx.mkApp(objs.getfuncs(txnName + "_arg_" + exp.getName()), transaction);
			return result;

		case "E_Const_Bool":
			E_Const_Bool cb_exp = (E_Const_Bool) input_expr;
			return ctx.mkBool(cb_exp.val);

		case "E_Const_Num":
			E_Const_Num cn_exp = (E_Const_Num) input_expr;
			return ctx.mkBV(cn_exp.val, Constants._MAX_FIELD_INT);
		case "E_Const_Text":
			E_Const_Text ct_exp = (E_Const_Text) input_expr;
			return ctx.MkString(ct_exp.val);

		case "E_UnOp":
			E_UnOp uo_exp = (E_UnOp) input_expr;
			switch (uo_exp.un_op) {
			case NOT:
				return ctx.mkNot((BoolExpr) translateExpressionsToZ3Expr(txnName, transaction, uo_exp.exp, po));
			default:
				break;
			}
		case "E_Proj":
			E_Proj p_exp = (E_Proj) input_expr;
			Expr bv2int_val = ctx.mkBV2Int((BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, p_exp.e, po),
					false);
			Expr order = ctx.mkApp(objs.getfuncs("ro_from_int"), bv2int_val);

			Expr var_time = ctx.mkApp(objs.getfuncs(txnName + "_var_" + p_exp.v.getName() + "_gen_time"), transaction);
			Expr rec_expr = ctx.mkApp(objs.getfuncs(txnName + "_var_" + p_exp.v.getName()), transaction, order);
			return ctx.mkApp(objs.getfuncs("proj_" + p_exp.v.getTableName() + "_" + p_exp.f.getName()), rec_expr,
					transaction, var_time);
		case "E_Size":
			E_Size s_exp = (E_Size) input_expr;
			Expr size = ctx.mkApp(objs.getfuncs(txnName + "_var_" + s_exp.v.getName() + "_size"), transaction);
			Expr size_in_int = ctx.mkApp(objs.getfuncs("ro_to_int"), size);
			return ctx.mkInt2BV(Constants._MAX_FIELD_INT, (IntExpr) size_in_int);

		case "E_UUID":
			Expr uuid = ctx.mkApp(objs.getfuncs("uuid"), transaction, po);
			Expr uuid_in_int = ctx.mkApp(objs.getfuncs("uuid_to_int"), uuid);
			return ctx.mkInt2BV(Constants._MAX_FIELD_INT, (IntExpr) uuid_in_int);

		case "E_BinUp":
			E_BinUp bu_exp = (E_BinUp) input_expr;
			switch (bu_exp.op) {
			case GT:
				return ctx.mkBVUGT((BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1, po),
						(BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2, po));
			case LT:
				return ctx.mkBVULT((BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1, po),
						(BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2, po));
			case EQ:
				return ctx.mkEq(translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1, po),
						translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2, po));
			case MULT:
				return ctx.mkBVMul((BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1, po),
						(BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2, po));
			case PLUS:
				return ctx.mkBVAdd((BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1, po),
						(BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2, po));
			case MINUS:
				return ctx.mkBVSub((BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1, po),
						(BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2, po));
			case DIV:
				return ctx.mkBVUDiv((BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1, po),
						(BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2, po));
			case AND:
				return ctx.mkAnd((BoolExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1, po),
						(BoolExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2, po));
			case OR:
				return ctx.mkOr((BoolExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1, po),
						(BoolExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2, po));
			default:
				break;
			}

		default:
			assert (false) : "unhandled input expression type " + input_expr;
			break;
		}
		return null;
	}

	private BoolExpr translateWhereConstraintToZ3Expr(String txnName, Expr transaction, WHC_Constraint input_constraint,
			Expr record, Expr po) {
		BoolExpr result = null;
		Expr rhs = translateExpressionsToZ3Expr(txnName, transaction, input_constraint.getExpression(), po);
		String tableName = input_constraint.getTableName().getName();
		String fieldName = input_constraint.getFieldName().getName();
		FuncDecl projFunc = objs.getfuncs("proj_" + tableName + "_" + fieldName);
		Expr lhs = ctx.mkApp(projFunc, record, transaction, po);
		switch (input_constraint.getOp()) {
		case EQ:
			return ctx.mkEq(lhs, rhs);
		case GT:
			return ctx.mkGt((ArithExpr) lhs, (ArithExpr) rhs);
		case LT:
			return ctx.mkLt((ArithExpr) lhs, (ArithExpr) rhs);
		default:
			assert (false) : "unexpected binary operation used in a whc constraint";
			break;
		}
		return result;
	}

	public void printUnsatCore() {
		// TODO: print unsat core
	}
}
