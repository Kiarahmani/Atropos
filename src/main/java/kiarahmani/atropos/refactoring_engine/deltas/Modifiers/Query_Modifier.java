/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring_engine.deltas.Modifiers;

import kiarahmani.atropos.DML.query.Query;

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

	public abstract Query modify_single(Query input_query);
}
