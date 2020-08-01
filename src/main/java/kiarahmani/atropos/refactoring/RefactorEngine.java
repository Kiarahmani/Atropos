/**
 *
 * Copyright (C) Kia Rahmani, 2020 - All Rights Reserved
 *
 **/
package kiarahmani.atropos.refactoring;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiarahmani.atropos.Atropos;
import kiarahmani.atropos.dependency.DAI;
import kiarahmani.atropos.utils.Program_Utils;

/**
 * @author Kiarash
 * The refactoring engine
 * Newest Implementation (After OOPSLA )
 */
public class RefactorEngine {
	private static final Logger logger = LogManager.getLogger(Atropos.class);

	public ArrayList<DAI> pre_process(Program_Utils pu, ArrayList<DAI> dais) {
		for (DAI anml : dais) {
			String uid1 = anml.getQueryUniqueId(1);
			String uid2 = anml.getQueryUniqueId(2);
			String txnName = anml.getTransaction().getName();
			System.out.println(uid1);
			System.out.println(uid2);
			System.out.println(txnName);
		}
		
		
		return dais;
	}
	
	
	
	
}
