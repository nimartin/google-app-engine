package fr.licence.persistence;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

public class PMF {

	private static final PersistenceManagerFactory pmfinstance = JDOHelper.getPersistenceManagerFactory("transactions-optional");
	
	private PMF(){
		
		
	}
	
	public static PersistenceManagerFactory get(){
		return pmfinstance;
	}
}
