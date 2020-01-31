package kiarahmani.atropos.program_generators;

import java.util.ArrayList;

import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.E_BinOp;
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

public class TestInputProgramGenerator  implements ProgramGenerator{

	/*
	 * 
	 * 
	 * UNIT TESTING APPLICATION GENERATOR
	 * 
	 * 
	 */
	public Program generate(String... args) {

		ArrayList<String> txns = new ArrayList<>();
		for (String txn : args)
			txns.add(txn);

		Program_Utils pu = new Program_Utils("unit test");
		String table_name = "accs";
		pu.addTable(table_name, new FieldName("key", true, true, F_Type.NUM),
				new FieldName("value", false, false, F_Type.NUM));

		/*
		 * 
		 * 
		 * 
		 * 
		 */
		if (txns.contains("select-update-test-1")) {
			String txn_name = "test-1";
			pu.addTrnasaction(txn_name, "inc_id:int", "inc_amnt:int");
			WHC increments_DEC_S1_WHC = new WHC(pu.getIsAliveFieldName("accs"), new WHC_Constraint(
					pu.getTableName(table_name), pu.getFieldName("key"), BinOp.EQ, new E_Const_Num(1)));
			Select_Query increments_DEC_S1 = pu.addSelectQuery(txn_name, table_name, true, increments_DEC_S1_WHC,
					"value");
			pu.addQueryStatement(txn_name, increments_DEC_S1);

			// update
			WHC increments_DEC_U1_WHC = new WHC(pu.getIsAliveFieldName(table_name), new WHC_Constraint(
					pu.getTableName(table_name), pu.getFieldName("key"), BinOp.EQ, new E_Const_Num(1)));
			Update_Query increments_DEC_U1 = pu.addUpdateQuery(txn_name, table_name, true, increments_DEC_U1_WHC);
			increments_DEC_U1.addUpdateExp(pu.getFieldName("value"), new E_Const_Num(2));
			pu.addQueryStatement(txn_name, increments_DEC_U1);
		}

		if (txns.contains("select-update-test-2")) {
			String txn_name = "test-2";
			pu.addTrnasaction(txn_name, "inc_id:int", "inc_amnt:int");
			WHC increments_DEC_S1_WHC = new WHC(pu.getIsAliveFieldName("accs"), new WHC_Constraint(
					pu.getTableName(table_name), pu.getFieldName("key"), BinOp.EQ, new E_Const_Num(2)));
			Select_Query increments_DEC_S1 = pu.addSelectQuery(txn_name, table_name, true, increments_DEC_S1_WHC,
					"value");
			pu.addQueryStatement(txn_name, increments_DEC_S1);

			// update
			WHC increments_DEC_U1_WHC = new WHC(pu.getIsAliveFieldName(table_name), new WHC_Constraint(
					pu.getTableName(table_name), pu.getFieldName("key"), BinOp.EQ, new E_Const_Num(1)));
			Update_Query increments_DEC_U1 = pu.addUpdateQuery(txn_name, table_name, true, increments_DEC_U1_WHC);
			increments_DEC_U1.addUpdateExp(pu.getFieldName("value"), new E_Const_Num(2));
			pu.addQueryStatement(txn_name, increments_DEC_U1);
		}

		if (txns.contains("select-update-test-3")) {
			String txn_name = "test-3";
			pu.addTrnasaction(txn_name, "inc_id:int", "inc_amnt:int");
			WHC increments_DEC_S1_WHC = new WHC(pu.getIsAliveFieldName("accs"), new WHC_Constraint(
					pu.getTableName(table_name), pu.getFieldName("key"), BinOp.EQ, pu.getArg("inc_id")));
			Select_Query increments_DEC_S1 = pu.addSelectQuery(txn_name, table_name, true, increments_DEC_S1_WHC,
					"value");
			pu.addQueryStatement(txn_name, increments_DEC_S1);

			// update
			WHC increments_DEC_U1_WHC = new WHC(pu.getIsAliveFieldName(table_name), new WHC_Constraint(
					pu.getTableName(table_name), pu.getFieldName("key"), BinOp.EQ, new E_Const_Num(1)));
			Update_Query increments_DEC_U1 = pu.addUpdateQuery(txn_name, table_name, true, increments_DEC_U1_WHC);
			increments_DEC_U1.addUpdateExp(pu.getFieldName("value"), new E_Const_Num(2));
			pu.addQueryStatement(txn_name, increments_DEC_U1);
		}

		if (txns.contains("select-update-test-4")) {
			String txn_name = "test-4";
			pu.addTrnasaction(txn_name, "inc_id:int", "inc_amnt:int");
			WHC increments_DEC_S1_WHC = new WHC(pu.getIsAliveFieldName("accs"), new WHC_Constraint(
					pu.getTableName(table_name), pu.getFieldName("key"), BinOp.EQ, pu.getArg("inc_id")));
			Select_Query increments_DEC_S1 = pu.addSelectQuery(txn_name, table_name, true, increments_DEC_S1_WHC,
					"value");
			pu.addQueryStatement(txn_name, increments_DEC_S1);

			// update
			WHC increments_DEC_U1_WHC = new WHC(pu.getIsAliveFieldName(table_name), new WHC_Constraint(
					pu.getTableName(table_name), pu.getFieldName("key"), BinOp.EQ, pu.getArg("inc_id")));
			Update_Query increments_DEC_U1 = pu.addUpdateQuery(txn_name, table_name, true, increments_DEC_U1_WHC);
			increments_DEC_U1.addUpdateExp(pu.getFieldName("value"), new E_Const_Num(2));
			pu.addQueryStatement(txn_name, increments_DEC_U1);
		}

		if (txns.contains("select-update-test-5")) {
			String txn_name = "test-5";
			pu.addTrnasaction(txn_name, "inc_id:int", "inc_amnt:int");
			WHC increments_DEC_S1_WHC = new WHC(pu.getIsAliveFieldName("accs"), new WHC_Constraint(
					pu.getTableName(table_name), pu.getFieldName("key"), BinOp.EQ, pu.getArg("inc_id")));
			Select_Query increments_DEC_S1 = pu.addSelectQuery(txn_name, table_name, true, increments_DEC_S1_WHC,
					"value");
			pu.addQueryStatement(txn_name, increments_DEC_S1);

			// update
			WHC increments_DEC_U1_WHC = new WHC(pu.getIsAliveFieldName(table_name),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("key"), BinOp.EQ,
							new E_BinOp(BinOp.PLUS, new E_Const_Num(1), pu.getArg("inc_id"))));
			Update_Query increments_DEC_U1 = pu.addUpdateQuery(txn_name, table_name, true, increments_DEC_U1_WHC);
			increments_DEC_U1.addUpdateExp(pu.getFieldName("value"), new E_Const_Num(2));
			pu.addQueryStatement(txn_name, increments_DEC_U1);
		}

