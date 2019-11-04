package kiarahmani.atropos.encoding_engine.Z3;

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
import com.microsoft.z3.Model;
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
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.program.Transaction;
import kiarahmani.atropos.utils.Constants;

public class Z3Driver {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
	Context ctx;
	Solver slv;
	Model model;
	Model_Handler model_handler;
	Expr rec1, rec2, time1, time2, txn1, txn2, po1, po2, arg1, arg2, fld1, fld2, part1, part2, order1, order2, txn3,
			po3;
	DeclaredObjects objs;

	public Z3Driver(Program program, int current_cycle_length) {
		logger.debug("new Z3 Driver object is being created");
		HashMap<String, String> cfg = new HashMap<String, String>();
		cfg.put("model", "true");
		cfg.put("unsat_core", "true");
		ctx = new Context(cfg);
		objs = new DeclaredObjects();

		// begin encoding
		slv = ctx.mkSolver();
		addInitialHeader();
		addInitialStaticSorts();
		addNumericEnumSorts("Po", Constants._MAX_EXECUTION_PO);
		addNumericEnumSorts("Ro", Constants._MAX_ARG_INT);
		addNumericEnumSorts("Time", Constants._MAX_EXECECUTION_LENGTH);
		addNumericEnumSorts("Part", Constants._MAX_PARTITION_NUMBER);
		addEnumSorts("RecType", program.getAllTableNames());
		addEnumSorts("TxnType", program.getAllTxnNames());
		addTypingFuncs();
		addExecutionFuncs();
		initializeLocalVariables();
		addProjFuncsAndBounds(program);
		Expression_Maker em = new Expression_Maker(program, ctx, objs);
		Z3Logger.SubHeaderZ3("Properties of query functions");
		addAssertion("uniqueness of time", em.mk_uniqueness_of_time());
		addAssertion("bound_on_txn_instances", em.mk_bound_on_txn_instances(current_cycle_length));
		addAssertion("qry_time_respects_po", em.mk_qry_time_respects_po());
		Z3Logger.SubHeaderZ3("Properties of tables");
		for (Table t : program.getTables()) {
			String table_name = t.getTableName().getName();
			addAssertion("pk_must_not_change_" + table_name, em.mk_pk_must_not_change(t));
			addAssertion("eq_pk_then_eq_" + table_name, em.mk_eq_pk_eq_rec(t));
		}
		Z3Logger.HeaderZ3(program.getName() + " (Transactrions Sorts, types and functions)");
		addArgsFuncs(program);
		addVariablesFuncs(program);
		Z3Logger.SubHeaderZ3("is executed?");
		for (Transaction txn : program.getTransactions())
			for (Query q : txn.getAllQueries())
				addQryTypeToIsExecuted(txn, q);
		Z3Logger.HeaderZ3(program.getName() + " reads_from and writes_to functions");
		addWritesTo(program);
		constrainWritesTo(program);
		addReadsFrom(program);
		constrainReadsFrom(program);
		addConflictFuncs(program);
		constrainConflictFunc(program);
		addWRFuncs(program);
		constrainWR(program);
		addRWFuncs(program);
		constrainRW(program);

		// TODO: will be replaced by exact cycle requirements
		Z3Logger.SubHeaderZ3(";; Temporary constraint to ensure minimum number of transactions");
		addAssertions(em.mk_minimum_txn_instances(2));
		checkSAT(program);
	}

