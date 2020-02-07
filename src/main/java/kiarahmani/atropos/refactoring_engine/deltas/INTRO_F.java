/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring_engine.deltas;

import kiarahmani.atropos.DDL.F_Type;
import kiarahmani.atropos.DDL.FieldName;

public class INTRO_F extends Delta {
	boolean isUUID;
	boolean isDeltaField;

	public String getTableName() {
		return tableName;
	}

	public FieldName getNewName() {
		return newFieldName;
	}

	public boolean isUUID() {
		return this.isUUID;
	}

	private String tableName;
	private FieldName newFieldName;

	public INTRO_F(String tableName, String name, F_Type tp) {
		this.newFieldName = new FieldName(name, false, false, tp);
		this.tableName = tableName;
		this.isUUID = false;
	}

	public INTRO_F(String tableName, String name, F_Type tp, boolean isUUD, boolean isDF) {
		this.newFieldName = new FieldName(name, false, false, tp);
		this.tableName = tableName;
		this.isUUID = isUUD;
		this.isDeltaField = isDF;
		if (this.isUUID)
			newFieldName.setUUID();
		if (this.isDeltaField)
			newFieldName.setDelta();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see kiarahmani.atropos.refactoring_engine.deltas.Delta#getDesc()
	 */
	@Override
	public String getDesc() {
		return "field \"" + newFieldName + "\" added to table \"" + this.tableName + "\"";
	}

}
