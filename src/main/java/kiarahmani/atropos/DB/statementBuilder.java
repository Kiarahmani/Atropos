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
import kiarahmani.atropos.DML.expression.BinOp;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.DML.query.Query.Kind;
import kiarahmani.atropos.DML.query.Update_Query;
import kiarahmani.atropos.DML.where_clause.WHC;
import kiarahmani.atropos.DML.where_clause.WHCC;
import kiarahmani.atropos.program.Statement;
import kiarahmani.atropos.program.Table;
import kiarahmani.atropos.program.Transaction;
import kiarahmani.atropos.utils.Program_Utils;
import kiarahmani.atropos.utils.Tuple;

public class statementBuilder {
	Program_Utils pu;
	int ifId;
	boolean isIf;
	boolean isStar;
	String txnName;
	String tName;
	WHC whc;
	String[] flds;
	Query.Kind kind;
	ArrayList<WHCC> whccs;
	String varName;
	ArrayList<Tuple<FieldName, Expression>> upd_expressions;

	public statementBuilder(Program_Utils pu, String txnName) {
		this.pu = pu;
		this.ifId = -1;
		this.txnName = txnName;
		this.whccs = new ArrayList<>();
	}

	public statementBuilder(Program_Utils pu, String txnName, int ifId, boolean isIf) {
		this.pu = pu;
		this.ifId = ifId;
		this.txnName = txnName;
		this.whccs = new ArrayList<>();
		this.isIf = isIf;
	}

	public statementBuilder select(String... flds) {
		if (flds.length == 1 && flds[0].equals("*"))
			this.isStar = true;
		else {
			this.isStar = false;
			this.flds = flds;
		}
		this.kind = Kind.SELECT;
		return this;
	}

	public statementBuilder update(String tname) {
		this.tName = tname;
		this.upd_expressions = new ArrayList<>();
		this.kind = Kind.UPDATE;
		return this;
	}

	public statementBuilder as(String v) {
		assert (this.kind == Kind.SELECT);
		this.varName = v;
		return this;
	}

	public statementBuilder set(String fn, Expression exp) {
		this.upd_expressions.add(new Tuple<FieldName, Expression>(pu.getFieldName(fn), exp));
		return this;
	}

	public statementBuilder from(String tname) {
		this.tName = tname;
		if (this.isStar) {
			ArrayList<FieldName> flds_arr = pu.getTable(tname).getFieldNames();
			this.flds = new String[flds_arr.size() - 1];
			int i = 0;
			for (FieldName fn : flds_arr) {
				if (!fn.isAliveField())
					this.flds[i++] = fn.getName();
			}
		}
		return this;
	}

	public statementBuilder where(String fldName, String op, Expression exp) {
		this.whccs.add(new WHCC(pu.getTableName(this.tName), pu.getFieldName(fldName), BinOp.StringToBinOp(op), exp));
		return this;
	}

	public statementBuilder and(String fldName, String op, Expression exp) {
		this.whccs.add(new WHCC(pu.getTableName(this.tName), pu.getFieldName(fldName), BinOp.StringToBinOp(op), exp));
		return this;
	}

	public Statement done() {
		this.whc = new WHC(pu.getIsAliveFieldName(tName), whccs);
		switch (kind) {
		case SELECT:
			if (this.ifId == -1)
				return pu.addQueryStatement(txnName, pu.addSelectQuery(txnName, tName, varName, whc, flds));
			else
				return (isIf)
						? pu.addQueryStatementInIf(txnName, ifId, pu.addSelectQuery(txnName, tName, varName, whc, flds))
						: pu.addQueryStatementInElse(txnName, ifId,
								pu.addSelectQuery(txnName, tName, varName, whc, flds))

				;
		case UPDATE:
			Update_Query uq = pu.addUpdateQuery(txnName, tName, whc);
			for (Tuple<FieldName, Expression> t : this.upd_expressions)
				uq.addUpdateExp(t.x, t.y);
			if (this.ifId == -1)
				return pu.addQueryStatement(txnName, uq);
			else
				return (isIf) ? pu.addQueryStatementInIf(txnName, ifId, uq)
						: pu.addQueryStatementInElse(txnName, ifId, uq);
		default:
			assert (false) : "not implemented yet";
			return null;
		}
	}

}
