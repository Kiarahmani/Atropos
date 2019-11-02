package kiarahmani.atropos.encoding_engine.Z3;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BitVecExpr;
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
	Expression_Maker em;
	Expr rec1, rec2, time1, time2, txn1, txn2, po1, po2, arg1, arg2, fld1, fld2, part1, part2, order1, order2;

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

		em = new Expression_Maker(program, ctx, objs);
		Z3Logger.HeaderZ3(program.getName() + " (Schema sorts, types and functions)");
		initializeLocalVariables();
		em.addExecTypes();
		em.addRecFldDataTypes();
		em.addTxnOpDataTypes();
		em.addTypingFuncs();
		em.mk_time_to_int();
		for (int i = 0; i < Constants._MAX_EXECECUTION_LENGTH; i++) {
			addAssertion("define values of time_to_int function based on the timeVal type", em.bound_time_to_int(i));
			objs.addConst("cons_time_" + i, ctx.mkConst("cons_time_" + i, objs.getSort("TimeSort")));
			addAssertion("constants to their type", const_to_type("Time", i));
		}
		em.mk_po_to_int();
		for (int i = 0; i < Constants._MAX_EXECUTION_PO; i++) {
			addAssertion("define values of po_to_int function based on the poVal type", em.bound_po_to_int(i));
			objs.addConst("cons_po_" + i,   ctx.mkConst("cons_po_" + i, objs.getSort("PoSort")));
			addAssertion("constants to their type", const_to_type("Po", i));
		}
		for (int i = 0; i < Math.pow(2, Constants._MAX_FIELD_INT); i++){
			addAssertion("define values of ro_to_int function based on the roVal type", em.bound_ro_to_int(i));
			objs.addConst("cons_ro_" + i, ctx.mkConst("cons_ro_" + i, objs.getSort("RoSort")));
			addAssertion("constants to their type", const_to_type("Ro", i));
		}
		em.addExecutionFuncs();
		addProjFuncsAndBounds(program);
		Z3Logger.SubHeaderZ3("Properties of query functions");
		addAssertion("uniqueness of time", em.mk_uniqueness_of_time());
		addAssertion("bound_on_txn_instances", em.mk_bound_on_txn_instances(current_cycle_length));

		for (Transaction txn : program.getTransactions()) {
			int po = 0;
			for (String qry_name : txn.getAllStmtTypes())
				addAssertion("qry_type_to_po_" + qry_name, em.mk_po_val_to_qry_type(txn.getName(), qry_name, po++));
		}

		addAssertion("qry_time_respects_po", em.mk_qry_time_respects_po());
		for (Table t : program.getTables()) {
			String table_name = t.getTableName().getName();
			addAssertion("pk_must_not_change_" + table_name, em.mk_pk_must_not_change(t));
			addAssertion("eq_pk_eq_rec_" + table_name, em.mk_eq_pk_eq_rec(t));
		}
		Z3Logger.HeaderZ3(program.getName() + " (Transactrions Sorts, types and functions)");
		addArgsFuncs(program);
		addVariablesFuncs(program);
		addRecordsValConstraints(program);
		for (Transaction txn : program.getTransactions()) {
			int i = 0;
			for (Query q : txn.getAllQueries())
				addQryTypeToIsExecuted(txn, q, i++);
		}

		/* --- */
		checkSAT(program);
	}

	// Generic sorts
	private void addInitialStaticSorts() {
		Z3Logger.HeaderZ3("Basic Sorts and Types");
		objs.addSort("Rec", ctx.mkUninterpretedSort("Rec"));
		objs.addSort("Txn", ctx.mkUninterpretedSort("Txn"));
		objs.addSort("Bool", ctx.mkBoolSort());
		objs.addSort("Int", ctx.mkIntSort());
		objs.addSort("String", ctx.mkStringSort());
		objs.addSort("PoSort", ctx.mkUninterpretedSort("PoSort"));
		objs.addSort("RoSort", ctx.mkUninterpretedSort("RoSort"));
		objs.addSort("TimeSort", ctx.mkUninterpretedSort("TimeSort"));
		objs.addSort("PartSort", ctx.mkUninterpretedSort("PartSort"));
		objs.addSort("ArgSort", ctx.mkBitVecSort(Constants._MAX_ARG_INT));
		objs.addSort("FldSort", ctx.mkBitVecSort(Constants._MAX_FIELD_INT));
	}

	private void initializeLocalVariables() {
		txn1 = ctx.mkFreshConst("txn", objs.getSort("Txn"));
		txn2 = ctx.mkFreshConst("txn", objs.getSort("Txn"));
		rec1 = ctx.mkFreshConst("rec", objs.getSort("Rec"));
		rec2 = ctx.mkFreshConst("rec", objs.getSort("Rec"));
		time1 = ctx.mkFreshConst("time", objs.getSort("TimeSort"));
		time2 = ctx.mkFreshConst("time", objs.getSort("TimeSort"));
		po1 = ctx.mkFreshConst("po", objs.getSort("PoSort"));
		po2 = ctx.mkFreshConst("po", objs.getSort("PoSort"));
		arg1 = ctx.mkFreshConst("arg", objs.getSort("ArgSort"));
		arg2 = ctx.mkFreshConst("arg", objs.getSort("ArgSort"));
		fld1 = ctx.mkFreshConst("fld", objs.getSort("FldSort"));
		fld2 = ctx.mkFreshConst("fld", objs.getSort("FldSort"));
		part1 = ctx.mkFreshConst("part", objs.getSort("PartSort"));
		part2 = ctx.mkFreshConst("part", objs.getSort("PartSort"));
		order1 = ctx.mkFreshConst("order", objs.getSort("RoSort"));
		order2 = ctx.mkFreshConst("order", objs.getSort("RoSort"));
	}

	private BoolExpr const_to_type(String tp, int i) {
		return ctx.mkEq(
				ctx.mkApp(objs.getfuncs(tp.toLowerCase() + "_getVal"),
						objs.getConst("cons_" + tp.toLowerCase() + "_" + i)),
				ctx.mkApp(objs.getConstructor(tp + "Val", tp.toLowerCase() + i)));
	}

	private void addQryTypeToIsExecuted(Transaction txn, Query q, int i) {
		BoolExpr pre_condition = ctx.mkEq(ctx.mkApp(objs.getfuncs("po_getVal"), po1),
				ctx.mkApp(objs.getConstructor("PoVal", "po" + i)));
		BoolExpr body = ctx.mkEq(ctx.mkApp(objs.getfuncs("qry_is_executed"), txn1, po1),
				(BoolExpr) translateExpressionsToZ3Expr(txn.getName(), txn1, q.getPathCondition()));
		BoolExpr result = ctx.mkForall(new Expr[] { txn1, po1 }, ctx.mkImplies(pre_condition, body), 1, null, null,
				null, null);
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
		// Expr txn_expr = ctx.mkFreshConst("txn", objs.getSort("Txn"));
		// Expr order_expr = ctx.mkFreshConst("order", objs.getSort("Int"));
		// Expr qry_expr = ctx.mkFreshConst("qry", objs.getSort("Qry"));
		// Expr rec_expr = ctx.mkFreshConst("rec", objs.getSort("Rec"));
		for (Transaction txn : program.getTransactions()) {
			int i = 0;
			for (Query q : txn.getAllQueries())
				if (!q.isWrite()) {
					Variable current_var = ((Select_Query) q).getVariable();
					String funcName = txn.getName() + "_var_" + current_var.getName();
					Z3Logger.SubHeaderZ3("Functions and properties for " + current_var.getName());

					// def main function
					Z3Logger.LogZ3("\n;; definition of var function " + funcName);
//					objs.addFunc(funcName,
//							ctx.mkFuncDecl(funcName,
//									new Sort[] { objs.getSort("Txn"), objs.getSort("RoSort"), objs.getSort("Rec") },
//									objs.getSort("Bool")));

					// def helper function, which returns the record that main functions is true for
					objs.addFunc(funcName + "_get_rec", ctx.mkFuncDecl(funcName + "_get_rec",
							new Sort[] { objs.getSort("Txn"), objs.getSort("RoSort") }, objs.getSort("Rec")));

					// def time func
					String timeFuncName = txn.getName() + "_var_" + current_var.getName() + "_gen_time";
					objs.addFunc(timeFuncName,
							ctx.mkFuncDecl(timeFuncName, new Sort[] { objs.getSort("Txn") }, objs.getSort("TimeSort")));

					// properties of the helper function
					// BoolExpr lhs0 = (BoolExpr) ctx.mkApp(objs.getfuncs(funcName), txn_expr,
					// order_expr, rec_expr);
					// Quantifier ass0 = ctx.mkForall(new Expr[] { txn_expr, order_expr, rec_expr },
					// ctx.mkImplies(lhs0, ctx.mkEq(
					// ctx.mkApp(objs.getfuncs(funcName + "_get_rec"), txn_expr, order_expr),
					// rec_expr)),
					// 1, null, null, null, null);
					// addAssertion("Any record that satisfies " + funcName + " must also be
					// returned by " + funcName
					// + "_get_rec", ass0);

					// properties of time func
					Expr generated_time = ctx.mkApp(objs.getfuncs(timeFuncName), txn1);
					Expr time_of_query = ctx.mkApp(objs.getfuncs("qry_time"), txn1, po1);
					BoolExpr rhs = ctx.mkEq(generated_time, time_of_query);

					Expr expected_type = ctx.mkApp(objs.getConstructor("PoVal", "po" + i));
					BoolExpr lhs = ctx.mkEq(ctx.mkApp(objs.getfuncs("po_getVal"), po1), expected_type);

					BoolExpr body = ctx.mkImplies(lhs, rhs);
					Quantifier ass1 = ctx.mkForall(new Expr[] { txn1, po1 }, body, 1, null, null, null, null);
					addAssertion("assertion that the time matches the select time for " + current_var.getName(), ass1);

					// properties of main func
					// prop#1: a record is in var only if satisfies the whc
					Expr rec_time = ctx.mkApp(objs.getfuncs(timeFuncName), txn1);
					Expr the_record = ctx.mkApp(objs.getfuncs(funcName + "_get_rec"), txn1, order1);
					BoolExpr where_body = translateWhereClauseToZ3Expr(txn.getName(), txn1, q.getWHC(), the_record,
							rec_time);
					BoolExpr is_alive_body = (BoolExpr) ctx.mkApp(objs.getfuncs("is_alive"), the_record, rec_time);
					Quantifier where_assertion = ctx.mkForall(new Expr[] { txn1, order1 },
							ctx.mkAnd(where_body, is_alive_body), 1, null, null, null, null);
					addAssertion("any *alive* record in " + current_var.getName()
							+ " must satisfy the associated where clause", where_assertion);

					// prop#2: if record satisfies whc then it must be in var

					BoolExpr where_body2 = translateWhereClauseToZ3Expr(txn.getName(), txn1, q.getWHC(), rec1,
							rec_time);
					BoolExpr body2 = ctx.mkEq(the_record, rec1);
					BoolExpr exists_clause = ctx.mkExists(new Expr[] { order1 }, body2, 1, null, null, null, null);
					Quantifier where_assertion2 = ctx.mkForall(new Expr[] { txn1, rec1 },
							ctx.mkImplies(where_body2, exists_clause), 1, null, null, null, null);
					addAssertion(
							"if an *alive* record satisfies the where clause, it must be in " + current_var.getName(),
							where_assertion2);

					i++;
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
					objs.addFunc(funcName, ctx.mkFuncDecl(funcName, objs.getSort("Txn"), objs.getSort("ArgSort")));
					// create bound for the function
					// ArithExpr ret_val = (ArithExpr) ctx.mkApp(objs.getfuncs(funcName), txn_expr);
					// BoolExpr bodyGT = ctx.mkGt(ret_val, ctx.mkInt(0));
					// BoolExpr bodyLT = ctx.mkLt(ret_val, ctx.mkInt(Constants._MAX_ARG_INT));
					// BoolExpr body = ctx.mkAnd(bodyGT, bodyLT);
					// Quantifier result = ctx.mkForall(new Expr[] { txn_expr }, body, 1, null,
					// null, null, null);
					// addAssertion("bound_on_" + funcName, result);
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
		// is_alive proj function for all tables and records
		Z3Logger.LogZ3("\n;; definition of is_alive projection function for all tables");
		String funcName = "is_alive";
		objs.addFunc(funcName, ctx.mkFuncDecl(funcName, new Sort[] { objs.getSort("Rec"), objs.getSort("TimeSort") },
				objs.getSort("Bool")));
		// definition of projection functions
		for (Table t : program.getTables()) {
			Z3Logger.SubHeaderZ3("Projection functions for " + t.getTableName().getName());
			for (FieldName fn : t.getFieldNames()) {
				funcName = "proj_" + t.getTableName().getName() + "_" + fn.getName();
				Z3Logger.LogZ3("\n;; definition of projection function " + funcName);
				switch (fn.getType()) {
				case NUM:
					// define function
					objs.addFunc(funcName, ctx.mkFuncDecl(funcName,
							new Sort[] { objs.getSort("Rec"), objs.getSort("TimeSort") }, objs.getSort("ArgSort")));
					// create bound for the function
					// ArithExpr ret_val = (ArithExpr) ctx.mkApp(objs.getfuncs(funcName), rec1,
					// time1);
					// BoolExpr bodyGT = ctx.mkGt(ret_val, ctx.mkInt(0));
					// BoolExpr bodyLT = ctx.mkLt(ret_val, ctx.mkInt(Constants._MAX_FIELD_INT));
					// BoolExpr body = ctx.mkAnd(bodyGT, bodyLT);
					// Quantifier result = ctx.mkForall(new Expr[] { rec1, time1 }, body, 1, null,
					// null, null, null);
					// addAssertion("assigning bounds on " + funcName, result);
					break;
				case TEXT:
					// define function
					objs.addFunc(funcName, ctx.mkFuncDecl(funcName,
							new Sort[] { objs.getSort("Rec"), objs.getSort("TimeSort") }, objs.getSort("String")));
					// define bounds
					Expr ret_val2 = ctx.mkApp(objs.getfuncs(funcName), rec1, time1);
					BoolExpr[] eqs = new BoolExpr[Constants._MAX_FIELD_STRING];
					for (int i = 0; i < Constants._MAX_FIELD_STRING; i++)
						eqs[i] = ctx.mkEq(ret_val2, ctx.MkString("string-val#" + i));
					BoolExpr body2 = ctx.mkOr(eqs);
					Quantifier result2 = ctx.mkForall(new Expr[] { rec1, time1 }, body2, 1, null, null, null, null);
					addAssertion("assigning bounds on " + funcName, result2);
					break;
				case BOOL:
					objs.addFunc(funcName, ctx.mkFuncDecl(funcName,
							new Sort[] { objs.getSort("Rec"), objs.getSort("TimeSort") }, objs.getSort("Bool")));
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
			// model_handler.printUniverse();
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
			Expr order = ctx.mkApp(
					objs.getConstructor("RoVal", "ro" + translateExpressionsToZ3Expr(txnName, transaction, p_exp.e)));
			Expr new_constant = ctx.mkConst("constant_" + order, objs.getSort("RoSort"));
			addAssertion("local constants value", ctx.mkEq(order, ctx.mkApp(objs.getfuncs("ro_getVal"), new_constant)));
			Expr var_time = ctx.mkApp(objs.getfuncs(txnName + "_var_" + p_exp.v.getName() + "_gen_time"), transaction);
			Expr rec_expr = ctx.mkApp(objs.getfuncs(txnName + "_var_" + p_exp.v.getName() + "_get_rec"), transaction,
					new_constant);
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
				return ctx.mkBVSDiv((BitVecExpr) translateExpressionsToZ3Expr(txnName, transaction, bu_exp.oper1),
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
