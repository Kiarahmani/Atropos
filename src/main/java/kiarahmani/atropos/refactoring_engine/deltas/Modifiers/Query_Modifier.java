/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring_engine.deltas.Modifiers;

import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.program.Statement;

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

	// update all subsequent queries
	public abstract Statement propagatedModification(Statement input_stmt);
}