	private void constrainRF(Program program) {
		Z3Logger.SubHeaderZ3(";; constraints on RF functions");
		for (Table t : program.getTables()) {
			for (FieldName fn : t.getFieldNames())
				if (!fn.isPK()) {
					String funcName_rf = "RF_" + t.getTableName().getName() + "_" + fn.getName();
					String funcName_wr = "WR_" + t.getTableName().getName() + "_" + fn.getName();
					BoolExpr exists_rf = (BoolExpr) ctx.mkApp(objs.getfuncs(funcName_rf), txn1, po1, txn2, po2);
					BoolExpr exists_wr = (BoolExpr) ctx.mkApp(objs.getfuncs(funcName_wr), txn1, po1, txn2, po2);
					Expr local_int = ctx.mkFreshConst("int", objs.getSort("Int"));
					Expr po3_from_int = ctx.mkApp(objs.getfuncs("po_from_int"), local_int);
					BoolExpr exists_new_wr = (BoolExpr) ctx.mkApp(objs.getfuncs(funcName_wr), txn3, po3_from_int, txn2,
							po2);

					ArithExpr int_of_time1 = (ArithExpr) ctx.mkApp(objs.getfuncs("time_to_int"),
							ctx.mkApp(objs.getfuncs("qry_time"), txn1, po1));
					ArithExpr int_of_time2 = (ArithExpr) ctx.mkApp(objs.getfuncs("time_to_int"),
							ctx.mkApp(objs.getfuncs("qry_time"), txn2, po2));

					ArithExpr int_of_time3 = (ArithExpr) ctx.mkApp(objs.getfuncs("time_to_int"),
							ctx.mkApp(objs.getfuncs("qry_time"), txn3, po3_from_int));

					BoolExpr time_is_gt = ctx.mkGt((ArithExpr) int_of_time3, int_of_time1);
					BoolExpr time_is_lt = ctx.mkLt((ArithExpr) int_of_time3, int_of_time2);

					BoolExpr no_new_wr = ctx.mkForall(new Expr[] { txn3, local_int },
							ctx.mkImplies(ctx.mkAnd(time_is_lt, time_is_gt), ctx.mkNot(exists_new_wr)), 1, null, null,
							null, null);

					Expr conditions = ctx.mkAnd(exists_wr, no_new_wr);

					Quantifier result = ctx.mkForall(new Expr[] { txn1, txn2, po1, po2 },
							ctx.mkEq(exists_rf, conditions), 1, null, null, null, null);
					Z3Logger.LogZ3(";; conditions that must hold if RF exists");
					addAssertions(result);
				}
		}
	}

	private void constrainConflictFunc(Program program) {
		Z3Logger.SubHeaderZ3(";; constraints on Conflict functions");
		for (Table t : program.getTables()) {
			for (FieldName fn : t.getFieldNames())
				if (!fn.isPK()) {
					String funcName = "conflict_on_" + t.getTableName().getName() + "_" + fn.getName();
					String rf_funcName = "reads_from_" + t.getTableName().getName() + "_" + fn.getName();
					String wt_funcName = "writes_to_" + t.getTableName().getName() + "_" + fn.getName();
					BoolExpr writes_to = (BoolExpr) ctx.mkApp(objs.getfuncs(wt_funcName), txn1, po1, rec1);
					BoolExpr reads_from = (BoolExpr) ctx.mkApp(objs.getfuncs(rf_funcName), txn2, po2, rec1);
					BoolExpr q1_is_executed = (BoolExpr) ctx.mkApp(objs.getfuncs("qry_is_executed"), txn1, po1);
					BoolExpr q2_is_executed = (BoolExpr) ctx.mkApp(objs.getfuncs("qry_is_executed"), txn2, po2);
					BoolExpr txns_are_different = ctx.mkDistinct(txn1, txn2);
					BoolExpr exists_a_record = ctx.mkExists(new Expr[] { rec1 }, ctx.mkAnd(writes_to, reads_from), 1,
							null, null, null, null);
					BoolExpr exists_func = (BoolExpr) ctx.mkApp(objs.getfuncs(funcName), txn1, po1, txn2, po2);
					Z3Logger.LogZ3(";; constrain conflict relation only if a conflicting record exists");
					Quantifier no_rec_no_wr = ctx.mkForall(new Expr[] { txn1, txn2, po1, po2 },
							ctx.mkEq(ctx.mkAnd(q1_is_executed, q2_is_executed, exists_a_record,txns_are_different), exists_func), 1, null,
							null, null, null);
					addAssertions(no_rec_no_wr);
				}
		}
	}

