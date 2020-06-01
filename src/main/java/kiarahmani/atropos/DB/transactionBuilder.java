/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.DB;

import java.util.ArrayList;

import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.program.Transaction;
import kiarahmani.atropos.utils.Program_Utils;

public class transactionBuilder {
	String txnName;
	Program_Utils pu;
	ArrayList<String> args;

	public transactionBuilder(Program_Utils pu, String txnName) {
		this.pu = pu;
		this.txnName = txnName;
		args = new ArrayList<>();
	}

	public transactionBuilder arg(String a) {
		this.args.add(a);
		return this;
	}

	public Transaction done() {
		return pu.mkTrnasaction(txnName, args);
	}

}
