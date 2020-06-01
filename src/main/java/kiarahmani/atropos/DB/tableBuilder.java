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
import kiarahmani.atropos.utils.Program_Utils;

public class tableBuilder {
	private ArrayList<FieldName> fields = new ArrayList<>();
	String tname;
	Program_Utils pu;

	public tableBuilder(Program_Utils pu, String tname) {
		this.pu = pu;
		this.tname = tname;
	}

	public tableBuilder field(String fname, String type) {
		fields.add(new FieldName(fname, false, false, F_Type.stringTypeToFType(type)));
		return this;
	}

	public tableBuilder pk(String fname, String type) {
		fields.add(new FieldName(fname, true, false, F_Type.stringTypeToFType(type)));
		return this;
	}

	public Table done() {
		return pu.mkTable(tname, fields);
	}
}
