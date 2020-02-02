/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring_engine.deltas;

import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.FieldName;

public class INTRO_F extends Delta {
	public String getTableName() {
		return tableName;
	}

	public FieldName getNewName() {
		return newFieldName;
	}

	private String tableName;
	private FieldName newFieldName;

	public INTRO_F(String tableName, String name, F_Type tp) {
		this.newFieldName = new FieldName(name, false, false, tp);
		this.tableName = tableName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.refactoring_engine.deltas.Delta#getDesc()
	 */
	@Override
	public String getDesc() {
		return "Field \"" + newFieldName + "\" added to table \"" + this.tableName + "\"";
	}

}