		/*
		 * 
		 * 
		 * 
		 * 
		 * select-insert tests
		 * 
		 * 
		 * 
		 * 
		 */
		if (txns.contains("select-insert-test-1")) {
			String txn_name = "test-1";
			pu.addTrnasaction(txn_name, "inc_id:int", "inc_amnt:int");
			WHC increments_DEC_S1_WHC = new WHC(pu.getIsAliveFieldName("accs"), new WHC_Constraint(
					pu.getTableName(table_name), pu.getFieldName("key"), BinOp.EQ, pu.getArg("inc_id")));
			Select_Query increments_DEC_S1 = pu.addSelectQuery(txn_name, table_name, true, increments_DEC_S1_WHC,
					"value");
			pu.addQueryStatement(txn_name, increments_DEC_S1);

			// insert
			WHC_Constraint increments_DEC_U1_WHC2 = new WHC_Constraint(pu.getTableName("accs"), pu.getFieldName("key"),
					BinOp.EQ, new E_Const_Num(1));
			Insert_Query increments_DEC_U12 = pu.addInsertQuery(txn_name, "accs", true, increments_DEC_U1_WHC2);
			increments_DEC_U12.addInsertExp(pu.getFieldName("value"), pu.getArg("inc_amnt"));
			pu.addQueryStatement(txn_name, increments_DEC_U12);
		}

