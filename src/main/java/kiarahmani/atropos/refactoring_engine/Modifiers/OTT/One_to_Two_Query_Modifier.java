/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring_engine.Modifiers.OTT;

import kiarahmani.atropos.DML.expression.Expression;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.program.Statement;
import kiarahmani.atropos.program.statements.Query_Statement;
import kiarahmani.atropos.refactoring_engine.Modifiers.Query_Modifier;
import kiarahmani.atropos.refactoring_engine.deltas.Modifiers.Query_Modifier.QM_TYPE;
import kiarahmani.atropos.utils.Tuple;

/**
 * @author Kiarash
 *
 */
public abstract class One_to_Two_Query_Modifier extends Query_Modifier {
	// update a single targeted query
	public abstract Tuple<Query, Query> atIndexModification(Query input_query);

	public One_to_Two_Query_Modifier() {
		super.type = QM_TYPE.OTT;
	}

}
