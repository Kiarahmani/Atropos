package kiarahmani.atropos.encoding_engine.Z3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Model;
import com.microsoft.z3.Sort;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.expression.E_Arg;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.program.Transaction;
import kiarahmani.atropos.utils.Constants;

public class Model_Handler {
	Model model;
	DeclaredObjects objs;
	Program program;
	Context ctx;

	public Model_Handler(Model model, Context ctx, DeclaredObjects objs, Program program) {
		this.model = model;
		this.objs = objs;
		this.program = program;
		this.ctx = ctx;
	}

	public void printRawModelInToFile() {
		File file = new File("smt2/model.smt2");
		PrintWriter printer;
		FileWriter writer;
		try {
			writer = new FileWriter(file, false);
			printer = new PrintWriter(writer);
			printer.append(model.toString());
			printer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void printUniverse() {
		Sort txnSort = null, recSort = null;
		for (Sort sort : model.getSorts())
			if (sort.toString().equals("Rec"))
				recSort = sort;
			else if (sort.toString().equals("Txn"))
				txnSort = sort;

		System.out.println("\n\n## MODEL UNIVERSE");
		String indent = "  ";
		for (Expr txn_instance : model.getSortUniverse(txnSort)) {
			String txn_type = model.eval(objs.getfuncs("txn_type").apply(txn_instance), true).toString();
			String args = "";
			String delim;
			for (Transaction txn : program.getTransactions()) {
				delim = "";
				if (txn.getName().equalsIgnoreCase(txn_type)) {
					for (E_Arg a : txn.getArgs()) {
						String arg_val = model
								.eval(objs.getfuncs(txn_type + "_arg_" + a.toString()).apply(txn_instance), true)
								.toString();
						args += delim + a + ":" + arg_val;
						delim = ",";
					}
					System.out.println("\n"+indent + txn_instance.toString().replace("xn!val!", "") + ":" + txn_type + "("
							+ args + ")");
					// print queries
					for (Query q : txn.getAllQueries()) {
						Expr po_expr = objs.getEnumConstructor("Po", "po_" + q.getPo());
						boolean is_executed = model
								.eval(objs.getfuncs("qry_is_executed").apply(txn_instance, po_expr), true).toString()
								.equals("true");
						if (is_executed) {
							System.out.println(indent + indent + "(Q" + q.getPo() + ") " + q.getId().replace("#", ""));
							Expr current_po = model.eval(objs.getfuncs("po_from_int").apply(ctx.mkInt(q.getPo())),
									true);
							for (Expr rec_instance : model.getSortUniverse(recSort)) {
								System.out.print(indent + indent + indent
										+ rec_instance.toString().replace("ec!val!", "") + ": ");
								String rec_type = model.eval(objs.getfuncs("rec_type").apply(rec_instance), true)
										.toString();
								String rec_val = " (";
								delim = "";
								for (Table tab : program.getTables())
									if (rec_type.equals(tab.getTableName().getName())) {
										for (FieldName fn : tab.getFieldNames()) {
											String fn_val = model
													.eval(objs
															.getfuncs("proj_" + tab.getTableName().getName() + "_" + fn)
															.apply(rec_instance, txn_instance, current_po), true)
													.toString();
											rec_val += delim + fn_val;
											delim = ",";
										}
										rec_val += "," + model.eval(
												objs.getfuncs("is_alive").apply(rec_instance, txn_instance, current_po),
												true);
										Expr is_read_expression = model
												.eval(objs.getfuncs("reads_from_accounts_acc_balance")
														.apply(txn_instance, po_expr, rec_instance), true);
										Expr is_written_expression = model
												.eval(objs.getfuncs("writes_to_accounts_acc_balance")
														.apply(txn_instance, po_expr, rec_instance), true);
										String is_read_string = (is_read_expression.toString().contains("true")) ? "(R)"
												: "";
										String is_written_string = (is_written_expression.toString().contains("true"))
												? "(W)"
												: "";

										System.out.println(rec_type.toUpperCase() + rec_val + ")" + is_read_string
												+ is_written_string);
									}
							}
						}
					}
				}
			}

		}

		System.out.println("\n\n\n\n\n");
	}

}
