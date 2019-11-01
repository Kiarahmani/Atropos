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
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Model;
import com.microsoft.z3.Quantifier;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Sort;
import com.microsoft.z3.Status;

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
	Program_Relations program_relations;

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

		Z3Logger.HeaderZ3(program.getName() + " (Schema sorts, types and functions)");
		program_relations = new Program_Relations(program, ctx, objs);
		program_relations.addRecFldDataTypes();
		program_relations.addTxnOpDataTypes();
		program_relations.addTypingFuncs();
		program_relations.addExecutionFuncs();
		program_relations.addProgramOrderFunc();
		program_relations.addParentFunc();

		addProjFuncsAndBounds(program);
		addAssertion("uniqueness_of_time", program_relations.mk_uniqueness_of_time());
		addAssertion("bound_on_qry_time", program_relations.mk_bound_on_qry_time());
		addAssertion("bound_on_qry_part", program_relations.mk_bound_on_qry_part());
		addAssertion("bound_on_qry_po", program_relations.mk_bound_on_po_part());
		addAssertion("bound_on_txn_instances", program_relations.mk_bound_on_txn_instances(current_cycle_length));
		for (Transaction txn : program.getTransactions()) {
			int po = 1;
			for (String qry_name : txn.getAllStmtTypes())
				addAssertion("qry_type_to_po_" + qry_name, program_relations.mk_qry_type_to_po(qry_name, po++));
		}
		for (Transaction txn : program.getTransactions())
			for (String qry_name : txn.getAllStmtTypes())
				addAssertion("qry_types_to_txn_types_" + txn.getName() + "_" + qry_name,
						program_relations.mk_qry_types_to_txn_types(txn.getName(), qry_name));
		addAssertion("qry_time_respects_po", program_relations.mk_qry_time_respects_po());
		addAssertion("eq_types_and_eq_par_then_eq", program_relations.mk_eq_types_and_eq_par_then_eq());
		for (Table t : program.getTables()) {
			String table_name = t.getTableName().getName();
			addAssertion("pk_must_not_change_" + table_name, program_relations.mk_pk_must_not_change(t));
			addAssertion("eq_pk_eq_rec_" + table_name, program_relations.mk_eq_pk_eq_rec(t));
		}
		Z3Logger.HeaderZ3(program.getName() + " (Transactrions Sorts, types and functions)");
		addArgsFuncs(program);
		addVariablesFuncs(program);
		addRecordsValConstraints(program);
		for (Transaction txn : program.getTransactions())
			for (Query q : txn.getAllQueries())
				addQryTypeToIsExecuted(txn, q);

		Expr rec_expr = ctx.mkFreshConst("rec", objs.getSort("Rec"));
		Expr txn_expr = ctx.mkFreshConst("txn", objs.getSort("Txn"));
		Expr order_expr = ctx.mkFreshConst("order", objs.getSort("Int"));
		addAssertion("ass", ctx.mkExists(new Expr[] { txn_expr, order_expr, rec_expr },
				ctx.mkApp(objs.getfuncs("dec_var_dec_v0"), txn_expr, order_expr, rec_expr), 1, null, null, null, null));
		addAssertion("ass", ctx.mkExists(new Expr[] { txn_expr, order_expr, rec_expr },
				ctx.mkApp(objs.getfuncs("inc_var_inc_v0"), txn_expr, order_expr, rec_expr), 1, null, null, null, null));

		/* --- */
		checkSAT(program);
	}

	private void addQryTypeToIsExecuted(Transaction txn, Query q) {
		Expr txn_expr = ctx.mkFreshConst("txn", objs.getSort("Txn"));
		Expr qry_expr = ctx.mkFreshConst("qry", objs.getSort("Qry"));
		BoolExpr lhs1 = ctx.mkEq(ctx.mkApp(objs.getfuncs("parent"), qry_expr), txn_expr);
		Expr expected_type = ctx.mkApp(objs.getConstructor("QryType", txn.getName() + "-" + q.getId()));
		Expr type_get_qry = ctx.mkApp(objs.getfuncs("qry_type"), qry_expr);
		BoolExpr lhs2 = ctx.mkEq(type_get_qry, expected_type);
		BoolExpr lhs = ctx.mkAnd(lhs1, lhs2);
		BoolExpr rhs = ctx.mkEq(ctx.mkApp(objs.getfuncs("qry_is_executed"), qry_expr),
				(BoolExpr) translateExpressionsToZ3Expr(txn.getName(), txn_expr, q.getPathCondition()));
		BoolExpr body = ctx.mkImplies(lhs, rhs);
		BoolExpr result = ctx.mkForall(new Expr[] { txn_expr, qry_expr }, body, 1, null, null, null, null);
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
		Expr txn_expr = ctx.mkFreshConst("txn", objs.getSort("Txn"));
		Expr order_expr = ctx.mkFreshConst("order", objs.getSort("Int"));
		Expr qry_expr = ctx.mkFreshConst("qry", objs.getSort("Qry"));
		Expr rec_expr = ctx.mkFreshConst("rec", objs.getSort("Rec"));
		for (Transaction txn : program.getTransactions()) {
			for (Query q : txn.getAllQueries())
				if (!q.isWrite()) {
					Variable current_var = ((Select_Query) q).getVariable();
					String funcName = txn.getName() + "_var_" + current_var.getName();
					Z3Logger.SubHeaderZ3("Functions and properties for " + current_var.getName());

					// def main function
					Z3Logger.LogZ3("\n;; definition of var function " + funcName);
					objs.addFunc(funcName,
							ctx.mkFuncDecl(funcName,
									new Sort[] { objs.getSort("Txn"), objs.getSort("Int"), objs.getSort("Rec") },
									objs.getSort("Bool")));

					// def helper function, which returns the record that main functions is true for
					objs.addFunc(funcName + "_get_rec", ctx.mkFuncDecl(funcName + "_get_rec",
							new Sort[] { objs.getSort("Txn"), objs.getSort("Int") }, objs.getSort("Rec")));

					// def time func
					String timeFuncName = txn.getName() + "_var_" + current_var.getName() + "_gen_time";
					objs.addFunc(timeFuncName,
							ctx.mkFuncDecl(timeFuncName, new Sort[] { objs.getSort("Txn") }, objs.getSort("Int")));

					// properties of the helper function
					BoolExpr lhs0 = (BoolExpr) ctx.mkApp(objs.getfuncs(funcName), txn_expr, order_expr, rec_expr);
					Quantifier ass0 = ctx.mkForall(new Expr[] { txn_expr, order_expr, rec_expr },
							ctx.mkImplies(lhs0, ctx.mkEq(
									ctx.mkApp(objs.getfuncs(funcName + "_get_rec"), txn_expr, order_expr), rec_expr)),
							1, null, null, null, null);
					addAssertion("Any record that satisfies " + funcName + " must also be returned by " + funcName
							+ "_get_rec", ass0);

					// properties of time func
					Expr generated_time = ctx.mkApp(objs.getfuncs(timeFuncName), txn_expr);
					Expr time_of_query = ctx.mkApp(objs.getfuncs("qry_time"), qry_expr);
					BoolExpr lhs1 = ctx.mkEq(ctx.mkApp(objs.getfuncs("parent"), qry_expr), txn_expr);
					Expr type_get_qry = ctx.mkApp(objs.getfuncs("qry_type"), qry_expr);
					Expr expected_type = ctx.mkApp(objs.getConstructor("QryType", txn.getName() + "-" + q.getId()));
					BoolExpr lhs2 = ctx.mkEq(type_get_qry, expected_type);
					BoolExpr lhs = ctx.mkAnd(lhs1, lhs2);
					BoolExpr rhs = ctx.mkEq(generated_time, time_of_query);
					BoolExpr body = ctx.mkImplies(lhs, rhs);
					Quantifier ass1 = ctx.mkForall(new Expr[] { txn_expr, qry_expr }, body, 1, null, null, null, null);
					addAssertion("assertion that the time matches the select time for " + current_var.getName(), ass1);

					// properties of main func
					// prop#1: a record is in var only if satisfies the whc
					Expr rec_time = ctx.mkApp(objs.getfuncs(timeFuncName), txn_expr);
					BoolExpr is_the_record = (BoolExpr) ctx.mkApp(objs.getfuncs(funcName), txn_expr, order_expr,
							rec_expr);
					BoolExpr where_body = translateWhereClauseToZ3Expr(txn.getName(), txn_expr, q.getWHC(), rec_expr,
							rec_time);
					BoolExpr is_alive_body = (BoolExpr) ctx.mkApp(objs.getfuncs("is_alive"), rec_expr, rec_time);
					Quantifier where_assertion = ctx.mkForall(new Expr[] { txn_expr, order_expr, rec_expr },
							ctx.mkImplies(is_the_record, ctx.mkAnd(where_body, is_alive_body)), 1, null, null, null,
							null);
					addAssertion("any *alive* record in " + current_var.getName()
							+ " must satisfy the associated where clause", where_assertion);
					// prop#2: if record satisfies whc then it must be in var
					BoolExpr body1 = (BoolExpr) ctx.mkApp(objs.getfuncs(funcName), txn_expr, order_expr, rec_expr);
					BoolExpr body2 = ctx.mkLt((ArithExpr) order_expr, ctx.mkInt(Constants._MAX_FIELD_INT));
					BoolExpr body3 = ctx.mkGt((ArithExpr) order_expr, ctx.mkInt(0));
					Quantifier is_the_record2 = ctx.mkExists(new Expr[] { order_expr }, ctx.mkAnd(body1, body2, body3),
							1, null, null, null, null);
					Quantifier where_assertion2 = ctx.mkForall(new Expr[] { txn_expr, rec_expr },
							ctx.mkImplies(ctx.mkAnd(where_body, is_alive_body), is_the_record2), 1, null, null, null,
							null);
					addAssertion(
							"if an *alive* record satisfies the where clause, it must be in " + current_var.getName(),
							where_assertion2);
				}
		}
	}

	public void addArgsFuncs(Program program) {
		for (Transaction txn : program.getTransactions()) {
			Z3Logger.SubHeaderZ3("Transaction: " + txn.getName());
			Expr txn_expr = ctx.mkFreshConst("txn", objs.getSort("Txn"));
			for (E_Arg arg : txn.getArgs()) {
				String funcName = txn.getName() + "_arg_" + arg.getName();
				Z3Logger.LogZ3("\n;; definition of arg function " + funcName);

				switch (arg.getType()) {
				case NUM:
					objs.addFunc(funcName, ctx.mkFuncDecl(funcName, objs.getSort("Txn"), objs.getSort("Int")));
					// create bound for the function
					ArithExpr ret_val = (ArithExpr) ctx.mkApp(objs.getfuncs(funcName), txn_expr);
					BoolExpr bodyGT = ctx.mkGt(ret_val, ctx.mkInt(0));
					BoolExpr bodyLT = ctx.mkLt(ret_val, ctx.mkInt(Constants._MAX_ARG_INT));
					BoolExpr body = ctx.mkAnd(bodyGT, bodyLT);
					Quantifier result = ctx.mkForall(new Expr[] { txn_expr }, body, 1, null, null, null, null);
					addAssertion("bound_on_" + funcName, result);
					break;
				case TEXT:
					objs.addFunc(funcName, ctx.mkFuncDecl(funcName, objs.getSort("Txn"), objs.getSort("String")));
					// add bounds
					Expr ret_val2 = ctx.mkApp(objs.getfuncs(funcName), txn_expr);
					BoolExpr[] eqs = new BoolExpr[Constants._MAX_ARG_STRING];
					for (int i = 0; i < Constants._MAX_ARG_STRING; i++) {
						eqs[i] = ctx.mkEq(ret_val2, ctx.MkString("arg-val#" + i));
					}

					BoolExpr body2 = ctx.mkOr(eqs);
					Quantifier result2 = ctx.mkForall(new Expr[] { txn_expr }, body2, 1, null, null, null, null);
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
		Expr rec = ctx.mkFreshConst("rec", objs.getSort("Rec"));
		Expr time = ctx.mkFreshConst("time", objs.getSort("Int"));
		// is_alive proj function for all tables and records
		Z3Logger.LogZ3("\n;; definition of is_alive projection function for all tables");
		String funcName = "is_alive";
		objs.addFunc(funcName, ctx.mkFuncDecl(funcName, new Sort[] { objs.getSort("Rec"), objs.getSort("Int") },
				objs.getSort("Bool")));
		for (Table t : program.getTables()) {
			for (FieldName fn : t.getFieldNames()) {
				funcName = "proj_" + t.getTableName().getName() + "_" + fn.getName();
				Z3Logger.LogZ3("\n;; definition of projection function " + funcName);
				switch (fn.getType()) {
				case NUM:
					// define function
					objs.addFunc(funcName, ctx.mkFuncDecl(funcName,
							new Sort[] { objs.getSort("Rec"), objs.getSort("Int") }, objs.getSort("Int")));
					ArithExpr ret_val = (ArithExpr) ctx.mkApp(objs.getfuncs(funcName), rec, time);
					// create bound for the function
					BoolExpr bodyGT = ctx.mkGt(ret_val, ctx.mkInt(0));
					BoolExpr bodyLT = ctx.mkLt(ret_val, ctx.mkInt(Constants._MAX_FIELD_INT));
					BoolExpr body = ctx.mkAnd(bodyGT, bodyLT);
					Quantifier result = ctx.mkForall(new Expr[] { rec, time }, body, 1, null, null, null, null);
					addAssertion("bound_on_" + funcName, result);
					break;
				case TEXT:
					objs.addFunc(funcName, ctx.mkFuncDecl(funcName,
							new Sort[] { objs.getSort("Rec"), objs.getSort("Int") }, objs.getSort("String")));
					Expr ret_val2 = ctx.mkApp(objs.getfuncs(funcName), rec, time);
					BoolExpr[] eqs = new BoolExpr[Constants._MAX_FIELD_STRING];
					for (int i = 0; i < Constants._MAX_FIELD_STRING; i++) {
						eqs[i] = ctx.mkEq(ret_val2, ctx.MkString("field-val#" + i));
					}

					BoolExpr body2 = ctx.mkOr(eqs);
					Quantifier result2 = ctx.mkForall(new Expr[] { rec, time }, body2, 1, null, null, null, null);
					addAssertion("bound_on_" + funcName, result2);
					break;
				case BOOL:
					objs.addFunc(funcName, ctx.mkFuncDecl(funcName,
							new Sort[] { objs.getSort("Rec"), objs.getSort("Int") }, objs.getSort("Bool")));
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
			model_handler = new Model_Handler(model, ctx, objs, program);
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

	// Generic sorts and types for any encoding
	private void addInitialStaticSorts() {
		Z3Logger.HeaderZ3("Generic Sorts and Types");
		objs.addSort("Rec", ctx.mkUninterpretedSort("Rec"));
		objs.addSort("Txn", ctx.mkUninterpretedSort("Txn"));
		objs.addSort("Qry", ctx.mkUninterpretedSort("Qry"));
		objs.addSort("Bool", ctx.mkBoolSort());
		objs.addSort("Int", ctx.mkIntSort());
		objs.addSort("String", ctx.mkStringSort());
	}

	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * TRANSLATION FUNCTIONS
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
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
			Expr order = translateExpressionsToZ3Expr(txnName, transaction, p_exp.e);
			Expr var_time = ctx.mkApp(objs.getfuncs(txnName + "_var_" + p_exp.v.getName() + "_gen_time"), transaction);
			Expr rec_expr = ctx.mkApp(objs.getfuncs(txnName + "_var_" + p_exp.v.getName() + "_get_rec"), transaction,
					order);
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
				return ctx.mkGt((ArithExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1),
						(ArithExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2));
			case LT:
				return ctx.mkLt((ArithExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1),
						(ArithExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2));
			case EQ:
				return ctx.mkEq(translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1),
						translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2));
			case MULT:
				return ctx.mkMul((ArithExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1),
						(ArithExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2));
			case PLUS:
				return ctx.mkAdd((ArithExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1),
						(ArithExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2));
			case MINUS:
				return ctx.mkSub((ArithExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1),
						(ArithExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2));
			case DIV:
				return ctx.mkDiv((ArithExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1),
						(ArithExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper2));
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
