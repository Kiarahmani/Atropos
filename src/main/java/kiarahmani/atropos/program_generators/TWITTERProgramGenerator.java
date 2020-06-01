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
import kiarahmani.atropos.DML.where_clause.WHCC;
import kiarahmani.atropos.program.Program;
import kiarahmani.atropos.program_generators.ProgramGenerator;
import kiarahmani.atropos.utils.Program_Utils;

public class TWITTERProgramGenerator implements ProgramGenerator {

	private Program_Utils pu;

	public TWITTERProgramGenerator(Program_Utils pu) {
		this.pu = pu;
	}

	public Program generate(String... args) {
		String txn_name = "";
		ArrayList<String> txns = new ArrayList<>();
		for (String txn : args)
			txns.add(txn);

		/*
		 * ****************** Tables ******************
		 */
		// user_profiles
		String table_name = "user_profiles";
		String prefix = "up_";
		FieldName[] fns = new FieldName[] { new FieldName(prefix + "id", true, true, F_Type.NUM),
				new FieldName(prefix + "name", false, false, F_Type.TEXT),
				new FieldName(prefix + "email", false, false, F_Type.TEXT),
				new FieldName(prefix + "partitionid", false, false, F_Type.NUM),
				new FieldName(prefix + "partitionid2", false, false, F_Type.NUM),
				new FieldName(prefix + "followers", false, false, F_Type.NUM),
				new FieldName(prefix + "tweet_counts", false, false, F_Type.NUM) };
		pu.mkTable(table_name, fns);

		// followers
		table_name = "followers";
		prefix = "fr_";
		fns = new FieldName[] { new FieldName(prefix + "f1", true, true, F_Type.NUM),
				new FieldName(prefix + "f2", true, false, F_Type.NUM) };
		pu.mkTable(table_name, fns);

		// follows
		table_name = "follows";
		prefix = "fo_";
		fns = new FieldName[] { new FieldName(prefix + "f1", true, true, F_Type.NUM),
				new FieldName(prefix + "f2", true, false, F_Type.NUM) };
		pu.mkTable(table_name, fns);

		// tweets
		table_name = "tweets";
		prefix = "t_";
		fns = new FieldName[] { new FieldName(prefix + "id", true, true, F_Type.NUM),
				new FieldName(prefix + "u_id", false, false, F_Type.NUM),
				new FieldName(prefix + "text", false, false, F_Type.TEXT),
				new FieldName(prefix + "createdate", false, false, F_Type.NUM) };
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
		 * getFollowers
		 */
		if (txns.contains("getFollowers")) {
			txn_name = "getFollowers";
			prefix = "gf_";
			pu.mkTrnasaction(txn_name, prefix + "uid:int");

			// get followers ids from followers table
			table_name = "followers";
			WHC whc1 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("fr_f1"), BinOp.EQ, pu.getArg("gf_uid")));
			Select_Query select1 = pu.addSelectQuery(txn_name, table_name, whc1, "fr_f2");
			pu.addQueryStatement(txn_name, select1);

