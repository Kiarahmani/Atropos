package kiarahmani.atropos.DML.query;

import java.util.ArrayList;

import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DDL.TableName;
import kiarahmani.atropos.DML.Query;
import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.utils.Tuple;

public class Update_Query extends Query {
	private TableName tableName;
	private ArrayList<Tuple<FieldName, Expression>> update_expressions;

}
