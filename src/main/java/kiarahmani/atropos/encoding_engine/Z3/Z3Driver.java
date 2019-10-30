package kiarahmani.atropos.encoding_engine.Z3;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Model;
import com.microsoft.z3.Quantifier;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Sort;
import com.microsoft.z3.Status;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.program.Transaction;
import kiarahmani.atropos.utils.Constants;

public class Z3Driver {
	private static final Logger logger = LogManager.getLogger(Atropos.class);
	Context ctx;
	Solver slv;
	Axioms axioms;
	Model model;
	Program_Relations program_relations;

	DeclaredObjects objs;

	public Z3Driver(Program program, int current_cycle_length) {
		logger.debug("new Z3 Driver object is being created");
		HashMap<String, String> cfg = new HashMap<String, String>();
		cfg.put("model", "true");
		cfg.put("unsat_core", "true");
		ctx = new Context(cfg);
		objs = new DeclaredObjects();
		axioms = new Axioms();

		// begin encoding
		slv = ctx.mkSolver();
		addInitialHeader();
		addInitialStaticSorts();

		program_relations = new Program_Relations(program, ctx, objs);
		program_relations.addRecFldDataTypes();
		program_relations.addTxnOpDataTypes();
		program_relations.addTypingFuncs();
		program_relations.addExecutionFuncs();
		program_relations.addProgramOrderFunc();
		program_relations.addParentFunc();
		addProjFuncsAndBounds(program);
		addAssertion("bound_on_qry_time", program_relations.mk_bound_on_qry_time());
		addAssertion("bound_on_qry_part", program_relations.mk_bound_on_qry_part());
		addAssertion("bound_on_qry_po", program_relations.mk_bound_on_po_part());
		addAssertion("bound_on_txn_instances", program_relations.mk_bound_on_txn_instances(current_cycle_length));
		for (Transaction txn : program.getTransactions()) {
			int po = 1;
			for (String qry_name : txn.getAllStmtTypes())
				addAssertion("qry_type_to_po", program_relations.mk_qry_type_to_po(qry_name, po++));
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

		/*
		 * 
		 * 
		 * 
		 */
		checkSAT();
	}

	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	/*
	 * final call to Z3 when the context is completely done
	 */

	private void addProjFuncsAndBounds(Program program) {
		Expr rec = ctx.mkFreshConst("rec", objs.getSort("Rec"));
		Expr time = ctx.mkFreshConst("time", objs.getSort("Int"));
		for (Table t : program.getTables())
			for (FieldName fn : t.getFieldNames()) {
				String funcName = "proj_" + t.getTableName().getName() + "_" + fn.getName();
				switch (fn.getType()) {
				case NUM:
					// define function
					Z3Logger.LogZ3(";; definition of projection function " + funcName);
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

	private void checkSAT() {
		long begin = System.currentTimeMillis();
		if (slv.check() == Status.SATISFIABLE) {
			model = slv.getModel();
			long end = System.currentTimeMillis();
			System.out.println(
					"\n\n==================\n" + "SATISFIABLE (" + (end - begin) + "ms)" + "\n==================\n\n");
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
		Z3Logger.LogZ3(";" + name);
		objs.addAssertion(name, ass);
		slv.add(ass);
		// System.out.println("add#: " + this.globalIter++ + ": " + name);
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

}
