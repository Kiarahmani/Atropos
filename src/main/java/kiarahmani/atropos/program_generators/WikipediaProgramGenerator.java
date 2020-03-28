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

public class WikipediaProgramGenerator implements ProgramGenerator {

	private Program_Utils pu;

	public WikipediaProgramGenerator(Program_Utils pu) {
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
		// ipblocks
		String table_name = "ipblocks";
		String prefix = "ipb_";
		FieldName[] fns = new FieldName[] { new FieldName("ipb_id", true, true, F_Type.NUM),
				new FieldName("ipb_address", false, false, F_Type.TEXT),
				new FieldName("ipb_user", false, false, F_Type.NUM), new FieldName("ipb_by", false, false, F_Type.NUM),
				new FieldName("ipb_by_text", false, false, F_Type.TEXT),
				new FieldName("ipb_reason", false, false, F_Type.TEXT),
				new FieldName("ipb_timestamp", false, false, F_Type.TEXT),
				new FieldName("ipb_auto", false, false, F_Type.NUM),
				new FieldName("ipb_anon_only", false, false, F_Type.NUM),
				new FieldName("ipb_create_account", false, false, F_Type.NUM),
				new FieldName("ipb_enable_autoblock", false, false, F_Type.NUM),
				new FieldName("ipb_expiry", false, false, F_Type.TEXT),
				new FieldName("ipb_range_start", false, false, F_Type.TEXT),
				new FieldName("ipb_range_end", false, false, F_Type.TEXT),
				new FieldName("ipb_deleted", false, false, F_Type.NUM),
				new FieldName("ipb_block_email", false, false, F_Type.NUM),
				new FieldName("ipb_allow_usertalk", false, false, F_Type.NUM) };
		pu.mkTable(table_name, fns);

		// useracct
		table_name = "useracct";
		prefix = "user_";
		fns = new FieldName[] { new FieldName("user_id", true, true, F_Type.NUM),
				new FieldName("user_name", false, false, F_Type.TEXT),
				new FieldName("user_real_name", false, false, F_Type.TEXT),
				new FieldName("user_password", false, false, F_Type.TEXT),
				new FieldName("user_newpassword", false, false, F_Type.TEXT),
				new FieldName("user_newpass_time", false, false, F_Type.TEXT),
				new FieldName("user_email", false, false, F_Type.TEXT),
				new FieldName("user_options", false, false, F_Type.TEXT),
				new FieldName("user_touched", false, false, F_Type.TEXT),
				new FieldName("user_token", false, false, F_Type.TEXT),
				new FieldName("user_email_authenticated", false, false, F_Type.TEXT),
				new FieldName("user_email_token", false, false, F_Type.TEXT),
				new FieldName("user_email_token_expires", false, false, F_Type.TEXT),
				new FieldName("user_registration", false, false, F_Type.TEXT),
				new FieldName("user_editcount", false, false, F_Type.NUM) };
		pu.mkTable(table_name, fns);

		// logging
		table_name = "logging";
		prefix = "log_";
		fns = new FieldName[] { new FieldName("log_id", true, true, F_Type.NUM),
				new FieldName("log_type", false, false, F_Type.TEXT),
				new FieldName("log_action", false, false, F_Type.TEXT),
				new FieldName("log_timestamp", false, false, F_Type.TEXT),
				new FieldName("log_user", false, false, F_Type.NUM),
				new FieldName("log_namespace", false, false, F_Type.NUM),
				new FieldName("log_title", false, false, F_Type.TEXT),
				new FieldName("log_comment", false, false, F_Type.TEXT),
				new FieldName("log_params", false, false, F_Type.TEXT),
				new FieldName("log_deleted", false, false, F_Type.NUM),
				new FieldName("log_user_text", false, false, F_Type.TEXT),
				new FieldName("log_page", false, false, F_Type.NUM) };
		pu.mkTable(table_name, fns);

		// page
		table_name = "page";
		prefix = "page_";
		fns = new FieldName[] { new FieldName("page_id", true, true, F_Type.NUM),
				new FieldName("page_namespace", false, false, F_Type.NUM),
				new FieldName("page_title", false, false, F_Type.TEXT),
				new FieldName("page_restrictions", false, false, F_Type.TEXT),
				new FieldName("page_counter", false, false, F_Type.NUM),
				new FieldName("page_is_redirect", false, false, F_Type.NUM),
				new FieldName("page_is_new", false, false, F_Type.NUM),
				new FieldName("page_random", false, false, F_Type.NUM),
				new FieldName("page_touched", false, false, F_Type.TEXT),
				new FieldName("page_latest", false, false, F_Type.NUM),
				new FieldName("page_len", false, false, F_Type.NUM) };
		pu.mkTable(table_name, fns);

		// page_backup
		table_name = "page_backup";
		prefix = "page_backup_";
		fns = new FieldName[] { new FieldName("page_backup_id", true, true, F_Type.NUM),
				new FieldName("page_backup_namespace", false, false, F_Type.NUM),
				new FieldName("page_backup_title", false, false, F_Type.TEXT),
				new FieldName("page_backup_restrictions", false, false, F_Type.TEXT),
				new FieldName("page_backup_counter", false, false, F_Type.NUM),
				new FieldName("page_backup_is_redirect", false, false, F_Type.NUM),
				new FieldName("page_backup_is_new", false, false, F_Type.NUM),
				new FieldName("page_backup_random", false, false, F_Type.NUM),
				new FieldName("page_backup_touched", false, false, F_Type.TEXT),
				new FieldName("page_backup_latest", false, false, F_Type.NUM),
				new FieldName("page_backup_len", false, false, F_Type.NUM) };
		pu.mkTable(table_name, fns);

		// page_restrictions
		table_name = "page_restrictions";
		prefix = "pr_";
		fns = new FieldName[] { new FieldName("pr_id", true, true, F_Type.NUM),
				new FieldName("pr_page", false, false, F_Type.NUM), new FieldName("pr_type", false, false, F_Type.TEXT),
				new FieldName("pr_level", false, false, F_Type.TEXT),
				new FieldName("pr_cascade", false, false, F_Type.NUM),
				new FieldName("pr_user", false, false, F_Type.NUM),
				new FieldName("pr_expiry", false, false, F_Type.TEXT) };
		pu.mkTable(table_name, fns);

		// recentchanges
		table_name = "recentchanges";
		prefix = "rc_";
		fns = new FieldName[] { new FieldName("rc_id", true, true, F_Type.NUM),
				new FieldName("rc_timestamp", false, false, F_Type.TEXT),
				new FieldName("rc_cur_time", false, false, F_Type.TEXT),
				new FieldName("rc_user", false, false, F_Type.NUM),
				new FieldName("rc_user_text", false, false, F_Type.TEXT),
				new FieldName("rc_namespace", false, false, F_Type.NUM),
				new FieldName("rc_title", false, false, F_Type.TEXT),
				new FieldName("rc_comment", false, false, F_Type.TEXT),
				new FieldName("rc_minor", false, false, F_Type.NUM), new FieldName("rc_bot", false, false, F_Type.NUM),
				new FieldName("rc_new", false, false, F_Type.NUM), new FieldName("rc_cur_id", false, false, F_Type.NUM),
				new FieldName("rc_this_oldid", false, false, F_Type.NUM),
				new FieldName("rc_last_oldid", false, false, F_Type.NUM),
				new FieldName("rc_type", false, false, F_Type.NUM),
				new FieldName("rc_moved_to_ns", false, false, F_Type.NUM),
				new FieldName("rc_moved_to_title", false, false, F_Type.TEXT),
				new FieldName("rc_patrolled", false, false, F_Type.NUM),
				new FieldName("rc_ip", false, false, F_Type.TEXT),
				new FieldName("rc_old_len", false, false, F_Type.NUM),
				new FieldName("rc_new_len", false, false, F_Type.NUM),
				new FieldName("rc_deleted", false, false, F_Type.NUM),
				new FieldName("rc_logid", false, false, F_Type.NUM),
				new FieldName("rc_log_type", false, false, F_Type.TEXT),
				new FieldName("rc_log_action", false, false, F_Type.TEXT),
				new FieldName("rc_params", false, false, F_Type.TEXT) };
		pu.mkTable(table_name, fns);

		// revision
		table_name = "revision";
		prefix = "rev_";
		fns = new FieldName[] { new FieldName("rev_id", true, true, F_Type.NUM),
				new FieldName("rev_page", false, false, F_Type.NUM),
				new FieldName("rev_text_id", false, false, F_Type.NUM),
				new FieldName("rev_comment", false, false, F_Type.TEXT),
				new FieldName("rev_user", false, false, F_Type.NUM),
				new FieldName("rev_user_text", false, false, F_Type.TEXT),
				new FieldName("rev_timestamp", false, false, F_Type.TEXT),
				new FieldName("rev_minor_edit", false, false, F_Type.NUM),
				new FieldName("rev_deleted", false, false, F_Type.NUM),
				new FieldName("rev_len", false, false, F_Type.NUM),
				new FieldName("rev_pcarenct_id", false, false, F_Type.NUM) };
		pu.mkTable(table_name, fns);

		// text
		table_name = "text";
		prefix = "text_";
		fns = new FieldName[] { new FieldName("old_id", true, true, F_Type.NUM),
				new FieldName("old_text", false, false, F_Type.TEXT),
				new FieldName("old_flags", false, false, F_Type.TEXT),
				new FieldName("old_page", false, false, F_Type.NUM) };
		pu.mkTable(table_name, fns);

		// user_groups
		table_name = "user_groups";
		prefix = "ug_";
		fns = new FieldName[] { new FieldName("ug_user", true, true, F_Type.NUM),
				new FieldName("ug_group", true, false, F_Type.NUM) };
		pu.mkTable(table_name, fns);

		// value_backup
		table_name = "value_backup";
		prefix = "vb_";
		fns = new FieldName[] { new FieldName("table_name", true, true, F_Type.TEXT),
				new FieldName("maxid", true, false, F_Type.NUM) };
		pu.mkTable(table_name, fns);

		// watchlist
		table_name = "watchlist";
		prefix = "wl_";
		fns = new FieldName[] { new FieldName("wl_user", true, true, F_Type.NUM),
				new FieldName("wl_namespace", true, false, F_Type.NUM),
				new FieldName("wl_title", true, false, F_Type.TEXT),
				new FieldName("wl_notificationtimestamp", false, false, F_Type.TEXT) };
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
		 *************************************************************************
		 ************************* Transactions **********************************
		 *************************************************************************
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
		 * addWatchList
		 */
		if (txns.contains("addWatchList")) {
			txn_name = "addWatchList";
			prefix = "awl_";
			pu.mkTrnasaction(txn_name, prefix + "userId:int", prefix + "nameSpace:int", prefix + "pageTitle:string",
					prefix + "timestamp:string");

			// if userId>0
			Expression if1 = new E_BinOp(BinOp.GT, pu.getArg("awl_userId"), new E_Const_Num(0));
			pu.addIfStatement(txn_name, if1);

			// insert into watchlist
			table_name = "watchlist";
			Insert_Query insert1 = pu.addInsertQuery(txn_name, table_name,
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("wl_user"), BinOp.EQ,
							pu.getArg("awl_userId")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("wl_namespace"), BinOp.EQ,
							pu.getArg("awl_nameSpace")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("wl_title"), BinOp.EQ,
							pu.getArg("awl_pageTitle")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("wl_notificationtimestamp"),
							BinOp.EQ, new E_Const_Text("null")));
			insert1.addInsertExp(pu.getFieldName("wl_user"), pu.getArg("awl_userId"));
			insert1.addInsertExp(pu.getFieldName("wl_namespace"), pu.getArg("awl_nameSpace"));
			insert1.addInsertExp(pu.getFieldName("wl_title"), pu.getArg("awl_pageTitle"));
			insert1.addInsertExp(pu.getFieldName("wl_notificationtimestamp"), new E_Const_Text("null"));
			pu.addQueryStatementInIf(txn_name, 0, insert1);

			// if nameSpace=0
			Expression if2 = new E_BinOp(BinOp.EQ, pu.getArg("awl_nameSpace"), new E_Const_Num(0));
			pu.addIfStatementInIf(txn_name, 0, if2);

			// insert into watchlist
			table_name = "watchlist";
			Insert_Query insert2 = pu.addInsertQuery(txn_name, table_name,
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("wl_user"), BinOp.EQ,
							pu.getArg("awl_userId")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("wl_namespace"), BinOp.EQ,
							new E_Const_Num(1)),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("wl_title"), BinOp.EQ,
							pu.getArg("awl_pageTitle")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("wl_notificationtimestamp"),
							BinOp.EQ, new E_Const_Text("null")));
			insert2.addInsertExp(pu.getFieldName("wl_user"), pu.getArg("awl_userId"));
			insert2.addInsertExp(pu.getFieldName("wl_namespace"), new E_Const_Num(1));
			insert2.addInsertExp(pu.getFieldName("wl_title"), pu.getArg("awl_pageTitle"));
			insert2.addInsertExp(pu.getFieldName("wl_notificationtimestamp"), new E_Const_Text("null"));
			pu.addQueryStatementInIf(txn_name, 1, insert2);

			// update useracct
			table_name = "useracct";
			WHC whc3 = new WHC(pu.getIsAliveFieldName(table_name), new WHC_Constraint(pu.getTableName(table_name),
					pu.getFieldName("user_id"), BinOp.EQ, pu.getArg("awl_userId")));
			Update_Query update3 = pu.addUpdateQuery(txn_name, table_name, whc3);
			update3.addUpdateExp(pu.getFieldName("user_touched"), pu.getArg("awl_timestamp"));
			pu.addQueryStatementInIf(txn_name, 0, update3);

		}

		/*
		 * getPageAnonymous
		 */
		if (txns.contains("getPageAnonymous")) {
			txn_name = "getPageAnonymous";
			prefix = "gpan_";
			pu.mkTrnasaction(txn_name, prefix + "forSelect:int", prefix + "userIp:string", prefix + "pageNamespace:int",
					prefix + "pageTitle:string");

			// retrieve the page
			table_name = "page";
			WHC whc1 = new WHC(pu.getIsAliveFieldName(table_name),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("page_namespace"), BinOp.EQ,
							pu.getArg("gpan_pageNamespace")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("page_title"), BinOp.EQ,
							pu.getArg("gpan_pageTitle")));

			Select_Query select1 = pu.addSelectQuery(txn_name, table_name, whc1, "page_id", "page_title",
					"page_restrictions", "page_counter", "page_is_redirect", "page_is_new", "page_random",
					"page_touched", "page_latest", "page_len");
			pu.addQueryStatement(txn_name, select1);

			// retrieve its restrictions
			table_name = "page_restrictions";
			WHC whc2 = new WHC(pu.getIsAliveFieldName(table_name), new WHC_Constraint(pu.getTableName(table_name),
					pu.getFieldName("pr_page"), BinOp.EQ, pu.mkProjExpr(txn_name, 0, "page_id", 1)));
			Select_Query select2 = pu.addSelectQuery(txn_name, table_name, whc2, "pr_id", "pr_type", "pr_level",
					"pr_user", "pr_expiry");
			pu.addQueryStatement(txn_name, select2);

			// check blocked ips
			table_name = "ipblocks";
			WHC whc3 = new WHC(pu.getIsAliveFieldName(table_name), new WHC_Constraint(pu.getTableName(table_name),
					pu.getFieldName("ipb_address"), BinOp.EQ, pu.getArg("gpan_userIp")));
			Select_Query select3 = pu.addSelectQuery(txn_name, table_name, whc3, "ipb_id", "ipb_user", "ipb_by",
					"ipb_by_text", "ipb_reason", "ipb_timestamp", "ipb_auto", "ipb_anon_only", "ipb_create_account",
					"ipb_enable_autoblock", "ipb_expiry", "ipb_range_start", "ipb_range_end", "ipb_deleted",
					"ipb_block_email", "ipb_allow_usertalk");
			pu.addQueryStatement(txn_name, select3);

			// retrieve page's revision history
			table_name = "revision";
			WHC whc4 = new WHC(pu.getIsAliveFieldName(table_name),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rev_page"), BinOp.EQ,
							pu.mkProjExpr(txn_name, 0, "page_id", 1)),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rev_id"), BinOp.EQ,
							pu.mkProjExpr(txn_name, 0, "page_latest", 1)));

			Select_Query select4 = pu.addSelectQuery(txn_name, table_name, whc4, "rev_text_id", "rev_comment",
					"rev_user", "rev_user_text", "rev_timestamp", "rev_minor_edit", "rev_deleted", "rev_len",
					"rev_pcarenct_id");
			pu.addQueryStatement(txn_name, select4);

			// retrieve text
			table_name = "text";
			WHC whc5 = new WHC(pu.getIsAliveFieldName(table_name), new WHC_Constraint(pu.getTableName(table_name),
					pu.getFieldName("old_id"), BinOp.EQ, pu.mkProjExpr(txn_name, 3, "rev_text_id", 1)));
			Select_Query select5 = pu.addSelectQuery(txn_name, table_name, whc5, "old_text", "old_flags");
			pu.addQueryStatement(txn_name, select5);

		}

		/*
		 * getPageAuthenticated
		 */
		if (txns.contains("getPageAuthenticated")) {
			txn_name = "getPageAuthenticated";
			prefix = "gpat_";
			pu.mkTrnasaction(txn_name, prefix + "forSelect:int", prefix + "userIp:string", prefix + "userId:int",
					prefix + "nameSpace:int", "pageTitle:string");

			// retrieve user's account
			table_name = "useracct";
			WHC whc11 = new WHC(pu.getIsAliveFieldName(table_name), new WHC_Constraint(pu.getTableName(table_name),
					pu.getFieldName("user_id"), BinOp.EQ, pu.getArg("gpat_userId")));
			Select_Query select11 = pu.addSelectQuery(txn_name, table_name, whc11, "user_touched");
			pu.addQueryStatement(txn_name, select11);

			// retrieve groups that user belongs to
			table_name = "user_groups";
			WHC whc12 = new WHC(pu.getIsAliveFieldName(table_name), new WHC_Constraint(pu.getTableName(table_name),
					pu.getFieldName("ug_user"), BinOp.EQ, pu.getArg("gpat_userId")));
			Select_Query select12 = pu.addSelectQuery(txn_name, table_name, whc12, "ug_group");
			pu.addQueryStatement(txn_name, select12);

			// retrieve the page
			table_name = "page";
			WHC whc1 = new WHC(pu.getIsAliveFieldName(table_name),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("page_namespace"), BinOp.EQ,
							pu.getArg("gpan_pageNamespace")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("page_title"), BinOp.EQ,
							pu.getArg("gpan_pageTitle")));

			Select_Query select1 = pu.addSelectQuery(txn_name, table_name, whc1, "page_id", "page_title",
					"page_restrictions", "page_counter", "page_is_redirect", "page_is_new", "page_random",
					"page_touched", "page_latest", "page_len");
			pu.addQueryStatement(txn_name, select1);

			// retrieve page's restrictions
			table_name = "page_restrictions";
			WHC whc2 = new WHC(pu.getIsAliveFieldName(table_name), new WHC_Constraint(pu.getTableName(table_name),
					pu.getFieldName("pr_page"), BinOp.EQ, pu.mkProjExpr(txn_name, 2, "page_id", 1)));
			Select_Query select2 = pu.addSelectQuery(txn_name, table_name, whc2, "pr_id", "pr_type", "pr_level",
					"pr_user", "pr_expiry");
			pu.addQueryStatement(txn_name, select2);

			// retrive blocked ips
			table_name = "ipblocks";
			WHC whc3 = new WHC(pu.getIsAliveFieldName(table_name), new WHC_Constraint(pu.getTableName(table_name),
					pu.getFieldName("ipb_address"), BinOp.EQ, pu.getArg("gpan_userIp")));
			Select_Query select3 = pu.addSelectQuery(txn_name, table_name, whc3, "ipb_id", "ipb_user", "ipb_by",
					"ipb_by_text", "ipb_reason", "ipb_timestamp", "ipb_auto", "ipb_anon_only", "ipb_create_account",
					"ipb_enable_autoblock", "ipb_expiry", "ipb_range_start", "ipb_range_end", "ipb_deleted",
					"ipb_block_email", "ipb_allow_usertalk");
			pu.addQueryStatement(txn_name, select3);

			// retrieve revision history
			table_name = "revision";
			WHC whc4 = new WHC(pu.getIsAliveFieldName(table_name),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rev_page"), BinOp.EQ,
							pu.mkProjExpr(txn_name, 2, "page_id", 1)),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rev_id"), BinOp.EQ,
							pu.mkProjExpr(txn_name, 2, "page_latest", 1)));

			Select_Query select4 = pu.addSelectQuery(txn_name, table_name, whc4, "rev_text_id", "rev_comment",
					"rev_user", "rev_user_text", "rev_timestamp", "rev_minor_edit", "rev_deleted", "rev_len",
					"rev_pcarenct_id");
			pu.addQueryStatement(txn_name, select4);

			// retrieve text
			table_name = "text";
			WHC whc5 = new WHC(pu.getIsAliveFieldName(table_name), new WHC_Constraint(pu.getTableName(table_name),
					pu.getFieldName("old_id"), BinOp.EQ, pu.mkProjExpr(txn_name, 5, "rev_text_id", 1)));
			Select_Query select5 = pu.addSelectQuery(txn_name, table_name, whc5, "old_text", "old_flags");
			pu.addQueryStatement(txn_name, select5);
		}

		/*
		 * removeWatchList
		 */
		if (txns.contains("removeWatchList")) {
			txn_name = "removeWatchList";
			prefix = "rwl_";
			pu.mkTrnasaction(txn_name, prefix + "userId:int", prefix + "nameSpace:int", prefix + "pageTitle:string",
					prefix + "timestamp:string");

			// if userId>0
			Expression if1 = new E_BinOp(BinOp.GT, pu.getArg("rwl_userId"), new E_Const_Num(0));
			pu.addIfStatement(txn_name, if1);

			// delete from watchlist
			table_name = "watchlist";
			WHC delete1 = new WHC(pu.getIsAliveFieldName(table_name),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("wl_user"), BinOp.EQ,
							pu.getArg("rwl_userId")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("wl_namespace"), BinOp.EQ,
							pu.getArg("rwl_nameSpace")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("wl_title"), BinOp.EQ,
							pu.getArg("rwl_pageTitle")));
			Delete_Query delivery1 = pu.addDeleteQuery(txn_name, table_name, delete1);
			pu.addQueryStatementInIf(txn_name, 0, delivery1);

			// if nameSpace=0
			Expression if2 = new E_BinOp(BinOp.EQ, pu.getArg("awl_nameSpace"), new E_Const_Num(0));
			pu.addIfStatementInIf(txn_name, 0, if2);

			// delete from watchlist
			table_name = "watchlist";
			WHC delete2 = new WHC(pu.getIsAliveFieldName(table_name),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("wl_user"), BinOp.EQ,
							pu.getArg("rwl_userId")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("wl_namespace"), BinOp.EQ,
							new E_Const_Num(1)),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("wl_title"), BinOp.EQ,
							pu.getArg("rwl_pageTitle")));
			Delete_Query delivery2 = pu.addDeleteQuery(txn_name, table_name, delete2);
			pu.addQueryStatementInIf(txn_name, 1, delivery2);

			// update useracct
			table_name = "useracct";
			WHC whc3 = new WHC(pu.getIsAliveFieldName(table_name), new WHC_Constraint(pu.getTableName(table_name),
					pu.getFieldName("user_id"), BinOp.EQ, pu.getArg("rwl_userId")));
			Update_Query update3 = pu.addUpdateQuery(txn_name, table_name, whc3);
			update3.addUpdateExp(pu.getFieldName("user_touched"), pu.getArg("rwl_timestamp"));
			pu.addQueryStatementInIf(txn_name, 0, update3);

		}

		/*
		 * updatePageLog
		 */
		if (txns.contains("updatePageLog")) {
			txn_name = "updatePageLog";
			prefix = "upl_";
			pu.mkTrnasaction(txn_name, prefix + "textId:int", prefix + "pageId:int", prefix + "pageTitle:string",
					prefix + "pageText:string", prefix + "pageNamespace:int", prefix + "userId:int",
					prefix + "userIp:string", prefix + "userText:string", prefix + "revisionId:int",
					prefix + "revComment:string", prefix + "revMinorEdit:int", prefix + "timestamp:string");

			// insert into pagelog
			table_name = "logging";
			Insert_Query insert1 = pu.addInsertQuery(txn_name, table_name,
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("log_id"), BinOp.EQ,
							new E_Const_Num(1)),

					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("log_type"), BinOp.EQ,
							new E_Const_Text("patrol")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("log_action"), BinOp.EQ,
							new E_Const_Text("patrol")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("log_timestamp"), BinOp.EQ,
							pu.getArg("upl_timestamp")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("log_user"), BinOp.EQ,
							pu.getArg("upl_userId")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("log_namespace"), BinOp.EQ,
							pu.getArg("upl_pageNamespace")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("log_title"), BinOp.EQ,
							pu.getArg("upl_pageTitle")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("log_comment"), BinOp.EQ,
							pu.getArg("upl_revComment")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("log_params"), BinOp.EQ,
							new E_Const_Text("")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("log_deleted"), BinOp.EQ,
							new E_Const_Num(0)),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("log_user_text"), BinOp.EQ,
							pu.getArg("upl_userText")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("log_page"), BinOp.EQ,
							pu.getArg("upl_pageId")));
			insert1.addInsertExp(pu.getFieldName("log_id"), new E_Const_Text("patrol"));
			insert1.addInsertExp(pu.getFieldName("log_type"), new E_Const_Text("patrol"));
			insert1.addInsertExp(pu.getFieldName("log_action"), pu.getArg("upl_timestamp"));
			insert1.addInsertExp(pu.getFieldName("log_timestamp"), pu.getArg("upl_timestamp"));
			insert1.addInsertExp(pu.getFieldName("log_user"), pu.getArg("upl_userId"));
			insert1.addInsertExp(pu.getFieldName("log_namespace"), pu.getArg("upl_pageNamespace"));
			insert1.addInsertExp(pu.getFieldName("log_title"), pu.getArg("upl_pageTitle"));
			insert1.addInsertExp(pu.getFieldName("log_comment"), pu.getArg("upl_revComment"));
			insert1.addInsertExp(pu.getFieldName("log_params"), new E_Const_Text(""));
			insert1.addInsertExp(pu.getFieldName("log_deleted"), new E_Const_Num(0));
			insert1.addInsertExp(pu.getFieldName("log_user_text"), pu.getArg("upl_userText"));
			insert1.addInsertExp(pu.getFieldName("log_page"), pu.getArg("upl_pageId"));
			pu.addQueryStatement(txn_name, insert1);

			// retrieve user's details
			table_name = "useracct";
			WHC whc11 = new WHC(pu.getIsAliveFieldName(table_name), new WHC_Constraint(pu.getTableName(table_name),
					pu.getFieldName("user_id"), BinOp.EQ, pu.getArg("upl_userId")));
			Select_Query select11 = pu.addSelectQuery(txn_name, table_name, whc11, "user_editcount", "user_touched");
			pu.addQueryStatement(txn_name, select11);

			// update user's details
			Update_Query update3 = pu.addUpdateQuery(txn_name, table_name, whc11);
			update3.addUpdateExp(pu.getFieldName("user_editcount"),
					new E_BinOp(BinOp.PLUS, pu.mkProjExpr(txn_name, 0, "user_editcount", 1), new E_Const_Num(1)));
			update3.addUpdateExp(pu.getFieldName("user_touched"), new E_Const_Text("now"));
			pu.addQueryStatement(txn_name, update3);

		}

		/*
		 * updatePage
		 */
		if (txns.contains("updatePage")) {
			txn_name = "updatePage";
			prefix = "up_";
			pu.mkTrnasaction(txn_name, prefix + "textId:int", prefix + "pageId:int", prefix + "pageTitle:string",
					prefix + "pageText:string", prefix + "pageNamespace:int", prefix + "userId:int",
					prefix + "userIp:string", prefix + "userText:string", prefix + "revisionId:int",
					prefix + "revComment:string", prefix + "revMinorEdit:int", prefix + "userTextuserText:string",
					prefix + "timestamp:string");

			// retrieve text
			table_name = "text";
			WHC whc11 = new WHC(pu.getIsAliveFieldName(table_name), new WHC_Constraint(pu.getTableName(table_name),
					pu.getFieldName("old_page"), BinOp.EQ, pu.getArg("up_pageId")));
			Select_Query select11 = pu.addSelectQuery(txn_name, table_name, whc11, "old_id");
			pu.addQueryStatement(txn_name, select11);

			// insert new text
			table_name = "text";
			Insert_Query insert1 = pu.addInsertQuery(txn_name, table_name,
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("old_id"), BinOp.EQ,
							new E_BinOp(BinOp.PLUS, pu.mkProjExpr(txn_name, 0, "old_id", 1), new E_Const_Num(1))),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("old_text"), BinOp.EQ,
							pu.getArg("up_pageText")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("old_flags"), BinOp.EQ,
							new E_Const_Text("utf-8")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("old_page"), BinOp.EQ,
							pu.getArg("up_pageId")));
			insert1.addInsertExp(pu.getFieldName("old_id"),
					new E_BinOp(BinOp.PLUS, pu.mkProjExpr(txn_name, 0, "old_id", 1), new E_Const_Num(1)));
			insert1.addInsertExp(pu.getFieldName("old_text"), pu.getArg("up_pageText"));
			insert1.addInsertExp(pu.getFieldName("old_flags"), new E_Const_Text("utf-8"));
			insert1.addInsertExp(pu.getFieldName("old_page"), pu.getArg("up_pageId"));
			pu.addQueryStatement(txn_name, insert1);

			// select revision history
			table_name = "revision";
			WHC whc2 = new WHC(pu.getIsAliveFieldName(table_name), new WHC_Constraint(pu.getTableName(table_name),
					pu.getFieldName("rev_page"), BinOp.EQ, pu.getArg("up_pageId")));
			Select_Query select2 = pu.addSelectQuery(txn_name, table_name, whc2, "rev_id");
			pu.addQueryStatement(txn_name, select2);

			// insert new revision
			table_name = "revision";
			Insert_Query insert2 = pu.addInsertQuery(txn_name, table_name,
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rev_id"), BinOp.EQ,
							new E_BinOp(BinOp.PLUS, pu.mkProjExpr(txn_name, 1, "rev_id", 1), new E_Const_Num(1))),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rev_page"), BinOp.EQ,
							pu.getArg("up_pageId")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rev_text_id"), BinOp.EQ,
							new E_BinOp(BinOp.PLUS, pu.mkProjExpr(txn_name, 0, "old_id", 1), new E_Const_Num(1))),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rev_comment"), BinOp.EQ,
							pu.getArg("up_revComment")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rev_user"), BinOp.EQ,
							pu.getArg("up_userId")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rev_user_text"), BinOp.EQ,
							pu.getArg("up_userText")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rev_timestamp"), BinOp.EQ,
							pu.getArg("up_timestamp")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rev_minor_edit"), BinOp.EQ,
							pu.getArg("up_revMinorEdit")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rev_deleted"), BinOp.EQ,
							new E_Const_Num(0)),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rev_len"), BinOp.EQ,
							new E_Const_Num(10)),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rev_pcarenct_id"), BinOp.EQ,
							pu.getArg("up_revisionId")));
			insert2.addInsertExp(pu.getFieldName("rev_id"),
					new E_BinOp(BinOp.PLUS, pu.mkProjExpr(txn_name, 1, "rev_id", 1), new E_Const_Num(1)));
			insert2.addInsertExp(pu.getFieldName("rev_page"), pu.getArg("up_pageId"));
			insert2.addInsertExp(pu.getFieldName("rev_text_id"),
					new E_BinOp(BinOp.PLUS, pu.mkProjExpr(txn_name, 0, "old_id", 1), new E_Const_Num(1)));
			insert2.addInsertExp(pu.getFieldName("rev_comment"), pu.getArg("up_revComment"));
			insert2.addInsertExp(pu.getFieldName("rev_user"), pu.getArg("up_userId"));
			insert2.addInsertExp(pu.getFieldName("rev_user_text"), pu.getArg("up_userText"));
			insert2.addInsertExp(pu.getFieldName("rev_timestamp"), pu.getArg("up_timestamp"));
			insert2.addInsertExp(pu.getFieldName("rev_minor_edit"), pu.getArg("up_revMinorEdit"));
			insert2.addInsertExp(pu.getFieldName("rev_deleted"), new E_Const_Num(0));
			insert2.addInsertExp(pu.getFieldName("rev_len"), new E_Const_Num(10));
			insert2.addInsertExp(pu.getFieldName("rev_pcarenct_id"), pu.getArg("up_revisionId"));
			pu.addQueryStatement(txn_name, insert2);

			// update page
			table_name = "page";
			Update_Query update3 = pu.addUpdateQuery(txn_name, table_name, whc11);
			update3.addUpdateExp(pu.getFieldName("page_id"), pu.getArg("up_pageId"));
			update3.addUpdateExp(pu.getFieldName("page_latest"),
					new E_BinOp(BinOp.PLUS, pu.mkProjExpr(txn_name, 1, "rev_id", 1), new E_Const_Num(1)));
			update3.addUpdateExp(pu.getFieldName("page_touched"), pu.getArg("up_timestamp"));
			update3.addUpdateExp(pu.getFieldName("page_is_new"), new E_Const_Num(0));
			update3.addUpdateExp(pu.getFieldName("page_is_redirect"), new E_Const_Num(0));
			update3.addUpdateExp(pu.getFieldName("page_len"), new E_Const_Num(10));
			pu.addQueryStatement(txn_name, update3);

			// insert new recentchange
			table_name = "recentchanges";
			Insert_Query insert3 = pu.addInsertQuery(txn_name, table_name,
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_id"), BinOp.EQ,
							new E_Const_Num(10)),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_timestamp"), BinOp.EQ,
							pu.getArg("up_timestamp")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_cur_time"), BinOp.EQ,
							pu.getArg("up_timestamp")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_user"), BinOp.EQ,
							pu.getArg("up_userId")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_user_text"), BinOp.EQ,
							pu.getArg("up_userText")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_namespace"), BinOp.EQ,
							pu.getArg("up_pageNamespace")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_title"), BinOp.EQ,
							pu.getArg("up_pageTitle")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_comment"), BinOp.EQ,
							pu.getArg("up_revComment")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_minor"), BinOp.EQ,
							new E_Const_Num(0)),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_bot"), BinOp.EQ,
							new E_Const_Num(0)),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_new"), BinOp.EQ,
							new E_Const_Num(0)),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_cur_id"), BinOp.EQ,
							pu.getArg("up_pageId")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_this_oldid"), BinOp.EQ,
							new E_BinOp(BinOp.PLUS, pu.mkProjExpr(txn_name, 0, "old_id", 1), new E_Const_Num(1))),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_last_oldid"), BinOp.EQ,
							pu.getArg("up_textId")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_type"), BinOp.EQ,
							new E_Const_Num(0)),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_moved_to_ns"), BinOp.EQ,
							new E_Const_Num(0)),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_moved_to_title"), BinOp.EQ,
							new E_Const_Text("")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_patrolled"), BinOp.EQ,
							new E_Const_Num(0)),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_ip"), BinOp.EQ,
							pu.getArg("up_userIp")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_old_len"), BinOp.EQ,
							new E_Const_Num(10)),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_new_len"), BinOp.EQ,
							new E_Const_Num(10)),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_deleted"), BinOp.EQ,
							new E_Const_Num(0)),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_logid"), BinOp.EQ,
							new E_Const_Num(0)),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_log_type"), BinOp.EQ,
							new E_Const_Text("")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_log_action"), BinOp.EQ,
							new E_Const_Text("")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("rc_params"), BinOp.EQ,
							new E_Const_Text("")));

			insert3.addInsertExp(pu.getFieldName("rc_id"), new E_Const_Num(10));
			insert3.addInsertExp(pu.getFieldName("rc_timestamp"), pu.getArg("up_timestamp"));
			insert3.addInsertExp(pu.getFieldName("rc_cur_time"), pu.getArg("up_timestamp"));
			insert3.addInsertExp(pu.getFieldName("rc_user"), pu.getArg("up_userId"));
			insert3.addInsertExp(pu.getFieldName("rc_user_text"), pu.getArg("up_userText"));
			insert3.addInsertExp(pu.getFieldName("rc_namespace"), pu.getArg("up_pageNamespace"));
			insert3.addInsertExp(pu.getFieldName("rc_title"), pu.getArg("up_pageTitle"));
			insert3.addInsertExp(pu.getFieldName("rc_comment"), pu.getArg("up_revComment"));
			insert3.addInsertExp(pu.getFieldName("rc_minor"), new E_Const_Num(0));
			insert3.addInsertExp(pu.getFieldName("rc_bot"), new E_Const_Num(0));
			insert3.addInsertExp(pu.getFieldName("rc_new"), new E_Const_Num(0));
			insert3.addInsertExp(pu.getFieldName("rc_cur_id"), pu.getArg("up_pageId"));
			insert3.addInsertExp(pu.getFieldName("rc_this_oldid"),
					new E_BinOp(BinOp.PLUS, pu.mkProjExpr(txn_name, 0, "old_id", 1), new E_Const_Num(1)));
			insert3.addInsertExp(pu.getFieldName("rc_last_oldid"), pu.getArg("up_textId"));
			insert3.addInsertExp(pu.getFieldName("rc_type"), new E_Const_Num(0));
			insert3.addInsertExp(pu.getFieldName("rc_moved_to_ns"), new E_Const_Num(0));
			insert3.addInsertExp(pu.getFieldName("rc_moved_to_title"), new E_Const_Text(""));
			insert3.addInsertExp(pu.getFieldName("rc_patrolled"), new E_Const_Num(0));
			insert3.addInsertExp(pu.getFieldName("rc_ip"), pu.getArg("up_userIp"));
			insert3.addInsertExp(pu.getFieldName("rc_old_len"), new E_Const_Num(10));
			insert3.addInsertExp(pu.getFieldName("rc_new_len"), new E_Const_Num(10));
			insert3.addInsertExp(pu.getFieldName("rc_deleted"), new E_Const_Num(0));
			insert3.addInsertExp(pu.getFieldName("rc_logid"), new E_Const_Num(0));
			insert3.addInsertExp(pu.getFieldName("rc_log_type"), new E_Const_Text(""));
			insert3.addInsertExp(pu.getFieldName("rc_log_action"), new E_Const_Text(""));
			insert3.addInsertExp(pu.getFieldName("rc_params"), new E_Const_Text(""));

			pu.addQueryStatement(txn_name, insert3);

			// retrieve watchlist
			table_name = "watchlist";
			WHC whc111 = new WHC(pu.getIsAliveFieldName(table_name),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("wl_namespace"), BinOp.EQ,
							pu.getArg("up_pageNamespace")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("wl_title"), BinOp.EQ,
							pu.getArg("up_pageTitle")));
			Select_Query select111 = pu.addSelectQuery(txn_name, table_name, whc111, "wl_user");
			pu.addQueryStatement(txn_name, select111);

			// update watchlist
			table_name = "watchlist";
			WHC whc1112 = new WHC(pu.getIsAliveFieldName(table_name),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("wl_namespace"), BinOp.EQ,
							pu.getArg("up_pageNamespace")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("wl_title"), BinOp.EQ,
							pu.getArg("up_pageTitle")),
					new WHC_Constraint(pu.getTableName(table_name), pu.getFieldName("wl_user"), BinOp.EQ,
							pu.mkProjExpr(txn_name, 2, "wl_user", 1)));
			Update_Query update31 = pu.addUpdateQuery(txn_name, table_name, whc1112);
			update31.addUpdateExp(pu.getFieldName("wl_notificationtimestamp"), pu.getArg("up_timestamp"));
			pu.addQueryStatement(txn_name, update31);
		}

		return pu.generateProgram();
	}
}