		if (txns.contains("select-insert-test-2")) {
			String txn_name = "test-2";
			pu.addTrnasaction(txn_name, "inc_id:int", "inc_amnt:int");
			WHC increments_DEC_S1_WHC = new WHC(pu.getIsAliveFieldName("accs"), new WHC_Constraint(
					pu.getTableName(table_name), pu.getFieldName("key"), BinOp.EQ, pu.getArg("inc_id")));
			Select_Query increments_DEC_S1 = pu.addSelectQuery(txn_name, table_name, true, increments_DEC_S1_WHC,
					"value");
			pu.addQueryStatement(txn_name, increments_DEC_S1);

			// insert
			WHC_Constraint increments_DEC_U1_WHC2 = new WHC_Constraint(pu.getTableName("accs"), pu.getFieldName("key"),
					BinOp.EQ, new E_UUID());
			Insert_Query increments_DEC_U12 = pu.addInsertQuery(txn_name, "accs", true, increments_DEC_U1_WHC2);
			increments_DEC_U12.addInsertExp(pu.getFieldName("value"), pu.getArg("inc_amnt"));
			pu.addQueryStatement(txn_name, increments_DEC_U12);
		}

		if (txns.contains("select-insert-test-3")) {
			String txn_name = "test-3";
			pu.addTrnasaction(txn_name, "inc_id:int", "inc_amnt:int");
			WHC increments_DEC_S1_WHC = new WHC(pu.getIsAliveFieldName("accs"), new WHC_Constraint(
					pu.getTableName(table_name), pu.getFieldName("key"), BinOp.EQ, new E_Const_Num(1)));
			Select_Query increments_DEC_S1 = pu.addSelectQuery(txn_name, table_name, true, increments_DEC_S1_WHC,
					"value");
			pu.addQueryStatement(txn_name, increments_DEC_S1);

			// insert
			WHC_Constraint increments_DEC_U1_WHC2 = new WHC_Constraint(pu.getTableName("accs"), pu.getFieldName("key"),
					BinOp.EQ, new E_UUID());
			Insert_Query increments_DEC_U12 = pu.addInsertQuery(txn_name, "accs", true, increments_DEC_U1_WHC2);
			increments_DEC_U12.addInsertExp(pu.getFieldName("value"), pu.getArg("inc_amnt"));
			pu.addQueryStatement(txn_name, increments_DEC_U12);
		}

		if (txns.contains("select-insert-test-4")) {
			String txn_name = "test-4";
			pu.addTrnasaction(txn_name, "inc_id:int", "inc_amnt:int");
			WHC increments_DEC_S1_WHC = new WHC(pu.getIsAliveFieldName("accs"), new WHC_Constraint(
					pu.getTableName(table_name), pu.getFieldName("key"), BinOp.EQ, pu.getArg("inc_id")));
			Select_Query increments_DEC_S1 = pu.addSelectQuery(txn_name, table_name, true, increments_DEC_S1_WHC,
					"value");
			pu.addQueryStatement(txn_name, increments_DEC_S1);

			// insert
			WHC_Constraint increments_DEC_U1_WHC2 = new WHC_Constraint(pu.getTableName("accs"), pu.getFieldName("key"),
					BinOp.EQ, pu.getArg("inc_id"));
			Insert_Query increments_DEC_U12 = pu.addInsertQuery(txn_name, "accs", true, increments_DEC_U1_WHC2);
			increments_DEC_U12.addInsertExp(pu.getFieldName("value"), pu.getArg("inc_amnt"));
			pu.addQueryStatement(txn_name, increments_DEC_U12);
		}

		if (txns.contains("select-insert-test-5")) {
			String txn_name = "test-5";
			pu.addTrnasaction(txn_name, "inc_id:int", "inc_amnt:int");
			WHC increments_DEC_S1_WHC = new WHC(pu.getIsAliveFieldName("accs"),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("key"), BinOp.EQ, new E_UUID()));
			Select_Query increments_DEC_S1 = pu.addSelectQuery(txn_name, table_name, true, increments_DEC_S1_WHC,
					"value");
			pu.addQueryStatement(txn_name, increments_DEC_S1);

