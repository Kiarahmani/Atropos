/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring;

import java.beans.Statement;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.DDL.FieldName;
import kiarahmani.atropos.DML.query.Query;
import kiarahmani.atropos.dependency.DAI;
import kiarahmani.atropos.program.Block;
import kiarahmani.atropos.program.Transaction;
import kiarahmani.atropos.program.Block.BlockType;
import kiarahmani.atropos.program.statements.If_Statement;
import kiarahmani.atropos.program.statements.Query_Statement;
import kiarahmani.atropos.refactoring_engine.Modifiers.Query_Modifier;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTO.One_to_One_Query_Modifier;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTT.One_to_Two_Query_Modifier;
import kiarahmani.atropos.refactoring_engine.Modifiers.OTT.SELECT_Splitter;
import kiarahmani.atropos.refactoring_engine.Modifiers.TTO.Two_to_One_Query_Modifier;
import kiarahmani.atropos.utils.Program_Utils;
import kiarahmani.atropos.utils.Tuple;

/**
 * @author Kiarash The refactoring engine Newest Implementation (After OOPSLA )
 */
public class RefactorEngine {
	private static final Logger logger = LogManager.getLogger(Atropos.class);

	public ArrayList<DAI> pre_process(Program_Utils pu, ArrayList<DAI> dais) {

		// initialize anomalies of each query
		for (DAI anml : dais) {
			pu.getByUniqueId(anml.getQueryUniqueId(1)).addAnml(anml.getFieldNames(1));
			pu.getByUniqueId(anml.getQueryUniqueId(2)).addAnml(anml.getFieldNames(2));
		}
		return dais;
	}

	private void splitQuery(Program_Utils pu, String QueryUniqueId, ArrayList<FieldName> excludedFns) {
		for (Transaction t : pu.getTrasnsactionMap().values()) {
			// for (Statement)
		}
	}

}
