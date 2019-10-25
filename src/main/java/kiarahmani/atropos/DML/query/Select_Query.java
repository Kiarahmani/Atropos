package kiarahmani.atropos.DML.query;

import java.util.ArrayList;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.Query;
import kiarahmani.atropos.DML.Variable;

public class Select_Query extends Query {
	private TableName tableName;
	private Variable variable;
	private ArrayList<FieldName> fieldNames;
}
