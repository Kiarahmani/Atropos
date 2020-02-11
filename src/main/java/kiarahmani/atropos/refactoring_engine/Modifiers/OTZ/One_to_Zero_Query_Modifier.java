/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring_engine.Modifiers.OTZ;

import kiarahmani.atropos.refactoring_engine.Modifiers.QM_Type;
import kiarahmani.atropos.refactoring_engine.Modifiers.Query_Modifier;

/**
 * @author Kiarash
 *
 */
public abstract class One_to_Zero_Query_Modifier extends Query_Modifier {
	private boolean is_set;

	public One_to_Zero_Query_Modifier() {
		super.type = QM_Type.OTZ;
	}

}
