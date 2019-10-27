package kiarahmani.atropos;

import java.util.ArrayList;

import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.Variable;
import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.E_Arg;
import kiarahmani.atropos.DML.expression.E_BinUp;
import kiarahmani.atropos.DML.expression.E_Proj;
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
		E_BinUp cond1 = new E_BinUp(BinOp.GT, new E_Proj(v2, acc_bal, new E_Const_Num(1)), amount2);

		Select_Query S1 = new Select_Query(1, false, acc, field_names1, v1, whc_S1);
		Update_Query U1 = new Update_Query(1, false, acc, whc_U1);

		Select_Query S2 = new Select_Query(2, false, acc, field_names2, v2, whc_S2);
		Update_Query U2 = new Update_Query(2, false, acc, whc_U2);

		U1.add_update_expressions(acc_bal,
				new E_BinUp(BinOp.PLUS, new E_Proj(v1, acc_bal, new E_Const_Num(1)), new E_Const_Num(1)));
		U2.add_update_expressions(acc_bal,
				new E_BinUp(BinOp.MINUS, new E_Proj(v2, acc_bal, new E_Const_Num(1)), new E_Const_Num(1)));

		Statement read1 = new Query_Statement(S1);
		Statement write1 = new Query_Statement(U1);
		Statement read2 = new Query_Statement(S2);
		Statement write2 = new Query_Statement(U2);
		ArrayList<Statement> if1_stmts = new ArrayList<>();
		Statement if1 = new If_Statement(cond1, if1_stmts);

		if1_stmts.add(write2);

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
}
