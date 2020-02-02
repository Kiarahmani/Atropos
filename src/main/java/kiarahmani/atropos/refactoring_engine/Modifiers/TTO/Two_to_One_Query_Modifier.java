/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring_engine.Modifiers.TTO;

import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.refactoring_engine.Modifiers.QM_Type;
import kiarahmani.atropos.refactoring_engine.Modifiers.Query_Modifier;

/**
 * @author Kiarash
 *
 */
public abstract class Two_to_One_Query_Modifier extends Query_Modifier {
	// update a single targeted query
	public abstract Query atIndexModification(Query input_query_1, Query input_query_2);

	public abstract boolean isValid(Query input_query_1, Query input_query_2);

	public void set() {
		super.set();
	}

	public Two_to_One_Query_Modifier() {
		super.type = QM_Type.TTO;
	}

}