	private void addRWFuncs(Program program) {
		Z3Logger.SubHeaderZ3(";; definition of RW functions");
		String funcName;
		for (Table t : program.getTables()) {
			Z3Logger.LogZ3(";; table: " + t.getTableName().getName());
			for (FieldName fn : t.getFieldNames())
				if (!fn.isPK()) {
					funcName = "RW_" + t.getTableName().getName() + "_" + fn.getName();
					objs.addFunc(funcName, ctx.mkFuncDecl(funcName, new Sort[] { objs.getSort("Txn"),
							objs.getEnum("Po"), objs.getSort("Txn"), objs.getEnum("Po") }, objs.getSort("Bool")));
				}
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

	private void addConflictFuncs(Program program) {
		Z3Logger.SubHeaderZ3(";; definition of conflict functions");
		String funcName;
		for (Table t : program.getTables()) {
			Z3Logger.LogZ3(";; table: " + t.getTableName().getName());
			for (FieldName fn : t.getFieldNames()) {
				if (!fn.isPK()) {
					funcName = "conflict_on_" + t.getTableName().getName() + "_" + fn.getName();
					objs.addFunc(funcName, ctx.mkFuncDecl(funcName, new Sort[] { objs.getSort("Txn"),
							objs.getEnum("Po"), objs.getSort("Txn"), objs.getEnum("Po") }, objs.getSort("Bool")));
				}
			}
		}
	}

	private void constrainWR(Program program) {
		Z3Logger.SubHeaderZ3(";; constraints on WR functions");
		for (Table t : program.getTables()) {
			for (FieldName fn : t.getFieldNames())
				if (!fn.isPK()) {
					String WRfuncName = "WR_" + t.getTableName().getName() + "_" + fn.getName();
					String conffuncName = "conflict_on_" + t.getTableName().getName() + "_" + fn.getName();
					BoolExpr exists_wr = (BoolExpr) ctx.mkApp(objs.getfuncs(WRfuncName), txn1, po1, txn2, po2);
					BoolExpr exists_conflict = (BoolExpr) ctx.mkApp(objs.getfuncs(conffuncName), txn1, po1, txn2, po2);
					ArithExpr int_of_time1 = (ArithExpr) ctx.mkApp(objs.getfuncs("time_to_int"),
							ctx.mkApp(objs.getfuncs("qry_time"), txn1, po1));
					ArithExpr int_of_time2 = (ArithExpr) ctx.mkApp(objs.getfuncs("time_to_int"),
							ctx.mkApp(objs.getfuncs("qry_time"), txn2, po2));
					BoolExpr time_is_gt = ctx.mkGt(int_of_time2, int_of_time1);
					Expr part_of_1 = ctx.mkApp(objs.getfuncs("qry_part"), txn1, po1);
					Expr part_of_2 = ctx.mkApp(objs.getfuncs("qry_part"), txn2, po2);
					BoolExpr parts_are_eq = ctx.mkEq(part_of_1, part_of_2);
					BoolExpr txns_are_different = ctx.mkDistinct(txn1, txn2);
					/*
					 * ArithExpr local_int = (ArithExpr) ctx.mkFreshConst("int",
					 * objs.getSort("Int")); BoolExpr local_int_is_lt_time2 = ctx.mkLt(local_int,
					 * int_of_time2); BoolExpr local_int_is_gt_time1 = ctx.mkGt(local_int,
					 * int_of_time1); Expr po_from_local_int =
					 * ctx.mkApp(objs.getfuncs("po_from_int"), local_int); Expr part_of_3 =
					 * ctx.mkApp(objs.getfuncs("qry_part"), txn3, po_from_local_int); BoolExpr
					 * new_parts_are_eq = ctx.mkEq(part_of_3, part_of_2); BoolExpr
					 * exists_new_conflict = (BoolExpr) ctx.mkApp(objs.getfuncs(conffuncName), txn3,
					 * po_from_local_int, txn2, po2); BoolExpr does_not_exists_a_newer_conflict =
					 * ctx .mkNot(ctx.mkForall( new Expr[] { txn3, local_int },
					 * ctx.mkAnd(local_int_is_gt_time1, local_int_is_lt_time2, exists_new_conflict,
					 * new_parts_are_eq), 1, null, null, null, null));
					 */
					Expr conditions = ctx.mkAnd(parts_are_eq, time_is_gt, exists_conflict, txns_are_different
					// ,does_not_exists_a_newer_conflict
					);
					Quantifier result = ctx.mkForall(new Expr[] { txn1, txn2, po1, po2 },
							ctx.mkEq(exists_wr, conditions), 1, null, null, null, null);
					Z3Logger.LogZ3(";; conditions that must hold if dep exists");
					addAssertions(result);
				}
		}
	}

	private void constrainRW(Program program) {
		Z3Logger.SubHeaderZ3(";; constraints on RW functions");
		for (Table t : program.getTables()) {
			for (FieldName fn : t.getFieldNames())
				if (!fn.isPK()) {
					String WRfuncName = "RW_" + t.getTableName().getName() + "_" + fn.getName();
					String conffuncName = "conflict_on_" + t.getTableName().getName() + "_" + fn.getName();
					BoolExpr exists_wr = (BoolExpr) ctx.mkApp(objs.getfuncs(WRfuncName), txn1, po1, txn2, po2);
					BoolExpr exists_conflict = (BoolExpr) ctx.mkApp(objs.getfuncs(conffuncName), txn2, po2, txn1, po1);
					ArithExpr int_of_time1 = (ArithExpr) ctx.mkApp(objs.getfuncs("time_to_int"),
							ctx.mkApp(objs.getfuncs("qry_time"), txn1, po1));
					ArithExpr int_of_time2 = (ArithExpr) ctx.mkApp(objs.getfuncs("time_to_int"),
							ctx.mkApp(objs.getfuncs("qry_time"), txn2, po2));
					Expr part_of_1 = ctx.mkApp(objs.getfuncs("qry_part"), txn1, po1);
					Expr part_of_2 = ctx.mkApp(objs.getfuncs("qry_part"), txn2, po2);
					BoolExpr parts_are_eq = ctx.mkEq(part_of_1, part_of_2);
					BoolExpr time_is_gt = ctx.mkGt(int_of_time2, int_of_time1);
					BoolExpr txns_are_different = ctx.mkDistinct(txn1, txn2);
					Expr conditions = ctx.mkAnd(ctx.mkNot(ctx.mkAnd(parts_are_eq, time_is_gt)), exists_conflict,
							txns_are_different);
					Quantifier result = ctx.mkForall(new Expr[] { txn1, txn2, po1, po2 },
							ctx.mkEq(exists_wr, conditions), 1, null, null, null, null);
					Z3Logger.LogZ3(";; conditions that must hold if dep exists");
					addAssertions(result);
				}
		}
	}

	private void addWRFuncs(Program program) {
		Z3Logger.SubHeaderZ3(";; definition of WR functions");
		String funcName;
		for (Table t : program.getTables()) {
			Z3Logger.LogZ3(";; table: " + t.getTableName().getName());
			for (FieldName fn : t.getFieldNames()) {
				funcName = "WR_" + t.getTableName().getName() + "_" + fn.getName();
				objs.addFunc(funcName, ctx.mkFuncDecl(funcName,
						new Sort[] { objs.getSort("Txn"), objs.getEnum("Po"), objs.getSort("Txn"), objs.getEnum("Po") },
						objs.getSort("Bool")));
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
		for (Transaction txn : program.getTransactions()) {
			Z3Logger.LogZ3(";; Queries of txn: " + txn.getName());
			for (Query q : txn.getAllQueries()) {
				Z3Logger.LogZ3(";; " + q.getId());
				for (Table t : program.getTables()) {
					for (FieldName fn : t.getFieldNames()) {
						if (q.getReadFieldNames().contains(fn)) {
							BoolExpr pre_condition1 = ctx.mkEq(ctx.mkApp(objs.getfuncs("txn_type"), txn1),
									objs.getEnumConstructor("TxnType", txn.getName()));
							BoolExpr pre_condition2 = ctx.mkEq(po1, objs.getEnumConstructor("Po", "po_" + q.getPo()));
							BoolExpr pre_condition = ctx.mkAnd(pre_condition1, pre_condition2);
							String funcName = "reads_from_" + t.getTableName().getName() + "_" + fn.getName();
							Expr query_time = ctx.mkApp(objs.getfuncs("qry_time"), txn1, po1);
							BoolExpr whc_to_expr = translateWhereClauseToZ3Expr(txn.getName(), txn1, q.getWHC(), rec1,
									query_time);
							BoolExpr body = ctx.mkEq(ctx.mkApp(objs.getfuncs(funcName), txn1, po1, rec1), whc_to_expr);
							Quantifier result = ctx.mkForall(new Expr[] { txn1, po1, rec1 },
									ctx.mkImplies(pre_condition, body), 1, null, null, null, null);
							addAssertions(result);
						} else {
							BoolExpr pre_condition1 = ctx.mkEq(ctx.mkApp(objs.getfuncs("txn_type"), txn1),
									objs.getEnumConstructor("TxnType", txn.getName()));
							BoolExpr pre_condition2 = ctx.mkEq(po1, objs.getEnumConstructor("Po", "po_" + q.getPo()));
							BoolExpr pre_condition = ctx.mkAnd(pre_condition1, pre_condition2);
							String funcName = "reads_from_" + t.getTableName().getName() + "_" + fn.getName();
							BoolExpr body = ctx.mkNot((BoolExpr) ctx.mkApp(objs.getfuncs(funcName), txn1, po1, rec1));
							Quantifier result = ctx.mkForall(new Expr[] { txn1, po1, rec1 },
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
		for (Transaction txn : program.getTransactions()) {
			Z3Logger.LogZ3(";; Queries of txn: " + txn.getName());
			for (Query q : txn.getAllQueries()) {
				Z3Logger.LogZ3(";; " + q.getId());
				for (Table t : program.getTables()) {
					for (FieldName fn : t.getFieldNames()) {
						if (q.isWrite() && q.getWrittenFieldNames().contains(fn)) {
							BoolExpr pre_condition1 = ctx.mkEq(ctx.mkApp(objs.getfuncs("txn_type"), txn1),
									objs.getEnumConstructor("TxnType", txn.getName()));
							BoolExpr pre_condition2 = ctx.mkEq(po1, objs.getEnumConstructor("Po", "po_" + q.getPo()));
							BoolExpr pre_condition = ctx.mkAnd(pre_condition1, pre_condition2);
							String funcName = "writes_to_" + t.getTableName().getName() + "_" + fn.getName();
							Expr query_time = ctx.mkApp(objs.getfuncs("qry_time"), txn1, po1);
							BoolExpr whc_to_expr = translateWhereClauseToZ3Expr(txn.getName(), txn1, q.getWHC(), rec1,
									query_time);
							BoolExpr body = ctx.mkEq(ctx.mkApp(objs.getfuncs(funcName), txn1, po1, rec1), whc_to_expr);
							Quantifier result = ctx.mkForall(new Expr[] { txn1, po1, rec1 },
									ctx.mkImplies(pre_condition, body), 1, null, null, null, null);
							addAssertions(result);
						} else {
							BoolExpr pre_condition1 = ctx.mkEq(ctx.mkApp(objs.getfuncs("txn_type"), txn1),
									objs.getEnumConstructor("TxnType", txn.getName()));
							BoolExpr pre_condition2 = ctx.mkEq(po1, objs.getEnumConstructor("Po", "po_" + q.getPo()));
							BoolExpr pre_condition = ctx.mkAnd(pre_condition1, pre_condition2);
							String funcName = "writes_to_" + t.getTableName().getName() + "_" + fn.getName();
							BoolExpr body = ctx.mkNot((BoolExpr) ctx.mkApp(objs.getfuncs(funcName), txn1, po1, rec1));
							Quantifier result = ctx.mkForall(new Expr[] { txn1, po1, rec1 },
									ctx.mkImplies(pre_condition, body), 1, null, null, null, null);
							addAssertions(result);
						}
					}
				}
			}
		}

	}

	private void addExecutionFuncs() {
		Z3Logger.LogZ3(";; query related functions");
		objs.addFunc("qry_time", ctx.mkFuncDecl("qry_time", new Sort[] { objs.getSort("Txn"), objs.getEnum("Po") },
				objs.getEnum("Time")));
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
		time1 = ctx.mkFreshConst("time", objs.getEnum("Time"));
		time2 = ctx.mkFreshConst("time", objs.getEnum("Time"));
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

	private void addQryTypeToIsExecuted(Transaction txn, Query q) {
		Expr expected_txn_type = objs.getEnumConstructor("TxnType", txn.getName());
		BoolExpr lhs1 = ctx.mkEq(po1, objs.getEnumConstructor("Po", "po_" + q.getPo()));
		BoolExpr lhs2 = ctx.mkEq(ctx.mkApp(objs.getfuncs("txn_type"), txn1), expected_txn_type);
		BoolExpr rhs = ctx.mkEq(ctx.mkApp(objs.getfuncs("qry_is_executed"), txn1, po1),
				(BoolExpr) translateExpressionsToZ3Expr(txn.getName(), txn1, q.getPathCondition()));
		BoolExpr body = ctx.mkImplies(ctx.mkAnd(lhs1, lhs2), rhs);
		BoolExpr result = ctx.mkForall(new Expr[] { txn1, po1 }, body, 1, null, null, null, null);
		addAssertion("qry_type_to_is_executed_" + q.getId(), result);
	}

	private void addRecordsValConstraints(Program program) {
		for (Transaction txn : program.getTransactions()) {
			for (Query q : txn.getAllQueries())
				if (q.isWrite()) {
					// TODO: Must implement relationship between updates and records
				}
		}
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
							ctx.mkFuncDecl(timeFuncName, new Sort[] { objs.getSort("Txn") }, objs.getEnum("Time")));
					// properties of time func
					Expr generated_time = ctx.mkApp(objs.getfuncs(timeFuncName), txn1);
					Expr time_of_query = ctx.mkApp(objs.getfuncs("qry_time"), txn1, po1);
					Expr expected_po = objs.getEnumConstructor("Po", "po_" + q.getPo());
					Expr expected_txn_type = objs.getEnumConstructor("TxnType", txn.getName());
					BoolExpr lhs1 = ctx.mkEq(po1, expected_po);
					BoolExpr lhs2 = ctx.mkEq(ctx.mkApp(objs.getfuncs("txn_type"), txn1), expected_txn_type);
					BoolExpr rhs = ctx.mkEq(generated_time, time_of_query);
					BoolExpr body = ctx.mkImplies(ctx.mkAnd(lhs1, lhs2), rhs);
					Quantifier ass1 = ctx.mkForall(new Expr[] { txn1, po1 }, body, 1, null, null, null, null);
					addAssertion("assertion that the time matches the select time for " + current_var.getName(), ass1);
					// properties of main func
					// prop#1: a record is in var only if satisfies the whc
					Expr gen_time = ctx.mkApp(objs.getfuncs(timeFuncName), txn1);
					Expr the_record = ctx.mkApp(objs.getfuncs(funcName), txn1, order1);
					BoolExpr where_body = translateWhereClauseToZ3Expr(txn.getName(), txn1, q.getWHC(), the_record,
							gen_time);
					BoolExpr is_alive_body = (BoolExpr) ctx.mkApp(objs.getfuncs("is_alive"), the_record, gen_time);
					Quantifier where_assertion = ctx.mkForall(new Expr[] { txn1, order1 },
							ctx.mkAnd(where_body, is_alive_body), 1, null, null, null, null);
					addAssertion("any record in " + current_var.getName()
							+ " must be alive and satisfy the associated where clause, at the time of generation",
							where_assertion);
					BoolExpr where_body2 = translateWhereClauseToZ3Expr(txn.getName(), txn1, q.getWHC(), rec1,
							gen_time);
					BoolExpr body2 = ctx.mkEq(the_record, rec1);
					BoolExpr exists_clause = ctx.mkExists(new Expr[] { order1 }, body2, 1, null, null, null, null);
					Quantifier where_assertion2 = ctx.mkForall(new Expr[] { txn1, rec1 },
							ctx.mkImplies(where_body2, exists_clause), 1, null, null, null, null);
					addAssertion(
							"if an *alive* record satisfies the where clause, it must be in " + current_var.getName(),
							where_assertion2);
				}
			}
		}
	}

	public void addArgsFuncs(Program program) {
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
						eqs[i] = ctx.mkEq(ret_val2, ctx.MkString("arg-val#" + i));
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
		Z3Logger.HeaderZ3(program.getName() + " (Schema sorts, types and functions)");
		Z3Logger.LogZ3("\n;; definition of is_alive projection function for all tables");
		String funcName = "is_alive";
		objs.addFunc(funcName, ctx.mkFuncDecl(funcName, new Sort[] { objs.getSort("Rec"), objs.getEnum("Time") },
				objs.getSort("Bool")));
		// definition of projection functions
		for (Table t : program.getTables()) {
			Z3Logger.SubHeaderZ3(t.getTableName().getName().toUpperCase());
			for (FieldName fn : t.getFieldNames()) {
				funcName = "proj_" + t.getTableName().getName() + "_" + fn.getName();
				Z3Logger.LogZ3(";; definition of projection function " + funcName);
				switch (fn.getType()) {
				case NUM:
					// define function
					objs.addFunc(funcName, ctx.mkFuncDecl(funcName,
							new Sort[] { objs.getSort("Rec"), objs.getEnum("Time") }, objs.getSort("Fld")));
					break;
				case TEXT:
					// define function
					objs.addFunc(funcName, ctx.mkFuncDecl(funcName,
							new Sort[] { objs.getSort("Rec"), objs.getEnum("Time") }, objs.getSort("String")));
					// define bounds
					Expr ret_val2 = ctx.mkApp(objs.getfuncs(funcName), rec1, time1);
					BoolExpr[] eqs = new BoolExpr[Constants._MAX_FIELD_STRING];
					for (int i = 0; i < Constants._MAX_FIELD_STRING; i++)
						eqs[i] = ctx.mkEq(ret_val2, ctx.MkString("string-val#" + i));
					BoolExpr body2 = ctx.mkOr(eqs);
					Quantifier result2 = ctx.mkForall(new Expr[] { rec1, time1 }, body2, 1, null, null, null, null);
					addAssertions(result2);
					break;
				case BOOL:
					objs.addFunc(funcName, ctx.mkFuncDecl(funcName,
							new Sort[] { objs.getSort("Rec"), objs.getEnum("Time") }, objs.getSort("Bool")));
					break;
				default:
					assert (false) : "unhandled field type";
				}

			}
		}
	}

	private void checkSAT(Program program) {
		long begin = System.currentTimeMillis();
		if (slv.check() == Status.SATISFIABLE) {
			model = slv.getModel();
			long end = System.currentTimeMillis();
			System.out.println(
					"\n\n==================\n" + "SATISFIABLE (" + (end - begin) + "ms)" + "\n==================\n\n");
			this.model_handler = new Model_Handler(model, ctx, objs, program);
			model_handler.printRawModelInToFile();
			model_handler.printUniverse();
		} else {
			System.out.println("\n\n================\n" + slv.check() + "\n================\n\n");
			for (Expr e : slv.getUnsatCore())
				System.out.println(e);
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
			Expr time) {
		ArrayList<WHC_Constraint> constraints = input_whc.getConstraints();
		BoolExpr[] result = new BoolExpr[constraints.size()];
		for (int i = 0; i < constraints.size(); i++)
			result[i] = translateWhereConstraintToZ3Expr(txnName, transaction, constraints.get(i), record, time);
		return ctx.mkAnd(result);
	}

	private Expr translateExpressionsToZ3Expr(String txnName, Expr transaction, Expression input_expr) {
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
			return ctx.mkInt(cn_exp.val);
		case "E_Const_Text":
			E_Const_Text ct_exp = (E_Const_Text) input_expr;
			return ctx.MkString(ct_exp.val);

		case "E_UnOp":
			E_UnOp uo_exp = (E_UnOp) input_expr;
			switch (uo_exp.un_op) {
			case NOT:
				return ctx.mkNot((BoolExpr) translateExpressionsToZ3Expr(txnName, transaction, uo_exp.exp));
			default:
				break;
			}
		case "E_Proj":
			E_Proj p_exp = (E_Proj) input_expr;
			Expr order = ctx.mkApp(objs.getfuncs("ro_from_int"),
					translateExpressionsToZ3Expr(txnName, transaction, p_exp.e));

			Expr var_time = ctx.mkApp(objs.getfuncs(txnName + "_var_" + p_exp.v.getName() + "_gen_time"), transaction);
			Expr rec_expr = ctx.mkApp(objs.getfuncs(txnName + "_var_" + p_exp.v.getName()), transaction, order);
			return ctx.mkApp(objs.getfuncs("proj_" + p_exp.v.getTableName() + "_" + p_exp.f.getName()), rec_expr,
					var_time);
		case "E_Size":
			// TODO: E_Size encoding must be implemented
			assert (false) : "TODO: E_Size encoding not implemented yet...";
			break;

		case "E_BinUp":
			E_BinUp bu_exp = (E_BinUp) input_expr;
			switch (bu_exp.op) {
			case GT:
				return ctx.mkBVUGT((BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1),
						(BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2));
			case LT:
				return ctx.mkBVULT((BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1),
						(BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2));
			case EQ:
				return ctx.mkEq(translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1),
						translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2));
			case MULT:
				return ctx.mkBVMul((BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1),
						(BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2));
			case PLUS:
				return ctx.mkBVAdd((BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1),
						(BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2));
			case MINUS:
				return ctx.mkBVSub((BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1),
						(BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2));
			case DIV:
				return ctx.mkBVUDiv((BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1),
						(BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2));
			case AND:
				return ctx.mkAnd((BoolExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1),
						(BoolExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2));
			case OR:
				return ctx.mkOr((BoolExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1),
						(BoolExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2));
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
			Expr record, Expr time) {
		BoolExpr result = null;
		Expr rhs = translateExpressionsToZ3Expr(txnName, transaction, input_constraint.getExpression());
		String tableName = input_constraint.getTableName().getName();
		String fieldName = input_constraint.getFieldName().getName();
		FuncDecl projFunc = objs.getfuncs("proj_" + tableName + "_" + fieldName);
		Expr lhs = ctx.mkApp(projFunc, record, time);
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
}
