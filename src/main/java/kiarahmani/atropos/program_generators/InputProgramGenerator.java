package kiarahmani.atropos.program_generators;

import java.util.ArrayList;

import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.E_BinUp;
import kiarahmani.atropos.DML.expression.E_UUID;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.constants.E_Const_Num;
import kiarahmani.atropos.DML.query.Delete_Query;
import kiarahmani.atropos.DML.query.Insert_Query;
import kiarahmani.atropos.DML.query.Select_Query;
import kiarahmani.atropos.DML.query.Update_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.DML.where_clause.WHC_Constraint;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.utils.Program_Utils;

public class InputProgramGenerator {

	/*
	 * 
	 * 
	 * UNIT TESTING APPLICATION GENERATOR
	 * 
	 * 
	 */
	public Program generateUnitTestProgram(String... args) {

		ArrayList<String> txns = new ArrayList<>();
		for (String txn : args)
			txns.add(txn);

		Program_Utils pu = new Program_Utils("unit test");
		String table_name = "accs";
		pu.addTable(table_name, new FieldName("key", true, true, F_Type.NUM),
				new FieldName("name", false, false, F_Type.TEXT), new FieldName("value", false, false, F_Type.NUM));

		if (txns.contains("txn")) {
			// dec transaction
			pu.addTrnasaction("txn", "arg_id:int", "arg_amnt:int");
			WHC DEC_S1_WHC = new WHC(pu.getIsAliveFieldName("accs"), new WHC_Constraint(pu.getTableName("table"),
					pu.getFieldName("key"), BinOp.EQ, pu.getArg("arg_id")));
			Select_Query DEC_S1 = pu.addSelectQuery("txn", "table", true, DEC_S1_WHC, "name", "value");
			pu.addQueryStatement("txn", DEC_S1);

			Expression DEC_IF1_C = new E_BinUp(BinOp.GT, pu.getProjExpr("txn", 0, "value", 1), pu.getArg("arg_amnt"));
			pu.addIfStatement("txn", DEC_IF1_C);

			WHC DEC_U1_WHC = new WHC(pu.getIsAliveFieldName("accs"), new WHC_Constraint(pu.getTableName("table"),
					pu.getFieldName("key"), BinOp.EQ, pu.getArg("arg_id")));
			Update_Query DEC_U1 = pu.addUpdateQuery("txn", "table", true, DEC_U1_WHC);
			DEC_U1.addUpdateExp(pu.getFieldName("value"),
					new E_BinUp(BinOp.MINUS, pu.getProjExpr("txn", 0, "value", 1), pu.getArg("arg_amnt")));
			pu.addQueryStatementInIf("txn", 0, DEC_U1);

			Update_Query DEC_U2 = pu.addUpdateQuery("txn", "table", true, DEC_U1_WHC);
			DEC_U2.addUpdateExp(pu.getFieldName("value"),
					new E_BinUp(BinOp.MINUS, pu.getProjExpr("txn", 0, "value", 1), new E_Const_Num(10)));
			pu.addQueryStatementInElse("txn", 0, DEC_U2);
		}

		if (txns.contains("increments")) {
			pu.addTrnasaction("increments", "inc_id:int", "inc_amnt:int");
			WHC increments_DEC_S1_WHC = new WHC(pu.getIsAliveFieldName("accs"), new WHC_Constraint(
					pu.getTableName("table"), pu.getFieldName("key"), BinOp.EQ, pu.getArg("inc_id")));
			Select_Query increments_DEC_S1 = pu.addSelectQuery("increments", "table", true, increments_DEC_S1_WHC,
					"name", "value");
			pu.addQueryStatement("increments", increments_DEC_S1);

			WHC increments_DEC_U1_WHC = new WHC(pu.getIsAliveFieldName("accs"), new WHC_Constraint(
					pu.getTableName("table"), pu.getFieldName("key"), BinOp.EQ, pu.getArg("inc_id")));
			Update_Query increments_DEC_U1 = pu.addUpdateQuery("increments", "table", true, increments_DEC_U1_WHC);
			increments_DEC_U1.addUpdateExp(pu.getFieldName("value"),
					new E_BinUp(BinOp.PLUS, pu.getProjExpr("increments", 0, "value", 1), pu.getArg("inc_amnt")));
			pu.addQueryStatement("increments", increments_DEC_U1);
		}

		if (txns.contains("read")) {
			String txn_name = "read";
			pu.addTrnasaction(txn_name, "read_id:int");
			WHC read_condition = new WHC(pu.getIsAliveFieldName(table_name), new WHC_Constraint(
					pu.getTableName(table_name), pu.getFieldName("key"), BinOp.EQ, new E_Const_Num(1)));
			Select_Query read_query = pu.addSelectQuery(txn_name, table_name, true, read_condition, "value");
			pu.addQueryStatement(txn_name, read_query);
		}

		if (txns.contains("inc")) {
			String txn_name = "inc";
			pu.addTrnasaction(txn_name, "inc_id:int", "inc_amnt:int");
			WHC increments_DEC_S1_WHC = new WHC(pu.getIsAliveFieldName("accs"),
					new WHC_Constraint(pu.getTableName("accs"), pu.getFieldName("key"), BinOp.EQ, new E_Const_Num(1)));
			Select_Query increments_DEC_S1 = pu.addSelectQuery(txn_name, "accs", true, increments_DEC_S1_WHC, "value");
			pu.addQueryStatement(txn_name, increments_DEC_S1);

			// update
			WHC increments_DEC_U1_WHC = new WHC(pu.getIsAliveFieldName("accs"),
					new WHC_Constraint(pu.getTableName("accs"), pu.getFieldName("key"), BinOp.EQ, new E_Const_Num(1)));
			Update_Query increments_DEC_U1 = pu.addUpdateQuery(txn_name, "accs", true, increments_DEC_U1_WHC);
			increments_DEC_U1.addUpdateExp(pu.getFieldName("value"), new E_Const_Num(1));
			// pu.addQueryStatement(txn_name, increments_DEC_U1);

			// delete
			WHC increments_DEC_U1_WHC1 = new WHC(pu.getIsAliveFieldName("accs"),
					new WHC_Constraint(pu.getTableName("accs"), pu.getFieldName("key"), BinOp.EQ,
							new E_BinUp(BinOp.PLUS, new E_Const_Num(67), new E_Const_Num(1))));
			Delete_Query increments_DEC_U11 = pu.addDeleteQuery("increments", "accs", true, increments_DEC_U1_WHC1);
			// pu.addQueryStatement("increments", increments_DEC_U11);

			// insert
			WHC_Constraint increments_DEC_U1_WHC2 = new WHC_Constraint(pu.getTableName("accs"), pu.getFieldName("key"),
					BinOp.EQ, new E_UUID());
			Insert_Query increments_DEC_U12 = pu.addInsertQuery(txn_name, "accs", true, increments_DEC_U1_WHC2);
			// increments_DEC_U12.addInsertExp(pu.getFieldName("value"),
			// pu.getArg("inc_amnt"));
			// increments_DEC_U12.addInsertExp(pu.getFieldName("name"), new
			// E_Const_Text("kia"));
			pu.addQueryStatement(txn_name, increments_DEC_U12);

		}

		return pu.getProgram();
	}
}
