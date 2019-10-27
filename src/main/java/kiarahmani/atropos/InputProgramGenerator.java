package kiarahmani.atropos;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.E_Arg;
import kiarahmani.atropos.DML.expression.E_BinUp;
import kiarahmani.atropos.DML.expression.E_Proj;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.constants.E_Const_Num;
import kiarahmani.atropos.DML.query.Select_Query;
import kiarahmani.atropos.DML.query.Update_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.DML.where_clause.WHC_Constraint;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program.Statement;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.program.Transaction;
import kiarahmani.atropos.program.statements.If_Statement;
import kiarahmani.atropos.program.statements.Query_Statement;
import kiarahmani.atropos.utils.Program_Utils;
import static java.util.stream.Collectors.toList;

public class InputProgramGenerator {

	/*
	 * 
	 * 
	 * Small Bank Example
	 * 
	 * 
	 */
	public Program generateBankProgram() {
		Program bank = new Program("bank");
		Transaction inc = new Transaction("inc");
		Transaction dec = new Transaction("dec");

		TableName acc = new TableName("Accounts");
		FieldName acc_id = new FieldName("acc_id", true, true, F_Type.NUM);
		FieldName acc_bal = new FieldName("acc_bal", false, false, F_Type.NUM);

		Table acc_table = new Table(acc, acc_id, acc_bal);

		ArrayList<FieldName> field_names1 = new ArrayList<>();
		field_names1.add(acc_id);
		field_names1.add(acc_bal);
		ArrayList<FieldName> field_names2 = new ArrayList<>();
		field_names2.add(acc_id);
		field_names2.add(acc_bal);

		E_Arg id1 = new E_Arg("id");
		E_Arg amount1 = new E_Arg("amount");

		E_Arg id2 = new E_Arg("id");
		E_Arg amount2 = new E_Arg("amount");

		Variable v1 = new Variable("v1");
		Variable v2 = new Variable("v2");

		WHC whc_S1 = new WHC(new WHC_Constraint(acc_id, BinOp.EQ, id1));
		WHC whc_U1 = new WHC(new WHC_Constraint(acc_id, BinOp.EQ, id1));

		WHC whc_S2 = new WHC(new WHC_Constraint(acc_id, BinOp.EQ, id2));
		WHC whc_U2 = new WHC(new WHC_Constraint(acc_id, BinOp.EQ, id2));
		WHC whc_U3 = new WHC(new WHC_Constraint(acc_id, BinOp.EQ, id2));
		E_BinUp cond1 = new E_BinUp(BinOp.GT, new E_Proj(v2, acc_bal, new E_Const_Num(1)), amount2);

		Select_Query S1 = new Select_Query(1, false, acc, field_names1, v1, whc_S1);
		Update_Query U1 = new Update_Query(1, false, acc, whc_U1);

		Select_Query S2 = new Select_Query(2, false, acc, field_names2, v2, whc_S2);
		Update_Query U2 = new Update_Query(2, false, acc, whc_U2);
		Update_Query U3 = new Update_Query(3, false, acc, whc_U3);

		U1.add_update_expressions(acc_bal,
				new E_BinUp(BinOp.PLUS, new E_Proj(v1, acc_bal, new E_Const_Num(1)), amount1));
		U2.add_update_expressions(acc_bal,
				new E_BinUp(BinOp.MINUS, new E_Proj(v2, acc_bal, new E_Const_Num(1)), amount2));
		U3.add_update_expressions(acc_bal,
				new E_BinUp(BinOp.MINUS, new E_Proj(v2, acc_bal, new E_Const_Num(1)), new E_Const_Num(10)));

		Statement read1 = new Query_Statement(1, S1);
		Statement write1 = new Query_Statement(2, U1);
		Statement read2 = new Query_Statement(1, S2);
		Statement write2 = new Query_Statement(3, U2);
		Statement write3 = new Query_Statement(4, U3);
		ArrayList<Statement> if1_stmts = new ArrayList<>();
		ArrayList<Statement> else1_stmts = new ArrayList<>();
		if1_stmts.add(write2);
		else1_stmts.add(write3);
		Statement if1 = new If_Statement(2, cond1, if1_stmts, else1_stmts);

		inc.addStatement(read1);
		inc.addStatement(write1);

		dec.addArg(id1);
		dec.addArg(amount1);

		inc.addArg(id2);
		inc.addArg(amount2);

		dec.addStatement(read2);
		dec.addStatement(if1);

		bank.addTable(acc_table);
		bank.addTransaction(inc);
		bank.addTransaction(dec);
		return bank;
	}

	/*
	 * 
	 * 
	 * Test Example
	 * 
	 * 
	 */
	public Program generateTestProgram() {
		Program_Utils program_utils = new Program_Utils("test");
		program_utils.addTrnasaction("txn1", "t1a1", "t1a2", "t1a3");
		program_utils.addTrnasaction("txn2", "t2a1", "t2a2");
		program_utils.addTrnasaction("txn3", "t3a1");
		program_utils.addBasicTable("Table1", "t1f1", "t1f2", "t1f3", "t1f4");
		program_utils.addBasicTable("Table2", "t2f1", "t2f2", "t2f3");

		WHC whc_S1 = new WHC(
				new WHC_Constraint(program_utils.getFieldName("t1f1"), BinOp.EQ, program_utils.getArg("t1a1")));
		WHC whc_U1 = new WHC(
				new WHC_Constraint(program_utils.getFieldName("t1f1"), BinOp.EQ, program_utils.getArg("t1a1")));
		Select_Query T1S1 = program_utils.addSelectQuery("txn1", "Table1", false, whc_S1, "t1f1", "t1f2");
		Update_Query T1U1 = program_utils.addUpdateQuery("txn1", "Table1", false, whc_U1);
		T1U1.add_update_expressions(program_utils.getFieldName("t1f1"),
				program_utils.getProjExpr("txn1", 0, "t1f1", 1));

		program_utils.addQueryStatement("txn1", T1S1);
		program_utils.addQueryStatement("txn1", T1U1);

		return program_utils.getProgram();
	}
}
