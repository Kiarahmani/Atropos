/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring_engine.deltas.Modifiers;

import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.program.Statement;
import kiarahmani.atropos.program.statements.Query_Statement;

/**
 * @author Kiarash
 *
 */
public abstract class Query_Modifier {
	private boolean is_set;

	public void set() {
		this.is_set = true;
	}

	public boolean isSet() {
		return this.is_set;
	}

	// update a single targeted query
	public abstract Query atIndexModification(Query input_query);

	// update all subsequent expressions (inside ifs)
	public abstract Expression propagatedExpModification(Expression input_exp);

	// update all subsequent queries
	public abstract Query_Statement propagatedQueryModification(Query_Statement input_exp);
}