			// insert
			WHC_Constraint increments_DEC_U1_WHC2 = new WHC_Constraint(pu.getTableName("accs"), pu.getFieldName("key"),
					BinOp.EQ, pu.getArg("inc_id"));
			Insert_Query increments_DEC_U12 = pu.addInsertQuery(txn_name, "accs", true, increments_DEC_U1_WHC2);
			increments_DEC_U12.addInsertExp(pu.getFieldName("value"), pu.getArg("inc_amnt"));
			pu.addQueryStatement(txn_name, increments_DEC_U12);
		}

		if (txns.contains("select-insert-test-6")) {
			String txn_name = "test-6";
			pu.addTrnasaction(txn_name, "inc_id:int", "inc_amnt:int");

			// insert
			WHC_Constraint increments_DEC_U1_WHC2 = new WHC_Constraint(pu.getTableName("accs"), pu.getFieldName("key"),
					BinOp.EQ, pu.getArg("inc_id"));
			Insert_Query increments_DEC_U12 = pu.addInsertQuery(txn_name, "accs", true, increments_DEC_U1_WHC2);
			increments_DEC_U12.addInsertExp(pu.getFieldName("value"), pu.getArg("inc_amnt"));
			pu.addQueryStatement(txn_name, increments_DEC_U12);

			// insert
			WHC_Constraint increments_DEC_U1_WHC22 = new WHC_Constraint(pu.getTableName("accs"), pu.getFieldName("key"),
					BinOp.EQ, pu.getArg("inc_id"));
			Insert_Query increments_DEC_U122 = pu.addInsertQuery(txn_name, "accs", true, increments_DEC_U1_WHC22);
			increments_DEC_U122.addInsertExp(pu.getFieldName("value"), pu.getArg("inc_amnt"));
			pu.addQueryStatement(txn_name, increments_DEC_U122);
		}

		if (txns.contains("select-insert-test-7")) {
			String txn_name = "test-7";
			pu.addTrnasaction(txn_name, "inc_id:int", "inc_amnt:int");

			// insert
			WHC_Constraint increments_DEC_U1_WHC2 = new WHC_Constraint(pu.getTableName("accs"), pu.getFieldName("key"),
					BinOp.EQ, new E_UUID());
			Insert_Query increments_DEC_U12 = pu.addInsertQuery(txn_name, "accs", true, increments_DEC_U1_WHC2);
			increments_DEC_U12.addInsertExp(pu.getFieldName("value"), pu.getArg("inc_amnt"));
			pu.addQueryStatement(txn_name, increments_DEC_U12);

			// insert
			WHC_Constraint increments_DEC_U1_WHC22 = new WHC_Constraint(pu.getTableName("accs"), pu.getFieldName("key"),
					BinOp.EQ, pu.getArg("inc_id"));
			Insert_Query increments_DEC_U122 = pu.addInsertQuery(txn_name, "accs", true, increments_DEC_U1_WHC22);
			increments_DEC_U122.addInsertExp(pu.getFieldName("value"), pu.getArg("inc_amnt"));
			pu.addQueryStatement(txn_name, increments_DEC_U122);
		}

		if (txns.contains("select-insert-test-8")) {
			String txn_name = "test-8";
			pu.addTrnasaction(txn_name, "inc_id:int", "inc_amnt:int");

			// insert
			WHC_Constraint increments_DEC_U1_WHC2 = new WHC_Constraint(pu.getTableName("accs"), pu.getFieldName("key"),
					BinOp.EQ, new E_UUID());
			Insert_Query increments_DEC_U12 = pu.addInsertQuery(txn_name, "accs", true, increments_DEC_U1_WHC2);
			increments_DEC_U12.addInsertExp(pu.getFieldName("value"), pu.getArg("inc_amnt"));
			pu.addQueryStatement(txn_name, increments_DEC_U12);

			// insert
			WHC_Constraint increments_DEC_U1_WHC22 = new WHC_Constraint(pu.getTableName("accs"), pu.getFieldName("key"),
					BinOp.EQ, new E_UUID());
			Insert_Query increments_DEC_U122 = pu.addInsertQuery(txn_name, "accs", true, increments_DEC_U1_WHC22);
			increments_DEC_U122.addInsertExp(pu.getFieldName("value"), pu.getArg("inc_amnt"));
			pu.addQueryStatement(txn_name, increments_DEC_U122);
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
		 */

		if (txns.contains("select-delete-test-1")) {
			String txn_name = "test-1";
			pu.addTrnasaction(txn_name, "inc_id:int", "inc_amnt:int");

			WHC increments_DEC_S1_WHC = new WHC(pu.getIsAliveFieldName("accs"), new WHC_Constraint(
					pu.getTableName(table_name), pu.getFieldName("key"), BinOp.EQ, pu.getArg("inc_id")));
			Select_Query increments_DEC_S1 = pu.addSelectQuery(txn_name, table_name, true, increments_DEC_S1_WHC,
					"value");
			pu.addQueryStatement(txn_name, increments_DEC_S1);

			// delete
			WHC increments_DEC_U1_WHC1 = new WHC(pu.getIsAliveFieldName("accs"),
					new WHC_Constraint(pu.getTableName("accs"), pu.getFieldName("key"), BinOp.EQ, pu.getArg("inc_id")));
			Delete_Query increments_DEC_U11 = pu.addDeleteQuery(txn_name, "accs", true, increments_DEC_U1_WHC1);
			pu.addQueryStatement(txn_name, increments_DEC_U11);
		}
		
		if (txns.contains("select-delete-test-2")) {
			String txn_name = "test-2";
			pu.addTrnasaction(txn_name, "inc_id:int", "inc_amnt:int");

			WHC increments_DEC_S1_WHC = new WHC(pu.getIsAliveFieldName("accs"), new WHC_Constraint(
					pu.getTableName(table_name), pu.getFieldName("key"), BinOp.EQ, pu.getArg("inc_id")));
			Select_Query increments_DEC_S1 = pu.addSelectQuery(txn_name, table_name, true, increments_DEC_S1_WHC,
					"value");
			pu.addQueryStatement(txn_name, increments_DEC_S1);

			// delete
			WHC increments_DEC_U1_WHC1 = new WHC(pu.getIsAliveFieldName("accs"),
					new WHC_Constraint(pu.getTableName("accs"), pu.getFieldName("key"), BinOp.EQ, pu.getArg("inc_amnt")));
			Delete_Query increments_DEC_U11 = pu.addDeleteQuery(txn_name, "accs", true, increments_DEC_U1_WHC1);
			pu.addQueryStatement(txn_name, increments_DEC_U11);
		}
		
		if (txns.contains("select-delete-test-3")) {
			String txn_name = "test-3";
			pu.addTrnasaction(txn_name, "inc_id:int", "inc_amnt:int");

			WHC increments_DEC_S1_WHC = new WHC(pu.getIsAliveFieldName("accs"), new WHC_Constraint(
					pu.getTableName(table_name), pu.getFieldName("key"), BinOp.EQ, pu.getArg("inc_id")));
			Select_Query increments_DEC_S1 = pu.addSelectQuery(txn_name, table_name, true, increments_DEC_S1_WHC,
					"value");
			pu.addQueryStatement(txn_name, increments_DEC_S1);

			// delete
			WHC increments_DEC_U1_WHC1 = new WHC(pu.getIsAliveFieldName("accs"),
					new WHC_Constraint(pu.getTableName("accs"), pu.getFieldName("key"), BinOp.EQ, new E_UUID()));
			Delete_Query increments_DEC_U11 = pu.addDeleteQuery(txn_name, "accs", true, increments_DEC_U1_WHC1);
			pu.addQueryStatement(txn_name, increments_DEC_U11);
		}
		
		

		return pu.getProgram();
	}

}
