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
public abstract class One_to_One_Query_Modifier extends Query_Modifier {
	private boolean is_set;

	public One_to_One_Query_Modifier() {
		super.type = QM_TYPE.OTO;
	}

	// update a single targeted query
	public abstract Query atIndexModification(Query input_query);

}
