/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring_engine.Modifiers.OTT;

import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.refactoring_engine.Modifiers.QM_Type;
import kiarahmani.atropos.refactoring_engine.Modifiers.Query_Modifier;
import kiarahmani.atropos.utils.Tuple;

/**
 * @author Kiarash
 *
 */
public abstract class One_to_Two_Query_Modifier extends Query_Modifier {
	// update a single targeted query
	public abstract Tuple<Query, Query> atIndexModification(Query input_query);

	public abstract boolean isValid(Query input_query);

	public void set() {
		super.set();
	}

	public One_to_Two_Query_Modifier() {
		super.type = QM_Type.OTT;
	}

}
