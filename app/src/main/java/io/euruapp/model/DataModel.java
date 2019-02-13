package io.euruapp.model;

/**
 * Base {@link DataModel} for every user object in the system
 */
abstract class DataModel implements EuruSearchableItem {
	/*Database reference*/
	abstract public String getKey();
	
	abstract public void setKey(String key);
	
	/*Item name*/
	abstract public String getName();
	
	abstract public void setName(String name);
}
