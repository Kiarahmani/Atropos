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
		System.out.println("\n\nMODEL UNIVERSE");
		for (Sort sort : model.getSorts()) {
			System.out.println("\n**Sort " + sort + ":");
			String indent = "   ";
			// print rec sort
			if (sort.toString().equals("Rec")) {
				for (int t = 0; t < Constants._MAX_EXECECUTION_LENGTH; t++) {
					System.out.println(indent + "time:" + t);
					for (Expr x : model.getSortUniverse(sort)) {
						System.out.print(indent + indent + x.toString().replace("!val!", "") + ": ");
						String rec_type = model.eval(objs.getfuncs("rec_type").apply(x), true).toString();
						String rec_val = " (";
						String delim = "";
						for (Table tab : program.getTables())
							if (rec_type.equals(tab.getTableName().getName())) {
								for (FieldName fn : tab.getFieldNames()) {
									String fn_val = model
											.eval(objs.getfuncs("proj_" + tab.getTableName().getName() + "_" + fn)
													.apply(x, ctx.mkInt(t)), true)
											.toString();
									rec_val += delim + fn_val;
									delim = ",";
								}
								rec_val += "," + model.eval(objs.getfuncs("is_alive").apply(x, ctx.mkInt(t)), true);
							}
						// if (!rec_type.equals("departments"))
						// rec_type += " ";
						System.out.println(rec_type.toUpperCase() + rec_val + ")");
					}
				}
				System.out.println();
			}
			
		
		/*	// print qry sort
			if (sort.toString().equals("Qry"))
				for (int t = 0; t < Constants._MAX_EXECECUTION_LENGTH; t++) {
					System.out.println(indent + "time:" + t);
					for (Expr x : model.getSortUniverse(sort)) {
						if (model.eval(objs.getfuncs("qry_time").apply(x), true).toString().equals(String.valueOf(t))) {
							String qry_type = model.eval(objs.getfuncs("qry_type").apply(x), true).toString();
							String parent_txn = model.eval(objs.getfuncs("parent").apply(x), true).toString();
							System.out.println(indent + indent + x.toString().replace("!val!", "") + ": "
									+ parent_txn.replace("!val!", "") + "." + qry_type);
						}
					}
				}
				
				*/
			// print txn sort
			if (sort.toString().equals("Txn"))
				for (Expr x : model.getSortUniverse(sort)) {
					String txn_type = model.eval(objs.getfuncs("txn_type").apply(x), true).toString();
					String args = "";
					String delim;
					for (Transaction txn : program.getTransactions()) {
						delim = "";
						if (txn.getName().equalsIgnoreCase(txn_type)) {
							for (E_Arg a : txn.getArgs()) {
								String arg_val = model
										.eval(objs.getfuncs(txn_type + "_arg_" + a.toString()).apply(x), true)
										.toString();
								args += delim + a + ":" + arg_val;
								delim = ",";
							}
						}
					}

					System.out.println(indent + x.toString().replace("!val!", "") + ": " + txn_type + "(" + args + ")");
				}
				
		}
		System.out.println("\n\n\n\n\n");
	}

}