			// retrieve their details from user_profiles
			table_name = "user_profiles";
			WHC whc2 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("up_id"), BinOp.EQ, pu.mkProjExpr(txn_name, 0, "fr_f2", 1)));
			Select_Query select2 = pu.addSelectQuery(txn_name, table_name, whc2, "up_name", "up_email");
			pu.addQueryStatement(txn_name, select2);

		}

		/*
		 * getTweets
		 */
		if (txns.contains("getTweets")) {
			txn_name = "getTweets";
			prefix = "gt_";
			pu.mkTrnasaction(txn_name, prefix + "tweet_id:int");

			// retrieve the tweet
			table_name = "tweets";
			WHC whc1 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("t_id"), BinOp.EQ, pu.getArg("gt_tweet_id")));
			Select_Query select1 = pu.addSelectQuery(txn_name, table_name, whc1, "t_u_id", "t_text", "t_createdate");
			pu.addQueryStatement(txn_name, select1);

		}
		/*
		 * getTweetsFromFollowing
		 */
		if (txns.contains("getTweetsFromFollowing")) {
			txn_name = "getTweetsFromFollowing";
			prefix = "gtff_";
			pu.mkTrnasaction(txn_name, prefix + "uid:int");

			// retrieve followings
			table_name = "follows";
			WHC whc1 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("fo_f1"), BinOp.EQ, pu.getArg("gtff_uid")));
			Select_Query select1 = pu.addSelectQuery(txn_name, table_name, whc1, "fo_f2");
			pu.addQueryStatement(txn_name, select1);

			// retrieve their tweets
			table_name = "tweets";
			WHC whc2 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("t_u_id"), BinOp.EQ, pu.mkProjExpr(txn_name, 0, "fo_f2", 1)));
			Select_Query select2 = pu.addSelectQuery(txn_name, table_name, whc2, "t_id", "t_text", "t_createdate");
			pu.addQueryStatement(txn_name, select2);

		}
		/*
		 * getUserTweets
		 */
		if (txns.contains("getUserTweets")) {
			txn_name = "getUserTweets";
			prefix = "gut_";
			pu.mkTrnasaction(txn_name, prefix + "uid:int");

			// retrieve tweets of the user
			table_name = "tweets";
			WHC whc1 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("t_u_id"), BinOp.EQ, pu.getArg("gut_uid")));
			Select_Query select1 = pu.addSelectQuery(txn_name, table_name, whc1, "t_id", "t_text", "t_createdate");
			pu.addQueryStatement(txn_name, select1);

		}
		/*
		 * insertTweet
		 */
		if (txns.contains("insertTweet")) {
			txn_name = "insertTweet";
			prefix = "it_";
			pu.mkTrnasaction(txn_name, prefix + "uid:int", prefix + "text:string", prefix + "time:int");

			// select tweets of the user
			table_name = "tweets";
			WHC whc1 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("t_u_id"), BinOp.EQ, pu.getArg("it_uid")));
			Select_Query select1 = pu.addSelectQuery(txn_name, table_name, whc1, "t_id", "t_text", "t_createdate");
			pu.addQueryStatement(txn_name, select1);

			// insert the new tweet
			table_name = "tweets";
			E_BinOp new_id = new E_BinOp(BinOp.PLUS, new E_Const_Num(1), pu.mkProjExpr(txn_name, 0, "t_id", 1));
			Insert_Query insert1 = pu.addInsertQuery(txn_name, table_name,
					new WHCC(pu.getTableName(table_name), pu.getFieldName("t_id"), BinOp.EQ, new_id),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("t_u_id"), BinOp.EQ,
							pu.getArg("it_uid")),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("t_text"), BinOp.EQ,
							pu.getArg("it_text")),
					new WHCC(pu.getTableName(table_name), pu.getFieldName("t_createdate"), BinOp.EQ,
							pu.getArg("it_time")));
			insert1.addInsertExp(pu.getFieldName("t_id"), new_id);
			insert1.addInsertExp(pu.getFieldName("t_u_id"), pu.getArg("it_uid"));
			insert1.addInsertExp(pu.getFieldName("t_text"), pu.getArg("it_text"));
			insert1.addInsertExp(pu.getFieldName("t_createdate"), pu.getArg("it_time"));
			pu.addQueryStatement(txn_name, insert1);

			// select user
			table_name = "user_profiles";
			WHC whc2 = new WHC(pu.getIsAliveFieldName(table_name), new WHCC(pu.getTableName(table_name),
					pu.getFieldName("up_id"), BinOp.EQ, pu.getArg("it_uid")));
			Select_Query select2 = pu.addSelectQuery(txn_name, table_name, whc2, "up_tweet_counts");
			pu.addQueryStatement(txn_name, select2);

			// update tweet counts
			Update_Query update3 = pu.addUpdateQuery(txn_name, table_name, whc2);
			update3.addUpdateExp(pu.getFieldName("up_tweet_counts"),
					new E_BinOp(BinOp.PLUS, pu.mkProjExpr(txn_name, 1, "up_tweet_counts", 1), new E_Const_Num(1)));
			pu.addQueryStatement(txn_name, update3);

		}

		return pu.generateProgram();
	}
}
