package kiarahmani.atropos.program_generators;

import java.util.ArrayList;

import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.E_BinOp;
import kiarahmani.atropos.DML.expression.E_Proj;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.expression.constants.E_Const_Num;
import kiarahmani.atropos.DML.expression.constants.E_Const_Text;
import kiarahmani.atropos.DML.query.Delete_Query;
import kiarahmani.atropos.DML.query.Insert_Query;
import kiarahmani.atropos.DML.query.Select_Query;
import kiarahmani.atropos.DML.query.Update_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.DML.where_clause.WHC_Constraint;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program_generators.ProgramGenerator;
import kiarahmani.atropos.utils.Program_Utils;

public class SIBenchProgramGenerator implements ProgramGenerator {

	private Program_Utils pu;

	public SIBenchProgramGenerator(Program_Utils pu) {
		this.pu = pu;
	}

	public Program generate(String... args) {
		String txn_name = "";
		String prefix = "";
		ArrayList<String> txns = new ArrayList<>();
		for (String txn : args)
			txns.add(txn);

		/*
		 * ****************** Tables ******************
		 */
		// user_profiles
		String table_name = "sitest";
		FieldName[] fns = new FieldName[] { new FieldName("id", true, true, F_Type.NUM),
				new FieldName("value", false, false, F_Type.NUM) };
		pu.mkTable(table_name, fns);

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
		 * ************************ Transactions ************************
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
		 * minRecord
		 */
		if (txns.contains("minRecord")) {
			txn_name = "minRecord";
			prefix = "m_";
			pu.mkTrnasaction(txn_name, prefix + "id:int");

			table_name = "sitest";
			WHC whc1 = new WHC(pu.getIsAliveFieldName(table_name), new WHC_Constraint(pu.getTableName(table_name),
					pu.getFieldName("id"), BinOp.EQ, pu.getArg("m_id")));
			Select_Query select1 = pu.addSelectQuery(txn_name, table_name, whc1, "value");
			pu.addQueryStatement(txn_name, select1);
		}

		/*
		 * updateRecord
		 */
		if (txns.contains("updateRecord")) {
			txn_name = "updateRecord";
			prefix = "u_";
			pu.mkTrnasaction(txn_name, prefix + "id:int");

			table_name = "sitest";
			WHC whc1 = new WHC(pu.getIsAliveFieldName(table_name), new WHC_Constraint(pu.getTableName(table_name),
					pu.getFieldName("id"), BinOp.EQ, pu.getArg("u_id")));
			Select_Query select1 = pu.addSelectQuery(txn_name, table_name, whc1, "value");
			pu.addQueryStatement(txn_name, select1);

			Update_Query update2 = pu.addUpdateQuery(txn_name, table_name, whc1);
			update2.addUpdateExp(pu.getFieldName("value"),
					new E_BinOp(BinOp.PLUS, pu.mkProjExpr(txn_name, 0, "value", 1), new E_Const_Num(1)));
			pu.addQueryStatement(txn_name, update2);
		}

		return pu.generateProgram();
	}
}
